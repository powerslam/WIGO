#include "direction_helper.h"
#include "java_bridge.h"
#include <cmath>
#include <glm/glm.hpp>

namespace {
    constexpr float kDirectionThreshold = 25.0f;
    constexpr int kRequiredMatchCount = 10;
}

DirectionHelper::DirectionHelper()
    : match_count_(0), check_enabled_(true) {}

void DirectionHelper::Reset() {
    match_count_ = 0;
    check_enabled_ = true;
}

void DirectionHelper::Check(const float* matrix, const Point& cam_pos, const Point& target) {
    glm::vec3 forward(-matrix[8], -matrix[9], -matrix[10]);
    float yaw_rad = std::atan2(forward.x, forward.z);
    float yaw_deg = glm::degrees(yaw_rad);
    if (yaw_deg < 0) yaw_deg += 360.0f;

    last_camera_yaw_ = yaw_deg;

    float dx = target.x - cam_pos.x;
    float dz = target.z - cam_pos.z;
    float path_deg = std::atan2(dx, dz) * 180.0f / M_PI;
    if (path_deg < 0) path_deg += 360.f;
    last_path_yaw_ = path_deg;

    float angle_diff = std::fabs(last_camera_yaw_ - last_path_yaw_);
    if (angle_diff > 180.f) angle_diff = 360.f - angle_diff;

    if (check_enabled_) {
        if (angle_diff < kDirectionThreshold) {
            match_count_++;
            if (match_count_ >= kRequiredMatchCount) {
                check_enabled_ = false;
                JavaBridge::VibrateOnce();
            } else {
            }
        } else {
            match_count_ = 0;
        }
    } else {
        if (angle_diff > kDirectionThreshold) {
            check_enabled_ = true;
            match_count_ = 0;
        }
    }
}

float DirectionHelper::GetLastCameraYaw() const {
    return last_camera_yaw_;
}

float DirectionHelper::GetLastPathYaw() const {
    return last_path_yaw_;
}


