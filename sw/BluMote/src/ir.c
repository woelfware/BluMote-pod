/*
 * Copyright (c) 2011 Woelfware
 */

#include <stdbool.h>
#include <msp430.h>
#include "blumote.h"
#include "config.h"
#include "hw.h"
#include "ir.h"

#define is_space() (P1IN & BIT3)
#define is_pulse() !is_space()

static int_fast32_t gap = 0;	/* time between packets */
static uint8_t ir_carrier_frequency = 0;	/* pulse tx frequency in kHz */

static void find_pkt_end(int starting_addr)
{
	uint16_t *ptr = (uint16_t *)&uber_buf.buf[starting_addr + 2];	/* +2 to get to the first space */
	uint16_t const * const end_addr =
		(uint16_t *)&uber_buf.buf[uber_buf.buf_size - (sizeof(uint16_t))];

	while (ptr <= end_addr) {
		if ((*ptr > gap) || (*ptr == UINT16_MAX)) {
			ptr++;
			uber_buf.wr_ptr = (uint8_t *)ptr - uber_buf.buf;
			break;
		}
		ptr += 2;	/* just need to check spaces, skip over pulses */
	}
}

static void fix_endianness(int i)
{
	int const stop_index = uber_buf.buf_size - 1;
	uint8_t tmp;

	while (i < stop_index) {
		tmp = uber_buf.buf[i];
		uber_buf.buf[i] = uber_buf.buf[i + 1];
		uber_buf.buf[i + 1] = tmp;
		i += 2;
	}
}

/* true - timeout
 * false - exited normally
 */
static void get_pkt()
{
	int_fast32_t pulse_duration = 0,
		space_duration = 0;
	uint16_t const * const MAX_UBER_BUF_ADDR = (uint16_t *)&uber_buf.buf[uber_buf.buf_size - 4];
	uint16_t *uber_buf_wr_ptr = (uint16_t *)&uber_buf.buf[uber_buf.wr_ptr];

	/* wait for end of pkt */
	while (space_duration < gap) {
		while (is_space()) {
			space_duration += get_us();
		}
		if (space_duration < gap) {
			space_duration = 0;
			while (is_pulse());
		}
	}
	space_duration = 0;
	while (is_space());

	while (1) {
		while (is_pulse());
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

static void get_carrier_frequency()
{
	int_fast32_t space = 0,
		elapsed_time = 0,
		ir_carrier_freq;
	uint16_t pulses = 0;
	uint8_t const MIN_IR_CARRIER_FREQ = 38;	/* adjust for limitations of the TACC0 register */

	/* wait until the start of the packet */
	(void)get_sys_tick();
	while (space < gap) {
		while (is_space());
		space = get_us();
	}

	space = 0;
	while (is_space());

	while (space < MAX_FILTERED_SPACE_TIME) {
		elapsed_time += space;
		while (is_pulse());
		pulses++;
		elapsed_time += get_us();
		while (is_space());
		space = get_us();
	}

	ir_carrier_freq = ((pulses * 1000000) / elapsed_time) + 500/*for rounding*/;	/* Hz */
	ir_carrier_freq /= 1000;	/* convert to kHz */
	if (ir_carrier_freq < UINT8_MAX) {
		if (ir_carrier_freq < MIN_IR_CARRIER_FREQ) {
			ir_carrier_freq = MIN_IR_CARRIER_FREQ;
		}
	} else {
		ir_carrier_freq = UINT8_MAX;
	}
	set_ir_carrier_frequency(ir_carrier_freq);

	return;
}

static void get_pkt_gap()
{
	int_fast32_t space = 0,
		my_gap[2] = {0, 0},
		ttl = 400000;	/* sample the gaps for 400 ms */

	gap = 0;

	while (is_space());	/* wait until the start of the space */

	(void)get_sys_tick();
	while (ttl > 0) {
		space = 0;
		while (is_pulse());
		ttl -= get_us();

		while (is_space());
		space = get_us();
		ttl -= space;

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

	/* filter out large gaps */
	if (gap > (my_gap[0] + my_gap[0] / 10)) {
		gap = my_gap[0];
		if (gap > (my_gap[1] + my_gap[1] / 10)) {
			gap = my_gap[1];
		}
	}
	gap -= gap / 25;	/* reduce by 4% */

	return;
}

static uint16_t get_ttl()
{
	uint16_t ttl = ((uint16_t)uber_buf.buf[uber_buf.rd_ptr] * 0x100)
			+ uber_buf.buf[uber_buf.rd_ptr + 1];
	uber_buf.rd_ptr += 2;
	return ttl;
}

static void mult_sys_tick(int starting_addr)
{
	int_fast32_t sys_time;
	uint16_t *ptr = (uint16_t *)&uber_buf.buf[starting_addr];
	uint16_t const * const end_addr =
		(uint16_t *)&uber_buf.buf[uber_buf.buf_size - (sizeof(*ptr))];

	while (ptr <= end_addr) {
		sys_time = *ptr * (int_fast32_t)US_PER_SYS_TICK;

		*ptr++ = (sys_time <= UINT16_MAX) ? sys_time : UINT16_MAX;
	} 
}

uint8_t get_ir_carrier_frequency()
{
	return ir_carrier_frequency;
}

bool ir_learn()
{
	int const starting_addr = uber_buf.wr_ptr;

	while (is_space());	/* wait for a pulse */
	get_pkt_gap();
	get_carrier_frequency();
	get_pkt();

	mult_sys_tick(starting_addr);
	find_pkt_end(starting_addr);
	fix_endianness(starting_addr);

	return false;
}

bool ir_tx(volatile struct buf *abort)
{
	int const rd_ptr = uber_buf.rd_ptr;
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

	uber_buf.rd_ptr = rd_ptr;

	return false;
}

void set_ir_carrier_frequency(uint8_t frequency)
{
	uint8_t const MIN_FREQ = 38;

	ir_carrier_frequency = (frequency >= MIN_FREQ) ? frequency : MIN_FREQ;
}
