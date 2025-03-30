#!/bin/bash

mkdir -p third_party
cd third_party

rm -rf ceres-solver-1.14
rm -rf eigen3
rm -rf glog
rm -rf opencv
rm -rf opencv_contrib
rm -rf boost_1_58_0

# Set up variables
NDK_PATH=$ANDROID_NDK_HOME      # 또는 직접 NDK 경로 적어도 됨
ANDROID_ABI=arm64-v8a
ANDROID_PLATFORM=android-24

# 1. Clone Eigen
git clone --branch 3.3.9 https://gitlab.com/libeigen/eigen.git
mv eigen eigen3

# 2. Clone Glog
git clone --branch v0.5.0 https://github.com/google/glog.git
mkdir -p glog/build && cd glog/build
cmake .. \
  -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake \
  -DANDROID_ABI=arm64-v8a \
  -DANDROID_NATIVE_API_LEVEL=android-24 \
  -DBUILD_SHARED_LIBS=OFF \
  -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
  -DBUILD_TESTING=OFF \
  -DCMAKE_INSTALL_PREFIX=$(pwd)/../install
cmake --build . --target install -- -j$(nproc)
cd ../../

# # 3. Clone Ceres
git clone https://ceres-solver.googlesource.com/ceres-solver ceres-solver-1.14
cd ceres-solver-1.14
git checkout 1.14.0
mkdir build && cd build

cmake .. \
  -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake \
  -DEIGEN_INCLUDE_DIR=$(pwd)/../../eigen3 \
  -DANDROID_ABI=arm64-v8a \
  -DANDROID_NATIVE_API_LEVEL=android-24 \
  -DBUILD_SHARED_LIBS=OFF \
  -DCMAKE_INSTALL_PREFIX=$(pwd)/../install \
  -DGLOG_INCLUDE_DIR=$(pwd)/../../glog/install/include \
  -DGLOG_LIBRARY=$(pwd)/../../glog/install/lib/libglog.a \
  -DCMAKE_CXX_FLAGS="-fopenmp" \
  -DCMAKE_C_FLAGS="-fopenmp" \
  -DOpenMP_CXX_FLAGS="-fopenmp" \
  -DOpenMP_C_FLAGS="-fopenmp" \
  -DCMAKE_SHARED_LINKER_FLAGS="-llog" \
  -DCMAKE_EXE_LINKER_FLAGS="-llog"
cmake --build . --target install -- -j$(nproc)
cd ../../

# 4. Download and install Boost 1.58
echo "Downloading Boost 1.58"
BOOST_VERSION="1.58.0"
BOOST_TAR="boost_${BOOST_VERSION//./_}.tar.gz"
echo $BOOST_TAR
BOOST_URL="https://downloads.sourceforge.net/project/boost/boost/1.58.0/boost_1_58_0.tar.gz"
# BOOST_URL="https://boostorg.jfrog.io/artifactory/main/release/1.58.0/source/$BOOST_TAR"
# curl -L $BOOST_URL -o $BOOST_TAR
tar -xvzf $BOOST_TAR
cd boost_${BOOST_VERSION//./_}

# 5. Build and Install Boost
./bootstrap.sh --with-libraries=filesystem,system,thread
./b2 install --prefix=$(pwd)/install --toolset=gcc --with-filesystem --with-system --with-thread

cd ../../

# 6. Clone OpenCV (version 3.4.16)
git clone --branch 4.2.0 https://github.com/opencv/opencv.git
git clone --branch 4.2.0 https://github.com/opencv/opencv_contrib.git

# 7. Build OpenCV with contrib
mkdir -p opencv/build && cd opencv/build
cmake .. \
  -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake \
  -DANDROID_ABI="arm64-v8a" \
  -DANDROID_PLATFORM="android-24" \
  -DBUILD_SHARED_LIBS=OFF \
  -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
  -DCMAKE_INSTALL_PREFIX=$(pwd)/../install \
  -DGLOG_INCLUDE_DIR=$(pwd)/../../glog/install/include \
  -DGLOG_LIBRARY=$(pwd)/../../glog/install/lib/libglog.a \
  -DBUILD_TESTS=OFF \
  -DBUILD_EXAMPLES=OFF \
  -DBUILD_ANDROID_PROJECTS=OFF \
  -DOPENCV_ENABLE_NONFREE=ON \
  -DOPENCV_EXTRA_MODULES_PATH=../../opencv_contrib/modules \
  -DANDROID_SDK_TOOLS=$ANDROID_SDK_ROOT/cmdline-tools/19.0
cmake --build . --target install -- -j$(nproc)
cd ../../

