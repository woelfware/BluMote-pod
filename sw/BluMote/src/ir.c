#include "config.h"
#include "hw.h"
#include "ir.h"
#include <stdint.h>
#include <string.h>

static inline bool is_space()
{
	return (P1IN & BIT3) ? true : false;
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
			(void)buf_enque(&ir_rx, (duration >> 8) & 0xFF);
			(void)buf_enque(&ir_rx, duration & 0xFF);
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
			(void)buf_enque(&ir_rx, (duration >> 8) & 0xFF);
			(void)buf_enque(&ir_rx, duration & 0xFF);
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
		while (!buf_deque(&ir_rx, NULL));
		break;
	}

	return run_again;
}

bool ir_main(int us)
{
	bool run_again = true;

	return run_again;
}
