#include "VINSMobileNode.h"

VINSMobileNode::VINSMobileNode():
    vins(), featuretracker()
{
    if(!feature_tracker)
        feature_tracker = new FeatureTracker();

    vins.setIMUModel();
    
    // /****************************************Init all the thread****************************************/
    // _condition=[[NSCondition alloc] init];
    // mainLoop=[[NSThread alloc]initWithTarget:self selector:@selector(run) object:nil];
    // [mainLoop setName:@"mainLoop"];
    // @@@@ -> process 함수를 0.01초 마다 실행함.

    // saveData=[[NSThread alloc]initWithTarget:self selector:@selector(saveData) object:nil];
    // [saveData setName:@"saveData"];

    // if(LOOP_CLOSURE)
    // {
    //     //loop closure thread
    //     loop_thread = [[NSThread alloc]initWithTarget:self selector:@selector(loop_thread) object:nil];
    //     [loop_thread setName:@"loop_thread"];
    //     [loop_thread start];
        
    //     globalLoopThread=[[NSThread alloc]initWithTarget:self selector:@selector(globalLoopThread) object:nil];
    //     [globalLoopThread setName:@"globalLoopThread"];
    //     [globalLoopThread start];
    // }

    bool deviceCheck = setGlobalParam(DeviceType::unDefine);
    vins.setExtrinsic();
    vins.setIMUModel();
    featuretracker.vins_pnp.setExtrinsic();
    featuretracker.vins_pnp.setIMUModel();
    
    isCapturing = true;

    img_sub = nh.subscribe("/usb_cam/image_raw", 1, &VINSMobileNode::processImage, this);
    imu_sub = nh.subscribe("/handsfree/imu", 1, &VINSMobileNode::imuStartUpdate, this);
}

