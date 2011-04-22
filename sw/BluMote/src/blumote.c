/*
 * Copyright (c) 2011 Woelfware
 */

#include "bluetooth.h"
#include "blumote.h"
#include "config.h"
#include "hw.h"
#include <string.h>

enum m_strcmp_rc {
	m_strcmp_match,
	m_strcmp_buf_underrun,
	m_strcmp_no_match
};

bool learn_ir_code = false,
	tx_ir_code = false;

static bool blumote_process_cmd()
{
	bool change_state = true;
	uint8_t c;

	(void)buf_deque(&gp_rx_tx, &c);

	switch (c) {
	case BLUMOTE_GET_VERSION: {
		char const response[] = {BLUMOTE_ACK,
			BLUMOTE_FW, VERSION_MAJOR, VERSION_MINOR, VERSION_REV}; 
		(void)bluetooth_puts(response, sizeof(response));
		}
		break;

	case BLUMOTE_LEARN:
		learn_ir_code = true;
		break;

	case BLUMOTE_IR_TRANSMIT:
		/* pop the reserved and length fields and let
		 * the ir handle the code
		 */
		(void)buf_deque(&gp_rx_tx, NULL);	/* reserved */
		(void)buf_deque(&gp_rx_tx, NULL);	/* length */
		tx_ir_code = true;
		break;
	
	default: {
		char const response[] = {BLUMOTE_NAK};
		(void)bluetooth_puts(response, sizeof(response));
		}
		break;
	}

	return change_state;
}

static enum m_strcmp_rc m_strcmp(char const *str, struct circular_buffer *que)
{
	enum m_strcmp_rc rc = m_strcmp_match;
	int i = 0;
	uint8_t m_buf[16];

	for ( ; i < strlen(str); i++) {
		if (!buf_deque(&gp_rx_tx, &m_buf[i])) { 
			if (m_buf[i] != str[i]) {
				rc = m_strcmp_no_match;
				for ( ; i >= 0; i--) {
					(void)buf_undeque(que, m_buf[i]);
				}
				break;
			}
		} else {
			rc = m_strcmp_buf_underrun;
			--i;
			for ( ; i >= 0; --i) {
				(void)buf_undeque(que, m_buf[i]);
			}
			break;
		}
	}

	return rc;
}

