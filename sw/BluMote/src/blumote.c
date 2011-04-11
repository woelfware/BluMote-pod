/*
 * Copyright (c) 2011 Woelfware
 */

#include "bluetooth.h"
#include "blumote.h"
#include <string.h>

static enum command_codes cmd_code;

static char buf[256];
static int nbr_bytes;

static enum command_codes blumote_get_cmd()
{
	return BLUMOTE_NONE;
}

static void blumote_process_cmd()
{
	switch (cmd_code) {
	case BLUMOTE_DISCONNECT:
		break;

	case BLUMOTE_RENAME_DEVICE:
		break;

	case BLUMOTE_LEARN:
		break;

	case BLUMOTE_GET_VERSION:
		break;

	case BLUMOTE_IR_TRANSMIT:
		break;

	case BLUMOTE_DEBUG:
		break;
	}
}

bool init_blumote(int ms)
{
	enum state {
		default_state = 0,
		wait_one_sec1 = 0,
		wait_one_sec2,
		request_cmd_mode,
		receive_cmd_mode,
		request_basic_settings,
		receive_basic_settings,
		request_set_command,
		receive_set_command,
		request_exit_cmd_mode,
		receive_exit_cmd_mode,
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
			current_state = request_cmd_mode;
		} 
		break;

	case request_cmd_mode: {
		char const *str = "$$$";
		if (bluetooth_puts(str, strlen(str)) != EOF) {
			current_state = receive_cmd_mode;
			ttl = 1000;
		}
		}
		break;

	case receive_cmd_mode:
		ttl -= ms;

		while ((c = bluetooth_getchar()) != EOF) {
			buf[nbr_bytes++] = c;
			nbr_bytes %= sizeof(buf);
			ttl = 1000;
		}
		
		if (ttl >= 0) {
			char const *str = "CMD\r\n";
			if (memcmp(buf, str, strlen(str)) == 0) {
				current_state = request_basic_settings;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
			}
		} else {	/* no response; already in CMD mode? */
			current_state = request_basic_settings;
			memset(buf, 0, nbr_bytes);
			nbr_bytes = 0;
		}
		break;

	case request_basic_settings:
		if (bluetooth_puts("D\r", 2) != EOF) {
			current_state = receive_basic_settings;
			ttl = 1000;
		}
		break;

	case receive_basic_settings:
		ttl -= ms;

		while ((c = bluetooth_getchar()) != EOF) {
			buf[nbr_bytes++] = c;
			nbr_bytes %= sizeof(buf);
			ttl = 1000;
		}

		if (ttl >= 0) {
			char const *str[] = {
				"ERR\r\n",
				"?\r\n"
			};
			if (memcmp(buf, str[0], strlen(str[0])) == 0
					|| memcmp(buf, str[1], strlen(str[1])) == 0) {
				/* the rn-42 is in a weird state so reset it */
				current_state = reset_bluetooth;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
			}
		} else {	/* got all the data */
			if (nbr_bytes) {
				current_state = request_exit_cmd_mode;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
			} else {	/* didn't get any data */
				current_state = reset_bluetooth;
			}
		}
		break;

	case request_set_command: {
		char const *str = "S-,BluMote\r\n";
		if (bluetooth_puts(str, strlen(str)) != EOF) {
			current_state = receive_set_command;
			ttl = 1000;
		}
		}
		break;

	case receive_set_command:
		ttl -= ms;
		
		while ((c = bluetooth_getchar()) != EOF) {
			buf[nbr_bytes++] = c;
			nbr_bytes %= sizeof(buf);
			ttl = 1000;
		}

		if (ttl >= 0) {
			char const *str[] = {
				"AOK\r\n",
				"ERR\r\n",
				"?\r\n"
			};
			if (memcmp(buf, str[0], strlen(str[0])) == 0) {
				current_state = request_exit_cmd_mode;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
				run_again = false;
			} else if (memcmp(buf, str[1], strlen(str[1])) == 0
					|| memcmp(buf, str[2], strlen(str[2])) == 0) {
				current_state = request_set_command;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
			}
		} else {	/* no response... there's a comm failure */
			current_state = request_exit_cmd_mode;
			memset(buf, 0, nbr_bytes);
			nbr_bytes = 0;
		}
		break;

	case request_exit_cmd_mode: {
		char const *str = "---\r\n";
		if (bluetooth_puts(str, strlen(str)) != EOF) {
			current_state = receive_exit_cmd_mode;
			ttl = 1000;
		}
		}
		break;

	case receive_exit_cmd_mode:
		ttl -= ms;

		while ((c = bluetooth_getchar()) != EOF) {
			buf[nbr_bytes++] = c;
			nbr_bytes %= sizeof(buf);
			ttl = 1000;
		}

		if (ttl >= 0) {
			char const *str = "END\r";
			if (memcmp(buf, str, strlen(str)) == 0) {
				current_state = default_state;
				memset(buf, 0, nbr_bytes);
				nbr_bytes = 0;
				run_again = false;
			}
		} else {	/* no response... there's a comm failure */
			current_state = default_state;
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
		get_cmd = 0,
		process_cmd
	};
	static enum state current_state = default_state;
	bool run_again = false;

	switch (current_state) {
	case get_cmd: {
		if ((cmd_code = blumote_get_cmd()) == BLUMOTE_NONE) {
		} else {
			current_state = process_cmd;
			run_again = true;
		}
		}
		break;

	case process_cmd:
		blumote_process_cmd();
		current_state = default_state;
		break;

	default:	/* shouldn't get here */
		current_state = default_state;
		break;
	}

	return run_again;
}
