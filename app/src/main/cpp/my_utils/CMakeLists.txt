file(GLOB source_file "*.h" "*.cpp" "*.c")
add_library(myUtils ${source_file})
target_include_directories(myUtils PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})