bool init_blumote(int ms)
{
	enum state {
		default_state = 0,
		wait_one_sec1 = 0,
		wait_one_sec2,
		tx_cmd_mode,
		rx_cmd_mode,
		tx_get_name,
		rx_get_name,
		tx_set_name,
		rx_set_name,
		tx_get_low_latency,
		rx_get_low_latency,
		tx_set_low_latency,
		rx_set_low_latency,
		tx_get_low_power,
		rx_get_low_power,
		tx_set_low_power,
		rx_set_low_power,
		tx_exit_cmd_mode,
		rx_exit_cmd_mode,
		reset_bluetooth
	};
	static enum state current_state = default_state;
	static int ttl;	/* time to live */
	int c;
	bool run_again = true;
	static bool reset_bt = false;

	switch (current_state) {
	case wait_one_sec1:
		ttl = 1000;
		current_state = wait_one_sec2;

		/* clear out the rx buffers */
		while (bluetooth_getchar() != EOF);
		buf_clear(&gp_rx_tx);
		break;

	case wait_one_sec2:
		ttl -= ms;
		if (ttl < 0) {
			current_state = tx_cmd_mode;
		} 
		break;

	case tx_cmd_mode: {
		char const *str = "$$$";
		if (bluetooth_puts(str, strlen(str)) != EOF) {
			current_state = rx_cmd_mode;
			ttl = 50;
		}
		}
		break;

	case rx_cmd_mode:
		ttl -= ms;

		while ((c = bluetooth_getchar()) != EOF) {
			(void)buf_enque(&gp_rx_tx, c);
			ttl = 20;
		}
		
		if (ttl >= 0) {
			enum m_strcmp_rc rc = m_strcmp("CMD\r\n", &gp_rx_tx);

			switch (rc) {
			case m_strcmp_match:
				current_state = tx_get_name;
				buf_clear(&gp_rx_tx);
				break;

			case m_strcmp_no_match:
				current_state = reset_bluetooth;
				break;
			}
		} else {	/* invalid/no response; already in CMD mode? */
			current_state = tx_get_name;
			buf_clear(&gp_rx_tx);
		}
		break;

	case tx_get_name: {
		char const *str = "GS-\r";
		if (bluetooth_puts(str, strlen(str)) != EOF) {
			current_state = rx_get_name;
			ttl = 50;
		}
		}
		break;

	case rx_get_name:
		ttl -= ms;

		while ((c = bluetooth_getchar()) != EOF) {
			(void)buf_enque(&gp_rx_tx, c);
			ttl = 20;
		}

		if (ttl >= 0) {
			if (m_strcmp("BluMote\r\n", &gp_rx_tx) == m_strcmp_match) {
				current_state = tx_exit_cmd_mode;
				buf_clear(&gp_rx_tx);
			} else if ((m_strcmp("?\r\n", &gp_rx_tx) == m_strcmp_match)
					|| (m_strcmp("ERR\r\n", &gp_rx_tx) == m_strcmp_match)) {
				current_state = reset_bluetooth;
			}
		} else {	/* no response or invalid name */
			current_state = tx_set_name;
			buf_clear(&gp_rx_tx);
		}
		break;

	case tx_set_name: {
		char const *str = "S-,BluMote\r\n";
		if (bluetooth_puts(str, strlen(str)) != EOF) {
			current_state = rx_set_name;
			ttl = 50;
		}
		}
		break;

	case tx_get_low_latency: {
		char const *str = "GSQ\r";
		if (bluetooth_puts(str, strlen(str)) != EOF) {
			current_state = rx_get_name;
			ttl = 50;
		}
		}
		break;

	case rx_get_low_latency:
		ttl -= ms;

		while ((c = bluetooth_getchar()) != EOF) {
			(void)buf_enque(&gp_rx_tx, c);
			ttl = 20;
		}

		if (ttl >= 0) {
			if (m_strcmp("16\r\n", &gp_rx_tx) == m_strcmp_match) {
				current_state = tx_exit_cmd_mode;
				buf_clear(&gp_rx_tx);
			} else if ((m_strcmp("?\r\n", &gp_rx_tx) == m_strcmp_match)
					|| (m_strcmp("ERR\r\n", &gp_rx_tx) == m_strcmp_match)) {
				current_state = reset_bluetooth;
			}
		} else {	/* no response or invalid name */
			current_state = tx_set_low_latency;
			buf_clear(&gp_rx_tx);
		}
		break;

	case tx_set_low_latency: {
		char const *str = "SQ,16\r\n";
		if (bluetooth_puts(str, strlen(str)) != EOF) {
			current_state = rx_set_low_latency;
			ttl = 50;
		}
		}
		break;

	case tx_get_low_power: {
		char const *str = "GSQ\r";
		if (bluetooth_puts(str, strlen(str)) != EOF) {
			current_state = rx_get_name;
			ttl = 50;
		}
		}
		break;

	case rx_get_low_power:
		ttl -= ms;

		while ((c = bluetooth_getchar()) != EOF) {
			(void)buf_enque(&gp_rx_tx, c);
			ttl = 20;
		}

		if (ttl >= 0) {
			if (m_strcmp("0050\r\n", &gp_rx_tx) == m_strcmp_match) {
				current_state = tx_exit_cmd_mode;
				buf_clear(&gp_rx_tx);
			} else if ((m_strcmp("?\r\n", &gp_rx_tx) == m_strcmp_match)
					|| (m_strcmp("ERR\r\n", &gp_rx_tx) == m_strcmp_match)) {
				current_state = reset_bluetooth;
			}
		} else {	/* no response or invalid name */
			current_state = tx_set_low_power;
			buf_clear(&gp_rx_tx);
		}
		break;

	case tx_set_low_power: {
		char const *str = "SW,0050\r\n";
		if (bluetooth_puts(str, strlen(str)) != EOF) {
			current_state = rx_set_low_power;
			ttl = 50;
		}
		}
		break;

	case rx_set_name:
	case rx_set_low_latency:
	case rx_set_low_power:
		ttl -= ms;

		while ((c = bluetooth_getchar()) != EOF) {
			(void)buf_enque(&gp_rx_tx, c);
			ttl = 20;
		}

		if (ttl >= 0) {
			if (m_strcmp("AOK\r\n", &gp_rx_tx) == m_strcmp_match) {
				switch (current_state) {
				case rx_set_name:
					current_state = tx_set_low_latency;
					reset_bt = true;
					break;

				case rx_set_low_latency:
					current_state = tx_exit_cmd_mode;
					reset_bt = true;
					break;

				case rx_set_low_power:
					current_state = tx_exit_cmd_mode;
					break;
				}
				run_again = false;
				buf_clear(&gp_rx_tx);
			} else if ((m_strcmp("?\r\n", &gp_rx_tx) == m_strcmp_match)
					|| (m_strcmp("ERR\r\n", &gp_rx_tx) == m_strcmp_match)) {
				current_state = reset_bluetooth;
			}
		} else {	/* invalid/no response */
			current_state = reset_bluetooth;
		}
		break;

	case tx_exit_cmd_mode: {
		char const *str = "---\r\n";
		if (bluetooth_puts(str, strlen(str)) != EOF) {
			current_state = rx_exit_cmd_mode;
			ttl = 50;
		}
		}
		break;

	case rx_exit_cmd_mode:
		ttl -= ms;

		while ((c = bluetooth_getchar()) != EOF) {
			(void)buf_enque(&gp_rx_tx, c);
			ttl = 20;
		}

		if (ttl >= 0) {
			if (m_strcmp("END\r\n", &gp_rx_tx) == m_strcmp_match) {
				if (!reset_bt) {
					current_state = tx_set_low_latency;
				} else {
					current_state = reset_bluetooth;
				}
				run_again = false;
				buf_clear(&gp_rx_tx);
			} else if ((m_strcmp("?\r\n", &gp_rx_tx) == m_strcmp_match)
					|| (m_strcmp("ERR\r\n", &gp_rx_tx) == m_strcmp_match)) {
				current_state = reset_bluetooth;
			}
		} else {	/* invalid/no response */
			current_state = reset_bluetooth;
		}
		break;

	case reset_bluetooth:
		if (issue_bluetooth_reset(ms) == false) {
			current_state = default_state;
			reset_bt = false;
		}
		buf_clear(&gp_rx_tx);
		break;

	default:	/* shouldn't get here */
		current_state = default_state;
		reset_bt = false;
		buf_clear(&gp_rx_tx);
		break;
	}

	return run_again;
}

