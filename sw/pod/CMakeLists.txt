# http://www.cmake.org/Wiki/CMake_Useful_Variables/Logging_Useful_Variables
cmake_minimum_required (VERSION 2.8)
project (bluemote_pod C)
set (BLUEMOTE_POD_VERSION_MAJOR 0)
set (BLUEMOTE_POD_VERSION_MINOR 1)
set (BLUEMOTE_POD_VERSION_REVISION 0)

configure_file ("${PROJECT_SOURCE_DIR}/src/config.h.in"
		"${PROJECT_BINARY_DIR}/config.h")

# use pkg-config to find dependencies
find_package(PkgConfig)
function(find_mod var mod)
	pkg_check_modules(${var} ${mod})
	if (${var}_FOUND)
		include_directories(${${var}_INCLUDE_DIRS})
	else()
		message(FATAL_ERROR "Failed to find ${mod}")
	endif()
endfunction()
find_mod(GLIB2 glib-2.0)
find_mod(BLUEZ bluez)

include_directories("${PROJECT_BINARY_DIR}")
include_directories("${PROJECT_SOURCE_DIR}")
include_directories("${PROJECT_SOURCE_DIR}/src")

set(EXTRA_LIBS ${EXTRA_LIBS} ${GLIB2_LIBRARIES} ${BLUEZ_LIBRARIES})

# add the executable
add_executable(bluemote_pod ${PROJECT_SOURCE_DIR}/src/bluemote_pod.c ${PROJECT_SOURCE_DIR}/src/main.c)
target_link_libraries(bluemote_pod ${EXTRA_LIBS})

#
# Regression tests
#
#enable_testing()

# does the application run
add_test(gcalcRuns gcalc-cli)

# does the application add
add_test(gcalcAdd gcalc-cli -a 10 20)
set_tests_properties(gcalcAdd
 PROPERTIES PASS_REGULAR_EXPRESSION "10 \\+ 20 = 30")

# does the application subtract
add_test(gcalcSub gcalc-cli -s 30 20)
set_tests_properties(gcalcSub
 PROPERTIES PASS_REGULAR_EXPRESSION "30 - 20 = 10")

# does the application multiply
add_test(gcalcMult gcalc-cli -m 10 20)
set_tests_properties(gcalcMult
 PROPERTIES PASS_REGULAR_EXPRESSION "10 \\* 20 = 200")

# does the application divide
add_test(gcalcDiv gcalc-cli -d 20 10)
set_tests_properties(gcalcDiv
 PROPERTIES PASS_REGULAR_EXPRESSION "20 / 10 = 2")

# does the usage message work
add_test(gcalcUsage gcalc-cli)
set_tests_properties(gcalcUsage
	PROPERTIES
	PASS_REGULAR_EXPRESSION "Usage:.*")

#
# build a CPack driven installer package
#
include (InstallRequiredSystemLibraries)
# generic package variables
set (CPACK_RESOURCE_FILE_LICENSE
        "${CMAKE_CURRENT_SOURCE_DIR}/License.txt")
set (CPACK_PACKAGE_DESCRIPTION_SUMMARY "A sample application for testing cmake, glib, and gtk")
set (CPACK_PACKAGE_NAME "gcalc")
set (CPACK_PACKAGE_VERSION_MAJOR "${GCALC_VERSION_MAJOR}")
set (CPACK_PACKAGE_VERSION_MINOR "${GCALC_VERSION_MINOR}")
set (CPACK_PACKAGE_VENDOR "Woelfware")
# rpm specific package variables
set (CPACK_RPM_PACKAGE_RELEASE "1")
include (CPack)
