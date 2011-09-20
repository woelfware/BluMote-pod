#include "bluetooth.h"
#include "blumote.h"
#include "config.h"
#include "hw.h"
#include "ir.h"
#include <stdint.h>
#include <string.h>

static int repeat_cnt = NBR_IR_BURSTS;
static bool txing_ir_code = false,
	abort_tx = false;

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

bool ir_learn(int_fast32_t us)
{
	enum state {
		default_state = 0,
		enable_ir_learn = 0,
		rx_start_of_pkt1,
		rx_start_of_pkt2,
		rx_pulses_spaces,
		handle_cleanup
	};
	static enum state current_state = default_state;
	static int_fast32_t ttl = IR_LEARN_CODE_TIMEOUT;
	bool run_again = true;

	ttl -= us;
	if (ttl < 0) {
		current_state = handle_cleanup;
	}

	switch (current_state) {
	case enable_ir_learn:
		ENABLE_IR_LEARN();
		current_state = rx_start_of_pkt1;
		break;
		
	case rx_start_of_pkt1:
	case rx_start_of_pkt2: {
		/* filter out the first packet to help ensure we don't store
		 * a partial packet
		 */
		int i;
		uint8_t ir_signal[4];
		bool got_pulse_space;

find_max_filtered_space_time:
		got_pulse_space = true;

		for (i = 0 ; i < 4; i++) {
			if (buf_deque(&gp_rx_tx, &ir_signal[i])) {
				for ( ; i >= 0; i--) {
					buf_undeque(&gp_rx_tx, ir_signal[i]);
				}
				got_pulse_space = false;
				break;
			}
		}

		if (got_pulse_space) {
			uint16_t space = (ir_signal[2] << 8) + ir_signal[3];
				
			if (space > MAX_SPACE_WAIT_TIME) {
				current_state = (current_state == rx_start_of_pkt1)
						? rx_start_of_pkt2 : rx_pulses_spaces;
			} else {
				/* keep popping off the numbers until we find what we're looking for
				 * or the buffer is empty.
				 */
				goto find_max_filtered_space_time;
			}
		}
		}
		break;

	case rx_pulses_spaces: {
		int i;
		struct circular_buffer ir_buf;
		uint16_t space;
		bool look_for_end = true,
			found_end = false;
		uint8_t ir_signal[4];

		if (buf_full(&gp_rx_tx)) {
			DISABLE_IR_LEARN();
		} else {
			break;
		}

		ir_buf = gp_rx_tx;
		
		/* the buffer is full
		 * remove extra packets from the response
		 */
		while (look_for_end) {
			for (i = 0 ; i < 4; i++) {
				if (buf_deque(&ir_buf, &ir_signal[i])) {
					look_for_end = false;
				}
			}

			if (!look_for_end) {
				break;
			}

			space = (ir_signal[2] << 8) + ir_signal[3];
				
			if (space > MAX_SPACE_WAIT_TIME) {
				/* sync gp_rx_tx to the detected full packet */
				while (gp_rx_tx.wr_ptr != ir_buf.rd_ptr) {
					(void)buf_unenque(&gp_rx_tx, NULL);
				}
				
				/* done */
				found_end = true;
				current_state = default_state;
				ttl = IR_LEARN_CODE_TIMEOUT;
				run_again = false;
				look_for_end = false;
			}
		}
		
		if (!found_end) {
			current_state = handle_cleanup;
		}
		}
		break;

	case handle_cleanup:
	default:
		current_state = default_state;
		DISABLE_IR_LEARN();
		ttl = IR_LEARN_CODE_TIMEOUT;
		run_again = false;
		buf_clear(&gp_rx_tx);
		break;
	}

	return run_again;
}

bool ir_main(int_fast32_t us)
{
	enum state {
		default_state = 0,
		wait_for_code = 0,
		tx_pulses,
		tx_spaces,
		handle_cleanup
	};
	static enum state current_state = default_state;
	static struct circular_buffer m_ir_buf;
	static int_fast32_t ttl;
	bool run_again = true;
	uint8_t c;

	if (!own_gp_buf(gp_buf_owner_ir)) {
		return run_again;
	}	

	switch (current_state) {
	case wait_for_code:
		if (buf_deque(&gp_rx_tx, &c)) {
			gp_buf_owner = gp_buf_owner_none;
			run_again = false;
		} else {
			/* have a code to tx */
			ttl = (int_fast32_t)c << 8;
			buf_undeque(&gp_rx_tx, c);
			memcpy(&m_ir_buf, &gp_rx_tx, sizeof(gp_rx_tx));
			buf_deque(&gp_rx_tx, NULL);
			if (!buf_deque(&gp_rx_tx, &c)) {
				ttl += c;
				current_state = tx_pulses;
				txing_ir_code = true;
				carrier_freq(true);
			} else {
				/* incomplete ir code */
				(void)bluetooth_putchar(BLUMOTE_NAK);
				current_state = handle_cleanup;
			}
		}
		break;

	case tx_pulses:
		ttl -= us;
		
		if (abort_tx) {
			ttl = 0;
			repeat_cnt = 0;
			buf_clear(&gp_rx_tx);
		}
		
		if (ttl <= 0) {
			carrier_freq(false);
			if (!buf_deque(&gp_rx_tx, &c)) {
				ttl = (int_fast32_t)c << 8;
				if (!buf_deque(&gp_rx_tx, &c)) {
					ttl += c;
					current_state = tx_spaces;
				} else {
					/* incomplete ir code */
					(void)bluetooth_putchar(BLUMOTE_NAK);
					current_state = handle_cleanup;
				}
			} else {
				/* done */
				repeat_cnt--;
				if (repeat_cnt > 0) {
					memcpy(&gp_rx_tx, &m_ir_buf, sizeof(gp_rx_tx));
					current_state = wait_for_code;
				} else {
					(void)bluetooth_putchar(BLUMOTE_ACK);
					current_state = handle_cleanup;
				}
			}
		}
		break;

	case tx_spaces:
		ttl -= us;
		
		if (abort_tx) {
			ttl = 0;
			repeat_cnt = 0;
			buf_clear(&gp_rx_tx);
		}
		
		if (ttl <= 0) {
			if (!buf_deque(&gp_rx_tx, &c)) {
				ttl = (int_fast32_t)c << 8;
				if (!buf_deque(&gp_rx_tx, &c)) {
					ttl += c;
					carrier_freq(true);
					current_state = tx_pulses;
				} else {
					/* incomplete ir code */
					(void)bluetooth_putchar(BLUMOTE_NAK);
					current_state = handle_cleanup;
				}
			} else {
				/* done */
				repeat_cnt--;
				if (repeat_cnt > 0) {
					memcpy(&gp_rx_tx, &m_ir_buf, sizeof(gp_rx_tx));
					current_state = wait_for_code;
				} else {
					(void)bluetooth_putchar(BLUMOTE_ACK);
					current_state = handle_cleanup;
				}
			}
		}
		break;

	case handle_cleanup:
	default:
		/* shouldn't get here */
		carrier_freq(false);
		gp_buf_owner = gp_buf_owner_none;
		current_state = default_state;
		run_again = false;
		repeat_cnt = NBR_IR_BURSTS;
		txing_ir_code = false;
		abort_tx = false;
		break;
	}

	return run_again;
}

bool ir_tx_abort()
{
	bool aborted = false;

	if (txing_ir_code) {
		abort_tx = true;
		aborted = true;
	}

	return aborted;
}

void set_ir_repeat_cnt(int cnt)
{
	if (cnt) {
		repeat_cnt = cnt;
	} else {
		repeat_cnt = NBR_IR_BURSTS;
	}
}