bool blumote_main(int ms)
{
	enum state {
		default_state = 0,
		rx_cmd1 = 0,
		rx_cmd2,
		process_cmd,
		handle_buf_overflow
	};
	static enum state current_state = default_state;
	static int ttl;
	int c;
	bool run_again = false;

	switch (current_state) {
	case rx_cmd1:
		/* get the first char */
		if ((c = bluetooth_getchar()) != EOF) {
			current_state = rx_cmd2;
			if (!own_gp_buf(gp_buf_owner_bt)) {
				/* couldn't get the gp_buf lock, put the byte back */
				(void)buf_undeque(&uart_rx, c);
			} else {
				(void)buf_enque(&gp_rx_tx, c);
			}
			ttl = 20;
			run_again = true;
		}
		break;

	case rx_cmd2:
		ttl -= ms;
		while ((c = bluetooth_getchar()) != EOF) {
			(void)buf_enque(&gp_rx_tx, c);
			ttl = 20;
		}

		if (ttl < 0) {
			/* should have the whole message by now */
			current_state = process_cmd;
		}

		run_again = true;
		break;

	case process_cmd:
		run_again = true;
		if (!blumote_process_cmd()) {
			break;
		}	/* else done, fallthrough */
	default:
		current_state = default_state;
		if (!tx_ir_code) {
			/* preserve the ir code for ir_main */
			buf_clear(&gp_rx_tx);
		}
		break;
	}

	return run_again;
}

bool tx_learned_code()
{
	enum state {
		default_state = 0,
		tx_status = 0,
		tx_code,
		wait_for_bt_buf
	};
	static enum state current_state = default_state;
	bool run_again = true;
	static uint8_t c; 

	switch (current_state) {
	case tx_status:
		if (!buf_deque(&gp_rx_tx, &c)) {
			char str[4] = {BLUMOTE_ACK, 0};
			str[2] = gp_rx_tx.cnt;
			str[3] = c;
			bluetooth_puts(str, sizeof(str));
			current_state = tx_code;
		} else {
			bluetooth_putchar(BLUMOTE_NAK);
			run_again = false;
		}
		break;

	case tx_code:
		while (!buf_deque(&gp_rx_tx, &c)) {
			if (bluetooth_putchar((int)c) == EOF) {
				current_state = wait_for_bt_buf;
				break;
			}
		}
		if (current_state == tx_code) {
			current_state = default_state;
			run_again = false;
		}
		break;

	case wait_for_bt_buf:
		if (bluetooth_putchar((int)c) != EOF) {
			current_state = tx_code;
		}
		break;

	default:
		current_state = default_state;
		run_again = false;
		break;
	}

	return run_again; 
}
