/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef HW_H_
#define HW_H_

#include "buffer.h"
#include <msp430.h>

/*
 * mutex on the general purpose buffer so
 * it doesn't get clobbered.
 */
enum gp_buf_owner {
	gp_buf_owner_none,
	gp_buf_owner_bt,
	gp_buf_owner_ir
};
extern enum gp_buf_owner gp_buf_owner;

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
 * The number of microseconds elapsed since the last call to get_us.
 * Don't use while in rx mode.
 */
int_fast32_t get_us();

/*
 * return true if the requesting owner already owns or
 * is granted ownership of gp_buf
 * return false if gp_buf is aleady owned by another task
 */
bool own_gp_buf(enum gp_buf_owner owner);

#endif /*HW_H_*/

