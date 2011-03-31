# the name of the target operating system
set(CMAKE_SYSTEM_NAME Generic)

set(ARCH MSP430)

# which compilers to use for C
set(CMAKE_C_COMPILER msp430-gcc)

# here is the target environment located
set(CMAKE_FIND_ROOT_PATH  /usr/msp430)

# adjust the default behaviour of the FIND_XXX() commands:
# search headers and libraries in the target environment, search 
# programs in the host environment
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
