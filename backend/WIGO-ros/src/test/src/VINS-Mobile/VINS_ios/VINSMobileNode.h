#ifndef _VINSMobileNode_H_
#define _VINSMobileNode_H_

#include <queue>
#include "VINS.hpp"
#include <ros/ros.h>
#include "keyframe.h"
#include "utility.hpp"
#include "loop_closure.h"
#include "draw_result.hpp"
#include "global_param.hpp"
#include <sensor_msgs/Imu.h>
#include <opencv2/opencv.hpp>
#include "keyfame_database.h"
#include "feature_tracker.hpp"
#include <condition_variable>
#include <sensor_msgs/Image.h>
#include <cv_bridge/cv_bridge.h>

struct IMU_MSG {
    double header;
    Vector3d acc;
    Vector3d gyr;
};

struct IMG_MSG {
    double header;
    map<int, Vector3d> point_clouds;
};

struct IMG_DATA {
    double header;
};

struct IMG_DATA_CACHE {
    double header;
    cv::Mat image;
    
};

struct VINS_DATA_CACHE {
    double header;
    Vector3f P;
    Matrix3f R;
};

typedef shared_ptr <IMU_MSG const > ImuConstPtr;
typedef shared_ptr <IMG_MSG const > ImgConstPtr;

class VINSMobileNode {
public:
    ros::NodeHandle nh;
    
    ros::Subscriber img_sub;
    ros::Subscriber imu_sub;

public:
    bool isCapturing;
    cv::Ptr<FeatureTracker> feature_tracker;
    cv::Size frameSize;

    FeatureTracker featuretracker;
    VINS vins;

    queue<ImgConstPtr> img_msg_buf;
    queue<ImuConstPtr> imu_msg_buf;
    
    queue<IMU_MSG_LOCAL> local_imu_msg_buf;

    int waiting_lists = 0;

    int frame_cnt = 0;

    std::mutex m_buf;

    std::condition_variable con;

    double current_time = -1;
    double lateast_imu_time = -1;

    int imu_prepare = 0;

    // Segment the trajectory using color when re-initialize
    int segmentation_index = 0;

    // Set true:  30 HZ pose output and AR rendering in front-end (very low latency)
    // Set false: 10 HZ pose output and AR rendering in back-end
    bool USE_PNP = false;

    // Lock the solved VINS data feedback to featuretracker
    std::mutex m_depth_feedback;

    // Lock the IMU data feedback to featuretracker
    std::mutex m_imu_feedback;

    // Solved VINS feature feedback to featuretracker
    list<IMG_MSG_LOCAL> solved_features;

    // Solved VINS status feedback to featuretracker
    VINS_RESULT solved_vins;

    /******************************* Loop Closure ******************************/

    // Raw image data buffer for extracting FAST feature
    queue<pair<cv::Mat, double>> image_buf_loop;

    // Lock the image_buf_loop
    std::mutex m_image_buf_loop;

    // Detect loop
    LoopClosure *loop_closure;

    // Keyframe database
    KeyFrameDatabase keyframe_database;

    // Control the loop detection frequency
    int keyframe_freq = 0;

    // Index the keyframe
    int global_frame_cnt = 0;

    // Record the checked loop frame
    int loop_check_cnt = 0;

    // Indicate if breif vocabulary read finish
    bool voc_init_ok = false;

    // Indicate the loop frame index
    int old_index = -1;

    // Translation drift
    Eigen::Vector3d loop_correct_t = Eigen::Vector3d(0, 0, 0);

    // Rotation drift
    Eigen::Matrix3d loop_correct_r = Eigen::Matrix3d::Identity();

    /******************************* Loop Closure ******************************/

    // for func(process)
    int kf_global_index;
    bool start_global_optimization = false;

    bool imageCacheEnabled = true;

    // for process image
    queue<IMG_DATA_CACHE> image_pool;
    queue<VINS_DATA_CACHE> vins_pool;
    IMG_DATA_CACHE image_data_cache;
    cv::Mat lateast_equa;
    // UIImage *lateast_image;
    Vector3f lateast_P;
    Matrix3f lateast_R;

    cv::Mat pnp_image;
    Vector3d pnp_P;
    Matrix3d pnp_R;

    bool imuDataFinished = false;
    bool vinsDataFinished = false;
    vector<IMU_MSG> gyro_buf;  // for Interpolation

    const bool LOOP_CLOSURE = true;

public:
    VINSMobileNode();

    void imuStartUpdate(const sensor_msgs::ImuConstPtr& msg);
    
    // 0.01초 마다 실행되는 함수.
    void process();

    std::vector<std::pair<std::vector<ImuConstPtr>, ImgConstPtr>> getMeasurements();
    void send_imu(const ImuConstPtr &imu_msg);

    void processImage(const sensor_msgs::ImageConstPtr& msg);
    vector<IMU_MSG_LOCAL> getImuMeasurements(double header);

    void loop_thread();
    void global_loop_thread();
};

#endif