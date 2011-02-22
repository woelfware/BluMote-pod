# Create a library called "bluemote"
# The extension is already found.  Any number of sources could be listed here.

if(UNIX)
	find_package(PkgConfig)
	pkg_check_modules(BLUEZ bluez)
	if(${BLUEZ_FOUND})
		include_directories(${BLUEZ_INCLUDE_DIRS})
	else()
		message(FATAL_ERROR "Failed to find bluez")
	endif()
endif()

set(sources
	bluemote.c
	bbluetooth.c
	ir.c)

add_library(libbluemote ${sources})
target_link_libraries(libbluemote ${BLUEZ_LIBRARIES})
