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

bool bluetooth_main()
{
	enum state {
		default_state = 0,
		pop_ch = 0,
		tx_ch
	};
	static enum state current_state = default_state;
	static uint8_t c;
	bool run_again = false;

	switch (current_state) {
	case pop_ch:
		if (!buf_deque(&uart_tx, (uint8_t *)&c)) {
			current_state = tx_ch;
			run_again = true;
		} else {
			current_state = default_state;
		}
		break;

	case tx_ch:
		if (IFG2 & UCA0TXIFG) {
			UCA0TXBUF = c;
			current_state = default_state;
		}
		run_again = true;
		break;

	default:	/* shouldn't get here */
		current_state = default_state;
		break;
	}

	return run_again;
}
