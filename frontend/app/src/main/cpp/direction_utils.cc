#include "direction_utils.h"
#include <cmath>

namespace nav {

    std::string GetTurnAudioFile(float angle) {
        // 뒤돌기 감지: 160도 이상 회전
        if (std::abs(angle) >= 160.0f) return "turn_back.m4a";

        if (std::abs(angle) < 20.0f) return "straight.m4a";  // 직진은 무시

        if (angle >= 20.0f && angle < 60.0f) return "slight_left.m4a";
        if (angle >= 60.0f && angle < 160.0f) return "left.m4a";

        if (angle <= -20.0f && angle > -60.0f) return "slight_right.m4a";
        if (angle <= -60.0f && angle > -160.0f) return "right.m4a";

        return "";  // 그 외는 무시
    }

}