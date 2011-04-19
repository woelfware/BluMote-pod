/*
 * Copyright (c) 2011 Woelfware
 */

#include "bluetooth.h"
#include "blumote.h"
#include "config.h"
#include "hw.h"
#include <string.h>

bool learn_ir_code = false;

static char buf[BLUMOTE_RX_BUF_SIZE];
static int nbr_bytes;

static void blumote_process_cmd()
{
	switch (buf[0]) {
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
		break;
	
	default: {
		char const response[] = {BLUMOTE_NAK};
		(void)bluetooth_puts(response, sizeof(response));
		}
		break;
	}
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
		tx_set_low_latency,
		rx_set_low_latency,
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

	switch (current_state) {
	case wait_one_sec1:
		ttl = 1000;
		current_state = wait_one_sec2;
		
		/* clear out the rx buffers */
		while (bluetooth_getchar() != EOF);
		memset(buf, 0, sizeof(buf));
		nbr_bytes = 0;
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
			buf[nbr_bytes++] = c;
			nbr_bytes &= sizeof(buf) - 1;
			ttl = 20;
		}
		
		if (ttl >= 0) {
			char const *str = "CMD\r\n";
			if (memcmp(buf, str, strlen(str)) == 0) {
				current_state = tx_get_name;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
			}
		} else {	/* no response; already in CMD mode? */
			current_state = tx_get_name;
			memset(buf, 0, nbr_bytes);
			nbr_bytes = 0;
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
			buf[nbr_bytes++] = c;
			nbr_bytes &= sizeof(buf) - 1;
			ttl = 20;
		}

		if (ttl >= 0) {
			if (buf[nbr_bytes - 1] == '\n') {
				char const *str[] = {
					"BluMote",
					"ERR\r\n",
					"?\r\n"
				};
				if (memcmp(buf, str[0], strlen(str[0])) == 0) {
					current_state = tx_exit_cmd_mode;
					memset(buf, 0, nbr_bytes);
					nbr_bytes = 0;
				} else if (memcmp(buf, str[1], strlen(str[1])) == 0
						|| memcmp(buf, str[2], strlen(str[2])) == 0) {
					current_state = reset_bluetooth;
					memset(buf, 0, nbr_bytes);
					nbr_bytes = 0;
				} else {
					current_state = tx_set_name;
					memset(buf, 0, nbr_bytes);
					nbr_bytes = 0;
				}
			}
		} else {	/* no response; already in CMD mode? */
			current_state = tx_set_name;
			memset(buf, 0, nbr_bytes);
			nbr_bytes = 0;
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

	case rx_set_name:
		ttl -= ms;
		
		while ((c = bluetooth_getchar()) != EOF) {
			buf[nbr_bytes++] = c;
			nbr_bytes &= sizeof(buf) - 1;
			ttl = 20;
		}

		if (ttl >= 0) {
			char const *str[] = {
				"AOK\r\n",
				"ERR\r\n",
				"?\r\n"
			};
			if (memcmp(buf, str[0], strlen(str[0])) == 0) {
				current_state = tx_set_low_latency;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
				run_again = false;
			} else if (memcmp(buf, str[1], strlen(str[1])) == 0
					|| memcmp(buf, str[2], strlen(str[2])) == 0) {
				current_state = reset_bluetooth;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
			}
		} else {	/* no response... there's a comm failure */
			current_state = reset_bluetooth;
			memset(buf, 0, nbr_bytes);
			nbr_bytes = 0;
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

	case rx_set_low_latency:
		ttl -= ms;
		
		while ((c = bluetooth_getchar()) != EOF) {
			buf[nbr_bytes++] = c;
			nbr_bytes &= sizeof(buf) - 1;
			ttl = 20;
		}

		if (ttl >= 0) {
			char const *str[] = {
				"AOK\r\n",
				"ERR\r\n",
				"?\r\n"
			};
			if (memcmp(buf, str[0], strlen(str[0])) == 0) {
				current_state = reset_bluetooth;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
				run_again = false;
			} else if (memcmp(buf, str[1], strlen(str[1])) == 0
					|| memcmp(buf, str[2], strlen(str[2])) == 0) {
				current_state = tx_set_low_latency;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
			}
		} else {	/* no response... there's a comm failure */
			current_state = reset_bluetooth;
			memset(buf, 0, nbr_bytes);
			nbr_bytes = 0;
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

	case rx_set_low_power:
		ttl -= ms;

		while ((c = bluetooth_getchar()) != EOF) {
			buf[nbr_bytes++] = c;
			nbr_bytes &= sizeof(buf) - 1;
			ttl = 20;
		}

		if (ttl >= 0) {
			char const *str[] = {
				"AOK\r\n",
				"ERR\r\n",
				"?\r\n"
			};
			if (memcmp(buf, str[0], strlen(str[0])) == 0) {
				current_state = reset_bluetooth;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
				run_again = false;
			} else if (memcmp(buf, str[1], strlen(str[1])) == 0
					|| memcmp(buf, str[2], strlen(str[2])) == 0) {
				current_state = tx_set_low_latency;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
			}
		} else {	/* no response... there's a comm failure */
			current_state = reset_bluetooth;
			memset(buf, 0, nbr_bytes);
			nbr_bytes = 0;
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
			buf[nbr_bytes++] = c;
			nbr_bytes &= sizeof(buf) - 1;
			ttl = 20;
		}

		if (ttl >= 0) {
			char const *str = "END\r\n";
			if (memcmp(buf, str, strlen(str)) == 0) {
				current_state = default_state;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
				run_again = false;
			}
		} else {	/* no response... there's a comm failure */
			current_state = reset_bluetooth;
			memset(buf, 0, nbr_bytes);
			nbr_bytes = 0;
		}
		break;

	case reset_bluetooth:
		if (issue_bluetooth_reset(ms) == false) {
			current_state = default_state;
		}
		break;

	default:	/* shouldn't get here */
		current_state = default_state;
		memset(buf, 0, nbr_bytes);
		nbr_bytes = 0;
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
		process_cmd
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
			buf[nbr_bytes++] = c;
			ttl = 20;
		}
		break;

	case rx_cmd2:
		ttl -= ms;
		while ((c = bluetooth_getchar()) != EOF) {
			buf[nbr_bytes++] = c;
			nbr_bytes &= sizeof(buf) - 1;
			ttl = 20;
		}

		if (ttl < 0) {
			/* should have the whole message by now */
			current_state = process_cmd;
		}
		break;

	case process_cmd:
		blumote_process_cmd();	/* done, fallthrough */
	default:	/* shouldn't get here */
		current_state = default_state;
		memset(buf, 0, nbr_bytes);
		nbr_bytes = 0;
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
		if (!buf_deque(&ir_rx, &c)) {
			char str[4] = {BLUMOTE_ACK, 0};
			str[2] = ir_rx.cnt;
			str[3] = c;
			bluetooth_puts(str, sizeof(str));
			current_state = tx_code;
		} else {
			bluetooth_putchar(BLUMOTE_NAK);
			run_again = false;
		}
		break;

	case tx_code:
		while (!buf_deque(&ir_rx, &c)) {
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

