#include "path_navigator.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "PathNavigator", __VA_ARGS__)

namespace {
    constexpr float kDeviationThreshold = 3.0f;
    constexpr float kReachThreshold = 0.8f;
}

PathNavigator::PathNavigator() {
    obstacles_ = GenerateObstacles();
}

void PathNavigator::SetGoals(const std::vector<Point>& goals) {
    while (!goal_queue_.empty()) goal_queue_.pop();
    for (const auto& g : goals) goal_queue_.push(g);
    goal_set_ = !goal_queue_.empty();
    path_generated_ = false;
    LOGI("✅ Goal Queue 설정 완료. 총 %zu개 목표", goals.size());

    std::queue<Point> goal_debug = goal_queue_;  // 원본 queue는 그대로 두고 복사본으로 출력
    int idx = 0;
    while (!goal_debug.empty()) {
        const Point& p = goal_debug.front();
        LOGI("🎯 Goal %d: x=%.2f, z=%.2f", idx++, p.x, p.z);
        goal_debug.pop();
    }
}

void PathNavigator::LoadPoseGraphFromFile(const std::string& path, int floor) {
    std::ifstream file(path);
    if (!file.is_open()) {
        LOGI("❌ pose_graph.txt 열기 실패: %s", path.c_str());
        return;
    }

    std::string line;
    while (std::getline(file, line)) {
        std::istringstream iss(line);
        std::vector<std::string> tokens;
        std::string token;

        while (iss >> token) tokens.push_back(token);
        if (tokens.size() < 8) continue;

        int id = std::stoi(tokens[0]);
        float x = std::stof(tokens[5]);
        float z = std::stof(tokens[7]);

        pose_graph_by_floor_[floor][id] = Point{x, z};
    }

    file.close();
    LOGI("✅ %d층 pose_graph.txt → %zu개 노드 로드 완료", floor, pose_graph_by_floor_[floor].size());
}

void PathNavigator::TryGeneratePathIfNeeded(const Point& camera_pos) {
    if (!goal_set_ || path_generated_ || goal_queue_.empty()) return;

    Point current_goal = goal_queue_.front();
    std::set<Point> obstacles = GenerateObstacles();

    path_ = astar(camera_pos, current_goal, obstacles);

    if (!path_.empty()) {
        path_generated_ = true;
        path_ready_to_render_ = true;
        arrival_ = false;
        LOGI("🚀 경로 생성 완료. 다음 목표: x=%.2f, z=%.2f", current_goal.x, current_goal.z);
        JavaBridge::SpeakText("경로 안내를 시작합니다.");
    } else {
        LOGI("❌ 경로 생성 실패");
    }
}


