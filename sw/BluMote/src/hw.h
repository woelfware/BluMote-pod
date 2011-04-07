/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef HW_H_
#define HW_H_

#include "buffer.h"
#include <msp430.h>

extern struct circular_buffer uart_rx,
	uart_tx,
	ir_rx;

/* 0.5ms system timer */
extern volatile int sys_tick;

/**
 * return bool
 * retval true if successful
 * retval false if failed
 */
void init_hw();

#endif /*HW_H_*/
