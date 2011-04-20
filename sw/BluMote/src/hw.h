/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef HW_H_
#define HW_H_

#include "buffer.h"
#include <msp430.h>

extern struct circular_buffer uart_rx,
	uart_tx,
	gp_rx_tx;	/* general purpose buffer. used by blumote, ir */
	
extern volatile bool got_pulse;

/**
 * return bool
 * retval true if successful
 * retval false if failed
 */
void init_hw();

/*
 * The number of milliseconds elapsed since the last call to get_ms.
 */
int get_ms();

/*
 * The number of microseconds elapsed since the last call to get_us.
 * Don't use while in rx mode.
 */
int get_us();

#endif /*HW_H_*/
