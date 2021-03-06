/*
 * Copyright (c) 2011 Woelfware
 */

#include <msp430.h>
#include "btime.h"
#include "config.h"
#include "hw.h"

static volatile int_fast32_t sys_tick = 0;
static uint16_t ccr0_timing = 0;

int_fast32_t baud_rate = 0;

void carrier_freq(bool on)
{
	if (on) {
		CCR0 = TAR + ccr0_timing;  /* Reset timing */
		CCTL0 |= CCIE;	/* CCR0 interrupt enabled */
	} else {
		CCTL0 &= ~CCIE;	/* CCR0 interrupt disabled */
		P1OUT &= ~(BIT4 | BIT5);	/* Turn off IR LED */
	}
}

int_fast32_t get_us()
{
	return get_sys_tick() * US_PER_SYS_TICK;
}

int_fast32_t get_sys_tick()
{
	int_fast32_t elapsed_time;

	_disable_interrupts();
	elapsed_time = sys_tick;
	sys_tick = 0;
	_enable_interrupts();

	return elapsed_time;
}

void msp430_init_dco()
{
	if (CALBC1_16MHZ != 0xFF) {
		DCOCTL = 0x00;	/* Clear DCL for BCL12 */
		/* Info is intact, use it. */
		BCSCTL1 = CALBC1_16MHZ;
		DCOCTL = CALDCO_16MHZ;
	} else {
		/* Info is missing; guess at a good value. */
		BCSCTL1 = 0x8f;	/* CALBC1_16MHZ at 0x10f9 */
		DCOCTL = 0x7f;	/* CALDCO_16MHZ at 0x10f8 */
	}
}

void init_hw()
{
	WDTCTL = WDTPW + WDTHOLD;	/* stop WDT */
	msp430_init_dco();
	P3SEL = BIT4 | BIT5;	/* P3.4,5 = USCI_A0 TXD/RXD */
	P3DIR = BIT0 | BIT1 | BIT3;	/* reset, baud_rate, PIO3 */ 
	P3OUT = BIT0;
	UCA0CTL1 |= UCSSEL_2;	/* SMCLK */
	set_baud_115200();
	UCA0CTL1 &= ~UCSWRST;	/* Initialize USCI state machine */
	IE2 |= UCA0RXIE;	/* Enable USCI_A0 RX interrupt */

	/* IR configs */
	P1SEL = BIT5;  /* Set as alternate function */
	P1DIR = BIT4 | BIT5;    /* P1.4,5 = IR_OUT1, IR_OUT2 */
	P1OUT &= ~(BIT4 | BIT5);	/* Turn off IR LED */
	CCTL1 = CCIE;	/* CCR1 interrupt enabled */
 	CCR1 = (SYS_CLK * US_PER_SYS_TICK) - 1;
	CCTL0 = OUTMOD_4;  /* CCR0 interrupt disabled and Toggle */
	TACTL = TASSEL_2 +  MC_2; /* SMCLK, continuous */

	__bis_SR_register(GIE);	/* interrupts enabled */
}

void reset_rn42()
{
	P3OUT &= ~BIT0;
	wait_us(BLUETOOTH_RESET_HOLD_TIME);
	P3OUT |= BIT0;
	wait_us(BLUETOOTH_STARTUP_TIME);
}

void set_baud_115200()
{
	UCA0BR0 = 138;		/* 16MHz 115200 */
	UCA0BR1 = 0;		/* 16MHz 115200 */
	UCA0MCTL = UCBRS2 + UCBRS1 + UCBRS0;	/* Modulation UCBRSx = 7 */
	baud_rate = 115200;
}

void set_baud_9600()
{
	UCA0BR0 = 138;		/* 16MHz 9600 */
	UCA0BR1 = 0;		/* 16MHz 9600 */
	UCA0MCTL = UCBRS2 + UCBRS1;	/* Modulation UCBRSx = 6 */
	baud_rate = 9600;
}

void update_ccr0_timing(uint8_t ir_carrier_frequency)
{
	ccr0_timing = (SYS_CLK * 1000) / (ir_carrier_frequency * 2) - 1;
}

#pragma vector = TIMERA0_VECTOR
__interrupt void TIMERA0_ISR(void)
{
	CCR0 += ccr0_timing;
	P1OUT ^= BIT4 | BIT5;
}

#pragma vector = TIMERA1_VECTOR
__interrupt void TIMERA1_ISR(void)
{
 	switch (TAIV) {
 	case TAIV_TACCR1:
		CCR1 += (SYS_CLK * US_PER_SYS_TICK) - 1;
 		sys_tick++;
		break;
	}
}
