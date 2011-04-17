#include "config.h"
#include "hw.h"
#include "ir.h"
#include <stdint.h>
#include <string.h>

static inline bool is_space()
{
	return (P1IN & BIT3) ? true : false;
}

bool ir_learn(int ir_ticks)
{
	enum state {
		default_state = 0,
		rx_start_of_pkt1 = 0,
		rx_start_of_pkt2,
		rx_pulses,
		rx_spaces
	};
	static enum state current_state = default_state;
	static uint16_t duration = 0;
	bool run_again = true;

	if (!ir_ticks) {
		return run_again;
	}

	switch (current_state) {
	case rx_start_of_pkt1:
	case rx_start_of_pkt2:
		/* filter out the first packet to help ensure we don't store
		 * a partial packet
		 */
		if (is_space()) {
			duration += ir_ticks;
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
			duration += ir_ticks;
		} else {
			(void)buf_enque(&ir_rx, *(uint8_t *)(&duration + 1));
			(void)buf_enque(&ir_rx, *(uint8_t *)(&duration));
			duration = 0;
			current_state = rx_spaces;
		}
		break;

	case rx_spaces:
		if (is_space()) {
			duration += ir_ticks;
			if (duration > MAX_SPACE_WAIT_TICKS) {
				run_again = false;
				duration = 0;
				current_state = default_state;
			}
		} else {
			(void)buf_enque(&ir_rx, *(uint8_t *)(&duration + 1));
			(void)buf_enque(&ir_rx, *(uint8_t *)(&duration));
			duration = 0;
			current_state = rx_pulses;
		}
		break;

	default:
		current_state = default_state;
		duration = 0;
		run_again = false;
		break;
	}

	return run_again;
}

bool ir_main(int ir_ticks)
{
	bool run_again = true;
	
	return run_again;
}
