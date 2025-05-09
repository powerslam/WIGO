#pragma once

#include "util.h"

class DirectionHelper {
public:
    DirectionHelper();

    // 방향 체크 진행, 방향 일치하면 true 반환
    void Check(const float* matrix, const Point& cam_pos, const Point& target);


    float GetLastCameraYaw() const;
    float GetLastPathYaw() const;

    // 상태 초기화
    void Reset();

private:
    int match_count_;
    bool check_enabled_;

    float last_camera_yaw_ = 0.0f;
    float last_path_yaw_ = 0.0f;
};
