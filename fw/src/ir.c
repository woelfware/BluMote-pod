/*
 * Copyright (c) 2011 Woelfware
 */

#include <stdbool.h>
#include <msp430.h>
#include "blumote.h"
#include "config.h"
#include "hw.h"
#include "ir.h"

#define CLAMP(x, l, h) (((x) > (h)) ? (h) : (((x) < (l)) ? (l) : (x)))

#define is_space() (P1IN & BIT3)
#define is_pulse() !is_space()

static uint8_t ir_carrier_frequency = 0;	/* pulse tx frequency in kHz */

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
static bool get_pkt()
{
	int_fast32_t pulse_duration = 0,
		space_duration = 0,
		ttl = IR_LEARN_CODE_TIMEOUT / US_PER_SYS_TICK;
	uint16_t const * const MAX_UBER_BUF_ADDR = (uint16_t *)&uber_buf.buf[uber_buf.buf_size - 4];
	uint16_t *uber_buf_wr_ptr = (uint16_t *)&uber_buf.buf[uber_buf.wr_ptr];

	/* wait for start of pkt */
	while (ttl >= 0) {
		if (is_pulse()) {
			break;
		}
		ttl -= get_sys_tick();
	}
	if (ttl < 0) {
		/* timed out */
		return true;
	}

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
				return false;
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

	/* wait until the start of a pulse */
	(void)get_sys_tick();
	while (space < (MAX_FILTERED_SPACE_TIME / US_PER_SYS_TICK)) {
		while (is_space());
		space = get_sys_tick();
	}

	space = 0;
	while (is_space());
	(void)get_sys_tick();

	while (space < (MAX_FILTERED_SPACE_TIME / US_PER_SYS_TICK)) {
		elapsed_time += space;
		while (is_pulse());
		pulses++;
		elapsed_time += get_sys_tick();
		while (is_space());
		space = get_sys_tick();
	}

	ir_carrier_freq = ((pulses * 1000000)
			/ (elapsed_time * US_PER_SYS_TICK))
		+ 500/*for rounding*/;	/* Hz */
	ir_carrier_freq /= 1000;	/* convert to kHz */
	ir_carrier_freq = CLAMP(ir_carrier_freq, MIN_IR_CARRIER_FREQ, UINT8_MAX);

	set_ir_carrier_frequency(ir_carrier_freq);

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

	if (get_pkt()) {
		return true;
	}
	get_carrier_frequency();

	mult_sys_tick(starting_addr);
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
