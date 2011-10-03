/*
 * Copyright (c) 2011 Woelfware
 */

#include <msp430.h>
#include <stdbool.h>
#include <stdint.h>
#include <string.h>
#include "bluetooth.h"
#include "blumote.h"
#include "buffer.h"
#include "hw.h"

/* initialize the bluetooth hardware, etc. */
static void init()
{
	init_hw();
	init_rn42();
}

int main()
{
	init();

	uber_buf.rd_ptr = uber_buf.wr_ptr = 0;
	set_bluetooth_rx_buf(&uber_buf);

	do {
		_BIS_SR(LPM4_bits + GIE);
		blumote_main();
	} while (1);
}
