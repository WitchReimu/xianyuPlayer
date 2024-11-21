# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

get_filename_component(arm64_v8a ${CMAKE_CURRENT_SOURCE_DIR} DIRECTORY)
set(arm64_v8a "${arm64_v8a}/jniLibs/arm64-v8a")

link_libraries(${arm64_v8a})
# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("xianyuplayer")


add_library(${CMAKE_PROJECT_NAME} SHARED native-lib.cpp )

include_directories(third_part/ffmpeg/include)
# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        z
        mediandk
        android
        log)