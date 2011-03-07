/*
 * Copyright (c) 2011 Woelfware
 */
 
#include "hw.h"
#include "msp430.h"

struct circular_buffer uart_rx,
	uart_tx,
	ir_rx;
volatile uint8_t buf_uart_rx[UART_RX_BUF_SIZE],
	buf_uart_tx[UART_TX_BUF_SIZE],
	buf_ir_rx[IR_RX_BUF_SIZE];
	
int rx_head = 0,
	rx_tail = 0,
	tx_head = 0,
	tx_tail = 0;

static void init_clocks()
{
	BCSCTL1 = CALBC1_1MHZ;		/* Set DCO */
	DCOCTL = CALDCO_1MHZ;
	P3SEL = 0x30;				/* P3.4,5 = USCI_A0 TXD/RXD */
	UCA0CTL1 |= UCSSEL_2;		/* SMCLK */
	UCA0BR0 = 8;				/* 1MHz 115200 */
	UCA0BR1 = 0;				/* 1MHz 115200 */
	UCA0MCTL = UCBRS2 + UCBRS0;	/* Modulation UCBRSx = 5 */
	UCA0CTL1 &= ~UCSWRST;		/* Initialize USCI state machine */
}

static void init_bufs()
{
	buf_init(&uart_rx, buf_uart_rx, sizeof(buf_uart_rx));
	buf_init(&uart_tx, buf_uart_tx, sizeof(buf_uart_tx));
	buf_init(&ir_rx, buf_ir_rx, sizeof(buf_ir_rx));
}

static void init_interrupts()
{
	IE2 |= UCA0RXIE;			/* Enable USCI_A0 RX interrupt */
}

void init_hw()
{
	WDTCTL = WDTPW + WDTHOLD;	/* Stop Watchdog Timer */
	init_clocks();
	init_bufs();
	init_interrupts();
	__bis_SR_register(LPM0_bits + GIE);	/* Enter LPM0, interrupts enabled */
}

/* Echo back RXed character, confirm TX buffer is ready first */
#pragma vector = USCIAB0RX_VECTOR
__interrupt void USCI0RX_ISR(void)
{
	(void)buf_enque(&uart_rx, UCA0RXBUF);
}
