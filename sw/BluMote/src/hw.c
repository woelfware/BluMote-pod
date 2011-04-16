/*
 * Copyright (c) 2011 Woelfware
 */

#include "config.h" 
#include "hw.h"
#include "msp430.h"

struct circular_buffer uart_rx,
	uart_tx,
	ir_rx;
static volatile uint8_t buf_uart_rx[UART_RX_BUF_SIZE],
	buf_uart_tx[UART_TX_BUF_SIZE],
	buf_ir_rx[IR_RX_BUF_SIZE];

static volatile int ir_tick = 0;
static volatile int sys_tick = 0;
volatile bool got_pulse = false;

static volatile uint16_t duration = 0;

static void init_bufs()
{
	buf_init(&uart_rx, buf_uart_rx, sizeof(buf_uart_rx));
	buf_init(&uart_tx, buf_uart_tx, sizeof(buf_uart_tx));
	buf_init(&ir_rx, buf_ir_rx, sizeof(buf_ir_rx));
}

void init_hw()
{
	WDTCTL = WDT_MDLY_32;	/* Set Watchdog interval to 2ms @ 16MHz */
	IE1 |= WDTIE;		/* Enable WDT interrupt */
	BCSCTL1 = CALBC1_16MHZ;	/* Set DCO */
	DCOCTL = CALDCO_16MHZ;
	P1DIR = BIT4 | BIT5;    /* P1.4,5 = IR_OUT1, IR_OUT2 */
	P3SEL = BIT4 | BIT5;	/* P3.4,5 = USCI_A0 TXD/RXD */
	P3DIR = BIT0 | BIT1 | BIT3;	/* reset, baud_rate, PIO3 */ 
	P3OUT = BIT0;
	UCA0CTL1 |= UCSSEL_2;	/* SMCLK */
	UCA0BR0 = 138;		/* 16MHz 115200 */
	UCA0BR1 = 0;		/* 16MHz 115200 */
	UCA0MCTL = UCBRS2 + UCBRS1 + UCBRS0;	/* Modulation UCBRSx = 7 */
	UCA0CTL1 &= ~UCSWRST;	/* **Initialize USCI state machine** */
	IE2 |= UCA0RXIE;	/* Enable USCI_A0 RX interrupt */
#if 0
	//P1IE = BIT3;	/* Enable IR_IN interrupt */
	CCTL0 = CCIE;	/* CCR0 interrupt enabled */
 	CCR0 = 159;     /*10us*/
	TACTL = TASSEL_2 +  MC_1; /* SMCLK, upmode */
#endif
	__bis_SR_register(GIE);	/* interrupts enabled */

	init_bufs();
}

int get_ms()
{
	int elapsed_time = sys_tick << 1;
	sys_tick = 0;
	return elapsed_time;
}

#pragma vector = USCIAB0RX_VECTOR
__interrupt void USCI0RX_ISR(void)
{
	(void)buf_enque(&uart_rx, UCA0RXBUF);
	_BIC_SR(LPM4_EXIT);	/* wake up from low power mode */
}

#pragma vector = WDT_VECTOR
__interrupt void watchdog_timer(void)
{
	sys_tick++;
}
