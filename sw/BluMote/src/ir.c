#include "config.h"
#include "hw.h"
#include "ir.h"
#include <stdint.h>
#include <string.h>

static inline bool is_space()
{
	return (P1IN & BIT3) ? true : false;
}

static void carrier_freq(bool on)
{
	if (on){
		CCTL0 |= CCIE;	/* CCR0 interrupt enabled */		
	} else {
		CCTL0 &= ~CCIE;	/* CCR0 interrupt disabled */
		P1OUT &= ~(BIT4 + BIT5);	/* Turn off IR LED */
	}
}

int ir_getchar()
{
	int c;

	if (buf_deque(&gp_rx_tx, (uint8_t *)&c)) {
		c = EOF;
	}

	return c;
}

bool ir_learn(int us)
{
	enum state {
		default_state = 0,
		rx_start_of_pkt1 = 0,
		rx_start_of_pkt2,
		rx_pulses,
		rx_spaces,
		handle_timeout
	};
	static enum state current_state = default_state;
	static int32_t ttl = IR_LEARN_CODE_TIMEOUT;
	static uint16_t duration = 0;
	bool run_again = true;

	ttl -= us;
	if (ttl < 0) {
		current_state = handle_timeout;
	}

	switch (current_state) {
	case rx_start_of_pkt1:
	case rx_start_of_pkt2:
		/* filter out the first packet to help ensure we don't store
		 * a partial packet
		 */
		if (is_space()) {
			duration += us;
			if (duration > MAX_SPACE_WAIT_TIME) {
				duration = MAX_SPACE_WAIT_TIME;
			}
		} else {
			if (duration >= MAX_SPACE_WAIT_TIME) {
				current_state = (current_state == rx_start_of_pkt1)
					? rx_start_of_pkt2 : rx_pulses;
			}
			duration = 0;
		}
		break;

	case rx_pulses:
		if (!is_space()) {
			duration += us;
		} else {
			(void)buf_enque(&gp_rx_tx, (duration >> 8) & 0xFF);
			(void)buf_enque(&gp_rx_tx, duration & 0xFF);
			duration = 0;
			current_state = rx_spaces;
		}
		break;

	case rx_spaces:
		if (is_space()) {
			duration += us;
			if (duration > MAX_SPACE_WAIT_TIME) {
				run_again = false;
				duration = 0;
				ttl = IR_LEARN_CODE_TIMEOUT;
				current_state = default_state;
			}
		} else {
			(void)buf_enque(&gp_rx_tx, (duration >> 8) & 0xFF);
			(void)buf_enque(&gp_rx_tx, duration & 0xFF);
			duration = 0;
			current_state = rx_pulses;
		}
		break;

	case handle_timeout:
	default:
		current_state = default_state;
		duration = 0;
		ttl = IR_LEARN_CODE_TIMEOUT;
		run_again = false;
		buf_clear(&gp_rx_tx);
		break;
	}

	return run_again;
}

bool ir_main(int us)
{
	enum state {
		default_state = 0,
		tx_start = 0,
		tx_pulses,
		tx_spaces
	};
	static enum state current_state = default_state;
	static uint16_t duration = 0;
	static uint16_t stop_time_us = 0;
	bool run_again = true;
	bool get_next = false;

	if (!own_gp_buf(gp_buf_owner_ir)) {
		return run_again;
	}	
	
	if (!buf_empty(&gp_rx_tx)) {
		switch (current_state) {
		case tx_start:
			carrier_freq(true);	/*Start pulse clock*/
			get_next = true;
			break;

		case tx_pulses:
			if (duration < stop_time_us) {
				duration += us;
			} else {
				carrier_freq(false);	/*Stop Pulse Clock*/
				get_next = true;
				current_state = tx_spaces;
			}
			break;
	
		case tx_spaces:
			if (duration < stop_time_us) {
				duration += us;
			} else {
				carrier_freq(true);	/*Start pulse clock*/
				get_next = true;
				current_state = tx_pulses;
			}
			break;
	
		default:
			current_state = default_state;
			run_again = false;
			carrier_freq(false);	/*Stop Pulse Clock*/
			buf_clear(&gp_rx_tx);
			break;
		}
		
		if (get_next) {
			stop_time_us = ir_getchar() << 8;
			stop_time_us += ir_getchar();
			if ((int)stop_time_us == EOF) {
				carrier_freq(false);	/*Stop Pulse Clock*/
				run_again = false;
				current_state = default_state;
			}
			get_next = false;
			duration = 0;
		}
	} else {
		carrier_freq(false);	/*Stop Pulse Clock*/
		current_state = default_state;
	}

	return run_again;
}
