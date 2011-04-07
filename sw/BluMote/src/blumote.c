/*
 * Copyright (c) 2011 Woelfware
 */

#include "bluetooth.h"
#include "blumote.h"
#include <string.h>

static enum command_codes cmd_code;

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
		request_cmd_mode = 0,
		receive_cmd_mode,
		request_exit_cmd_mode,
		receive_exit_cmd_mode,
		request_basic_settings,
		receive_basic_settings
	};
	static enum state current_state = default_state;
	bool run_again = true;
	static char buf[32] = {0};
	static int i = 0;
	int c;

	switch (current_state) {
	case request_cmd_mode:
		if (bluetooth_putchar('$') != EOF
			&& bluetooth_putchar('$') != EOF
			&& bluetooth_putchar('$') != EOF) {
			current_state = receive_cmd_mode;
		}
		break;

	case receive_cmd_mode:
		if ((c = bluetooth_getchar()) != EOF) {
			buf[i++] = c;
		}
		if (memcmp(buf, "CMD", 3) == 0) {
			i = 0;
			current_state = request_exit_cmd_mode;
		}
		break;

	case request_exit_cmd_mode:
		if (bluetooth_putchar('-') != EOF
			&& bluetooth_putchar('-') != EOF
			&& bluetooth_putchar('-') != EOF
			&& bluetooth_putchar('\r') != EOF) {
			current_state = receive_exit_cmd_mode;
		}
		break;

	case receive_exit_cmd_mode:
		if ((c = bluetooth_getchar()) != EOF) {
			buf[i++] = c;
		}
		if (memcmp(buf, "END", 3) == 0) {
			i = 0;
			current_state = default_state;
			run_again = false;
		}
		break;

	case request_basic_settings:
		if (bluetooth_putchar('D') != EOF) {
			current_state = receive_basic_settings;
		}
		break;

	case receive_basic_settings:
		if ((c = bluetooth_getchar()) != EOF) {
			buf[i++] = c;
		}
		break;

	default:	/* shouldn't get here */
		current_state = default_state;
		break;
	}

	return run_again;
}

bool blumote_main()
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
