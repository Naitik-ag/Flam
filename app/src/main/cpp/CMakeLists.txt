cmake_minimum_required(VERSION 3.18.1)
project("edgeviewer")

# Include OpenCV headers
include_directories(${CMAKE_SOURCE_DIR}/opencv/include)

# Dummy static library to force inclusion of libc++_shared.so
add_library(opencv_jni_shared STATIC dummy.cpp)

# Native JNI library
add_library(
        native-lib
        SHARED
        native_bridge.cpp
)

# Import OpenCV prebuilt library for each ABI
add_library(opencv_java4 SHARED IMPORTED)
set_target_properties(
        opencv_java4 PROPERTIES
        IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/opencv/libs/${ANDROID_ABI}/libopencv_java4.so
)

# Find NDK log + shared C++ runtime
find_library(log-lib log)
find_library(cpp_shared c++_shared)

# Link all libraries
target_link_libraries(
        native-lib
        opencv_java4
        opencv_jni_shared   # REQUIRED for OpenCV 4.10+
        ${cpp_shared}       # NDK C++ runtime
        ${log-lib}
)
