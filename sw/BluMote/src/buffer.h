/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef BUFFER_H
#define BUFFER_H

#include <stdint.h>

/* general purpose buffer */
struct buf {
	int rd_ptr,
		wr_ptr,
		buf_size;
	uint8_t *buf;
};

/* a buffer of size UBER_BUF_SIZE is allocated */
extern struct buf uber_buf;

#endif /*BUFFER_H*/
