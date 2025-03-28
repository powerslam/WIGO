#pragma once
#include <vector>
#include <set>

struct Point {
    float x, z;
    bool operator<(const Point& other) const;
};

float heuristic(const Point& a, const Point& b);
bool isObstacle(const Point& pos, const std::set<Point>& obstacles, float radius = 1.0f);
std::vector<Point> generateWall(const Point& start, const Point& end, float step = 0.1f);
std::vector<Point> astar(const Point& start, const Point& goal, const std::set<Point>& obstacles, float step_size = 0.5f);
