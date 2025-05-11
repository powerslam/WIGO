#pragma once

#include <vector>
#include <set>
#include "util.h"
#include "direction_helper.h"
#include "astar_pathfinding.h"
#include "java_bridge.h"
#include <cmath>
#include <android/log.h>
#include <glm/glm.hpp>

class PathNavigator {
public:
    PathNavigator();

    void SetGoal(const Point& goal);
    void TryGeneratePathIfNeeded(const Point& camera_pos);
    bool UpdateNavigation(const Point& cam_pos, const float* pose_raw, DirectionHelper& direction_helper);
    bool IsReadyToRender() const;
    void SetReadyToRenderFalse();
    const std::vector<Point>& GetPath() const;
    int GetCurrentPathIndex() const;
    void Reset();
    bool IsGoalSet() const { return goal_set_; }

    bool arrival_ = false;

private:
    Point goal_ = {9999.0f, 9999.0f};

    std::vector<Point> path_;
    bool path_generated_ = false;
    bool path_ready_to_render_ = false;
    int current_path_index_ = 0;

    bool goal_set_ = false;

    std::set<Point> obstacles_;  

    std::set<Point> GenerateObstacles();
};
