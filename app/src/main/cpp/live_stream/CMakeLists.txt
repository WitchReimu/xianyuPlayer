file(GLOB source_file "*.cpp" "*.h")
add_library(livestream ${source_file})
target_include_directories(livestream PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})
target_link_libraries(livestream PUBLIC FFMpeg_mode)