void VINSMobileNode::processImage(const sensor_msgs::ImageConstPtr& msg){
    cv_bridge::CvImagePtr cv_ptr = cv_bridge::toCvCopy(msg, sensor_msgs::image_encodings::BGR8);
    cv::Mat image = cv_ptr->image;

    if(isCapturing){
        //NSLog(@"image processing");
        float lowPart = image.at<float>(0,0);  //modify opencv library, timestamp was stored at index 0,0
        float highPart = image.at<float>(0,1);
        //image.at<float>(0,0) = image.at<float>(1,0);
        //image.at<float>(0,1) = image.at<float>(1,1);
        shared_ptr<IMG_MSG> img_msg(new IMG_MSG());
        //cout << (videoCamera->grayscaleMode) << endl;
        //img_msg->header = [[NSDate date] timeIntervalSince1970];
        img_msg->header = ros::Time::now().toNSec();
        float Group[2];
        Group[0] = lowPart;
        Group[1] = highPart;
        double* time_now_decode = (double*)Group;
        double time_stamp = *time_now_decode;
        
        if(lateast_imu_time <= 0)
        {
            cv::cvtColor(image, image, CV_BGRA2RGB);
            cv::flip(image,image,-1);
            return;
        }
        //img_msg->header = lateast_imu_time;
        img_msg->header = time_stamp;
        bool isNeedRotation = image.size() != frameSize;
        
        cv::Mat gray;
        cv::cvtColor(image, gray, CV_RGBA2GRAY);

        cv::Mat img_with_feature;
        cv::Mat img_equa;
        cv::Ptr<cv::CLAHE> clahe = cv::createCLAHE();
        clahe->setClipLimit(3);
        clahe->apply(gray, img_equa);
        
        TS(time_feature);
        
        m_depth_feedback.lock();
        featuretracker.solved_features = solved_features;
        featuretracker.solved_vins = solved_vins;
        m_depth_feedback.unlock();
        
        m_imu_feedback.lock();
        featuretracker.imu_msgs = getImuMeasurements(img_msg->header);
        m_imu_feedback.unlock();
        
        vector<Point2f> good_pts;
        vector<double> track_len;
        bool vins_normal = (vins.solver_flag == VINS::NON_LINEAR);
        featuretracker.use_pnp = USE_PNP;
        featuretracker.readImage(img_equa, img_with_feature,frame_cnt, good_pts, track_len, img_msg->header, pnp_P, pnp_R, vins_normal);
        TE(time_feature);
        
        //cvtColor(img_equa, img_equa, CV_GRAY2BGR);
        // keypoints 마다 점 찍기
        for (int i = 0; i < good_pts.size(); i++)
        {
            cv::circle(image, good_pts[i], 0, cv::Scalar(255 * (1 - track_len[i]), 0, 255 * track_len[i]), 7); //BGR
        }
        
        //image msg buf
        if(featuretracker.img_cnt==0)
        {
            img_msg->point_clouds = featuretracker.image_msg;
            //img_msg callback
            m_buf.lock();
            img_msg_buf.push(img_msg);
            //NSLog(@"Img timestamp %lf",img_msg_buf.front()->header);
            m_buf.unlock();
            con.notify_one();
            if(imageCacheEnabled)
            {
                image_data_cache.header = img_msg->header;
                image_data_cache.image = image;
                image_pool.push(image_data_cache);
            }
            
            if(LOOP_CLOSURE)
            {
                m_image_buf_loop.lock();
                cv::Mat loop_image = gray.clone();
                image_buf_loop.push(make_pair(loop_image, img_msg->header));
                if(image_buf_loop.size() > WINDOW_SIZE)
                    image_buf_loop.pop();
                m_image_buf_loop.unlock();
            }
        }
        
        featuretracker.img_cnt = (featuretracker.img_cnt + 1) % FREQ;
        for (int i = 0; i < good_pts.size(); i++)
        {
            cv::circle(image, good_pts[i], 0, cv::Scalar(255 * (1 - track_len[i]), 0, 255 * track_len[i]), 7); //BGR
        }

        cv::imshow("hihi", image);
        cv::waitKey(1);

        TS(visualize);
        if(imageCacheEnabled)
        {
            //use aligned vins and image
            if(!vins_pool.empty() && !image_pool.empty())
            {
                while(vins_pool.size() > 1)
                {
                    vins_pool.pop();
                }
                while(!image_pool.empty() && image_pool.front().header < vins_pool.front().header)
                {
                    image_pool.pop();
                }
                if(!vins_pool.empty() && !image_pool.empty())
                {
                    image = image_pool.front().image;
                    lateast_P = vins_pool.front().P;
                    lateast_R = vins_pool.front().R;
                }
            }
            else if(!image_pool.empty())
            {
                if(image_pool.size() > 10)
                    image_pool.pop();
            }
        }
        if(USE_PNP)
        {
            lateast_P = pnp_P.cast<float>();
            lateast_R = pnp_R.cast<float>();
        }
        if(vins.solver_flag != VINS::NON_LINEAR)  //show image and AR
        {
            cv::Mat tmp2;
            if(vins.solver_flag == VINS::NON_LINEAR)
            {
                cv::Mat tmp;
                vins.drawresult.startInit = true;
                vins.drawresult.drawAR(vins.imageAI, vins.correct_point_cloud, lateast_P, lateast_R);
                
                cv::cvtColor(image, tmp, CV_RGBA2BGR);
                cv::Mat mask;
                cv::Mat imageAI = vins.imageAI;
                if(!imageAI.empty())
                    cv::cvtColor(imageAI, mask, CV_RGB2GRAY);
                imageAI.copyTo(tmp,mask);
                cv::cvtColor(tmp, image, CV_BGRA2BGR);
            }
            if(VINS_DEBUG_MODE)
            {
                cv::flip(lateast_equa, image, -1);
            }
            else
            {
                cv::flip(image,tmp2,-1);
                image = tmp2;
                if(vins.solver_flag != VINS::NON_LINEAR)
                    cv::cvtColor(image, image, CV_RGBA2BGR);
            }
        }
        else //show VINS
        {
            if(vins.solver_flag == VINS::NON_LINEAR)
            {
                vins.drawresult.pose.clear();
                vins.drawresult.pose = keyframe_database.refine_path;
                vins.drawresult.segment_indexs = keyframe_database.segment_indexs;
                // vins.drawresult.Reprojection(vins.image_show, vins.correct_point_cloud, vins.correct_Rs, vins.correct_Ps, box_in_trajectory);
                vins.drawresult.Reprojection(vins.image_show, vins.correct_point_cloud, vins.correct_Rs, vins.correct_Ps, true);
            }
            cv::Mat tmp2 = vins.image_show;
            
            cv::Mat down_origin_image;
            cv::resize(image.t(), down_origin_image, cv::Size(200, 150));
            cv::cvtColor(down_origin_image, down_origin_image, CV_BGRA2RGB);
            cv::flip(down_origin_image,down_origin_image,0);
            cv::Mat imageROI;
            imageROI = tmp2(cv::Rect(10,COL - down_origin_image.rows- 10, down_origin_image.cols,down_origin_image.rows));
            cv::Mat mask;
            cv::cvtColor(down_origin_image, mask, CV_RGB2GRAY);
            down_origin_image.copyTo(imageROI, mask);
            
            
            cv::cvtColor(tmp2, image, CV_BGRA2BGR);
            cv::flip(image,tmp2,1);
            if (isNeedRotation)
                image = tmp2.t();
        }
        
        TE(visualize);
    } else {
        // Not capturing, means not started yet
        cv::cvtColor(image, image, CV_BGRA2RGB);
        cv::flip(image,image,-1);
        //BOOL isNeedRotation = image.size() != frameSize;
        //if (isNeedRotation)
        //    image = image.t();
    }
}

