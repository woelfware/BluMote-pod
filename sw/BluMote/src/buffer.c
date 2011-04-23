/*
 * Circular Buffer (Keep one slot open)
 * Copyright (c) 2011 Woelfware
 */

#include "buffer.h"

void buf_init(struct circular_buffer *que, volatile uint8_t *buf, size_t size)
{
	que->buf = buf;
	que->size = size;
	buf_clear(que);
}

bool buf_enque(struct circular_buffer *que, uint8_t k)
{
	bool isFull = buf_full(que);
	if (!isFull) {
		que->buf[que->wr_ptr++] = k;
		que->wr_ptr &= (que->size - 1);
		que->cnt++;
	}
	return isFull;
}

bool buf_deque(struct circular_buffer *que, uint8_t *pK)
{
	bool isEmpty = buf_empty(que);
	if (!isEmpty) {
		if (pK) {
			*pK = que->buf[que->rd_ptr];
		}
		que->rd_ptr = (que->rd_ptr + 1) & (que->size - 1);
		que->cnt--;
	}
	return isEmpty;
}

bool buf_undeque(struct circular_buffer *que, uint8_t k)
{
	bool isFull = buf_full(que);
	if (!isFull) {
		que->buf[--que->rd_ptr] = k;
		que->rd_ptr &= (que->size - 1);
		que->cnt++;
	}
	return isFull;
}

void buf_clear(struct circular_buffer *que)
{
	que->cnt = 0;
	que->wr_ptr = que->rd_ptr = 0;
}
