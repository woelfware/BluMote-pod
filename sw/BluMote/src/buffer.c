/*
 * Circular Buffer (Keep one slot open)
 * Copyright (c) 2011 Woelfware
 */

#include "buffer.h"

void buf_init(struct circular_buffer *pQue, volatile uint8_t *buf, uint8_t size)
{
	pQue->buf = buf;
	pQue->size = size;
	pQue->writePointer = pQue->readPointer = 0;
}

bool buf_enque(struct circular_buffer *que, uint8_t k)
{
	bool isFull = buf_full(que);
	if (!isFull) {
		que->buf[que->writePointer] = k;
		que->writePointer = (que->writePointer + 1) & (que->size - 1);
	}
	return isFull;
}

bool buf_deque(struct circular_buffer *que, uint8_t *pK)
{
	bool isEmpty = buf_empty(que);
	if (!isEmpty) {
		if (pK) {
			*pK = que->buf[que->readPointer];
		}
		que->readPointer = (que->readPointer + 1) & (que->size - 1);
	}
	return isEmpty;
}