bool PathNavigator::UpdateNavigation(const Point& cam_pos, const float* matrix, DirectionHelper& direction_helper) {
    if (!goal_set_) {
        // LOGI("❌ 목적지(goal_)가 설정되지 않아 경로 확인 생략");
        return true;
    }

    if (current_path_index_ >= path_.size()) {
        if (!arrival_) {
//            JavaBridge::EnqueueAudio("arrival.m4a");
            JavaBridge::SpeakText("목적지에 도착하였습니다. 경로 안내를 종료합니다.");
            arrival_ = true;
        }

        // 상태 업데이트 메시지 전달
        char buffer[128];
        snprintf(buffer, sizeof(buffer), "목적지에 도착하였습니다");
        JavaBridge::UpdatePathStatus(buffer);

        return true;
    }

    if (current_path_index_ > 0 && current_path_index_ < path_.size() - 2) {
        if (notified_turn_indices_.find(current_path_index_) == notified_turn_indices_.end()) {
            Point prev = path_[current_path_index_];
            Point current = path_[current_path_index_ + 1];
            Point next = path_[current_path_index_ + 2];

            float dx1 = current.x - prev.x;
            float dz1 = current.z - prev.z;
            float dx2 = next.x - current.x;
            float dz2 = next.z - current.z;

            float dot = dx1 * dx2 + dz1 * dz2;
            float mag1 = std::sqrt(dx1 * dx1 + dz1 * dz1);
            float mag2 = std::sqrt(dx2 * dx2 + dz2 * dz2);

            if (mag1 > 0.01f && mag2 > 0.01f) {
                float angle_cos = dot / (mag1 * mag2);
                if (angle_cos < 0.85f) {
                    JavaBridge::SpeakText("곧 방향 회전이 있습니다. 진동이 나는 방향을 찾아주세요.");
                    notified_turn_indices_.insert(current_path_index_);
                }
            }
        }
    }

    Point target = path_[current_path_index_];
    float dx = cam_pos.x - target.x;
    float dz = cam_pos.z - target.z;
    float distance = std::sqrt(dx * dx + dz * dz);

    if (distance > kDeviationThreshold) {
        LOGI("🚨 경로 이탈 감지됨. 재탐색 시작");
        JavaBridge::SpeakText("경로를 이탈하였습니다. 경로를 재탐색합니다.");

        // 현재 goal_queue_는 유지하고, 상태만 초기화
        Reset();  // 경로, 상태, index 초기화
        path_generated_ = false;

        TryGeneratePathIfNeeded(cam_pos);  // 동일한 목표로 경로 재생성
        return false;
    }

    direction_helper.Check(matrix, cam_pos, target);

    if (distance < kReachThreshold) {
        direction_helper.Reset();
        current_path_index_++;
        LOGI("✅ 경로 지점 %d 도달", current_path_index_);

        // 마지막 경로 지점 도달 시 다음 목표 처리
        if (current_path_index_ >= path_.size()) {
            goal_queue_.pop();
            path_generated_ = false;
            current_path_index_ = 0;
            notified_turn_indices_.clear();

            if (!goal_queue_.empty()) {
                LOGI("➡️ 다음 목표로 이동합니다");
                JavaBridge::SpeakText("다음 목표로 이동합니다.");
                TryGeneratePathIfNeeded(cam_pos);
            } else {
                LOGI("✅ 모든 목표 도달 완료");
                JavaBridge::SpeakText("모든 목적지에 도착하였습니다.");
                goal_set_ = false;
            }
            return true;
        }
    }

    // 상태 업데이트 메시지 전달
    char buffer[128];
    snprintf(buffer, sizeof(buffer), "📍 현재 경로 지점 %d / %.2fm 남음", current_path_index_, distance);
    JavaBridge::UpdatePathStatus(buffer);

    return false;
}

bool PathNavigator::IsReadyToRender() const {
    return path_ready_to_render_;
}

void PathNavigator::SetReadyToRenderFalse() {
    path_ready_to_render_ = false;
}

const std::vector<Point>& PathNavigator::GetPath() const {
    return path_;
}

int PathNavigator::GetCurrentPathIndex() const {
    return current_path_index_;
}


void PathNavigator::Reset() {
    path_.clear();
    path_generated_ = false;
    path_ready_to_render_ = false;
    arrival_ = false;
    current_path_index_ = 0;
    notified_turn_indices_.clear();
}

std::set<Point> PathNavigator::GenerateObstacles() {
    std::set<Point> obstacles;

    std::vector<Point> outer = {
        {-11.5f, 1.8f}, {-11.5f, -20.25f}, {1.5f, -20.25f}, {1.5f, 1.8f}
    };
    std::vector<Point> inner = {
        {-8.58f, -0.6f}, {-8.58f, -15.89f}, {-1.49f, -15.89f}, {-1.49f, -0.6f}
    };

    for (int i = 0; i < outer.size(); ++i) {
        auto wall = generateWall(outer[i], outer[(i + 1) % outer.size()]);
        obstacles.insert(wall.begin(), wall.end());
    }
    for (int i = 0; i < inner.size(); ++i) {
        auto wall = generateWall(inner[i], inner[(i + 1) % inner.size()]);
        obstacles.insert(wall.begin(), wall.end());
    }

    return obstacles;
}
