/*
 * Circular Buffer (Keep one slot open)
 * Copyright (c) 2011 Woelfware
 */

#include "buffer.h"

void buf_init(struct circular_buffer *pQue, volatile uint8_t *buf, size_t size)
{
	pQue->buf = buf;
	pQue->size = size;
	pQue->cnt = 0;
	pQue->wr_ptr = pQue->rd_ptr = 0;
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
