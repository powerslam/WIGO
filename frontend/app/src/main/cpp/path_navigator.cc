#include "path_navigator.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "PathNavigator", __VA_ARGS__)

namespace {
    constexpr float kDeviationThreshold = 5.0f;
    constexpr float kReachThreshold = 0.8f;
    const Point kGoal{-10.0f, -18.0f};
}

PathNavigator::PathNavigator() {
    obstacles_ = GenerateObstacles();
}

void PathNavigator::TryGeneratePathIfNeeded(const Point& camera_pos) {
    if (path_generated_) return;

    std::set<Point> obstacles = GenerateObstacles();

    path_ = astar(camera_pos, kGoal, obstacles);

    if (!path_.empty()) {
        path_generated_ = true;
        path_ready_to_render_ = true;
        arrival_audio_played_ = false;
        LOGI("🚀 경로 탐색 성공!");

//        audio::PlayAudioFromAssets("start.m4a");
        JavaBridge::SpeakText("경로 안내를 시작합니다.");

    } else {
        LOGI("❌ 경로 탐색 실패");
    }
}

bool PathNavigator::UpdateNavigation(const Point& cam_pos, const float* matrix, DirectionHelper& direction_helper) {
    if (current_path_index_ >= path_.size()) {
        if (!arrival_audio_played_) {
//            audio::PlayAudioFromAssets("arrival.m4a");
            JavaBridge::SpeakText("목적지에 도착하였습니다. 경로 안내를 종료합니다.");
            arrival_audio_played_ = true;
        }

        // 상태 업데이트 메시지 전달
        char buffer[128];
        snprintf(buffer, sizeof(buffer), "목적지에 도착하였습니다");
        JavaBridge::UpdatePathStatus(buffer);

        return true;
    }

    Point target = path_[current_path_index_];
    float dx = cam_pos.x - target.x;
    float dz = cam_pos.z - target.z;
    float distance = std::sqrt(dx * dx + dz * dz);

    if (distance > kDeviationThreshold) {
        LOGI("🚨 경로 이탈 감지됨. 재탐색 시작");
        audio::PlayAudioFromAssets("deviation.m4a");
        Reset();
        TryGeneratePathIfNeeded(cam_pos);
        return false;
    }

    direction_helper.Check(matrix, cam_pos, target);

    if (distance < kReachThreshold) {
        direction_helper.Reset();
        current_path_index_++;
        LOGI("✅ 경로 지점 %d 도달", current_path_index_);
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


void PathNavigator::Reset() {
    path_.clear();
    path_generated_ = false;
    path_ready_to_render_ = false;
    arrival_audio_played_ = false;
    current_path_index_ = 0;
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
