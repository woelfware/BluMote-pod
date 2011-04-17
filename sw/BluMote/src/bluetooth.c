#include "bluetooth.h"
#include "hw.h"
#include <stdint.h>

int bluetooth_getchar()
{
	int c;

	if (buf_deque(&uart_rx, (uint8_t *)&c)) {
		c = EOF;
	}

	return c;
}

int bluetooth_putchar(int character)
{
	int rc = character;

	if (buf_enque(&uart_tx, (uint8_t)character)) {
		rc = EOF;
	}

	return rc;
}

int bluetooth_puts(char const *str, int nbr_chars)
{
	while (nbr_chars-- > 0) {
		if (bluetooth_putchar(*str++) == EOF) {
			return EOF;
		}
	}

	return 1;
}

bool issue_bluetooth_reset(int ms)
{
	enum state {
		default_state = 0,
		issue_reset = 0,
		remove_reset,
		wait_for_power_up
	};
	static enum state current_state = default_state;
	static int ttl;
	bool run_again = true;

	switch (current_state) {
	case issue_reset:
		P3OUT |= BIT0;
		ttl = 10;
		current_state = remove_reset;
		break;

	case remove_reset:
		ttl -= ms;
		if (ttl < 0) {
			P3OUT &= ~BIT0;
			ttl = 10;
			current_state = wait_for_power_up;
		}
		break;

	case wait_for_power_up:
		ttl -= ms;
		if (ttl < 0) {
			current_state = default_state;
			run_again = false;
		}
		break;

	default:
		current_state = default_state;
		run_again = false;
		break;
	}

	return run_again;
}

bool bluetooth_main(int ms)
{
	bool run_again = true;

	if (IFG2 & UCA0TXIFG) {
		uint8_t c;
		if (buf_deque(&uart_tx, &c)) {
			run_again = false;
		} else {
			UCA0TXBUF = c;
		}
	}

	return run_again;
}
