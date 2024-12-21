# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

get_filename_component(arm64_v8a ${CMAKE_CURRENT_SOURCE_DIR} DIRECTORY)
set(arm64_v8a "${arm64_v8a}/jniLibs/arm64-v8a")
link_directories(${arm64_v8a})
include_directories(third_part/ffmpeg/include)
set(ffmpeg_lib "avdevice;avcodec;avfilter;avformat;avutil;postproc;swresample;swscale")

project("xianyuplayer")
file(GLOB root_source_file "*.cpp" "*.h")
add_library(${CMAKE_PROJECT_NAME} SHARED ${root_source_file})

find_package(oboe REQUIRED CONFIG)

add_subdirectory(FFMpegmode)
add_subdirectory(player)

target_link_libraries(${CMAKE_PROJECT_NAME}
        player_mode
        oboe::oboe
        z
        ${ffmpeg_lib}
        mediandk
        android
        log)
