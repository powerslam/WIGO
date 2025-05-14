#pragma once

#include "util.h"
#include <vector>
#include <set>

float heuristic(const Point& a, const Point& b);
std::vector<Point> astar(const Point& start, const Point& goal, float step_size=1.0f);
