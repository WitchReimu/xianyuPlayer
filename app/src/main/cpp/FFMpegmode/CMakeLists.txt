file(GLOB source_file "*.cpp" "*.h")
add_library(FFMpeg_mode ${source_file})
target_include_directories(FFMpeg_mode PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})
