# http://www.cmake.org/Wiki/CMake_Useful_Variables/Logging_Useful_Variables
cmake_minimum_required(VERSION 2.8)

project(bluemote_pod C)

set(BLUEMOTE_POD_VERSION_MAJOR 0)
set(BLUEMOTE_POD_VERSION_MINOR 1)
set(BLUEMOTE_POD_VERSION_REVISION 0)

set(BLUEMOTE_POD_NAME "Bluemote")
set(BLUEMOTE_POD_DESCRIPTION "IR XPDR")
set(BLUEMOTE_POD_PROVIDER "Woelfware")

configure_file("${PROJECT_SOURCE_DIR}/src/config.h.in"
		"${PROJECT_BINARY_DIR}/config.h")
include_directories(${PROJECT_BINARY_DIR})
include_directories(${PROJECT_SOURCE_DIR})
include_directories(${PROJECT_SOURCE_DIR}/src)
include_directories(${PROJECT_SOURCE_DIR}/src/bluemote)

add_subdirectory(src)

# add the executable
add_executable(bluemote ${PROJECT_SOURCE_DIR}/src/main.c)

# Make sure the linker can find the bluemote library once it is built.
link_directories(${PROJECT_BINARY_DIR}/bluemote)

target_link_libraries(bluemote libbluemote)
