/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef BUFFER_H_
#define BUFFER_H_

#include <stdbool.h>
#include <stdint.h>

struct circular_buffer {
	uint8_t writePointer,
		readPointer,
		size;
	volatile uint8_t *buf;
};

inline bool buf_full(struct circular_buffer *que)
{
	return (((que->writePointer + 1) & (que->size - 1)) == que->readPointer); 
}
 
inline bool buf_empty(struct circular_buffer *que)
{
	return (que->readPointer == que->writePointer); 
}

void buf_init(struct circular_buffer *pQue, volatile uint8_t *buf, uint8_t size);

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

#endif /*BUFFER_H_*/
