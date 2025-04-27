#include "pose_helper.h"

glm::vec3 PoseHelper::GetCameraPosition(const float* pose_raw) {
    return {pose_raw[4], pose_raw[5], pose_raw[6]};
}
