/*
 * Copyright (c) 2011 Woelfware
 */

#include <stdbool.h>
#include <msp430.h>
#include "buffer.h"
#include "config.h"
#include "hw.h"
#include "ir.h"

static inline bool is_space();

static void carrier_freq(bool on)
{
	if (on) {
		CCR0 = TAR + ((SYS_CLK * 1000) / (IR_CARRIER_FREQ * 2) - 1);  /* Reset timing */
		CCTL0 |= CCIE;	/* CCR0 interrupt enabled */
	} else {
		CCTL0 &= ~CCIE;	/* CCR0 interrupt disabled */
		P1OUT &= ~(BIT4 | BIT5);	/* Turn off IR LED */
	}
}

static void find_pkt_end(int starting_addr)
{
	uint16_t *ptr = (uint16_t *)&uber_buf.buf[starting_addr];
	uint16_t const * const end_addr =
		(uint16_t *)&uber_buf.buf[uber_buf.buf_size - (sizeof(uint16_t))];

	while (ptr < end_addr) {
		if (*ptr++ > MAX_SPACE_WAIT_TIME) {
			uber_buf.wr_ptr = (uint8_t *)ptr - uber_buf.buf;
			break;
		}
	} 
}

static void fix_endianness(int i)
{
	uint8_t tmp;

	while (i < uber_buf.buf_size) {
		tmp = uber_buf.buf[i];
		uber_buf.buf[i] = uber_buf.buf[i + 1];
		uber_buf.buf[i + 1] = tmp;
		i += 2;
	}
}

static void get_pkt()
{
	int_fast32_t pulse_duration = 0,
		space_duration = 0;
	uint16_t const * const MAX_UBER_BUF_ADDR = (uint16_t *)&uber_buf.buf[uber_buf.buf_size - 4];
	uint16_t *uber_buf_wr_ptr = (uint16_t *)&uber_buf.buf[uber_buf.wr_ptr];

	while (is_space());	/* wait for incoming packet */
	(void)get_sys_tick();

	while (1) {
		while (!is_space());
		pulse_duration += get_sys_tick();

		while (is_space());
		space_duration += get_sys_tick();

		if (space_duration <= MAX_FILTERED_SPACE_TIME / US_PER_SYS_TICK) {
			pulse_duration += space_duration;
			space_duration = 0;
		} else {
			if (uber_buf_wr_ptr <= MAX_UBER_BUF_ADDR) {
				*uber_buf_wr_ptr++ = pulse_duration;
				pulse_duration = 0;
				*uber_buf_wr_ptr++ = space_duration;
				space_duration = 0;
			} else {
				/* buffer filled up */
				uber_buf.wr_ptr = (uint8_t *)uber_buf_wr_ptr - uber_buf.buf;  
				return;
			}
		}
	}
}

static void get_rdy_for_pkt()
{
	int_fast32_t duration = 0;

	while (is_space());	/* wait for a pulse */
	(void)get_sys_tick();

	/* wait for space long enough to be between packets */
	while (1) {
		if (is_space()) {
			duration += get_us();
			if (duration > MAX_SPACE_WAIT_TIME) {
				return;
			}
		} else {
			duration = 0;
		}
	}	
}

static uint16_t get_ttl()
{
	uint16_t ttl = ((uint16_t)uber_buf.buf[uber_buf.rd_ptr] * 0x100)
			+ uber_buf.buf[uber_buf.rd_ptr + 1];
	uber_buf.rd_ptr += 2;
	return ttl;
}

static inline bool is_space()
{
	return !!(P1IN & BIT3);
}

static void mult_sys_tick(int starting_addr)
{
	int_fast32_t sys_time;
	uint16_t *ptr = (uint16_t *)&uber_buf.buf[starting_addr];
	uint16_t const * const end_addr =
		(uint16_t *)&uber_buf.buf[uber_buf.buf_size - (sizeof(uint16_t))];

	while (ptr < end_addr) {
		sys_time = *ptr * US_PER_SYS_TICK;

		*ptr++ = (sys_time <= UINT16_MAX) ? sys_time : UINT16_MAX;
	} 
}

void ir_learn()
{
	int const starting_addr = uber_buf.wr_ptr;

	get_rdy_for_pkt();
	get_pkt();
	mult_sys_tick(starting_addr);
	find_pkt_end(starting_addr);
	fix_endianness(starting_addr);
}

void ir_tx()
{
	int_fast32_t ttl;
	bool done = false;

	(void)get_sys_tick();
	while (!done) {
		/* send the pulse */
		carrier_freq(true);
		ttl = get_ttl();
		while (ttl > 0) {
			ttl -= get_us();
		}

		/* send the space */
		carrier_freq(false);
		ttl = get_ttl();
		if (uber_buf.rd_ptr >= uber_buf.wr_ptr) {
			done = true;
		}
		while (ttl > 0) {
			ttl -= get_us();
		}
	}
}
