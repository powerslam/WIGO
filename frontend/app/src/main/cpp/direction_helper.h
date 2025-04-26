#pragma once

#include "types.h"

class DirectionHelper {
public:
    DirectionHelper();

    // 방향 체크 진행, 방향 일치하면 true 반환
    void Check(float yaw_deg, const Point& cam_pos, const Point& target);

    // 카메라 yaw 추출 (행렬 → 각도 변환)
    static float ExtractYawDeg(const float* pose_matrix);

    // 상태 초기화
    void Reset();

private:
    int match_count_;
    bool check_enabled_;
};
