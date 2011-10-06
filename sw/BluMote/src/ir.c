/*
 * Copyright (c) 2011 Woelfware
 */

#include <stdbool.h>
#include <msp430.h>
#include "blumote.h"
#include "buffer.h"
#include "config.h"
#include "hw.h"
#include "ir.h"

static int_fast32_t gap;	/* time between packets */

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
		if (*ptr++ > gap) {
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

/* true - timeout
 * false - exited normally
 */
static void get_pkt(int_fast32_t *ttl)
{
	int_fast32_t pulse_duration = 0,
		space_duration = 0;
	uint16_t const * const MAX_UBER_BUF_ADDR = (uint16_t *)&uber_buf.buf[uber_buf.buf_size - 4];
	uint16_t *uber_buf_wr_ptr = (uint16_t *)&uber_buf.buf[uber_buf.wr_ptr];

	/* wait for end of pkt */
	while (space_duration < gap) {
		while (is_space()) {
			int_fast32_t const elapsed_time = get_us();
			space_duration += elapsed_time;
			*ttl += elapsed_time;
			if (*ttl >= IR_LEARN_CODE_TIMEOUT) {
				*ttl = IR_LEARN_CODE_TIMEOUT;
				return;
			}
		}
		if (space_duration < gap) {
			space_duration = 0;
			while (!is_space()) {
				*ttl += get_us();
				if (*ttl >= IR_LEARN_CODE_TIMEOUT) {
					*ttl = IR_LEARN_CODE_TIMEOUT;
					return;
				}
			}
		}
	}
	space_duration = 0;
	while (is_space()) {
		*ttl += get_us();
		if (*ttl >= IR_LEARN_CODE_TIMEOUT) {
			*ttl = IR_LEARN_CODE_TIMEOUT;
			return;
		}
	}
	*ttl += get_us();

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

static void get_pkt_specs(int_fast32_t *ttl)
{
	int_fast32_t space = 0,
		my_gap[2] = {0, 0};
	int_fast32_t const end_time = *ttl + 2000000;	/* sample the gaps for 2 second */

	gap = 0;

	while (is_space());	/* wait until the start of the space */

	while (*ttl <= end_time) {
		space = 0;
		while (!is_space());
		*ttl += get_sys_tick();

		while (is_space());
		space = get_us();
		*ttl += space;

		if (space > my_gap[1]) {
			if (space > my_gap[0]) {
				if (space > gap) {
					my_gap[1] = my_gap[0];
					my_gap[0] = gap;
					gap = space;
				} else {
					my_gap[1] = my_gap[0];
					my_gap[0] = space;
				}
			} else {
				my_gap[1] = space;
			}
		}
	}

	gap -= gap / 50;	/* reduce by 2% */

	return;
}

/* true - timeout
 * false - exited normally
 */
static void get_rdy_for_pkt(int_fast32_t *ttl)
{
	int_fast32_t duration = 0,
		elapsed_time;

	while (is_space()) {	/* wait for a pulse */
		*ttl += get_us();

		if (*ttl >= IR_LEARN_CODE_TIMEOUT) {
			*ttl = IR_LEARN_CODE_TIMEOUT;
			return;
		}
	}

	*ttl += get_sys_tick();

	/* wait for space long enough to be between packets */
	while (1) {
		if (is_space()) {
			elapsed_time += get_us();
			duration += elapsed_time;
			*ttl += elapsed_time;
			if (duration > gap) {
				return;
			} else if (*ttl >= IR_LEARN_CODE_TIMEOUT) {
				*ttl = IR_LEARN_CODE_TIMEOUT;
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

bool ir_learn()
{
	int_fast32_t ttl = 0;
	int const starting_addr = uber_buf.wr_ptr;

	get_rdy_for_pkt(&ttl);
	if (ttl >= IR_LEARN_CODE_TIMEOUT) {
		return true;
	}
	get_pkt_specs(&ttl);
	if (ttl >= IR_LEARN_CODE_TIMEOUT) {
		return true;
	}
	get_pkt(&ttl);
	if (ttl >= IR_LEARN_CODE_TIMEOUT) {
		return true;
	}
	mult_sys_tick(starting_addr);
	find_pkt_end(starting_addr);
	fix_endianness(starting_addr);

	return false;
}

bool ir_tx(volatile struct buf *abort)
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
		
		/* handle an abort command, ignore anything else */
		if (abort->wr_ptr) {
			if (abort->buf[0] == BLUMOTE_IR_TRANSMIT_ABORT) {
				return true;
			} else {
				abort->wr_ptr = 0;
			}
		}

		ttl = get_ttl();
		if (uber_buf.rd_ptr >= uber_buf.wr_ptr) {
			done = true;
		}
		while (ttl > 0) {
			ttl -= get_us();
		}
	}

	return false;
}
