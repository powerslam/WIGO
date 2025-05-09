cmake_minimum_required(VERSION 3.10)
project(pose_graph)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# 안드로이드용 컴파일 옵션
if(ANDROID)
    message(STATUS "Building for Android")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -O3 -g")
endif()

# ========== Dependencies ==========

# glog (static lib, imported)
add_library(glog STATIC IMPORTED)
set_target_properties(glog PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../../backend/third_party/glog/${ANDROID_ABI}/lib/libglog.a
    INTERFACE_INCLUDE_DIRECTORIES ${CMAKE_SOURCE_DIR}/../../backend/third_party/glog/${ANDROID_ABI}/include
)

find_package(OpenMP REQUIRED)

# ceres (static lib, imported)
add_library(ceres STATIC IMPORTED)
set_target_properties(ceres PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../../backend/third_party/ceres-solver/${ANDROID_ABI}/lib/libceres.a
    INTERFACE_INCLUDE_DIRECTORIES ${CMAKE_SOURCE_DIR}/../../backend/third_party/ceres-solver/${ANDROID_ABI}/include
)

# ========== Boost (added) ==========

set(Boost_NO_SYSTEM_PATHS ON)
set(Boost_INCLUDE_DIR ${CMAKE_SOURCE_DIR}/../../backend/third_party/boost/install/include)
set(Boost_LIBRARY_DIR ${CMAKE_SOURCE_DIR}/../../backend/third_party/boost/install/lib)

# Boost 라이브러리 경로 및 헤더 파일 경로 지정
link_directories(${Boost_LIBRARY_DIR})
include_directories(${Boost_INCLUDE_DIR})

# ========== OpenCV (added) ==========

# Set OpenCV path (adjust as necessary)
set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/../../backend/third_party/opencv/${ANDROID_ABI}/sdk/native/jni)
find_package(OpenCV REQUIRED)

# ========== pose_graph lib ==========

file(GLOB_RECURSE SRC_FILES
    src/*.cpp
)

add_library(pose_graph SHARED ${SRC_FILES})

target_include_directories(pose_graph PUBLIC
    ${CMAKE_SOURCE_DIR}/src
    ${OpenCV_INCLUDE_DIRS}
    ${Boost_INCLUDE_DIR}
    ${CMAKE_SOURCE_DIR}/../../backend/third_party
    ${CMAKE_SOURCE_DIR}/../../backend/third_party/eigen3
)

target_link_libraries(pose_graph
    ceres
    glog
    log        # Android logcat
    android    # Android native support
    ${OpenCV_LIBS}
    OpenMP::OpenMP_CXX
)