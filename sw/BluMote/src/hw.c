/*
 * Copyright (c) 2011 Woelfware
 */

#include "blumote.h"
#include "config.h" 
#include "hw.h"
#include "ir.h"
#include "msp430.h"

enum gp_buf_owner gp_buf_owner = gp_buf_owner_none;

struct circular_buffer uart_rx,
	uart_tx,
	gp_rx_tx;
static volatile uint8_t buf_uart_rx[UART_RX_BUF_SIZE],
	buf_uart_tx[UART_TX_BUF_SIZE],
	buf_gp[GP_BUF_SIZE];

static volatile int_fast32_t sys_tick = 0,
	ir_sys_tick = 0;
volatile bool got_pulse = false;

static volatile uint16_t duration = 0;

static void init_bufs()
{
	buf_init(&uart_rx, buf_uart_rx, sizeof(buf_uart_rx));
	buf_init(&uart_tx, buf_uart_tx, sizeof(buf_uart_tx));
	buf_init(&gp_rx_tx, buf_gp, sizeof(buf_gp));
}

void init_hw()
{
	WDTCTL = WDTPW + WDTHOLD;	/* stop WDT */
	BCSCTL1 = CALBC1_16MHZ;	/* Set DCO */
	DCOCTL = CALDCO_16MHZ;
	P3SEL = BIT4 | BIT5;	/* P3.4,5 = USCI_A0 TXD/RXD */
	P3DIR = BIT0 | BIT1 | BIT3;	/* reset, baud_rate, PIO3 */ 
	P3OUT = BIT0;
	UCA0CTL1 |= UCSSEL_2;	/* SMCLK */
	UCA0BR0 = 138;		/* 16MHz 115200 */
	UCA0BR1 = 0;		/* 16MHz 115200 */
	UCA0MCTL = UCBRS2 + UCBRS1 + UCBRS0;	/* Modulation UCBRSx = 7 */
	UCA0CTL1 &= ~UCSWRST;	/* **Initialize USCI state machine** */
	IE2 |= UCA0RXIE;	/* Enable USCI_A0 RX interrupt */

	/* IR configs */
	P1SEL = BIT5;  /* Set as alternate function */
	P1DIR = BIT4 | BIT5;    /* P1.4,5 = IR_OUT1, IR_OUT2 */
	P1OUT &= ~(BIT4 | BIT5);	/* Turn off IR LED */
	P1IES |= BIT3;	/* P1.3 falling edge */
	P1IFG &= ~BIT3;	/* P1.3 IFG cleared */
	CCTL1 = CCIE;	/* CCR1 interrupt enabled */
 	CCR1 = (SYS_CLK * US_PER_SYS_TICK) - 1;
	CCTL0 = OUTMOD_4;  /* CCR0 interrupt disabled and Toggle */
	CCR0 = ((SYS_CLK * 1000) / (IR_CARRIER_FREQ * 2) - 1);
	TACTL = TASSEL_2 +  MC_2; /* SMCLK, continuous */

	__bis_SR_register(GIE);	/* interrupts enabled */

	init_bufs();
}

int_fast32_t get_us()
{
	int_fast32_t elapsed_time;
	__disable_interrupt();
	elapsed_time = sys_tick * US_PER_SYS_TICK;
	sys_tick = 0;
	__enable_interrupt();
	return elapsed_time;
}

static int_fast32_t get_ir_us()
{
	int_fast32_t elapsed_time;
	__disable_interrupt();
	elapsed_time = ir_sys_tick * US_PER_SYS_TICK;
	ir_sys_tick = 0;
	__enable_interrupt();
	return elapsed_time;
}

bool own_gp_buf(enum gp_buf_owner owner)
{
	if (gp_buf_owner == owner
			|| gp_buf_owner == gp_buf_owner_none) {
		gp_buf_owner = owner;
		return true;
	}

	return false;
}

#pragma vector = USCIAB0RX_VECTOR
__interrupt void USCI0RX_ISR(void)
{
	(void)buf_enque(&uart_rx, UCA0RXBUF);
	_BIC_SR(LPM4_EXIT);	/* wake up from low power mode */

	/* A hack to abort an IR transmission.
	 * The IR is the only task running when an IR code is being transmitted.
	 * When we get an IR abort command, this will allow the IR task to
	 * service the abort.
	 */
	if (UCA0RXBUF == BLUMOTE_IR_TRANSMIT_ABORT) {
		if (ir_tx_abort()) {
			(void)buf_deque(&uart_rx, NULL);
		}
	}
}

#pragma vector = TIMERA0_VECTOR
__interrupt void TIMERA0_ISR(void)
{
	CCR0 += ((SYS_CLK * 1000) / (IR_CARRIER_FREQ * 2) - 1);
	P1OUT ^= BIT4;
}

#pragma vector = TIMERA1_VECTOR
__interrupt void TIMERA1_ISR(void)
{
 	switch (TAIV) {
 	case 2:
		CCR1 += ((SYS_CLK * US_PER_SYS_TICK) - 1);	/* Add Offset to CCR1 */
 		sys_tick++;
 		ir_sys_tick++;
		break;
	}
}

/* IR receive variables */
int_fast32_t pulse_accumulator = 0;

/* Port 1 interrupt service routine
 * 
 * P1.3 - Modulated IR code
 */
#pragma vector = PORT1_VECTOR
__interrupt void Port_1(void)
{
	if (P1IFG & BIT3) {
		int_fast32_t elapsed_time = get_ir_us();

		if (elapsed_time <= MAX_FILTERED_SPACE_TIME) {
			pulse_accumulator += elapsed_time;
		} else {
			/* store the pulse duration */
			(void)buf_enque(&gp_rx_tx, (pulse_accumulator >> 8) & 0xFF);
			(void)buf_enque(&gp_rx_tx, pulse_accumulator & 0xFF);
			/* store the space duration */
			(void)buf_enque(&gp_rx_tx, (elapsed_time >> 8) & 0xFF);
			(void)buf_enque(&gp_rx_tx, elapsed_time & 0xFF);
			/* reset accumulator */
			pulse_accumulator = 0;
		}
	
		/* setup for the next pulse/space */
		P1IES ^= BIT3;	/* P1.3 toggle edge detection */
		P1IFG &= ~BIT3;	/* P1.3 IFG cleared */
	}
}
