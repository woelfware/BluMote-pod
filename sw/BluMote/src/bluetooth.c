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
