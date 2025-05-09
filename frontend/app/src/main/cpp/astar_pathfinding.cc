#include "astar_pathfinding.h"
#include <cmath>
#include <algorithm>
#include <memory>
#include <queue>

float heuristic(const Point& a, const Point& b) {
    return std::hypot(a.x - b.x, a.z - b.z);
}

bool isObstacle(const Point& pos, const std::set<Point>& obstacles, float radius) {
    for (const auto& obs : obstacles) {
        if (heuristic(pos, obs) < radius)
            return true;
    }
    return false;
}

std::vector<Point> generateWall(const Point& start, const Point& end, float step) {
    std::vector<Point> wall;
    float dx = end.x - start.x;
    float dz = end.z - start.z;
    float dist = std::hypot(dx, dz);
    int steps = static_cast<int>(dist / step);
    for (int i = 0; i <= steps; ++i) {
        float x = std::round((start.x + dx * i / steps) * 100.0f) / 100.0f;
        float z = std::round((start.z + dz * i / steps) * 100.0f) / 100.0f;
        wall.push_back({x, z});
    }
    return wall;
}

std::vector<Point> astar(const Point& start, const Point& goal, const std::set<Point>& obstacles, float step_size) {
    struct Node {
        Point pos;
        std::shared_ptr<Node> parent;
        float g, h, f;
        Node(Point p, std::shared_ptr<Node> par = nullptr)
                : pos(p), parent(par), g(0), h(0), f(0) {}
        bool operator>(const Node& other) const { return f > other.f; }
    };

    std::priority_queue<Node, std::vector<Node>, std::greater<Node>> open;
    std::set<Point> closed;
    open.emplace(start);

    while (!open.empty()) {
        Node current = open.top();
        open.pop();

        if (closed.find(current.pos) != closed.end()) continue;
        closed.insert(current.pos);

        if (heuristic(current.pos, goal) < step_size) {
            std::vector<Point> path;
            for (auto node = std::make_shared<Node>(current); node; node = node->parent)
                path.push_back(node->pos);
            std::reverse(path.begin(), path.end());
            return path;
        }

        for (float dx : {-step_size, 0.f, step_size}) {
            for (float dz : {-step_size, 0.f, step_size}) {
                if (dx == 0 && dz == 0) continue;
                Point next {
                        std::round((current.pos.x + dx) * 100.0f) / 100.0f,
                        std::round((current.pos.z + dz) * 100.0f) / 100.0f
                };

                if (isObstacle(next, obstacles, 0.5f) || closed.count(next)) continue;

                Node neighbor(next, std::make_shared<Node>(current));
                neighbor.g = current.g + step_size;
                neighbor.h = heuristic(next, goal);
                neighbor.f = neighbor.g + neighbor.h;
                open.push(neighbor);
            }
        }
    }

    return {};
}
