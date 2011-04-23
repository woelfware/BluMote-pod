/*
 * Copyright (c) 2011 Woelfware
 */

#include "config.h" 
#include "hw.h"
#include "msp430.h"

enum gp_buf_owner gp_buf_owner = gp_buf_owner_none;

struct circular_buffer uart_rx,
	uart_tx,
	gp_rx_tx;
static volatile uint8_t buf_uart_rx[UART_RX_BUF_SIZE],
	buf_uart_tx[UART_TX_BUF_SIZE],
	buf_gp[GP_BUF_SIZE];

static volatile int ir_tick = 0;
static volatile int sys_tick = 0;
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

	/* IR configs */
	P1SEL |= BIT5;  /* Set as alternate function */
	CCTL1 = CCIE;	/* CCR1 interrupt enabled */
 	CCR1 = (SYS_CLK * US_PER_IR_TICK) - 1;
	CCTL0 = OUTMOD_4;  /* CCR0 interrupt disabled and Toggle */
	CCR0 = ((SYS_CLK * 1000) / (IR_CARRIER_FREQ * 2) - 1);
	TACTL = TASSEL_2 +  MC_2; /* SMCLK, continuous */

	__bis_SR_register(GIE);	/* interrupts enabled */

	init_bufs();
}

int get_ms()
{
	int elapsed_time;
	__disable_interrupt();
	elapsed_time = sys_tick << 1;
	sys_tick = 0;
	__enable_interrupt();
	return elapsed_time;
}

int get_us()
{
	int elapsed_time;
	__disable_interrupt();
	elapsed_time = ir_tick * US_PER_IR_TICK;
	ir_tick = 0;
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
		CCR1 += ((SYS_CLK * US_PER_IR_TICK) - 1);	/* Add Offset to CCR1 */
 		ir_tick++;	
		break;
	}
}

#pragma vector = WDT_VECTOR
__interrupt void watchdog_timer(void)
{
	sys_tick++;
}
