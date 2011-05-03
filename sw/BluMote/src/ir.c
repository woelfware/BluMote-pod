#include "bluetooth.h"
#include "blumote.h"
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
		rx_start_of_pkt1 = 0,
		rx_start_of_pkt2,
		rx_pulses,
		rx_spaces,
		handle_cleanup
	};
	static enum state current_state = default_state;
	static int_fast32_t ttl = IR_LEARN_CODE_TIMEOUT;
	static uint16_t duration = 0;
	bool run_again = true;

	ttl -= us;
	if (ttl < 0) {
		current_state = handle_cleanup;
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
			if (duration > MAX_SPACE_WAIT_TIME) {
				/* done */
				(void)buf_enque(&gp_rx_tx, (duration >> 8) & 0xFF);
				(void)buf_enque(&gp_rx_tx, duration & 0xFF);
				current_state = default_state;
				duration = 0;
				ttl = IR_LEARN_CODE_TIMEOUT;
				run_again = false;
			} else {
				duration += us;
			}
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
		} else {
			(void)buf_enque(&gp_rx_tx, (duration >> 8) & 0xFF);
			(void)buf_enque(&gp_rx_tx, duration & 0xFF);
			duration = 0;
			current_state = rx_pulses;
		}
		break;

	case handle_cleanup:
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
	static int repeat_cnt = 3;
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
				if (repeat_cnt) {
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
				/* incomplete ir code */
				(void)bluetooth_putchar(BLUMOTE_NAK);
				current_state = handle_cleanup;
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
		repeat_cnt = 3;
		break;
	}

	return run_again;
}
