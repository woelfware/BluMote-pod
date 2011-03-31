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
	BCSCTL1		= CALBC1_16MHZ;		/* Set DCO */
	DCOCTL		= CALDCO_16MHZ;
	UCA0CTL1	|= UCSSEL_2;		/* SMCLK */
	UCA0BR0		= 138;			/* 16MHz 115200 */
	UCA0BR1		= 0;			/* 16MHz 115200 */
	UCA0MCTL	= UCBRS2 + UCBRS0;	/* Modulation UCBRSx = 5 */
	UCA0CTL1	&= ~UCSWRST;		/* **Initialize USCI state machine** */

	__bis_SR_register(LPM4_bits + GIE);	/* Enter LPM4, interrupts enabled */
}

static void init_ports()
{
	P1DIR	= 0xFF;		/* All P1.x outputs */
	P1OUT	= 0;		/* All P1.x reset */
	P2SEL	= BIT1;		/* P2.1 = SMCLK, others GPIO */
	P2DIR	= 0xFF;		/* All P2.x outputs */
	P2OUT	= 0;		/* All P2.x reset */
	P3SEL	= BIT4 | BIT5 | BIT6;	/* P3.4,5 = USCI_A0 TXD/RXD, P3.6 = PIO */
	P3DIR	= 0xFF;		/* All P3.x outputs */
	P3OUT	= 0;		/* All P3.x reset */
	P4DIR	= 0xFF;		/* All P4.x outputs */
	P4OUT	= 0;		/* All P4.x reset */
}

static void init_bufs()
{
	buf_init(&uart_rx, buf_uart_rx, sizeof(buf_uart_rx));
	buf_init(&uart_tx, buf_uart_tx, sizeof(buf_uart_tx));
	buf_init(&ir_rx, buf_ir_rx, sizeof(buf_ir_rx));
}

static void init_interrupts()
{
	IE2 |= UCA0RXIE;	/* Enable USCI_A0 RX interrupt */
}

void init_hw()
{
	WDTCTL = WDTPW + WDTHOLD;	/* Stop Watchdog Timer */
	init_clocks();
	init_ports();
	init_bufs();
	init_interrupts();
	_BIS_SR(LPM4_bits + GIE);	/* Enter LPM0, interrupts enabled */
}

#pragma vector = USCIAB0RX_VECTOR
__interrupt void USCI0RX_ISR(void)
{
	(void)buf_enque(&uart_rx, UCA0RXBUF);
	_BIC_SR(LPM4_EXIT);	/* wake up from low power mode */
}
