#!/usr/bin/env bash
setserial /dev/ttyS0 uart none
modprobe lirc_serial
mode2
