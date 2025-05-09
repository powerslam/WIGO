#!/bin/bash

mkdir -p third_party
cd third_party

rm -rf eigen3
rm -rf glog
rm -rf ceres-solver
rm -rf boost
rm -rf opencv
rm -rf opencv_contrib

# Set up variables
NDK_PATH=$ANDROID_NDK_HOME
ANDROID_PLATFORMS="android-24"
ARCHS=("arm64-v8a" "armeabi-v7a" "x86")

# # 1. Clone Eigen
git clone --branch 3.3.9 https://gitlab.com/libeigen/eigen.git eigen3

# # 2. Clone Glog
git clone --branch v0.5.0 https://github.com/google/glog.git

for ARCH in "${ARCHS[@]}"; do
    mkdir -p glog/build_$ARCH && cd glog/build_$ARCH
    cmake .. \
      -DCMAKE_TOOLCHAIN_FILE=$NDK_PATH/build/cmake/android.toolchain.cmake \
      -DANDROID_ABI=$ARCH \
      -DANDROID_NATIVE_API_LEVEL=$ANDROID_PLATFORMS \
      -DBUILD_SHARED_LIBS=OFF \
      -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
      -DBUILD_TESTING=OFF \
      -DCMAKE_INSTALL_PREFIX=$(pwd)/../$ARCH
    cmake --build . --target install -- -j$(nproc)
    cd ../../
done

# 3. Clone Ceres
git clone --branch 1.14.0 https://ceres-solver.googlesource.com/ceres-solver ceres-solver

for ARCH in "${ARCHS[@]}"; do
    mkdir -p ceres-solver/build_$ARCH && cd ceres-solver/build_$ARCH
    cmake .. \
      -DCMAKE_TOOLCHAIN_FILE=$NDK_PATH/build/cmake/android.toolchain.cmake \
      -DEIGEN_INCLUDE_DIR=$(pwd)/../../eigen3 \
      -DANDROID_ABI=$ARCH \
      -DANDROID_NATIVE_API_LEVEL=$ANDROID_PLATFORMS \
      -DBUILD_SHARED_LIBS=OFF \
      -DCMAKE_INSTALL_PREFIX=$(pwd)/../$ARCH \
      -DGLOG_INCLUDE_DIR=$(pwd)/../../glog/$ARCH/include \
      -DGLOG_LIBRARY=$(pwd)/../../glog/$ARCH/lib/libglog.a \
      -DCMAKE_CXX_FLAGS="-fopenmp" \
      -DCMAKE_C_FLAGS="-fopenmp" \
      -DOpenMP_CXX_FLAGS="-fopenmp" \
      -DOpenMP_C_FLAGS="-fopenmp" \
      -DCMAKE_SHARED_LINKER_FLAGS="-llog" \
      -DCMAKE_EXE_LINKER_FLAGS="-llog"
    cmake --build . --target install -- -j$(nproc)
    cd ../../
done

# 4. Download and install Boost 1.58
BOOST_VERSION="1.58.0"
BOOST_TAR="boost_${BOOST_VERSION//./_}.tar.gz"
BOOST_URL="https://downloads.sourceforge.net/project/boost/boost/1.58.0/boost_1_58_0.tar.gz"
curl -L $BOOST_URL -o $BOOST_TAR
tar -xvzf $BOOST_TAR
mv boost_${BOOST_VERSION//./_} boost
cd boost

# 5. Build and Install Boost
./bootstrap.sh --with-libraries=filesystem,system,thread
./b2 install --prefix=$(pwd)/install --toolset=gcc --with-filesystem --with-system --with-thread

cd ..

# 6. Clone OpenCV
git clone --branch 4.2.0 https://github.com/opencv/opencv.git
git clone --branch 4.2.0 https://github.com/opencv/opencv_contrib.git

for ARCH in "${ARCHS[@]}"; do
    mkdir -p opencv/build_$ARCH && cd opencv/build_$ARCH
    cmake .. \
      -DCMAKE_TOOLCHAIN_FILE=$NDK_PATH/build/cmake/android.toolchain.cmake \
      -DANDROID_ABI="$ARCH" \
      -DANDROID_PLATFORM="$ANDROID_PLATFORMS" \
      -DBUILD_SHARED_LIBS=OFF \
      -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
      -DCMAKE_INSTALL_PREFIX=$(pwd)/../$ARCH \
      -DGLOG_INCLUDE_DIR=$(pwd)/../../glog/$ARCH/include \
      -DGLOG_LIBRARY=$(pwd)/../../glog/$ARCH/lib/libglog.a \
      -DBUILD_TESTS=OFF \
      -DBUILD_EXAMPLES=OFF \
      -DBUILD_ANDROID_PROJECTS=OFF \
      -DOPENCV_ENABLE_NONFREE=ON \
      -DOPENCV_EXTRA_MODULES_PATH=../../opencv_contrib/modules \
      -DANDROID_SDK_TOOLS=$ANDROID_SDK_ROOT/cmdline-tools/19.0
    cmake --build . --target install -- -j$(nproc)
    cd ../../
done