void VINSMobileNode::imuStartUpdate(const sensor_msgs::ImuConstPtr& msg){
    //interpolation
    shared_ptr<IMU_MSG> imu_msg(new IMU_MSG());
    imu_msg->header = msg->header.stamp.toNSec();

    imu_msg->acc << -msg->linear_acceleration.x * GRAVITY,
    -msg->linear_acceleration.y * GRAVITY,
    -msg->linear_acceleration.z * GRAVITY;

    imu_msg->gyr << -msg->angular_velocity.x * GRAVITY,
    -msg->angular_velocity.y * GRAVITY,
    -msg->angular_velocity.z * GRAVITY;

    //img_msg callback
    {
        IMU_MSG_LOCAL imu_msg_local;
        imu_msg_local.header = imu_msg->header;
        imu_msg_local.acc = imu_msg->acc;
        imu_msg_local.gyr = imu_msg->gyr;
        
        m_imu_feedback.lock();
        local_imu_msg_buf.push(imu_msg_local);
        m_imu_feedback.unlock();
    }

    m_buf.lock();
    imu_msg_buf.push(imu_msg);
    //NSLog(@"IMU_buf timestamp %lf, acc_x = %lf",imu_msg_buf.front()->header,imu_msg_buf.front()->acc.x());
    m_buf.unlock();
    con.notify_one();
}

vector<IMU_MSG_LOCAL> VINSMobileNode::getImuMeasurements(double header){
    vector<IMU_MSG_LOCAL> imu_measurements;
    static double last_header = -1;
    if(last_header < 0 || local_imu_msg_buf.empty())
    {
        last_header = header;
        return imu_measurements;
    }
    
    while(!local_imu_msg_buf.empty() && local_imu_msg_buf.front().header <= last_header)
        local_imu_msg_buf.pop();

    while(!local_imu_msg_buf.empty() && local_imu_msg_buf.front().header <= header)
    {
        imu_measurements.emplace_back(local_imu_msg_buf.front());
        local_imu_msg_buf.pop();
    }

    last_header = header;
    return imu_measurements;
}

std::vector<std::pair<std::vector<ImuConstPtr>, ImgConstPtr>>
VINSMobileNode::getMeasurements()
{
    // printf("enter\n");
    std::vector<std::pair<std::vector<ImuConstPtr>, ImgConstPtr>> measurements;
    while (true)
    {
        if (imu_msg_buf.empty() || img_msg_buf.empty())
            return measurements;
        
        if (!(imu_msg_buf.back()->header > img_msg_buf.front()->header))
        {
            ROS_INFO("wait for imu, only should happen at the beginning");
            return measurements;
        }
        
        if (!(imu_msg_buf.front()->header < img_msg_buf.front()->header))
        {
            ROS_INFO("throw img, only should happen at the beginning");
            img_msg_buf.pop();
            continue;
        }
        ImgConstPtr img_msg = img_msg_buf.front();
        img_msg_buf.pop();
        
        std::vector<ImuConstPtr> IMUs;
        while (imu_msg_buf.front()->header <= img_msg->header)
        {
            IMUs.emplace_back(imu_msg_buf.front());
            imu_msg_buf.pop();
        }
        //NSLog(@"IMU_buf = %d",IMUs.size());
        measurements.emplace_back(IMUs, img_msg);
    }
    return measurements;
}

