/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef BLUETOOTH_H
#define BLUETOOTH_H

#include <stdbool.h>
#include <stdint.h>
#include "buffer.h"
#include "config.h"

void bluetooth_tx();

void set_bluetooth_rx_buf(volatile struct buf *rx_buf);

#endif /*BLUETOOTH_H*/
