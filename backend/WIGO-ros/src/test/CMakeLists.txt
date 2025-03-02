cmake_minimum_required(VERSION 3.0.2)
project(test)

find_package(catkin REQUIRED COMPONENTS
  cv_bridge
  roscpp
  sensor_msgs
)

find_package(Eigen3 REQUIRED)
include_directories(${EIGEN3_INCLUDE_DIR})

find_package(PkgConfig REQUIRED)
pkg_check_modules(Glog REQUIRED libglog)
include_directories(${Glog_INCLUDE_DIRS})
link_directories(${Glog_LIBRARY_DIRS})
add_definitions(${Glog_CFLAGS_OTHER})

find_package(Ceres REQUIRED)
include_directories(${CERES_INCLUDE_DIRS})

## iOS에서만 포함되던 파일이 ROS에서도 필요하다면, ROS에 맞게 수정해야 합니다.
include_directories(${PROJECT_SOURCE_DIR}/src/VINS-Mobile/VINS_ios/delaunay)

catkin_package(
  CATKIN_DEPENDS cv_bridge roscpp sensor_msgs std_msgs
)

include_directories(
# include
  ${catkin_INCLUDE_DIRS}
)

find_package(OpenCV REQUIRED)
include_directories(${OpenCV_INCLUDE_DIRS})

file(GLOB_RECURSE VINS_SRC
  ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/VINS_ios/*.cpp
  ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/VINS_ios/loop/*.cpp
  ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/ThirdParty/DBoW/*.cpp
  ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/ThirdParty/DUtils/*.cpp
  ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/ThirdParty/DVision/*.cpp
)

message(${PROJECT_SOURCE_DIR})

list(APPEND SOURCE_FILES ${VINS_SRC})

## iOS에서만 사용하는 파일 자동 제거
if(NOT APPLE)
    list(REMOVE_ITEM SOURCE_FILES
        ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/VINS_ios/AppDelegate.mm
        ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/VINS_ios/ViewController.mm
        ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/VINS_ios/VINSMobileNode.mm
        ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/VINS_ios/main.mm
        ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/VINS_ios/VideoProcessor.mm
    )
endif()

add_definitions(-O2)

## 실행 파일 생성
add_executable(vins_mobile_node ${SOURCE_FILES})

target_include_directories(vins_mobile_node PRIVATE
    ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/VINS_ios/loop
    ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/ThirdParty/DBoW
    ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/ThirdParty/DUtils
    ${PROJECT_SOURCE_DIR}/src/VINS-Mobile/ThirdParty/DVision
)

## 메인 실행 파일에 필요한 라이브러리 링크
target_link_libraries(vins_mobile_node
  ${OpenCV_LIBS}
  ${catkin_LIBRARIES}
  ${EIGEN3_LIBS}
  ${Glog_LIBRARIES}
  ${CERES_LIBRARIES}
  ${OpenCV_LIBS}
)
