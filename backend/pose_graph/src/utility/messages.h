#include <vector>

#include <eigen3/Eigen/Core>
#include <opencv2/opencv.hpp>

namespace message {
struct ImgMSG {
    double stamp;
    cv::Mat image;
};

struct PoseMSG {
    double stamp;
    Eigen::Vector3d position;
    Eigen::Matrix3d orientation;
};

struct PointMSG {
    double stamp;
    std::vector<cv::Point3f> point3d;
    std::vector<cv::Point3f> point2d_uv;
    std::vector<cv::Point3f> point2d_normal;
    std::vector<double> point_id;
};
}

// 예시
// typedef boost::shared_ptr< ::morai_msgs::Obstacle const> ObstacleConstPtr;