void VINSMobileNode::send_imu(const ImuConstPtr &imu_msg)
{
    double t = imu_msg->header;
    if (current_time < 0)
        current_time = t;
    double dt = (t - current_time);
    current_time = t;
    
    double ba[]{0.0, 0.0, 0.0};
    double bg[]{0.0, 0.0, 0.0};
    
    double dx = imu_msg->acc.x() - ba[0];
    double dy = imu_msg->acc.y() - ba[1];
    double dz = imu_msg->acc.z() - ba[2];
    
    double rx = imu_msg->gyr.x() - bg[0];
    double ry = imu_msg->gyr.y() - bg[1];
    double rz = imu_msg->gyr.z() - bg[2];
    //NSLog(@"IMU %f, dt: %f, acc: %f %f %f, gyr: %f %f %f", t, dt, dx, dy, dz, rx, ry, rz);
    
    vins.processIMU(dt, Vector3d(dx, dy, dz), Vector3d(rx, ry, rz));
}

void VINSMobileNode::process(){
    std::vector<std::pair<std::vector<ImuConstPtr>, ImgConstPtr>> measurements;
    // std::unique_lock<std::mutex> lk(m_buf);
    measurements = getMeasurements();
    // con.wait(lk, [&]
    //     {
    //         measurements = getMeasurements();
    //         printf("measurements size : %d", measurements.size());
    //         return measurements.size() != 0;
    //     });
    // lk.unlock();

    if(measurements.size() == 0){
        return;
    }

    waiting_lists = measurements.size();
    for(auto &measurement : measurements)
    {
        for(auto &imu_msg : measurement.first)
        {
            send_imu(imu_msg);
        }
        
        auto img_msg = measurement.second;
        map<int, Vector3d> image = img_msg->point_clouds;
        
        double header = img_msg->header;
        
        TS(process_image);
        vins.processImage(image,header,waiting_lists);
        TE(process_image);
        
        double time_now = ros::Time::now().toNSec();
        double time_vins = vins.Headers[WINDOW_SIZE];
        ROS_INFO("vins delay %lf", time_now - time_vins);
        
        //update feature position for front-end
        if(vins.solver_flag == vins.NON_LINEAR)
        {
            m_depth_feedback.lock();
            solved_vins.header = vins.Headers[WINDOW_SIZE - 1];
            solved_vins.Ba = vins.Bas[WINDOW_SIZE - 1];
            solved_vins.Bg = vins.Bgs[WINDOW_SIZE - 1];
            solved_vins.P = vins.correct_Ps[WINDOW_SIZE-1].cast<double>();
            solved_vins.R = vins.correct_Rs[WINDOW_SIZE-1].cast<double>();
            solved_vins.V = vins.Vs[WINDOW_SIZE - 1];
            Vector3d R_ypr = Utility::R2ypr(solved_vins.R);
            solved_features.clear();
            for (auto &it_per_id : vins.f_manager.feature)
            {
                it_per_id.used_num = it_per_id.feature_per_frame.size();
                if (!(it_per_id.used_num >= 2 && it_per_id.start_frame < WINDOW_SIZE - 2))
                    continue;
                
                if (it_per_id.solve_flag != 1)
                    continue;
                
                int imu_i = it_per_id.start_frame;
                
                Vector3d pts_i = it_per_id.feature_per_frame[0].point * it_per_id.estimated_depth;
                IMG_MSG_LOCAL tmp_feature;
                
                tmp_feature.id = it_per_id.feature_id;
                tmp_feature.position = vins.r_drift * vins.Rs[imu_i] * (vins.ric * pts_i + vins.tic) + vins.r_drift * vins.Ps[imu_i] + vins.t_drift;
                tmp_feature.track_num = (int)it_per_id.feature_per_frame.size();
                
                solved_features.push_back(tmp_feature);
            }
            
            m_depth_feedback.unlock();
        }
        
        if(this->imageCacheEnabled)
        {
            //add state into vins buff for alignwith image
            if(vins.solver_flag == VINS::NON_LINEAR)
            {
                VINS_DATA_CACHE vins_data_cache;
                vins_data_cache.header = vins.Headers[WINDOW_SIZE-1];
                vins_data_cache.P = vins.correct_Ps[WINDOW_SIZE-1];
                vins_data_cache.R = vins.correct_Rs[WINDOW_SIZE-1];
                vins_pool.push(vins_data_cache);
            }
            else if(vins.failure_occur == true)
            {
                vins.drawresult.change_color = true;
                vins.drawresult.indexs.push_back(vins.drawresult.pose.size());
                segmentation_index++;
                keyframe_database.max_seg_index++;
                keyframe_database.cur_seg_index = keyframe_database.max_seg_index;
                
                while(!vins_pool.empty())
                    vins_pool.pop();
            }
        }
        /**
         *** start build keyframe database for loop closure
         **/
        if(LOOP_CLOSURE)
        {
            static bool first_frame = true;
            if(vins.solver_flag != vins.NON_LINEAR)
                first_frame = true;
            if(vins.marginalization_flag == vins.MARGIN_OLD && vins.solver_flag == vins.NON_LINEAR && !image_buf_loop.empty())
            {
                first_frame = false;
                if(!first_frame && keyframe_freq % LOOP_FREQ == 0)
                {
                    keyframe_freq = 0;
                    /**
                     ** save the newest keyframe to the keyframe database
                     ** only need to save the pose to the keyframe database
                     **/
                    Vector3d T_w_i = vins.Ps[WINDOW_SIZE - 2];
                    Matrix3d R_w_i = vins.Rs[WINDOW_SIZE - 2];
                    m_image_buf_loop.lock();
                    while(!image_buf_loop.empty() && image_buf_loop.front().second < vins.Headers[WINDOW_SIZE - 2])
                    {
                        image_buf_loop.pop();
                    }
                    //assert(vins.Headers[WINDOW_SIZE - 2] == image_buf_loop.front().second);
                    if(vins.Headers[WINDOW_SIZE - 2] == image_buf_loop.front().second)
                    {
                        // TODO: library로 바꿀 때에는 사용 환경(ROS, android) 에 맞추어 값이 들어가도록 수정해야 함. 
                        const char *pattern_file = "/home/foscar/capstone-2025-11/backend/test/src/VINS-Mobile/Resources/brief_pattern.yml";
                        KeyFrame* keyframe = new KeyFrame(vins.Headers[WINDOW_SIZE - 2], global_frame_cnt, T_w_i, R_w_i, image_buf_loop.front().first, pattern_file, keyframe_database.cur_seg_index);
                        keyframe->setExtrinsic(vins.tic, vins.ric);
                        /*
                         ** we still need save the measurement to the keyframe(not database) for add connection with looped old pose
                         ** and save the pointcloud to the keyframe for reprojection search correspondance
                         */
                        keyframe->buildKeyFrameFeatures(vins);
                        keyframe_database.add(keyframe);
                        
                        global_frame_cnt++;
                    }
                    m_image_buf_loop.unlock();
                    
                }
                else
                {
                    first_frame = false;
                }
                // update loop info
                for (int i = 0; i < WINDOW_SIZE; i++)
                {
                    if(vins.Headers[i] == vins.front_pose.header)
                    {
                        KeyFrame* cur_kf = keyframe_database.getKeyframe(vins.front_pose.cur_index);
                        if (abs(vins.front_pose.relative_yaw) > 30.0 || vins.front_pose.relative_t.norm() > 10.0)
                        {
                            printf("Wrong loop\n");
                            cur_kf->removeLoop();
                            break;
                        }
                        cur_kf->updateLoopConnection(vins.front_pose.relative_t,
                                                     vins.front_pose.relative_q,
                                                     vins.front_pose.relative_yaw);
                        break;
                    }
                }
                /*
                 ** update the keyframe pose when this frame slides out the window and optimize loop graph
                 */
                int search_cnt = 0;
                for(int i = 0; i < keyframe_database.size(); i++)
                {
                    search_cnt++;
                    KeyFrame* kf = keyframe_database.getLastKeyframe(i);
                    if(kf->header == vins.Headers[0])
                    {
                        kf->updateOriginPose(vins.Ps[0], vins.Rs[0]);
                        //update edge
                        // if loop happens in this frame, update pose graph;
                        if (kf->has_loop)
                        {
                            kf_global_index = kf->global_index;
                            start_global_optimization = true;
                        }
                        break;
                    }
                    else
                    {
                        if(search_cnt > 2 * WINDOW_SIZE)
                            break;
                    }
                }
                keyframe_freq++;
            }
        }
        waiting_lists--;    
    }
}

