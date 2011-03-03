add_subdirectory(lib)
add_subdirectory(arch)

include_directories(lib)

# add the executable
add_executable(bluemote ${PROJECT_SOURCE_DIR}/src/main.c)

# Make sure the linker can find the bluemote library once it is built.
link_directories(${PROJECT_BINARY_DIR}/lib)

target_link_libraries(bluemote libbluemote)
