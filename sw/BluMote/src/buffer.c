/*
 * Copyright (c) 2011 Woelfware
 */

#include "config.h"
#include "buffer.h"

static uint8_t buf[UBER_BUF_SIZE];
struct buf uber_buf = {0, 0, UBER_BUF_SIZE, buf};
