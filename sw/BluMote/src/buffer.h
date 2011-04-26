/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef BUFFER_H_
#define BUFFER_H_

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

struct circular_buffer {
	size_t size;
	volatile size_t wr_ptr,
		rd_ptr,
		cnt;
	volatile uint8_t *buf;
};

inline bool buf_full(struct circular_buffer *que)
{
	return (que->cnt + 1 == que->size); 
}

inline bool buf_empty(struct circular_buffer *que)
{
	return (que->cnt == 0); 
}

void buf_init(struct circular_buffer *que, volatile uint8_t *buf, size_t size);

/*
 * \return bool
 * \retval true		buffer was full
 * \retval false	buffer was not full
 */
bool buf_enque(struct circular_buffer *que, uint8_t k);

/*
 * \return bool
 * \retval true		buffer was empty
 * \retval false	buffer was not empty
 */
bool buf_deque(struct circular_buffer *que, uint8_t *pK);

/*
 * \return bool
 * \retval true		buffer was full
 * \retval false	buffer was not full
 */
bool buf_undeque(struct circular_buffer *que, uint8_t k);

/*
 * clear out the buffer
 */
void buf_clear(struct circular_buffer *que);

#endif /*BUFFER_H_*/
