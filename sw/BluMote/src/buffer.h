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
	return (((que->writePointer + 1) % que->size) == que->readPointer); 
}
 
inline bool buf_empty(struct circular_buffer *que)
{
	return (que->readPointer == que->writePointer); 
}

void buf_init(struct circular_buffer *pQue, volatile uint8_t *buf, uint8_t size);
bool buf_enque(struct circular_buffer *que, uint8_t k);
bool buf_deque(struct circular_buffer *que, uint8_t *pK);

#endif /*BUFFER_H_*/
