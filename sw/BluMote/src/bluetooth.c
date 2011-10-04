/*
 * Copyright (c) 2011 Woelfware
 */

#include <msp430.h>
#include <stdint.h>
#include <stdlib.h>
#include "bluetooth.h"

static volatile struct buf *bluetooth_rx_buf = NULL;

void bluetooth_tx(struct buf *tx_buf)
{
	while (tx_buf->rd_ptr < tx_buf->wr_ptr) {
		while (!(IFG2 & UCA0TXIFG));	/* tx buf ready? */
		UCA0TXBUF = tx_buf->buf[tx_buf->rd_ptr];
		tx_buf->rd_ptr++;
	}

	tx_buf->rd_ptr = tx_buf->wr_ptr = 0;
}

void set_bluetooth_rx_buf(volatile struct buf *rx_buf)
{
	_disable_interrupts();
	bluetooth_rx_buf = rx_buf;
	_enable_interrupts();
}

#pragma vector = USCIAB0RX_VECTOR
__interrupt void USCI0RX_ISR(void)
{
	if (bluetooth_rx_buf) {
		if (bluetooth_rx_buf->wr_ptr < bluetooth_rx_buf->buf_size - 1) {
			bluetooth_rx_buf->buf[bluetooth_rx_buf->wr_ptr++] = UCA0RXBUF;
		}
	}

	_BIC_SR(LPM4_EXIT);
}
