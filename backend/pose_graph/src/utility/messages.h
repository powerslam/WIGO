#include <vector>
#include <memory>

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

typedef std::shared_ptr<ImgMSG const> ImgMSGConstPtr;
typedef std::shared_ptr<PoseMSG const> PoseMSGConstPtr;
typedef std::shared_ptr<PointMSG const> PointMSGConstPtr;

}