void VINSMobileNode::loop_thread(){
    if(LOOP_CLOSURE && loop_closure == NULL)
    {
        ROS_INFO("loop start load voc");
        TS(load_voc);
        const char *voc_file = "/home/foscar/capstone-2025-11/backend/test/src/VINS-Mobile/Resources/brief_k10L6.bin";
        loop_closure = new LoopClosure(voc_file, COL, ROW);
        TE(load_voc);
        
        ROS_INFO("loop load voc finish");
        voc_init_ok = true;
    }

    // while(![[NSThread currentThread] isCancelled] )
    // {
        // if(!LOOP_CLOSURE)
        // {
        //     [NSThread sleepForTimeInterval:0.5];
        //     continue;
        // }
        
        bool loop_succ = false;
        if (loop_check_cnt < global_frame_cnt)
        {
            KeyFrame* cur_kf = keyframe_database.getLastKeyframe();
            //assert(loop_check_cnt == cur_kf->global_index);
            loop_check_cnt++;
            cur_kf->check_loop = 1;
            
            cv::Mat current_image;
            current_image = cur_kf->image;
            
            std::vector<cv::Point2f> measurements_old;
            std::vector<cv::Point2f> measurements_old_norm;
            std::vector<cv::Point2f> measurements_cur;
            std::vector<int> features_id;
            std::vector<cv::Point2f> measurements_cur_origin = cur_kf->measurements;
            
            vector<cv::Point2f> cur_pts;
            vector<cv::Point2f> old_pts;
            cur_kf->extractBrief(current_image);
            printf("loop extract %d feature\n", cur_kf->keypoints.size());
            loop_succ = loop_closure->startLoopClosure(cur_kf->keypoints, cur_kf->descriptors, cur_pts, old_pts, old_index);
            if(loop_succ)
            {
                KeyFrame* old_kf = keyframe_database.getKeyframe(old_index);
                if (old_kf == NULL)
                {
                    printf("NO such %dth frame in keyframe_database\n", old_index);
                    assert(false);
                }
                printf("loop succ with %drd image\n", old_index);
                assert(old_index!=-1);
                
                Vector3d T_w_i_old;
                Matrix3d R_w_i_old;
                
                old_kf->getPose(T_w_i_old, R_w_i_old);
                cur_kf->findConnectionWithOldFrame(old_kf, cur_pts, old_pts,
                                                   measurements_old, measurements_old_norm);
                measurements_cur = cur_kf->measurements;
                features_id = cur_kf->features_id;
                
                if(measurements_old_norm.size()>MIN_LOOP_NUM)
                {
                    
                    Quaterniond Q_loop_old(R_w_i_old);
                    RetriveData retrive_data;
                    retrive_data.cur_index = cur_kf->global_index;
                    retrive_data.header = cur_kf->header;
                    retrive_data.P_old = T_w_i_old;
                    retrive_data.Q_old = Q_loop_old;
                    retrive_data.use = true;
                    retrive_data.measurements = measurements_old_norm;
                    retrive_data.features_ids = features_id;
                    vins.retrive_pose_data = (retrive_data);
                    
                    //cout << "old pose " << T_w_i_old.transpose() << endl;
                    //cout << "refinded pose " << T_w_i_refine.transpose() << endl;
                    // add loop edge in current frame
                    cur_kf->detectLoop(old_index);
                    keyframe_database.addLoop(old_index);
                    old_kf->is_looped = 1;
                    // loop_old_index = old_index; // ui code
                }
            }
            cur_kf->image.release();
        }
        
        // if(loop_succ)
        //     [NSThread sleepForTimeInterval:2.0];
        // [NSThread sleepForTimeInterval:0.05];
    // }
}

void VINSMobileNode::global_loop_thread(){
    // while (![[NSThread currentThread] isCancelled])
    // {
        if(start_global_optimization)
        {
            start_global_optimization = false;
            TS(loop_thread);
            keyframe_database.optimize4DoFLoopPoseGraph(kf_global_index,
                                                        loop_correct_t,
                                                        loop_correct_r);
            vins.t_drift = loop_correct_t;
            vins.r_drift = loop_correct_r;
            TE(loop_thread);
            // [NSThread sleepForTimeInterval:1.17];
        }
        // [NSThread sleepForTimeInterval:0.03];
    // }
}

int main(int argc, char** argv){
    ros::init(argc, argv, "vins_mobile_node");

    VINSMobileNode node;

    ros::Rate rate(100);

    while(ros::ok()){
        ros::spinOnce();
        
        node.process();
        rate.sleep();
    }
}
