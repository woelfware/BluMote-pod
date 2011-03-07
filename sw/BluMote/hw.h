#ifndef HW_H_
#define HW_H_

#include "buffer.h"

extern struct circular_buffer uart_rx,
	uart_tx,
	ir_rx;

/**
 * return bool
 * retval true if successful
 * retval false if failed
 */
void init_hw();

#endif /*HW_H_*/
