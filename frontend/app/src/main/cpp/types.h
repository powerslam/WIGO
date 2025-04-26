#pragma once

struct Point {
    float x, z;
    bool operator<(const Point& other) const;
};