#pragma once

#include <glm/glm.hpp>

class PoseHelper {
public:
    // 카메라 포지션(x, y, z)만 가져오는 함수
    static glm::vec3 GetCameraPosition(const float* pose_raw);
};
