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

void DirectionHelper::Check(float yaw_deg, const Point& cam_pos, const Point& target) {
    float dx = target.x - cam_pos.x;
    float dz = target.z - cam_pos.z;
    float path_deg = glm::degrees(std::atan2(dx, dz));
    if (path_deg < 0) path_deg += 360.f;

    float angle_diff = std::fabs(yaw_deg - path_deg);
    if (angle_diff > 180.f) angle_diff = 360.f - angle_diff;

    JavaBridge::UpdateYaw(yaw_deg, path_deg);

    if (check_enabled_) {
        if (angle_diff < kDirectionThreshold) {
            match_count_++;
            if (match_count_ >= kRequiredMatchCount) {
                check_enabled_ = false;
                JavaBridge::VibrateOnce();
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

float DirectionHelper::ExtractYawDeg(const float* matrix) {
    glm::vec3 forward(-matrix[8], -matrix[9], -matrix[10]);
    float yaw_rad = std::atan2(forward.x, forward.z);
    float yaw_deg = glm::degrees(yaw_rad);
    if (yaw_deg < 0) yaw_deg += 360.f;
    return yaw_deg;
}
