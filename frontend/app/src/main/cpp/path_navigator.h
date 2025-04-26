#pragma once

#include <vector>
#include <set>
#include "types.h"
#include "direction_helper.h"

class PathNavigator {
public:
    PathNavigator();

    void TryGeneratePathIfNeeded(const Point& camera_pos);
    bool UpdateNavigation(const Point& cam_pos, float* pose_raw, DirectionHelper& direction_helper);
    bool IsReadyToRender() const;

    const std::vector<Point>& GetPath() const;
    bool HasPath() const;

    void Reset();

private:
    std::vector<Point> path_;
    bool path_generated_ = false;
    bool path_ready_to_render_ = false;
    bool arrival_audio_played_ = false;
    bool start_flag_ = false;

    int current_path_index_ = 0;

    std::set<Point> GenerateObstacles();
};
