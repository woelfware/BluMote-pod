#!/usr/bin/env sh
rm -rf build
mkdir build
cd build
cmake -DCMAKE_TOOLCHAIN_FILE=../Toolchain-msp430.cmake ..
