#pragma once

#include "util.h"
#include <vector>
#include <map>

class AStarPathfinder {
public:
    void LoadPoseGraph(const std::string& path, int floor);
    std::vector<Point> astar(const Point& start, const Point& goal, float step_size = 1.0f);

private:
    std::map<int, std::map<int, Point>> pose_graph_by_floor_;
};

float heuristic(const Point& a, const Point& b);