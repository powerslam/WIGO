#include "types.h"
#include <cmath>

bool Point::operator<(const Point& other) const {
    if (std::abs(x - other.x) > 1e-4) return x < other.x;
    return z < other.z;
}