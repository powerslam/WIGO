#!/bin/bash

mkdir -p third_party

# Set up variables
NDK_PATH=$ANDROID_NDK_HOME
ANDROID_PLATFORMS="android-24"
ARCHS=("arm64-v8a" "armeabi-v7a" "x86")

# # 1. Clone Eigen
cd third_party
git clone --branch 3.3.9 https://gitlab.com/libeigen/eigen.git eigen3
cd ..

# # 2. Clone Glog
git clone --branch v0.5.0 https://github.com/google/glog.git
mkdir -p third_party/glog

for ARCH in "${ARCHS[@]}"; do
    mkdir -p glog/build_$ARCH && cd glog/build_$ARCH
    cmake .. \
      -DCMAKE_TOOLCHAIN_FILE=$NDK_PATH/build/cmake/android.toolchain.cmake \
      -DANDROID_ABI=$ARCH \
      -DANDROID_NATIVE_API_LEVEL=$ANDROID_PLATFORMS \
      -DBUILD_SHARED_LIBS=OFF \
      -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
      -DBUILD_TESTING=OFF \
      -DCMAKE_INSTALL_PREFIX=$(pwd)/../../third_party/glog/$ARCH
    cmake --build . --target install -- -j$(nproc)
    cd ../../
    echo $(pwd)
done
rm -rf glog

3. Clone Ceres
git clone --branch 1.14.0 https://ceres-solver.googlesource.com/ceres-solver ceres-solver
mkdir -p third_party/ceres-solver

for ARCH in "${ARCHS[@]}"; do
    mkdir -p ceres-solver/build_$ARCH && cd ceres-solver/build_$ARCH
    cmake .. \
      -DCMAKE_TOOLCHAIN_FILE=$NDK_PATH/build/cmake/android.toolchain.cmake \
      -DEIGEN_INCLUDE_DIR=$(pwd)/../../third_party/eigen3 \
      -DANDROID_ABI=$ARCH \
      -DANDROID_NATIVE_API_LEVEL=$ANDROID_PLATFORMS \
      -DBUILD_SHARED_LIBS=OFF \
      -DCMAKE_INSTALL_PREFIX=$(pwd)/../../third_party/ceres-solver/$ARCH \
      -DGLOG_INCLUDE_DIR=$(pwd)/../../third_party/glog/$ARCH/include \
      -DGLOG_LIBRARY=$(pwd)/../../third_party/glog/$ARCH/lib/libglog.a \
      -DCMAKE_CXX_FLAGS="-fopenmp" \
      -DCMAKE_C_FLAGS="-fopenmp" \
      -DOpenMP_CXX_FLAGS="-fopenmp" \
      -DOpenMP_C_FLAGS="-fopenmp" \
      -DCMAKE_SHARED_LINKER_FLAGS="-llog" \
      -DCMAKE_EXE_LINKER_FLAGS="-llog"
    cmake --build . --target install -- -j$(nproc)
    cd ../../
done
rm -rf ceres-solver

# 4. Download and install Boost 1.58
BOOST_VERSION="1.58.0"
BOOST_TAR="boost_${BOOST_VERSION//./_}.tar.gz"
BOOST_URL="https://downloads.sourceforge.net/project/boost/boost/1.58.0/boost_1_58_0.tar.gz"
curl -L $BOOST_URL -o $BOOST_TAR
tar -xvzf $BOOST_TAR
mv boost_${BOOST_VERSION//./_} boost
cd boost

mkdir -p third_party/boost

# 5. Build and Install Boost
./bootstrap.sh --with-libraries=filesystem,system,thread
./b2 install --prefix=$(pwd)/../third_party/boost/install --toolset=gcc --with-filesystem --with-system --with-thread

cd ..
rm -rf boost*

# 6. Clone OpenCV
git clone --branch 4.2.0 https://github.com/opencv/opencv.git
git clone --branch 4.2.0 https://github.com/opencv/opencv_contrib.git
mkdir -p third_party/opencv

for ARCH in "${ARCHS[@]}"; do
    mkdir -p opencv/build_$ARCH && cd opencv/build_$ARCH
    cmake .. \
      -DCMAKE_TOOLCHAIN_FILE=$NDK_PATH/build/cmake/android.toolchain.cmake \
      -DANDROID_ABI="$ARCH" \
      -DANDROID_PLATFORM="$ANDROID_PLATFORMS" \
      -DBUILD_SHARED_LIBS=OFF \
      -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
      -DCMAKE_INSTALL_PREFIX=$(pwd)/../../third_party/opencv/$ARCH \
      -DGLOG_INCLUDE_DIR=$(pwd)/../../third_party/glog/$ARCH/include \
      -DGLOG_LIBRARY=$(pwd)/../../third_party/glog/$ARCH/lib/libglog.a \
      -DBUILD_TESTS=OFF \
      -DBUILD_EXAMPLES=OFF \
      -DBUILD_ANDROID_PROJECTS=OFF \
      -DOPENCV_ENABLE_NONFREE=ON \
      -DOPENCV_EXTRA_MODULES_PATH=$(pwd)/../../opencv_contrib/modules \
      -DANDROID_SDK_TOOLS=$ANDROID_SDK_ROOT/cmdline-tools/19.0
    cmake --build . --target install -- -j$(nproc)
    cd ../../
done
rm -rf opencv*
