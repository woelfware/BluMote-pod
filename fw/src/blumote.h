/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef BLUMOTE_H
#define BLUMOTE_H

#include <stdbool.h>
#include <stdint.h>

enum command_codes {
	BLUMOTE_GET_VERSION,
	BLUMOTE_LEARN,
	BLUMOTE_IR_TRANSMIT,
	BLUMOTE_IR_TRANSMIT_ABORT,
	BLUMOTE_RESET_BLUETOOTH,
	BLUMOTE_GET_CALIBRATION,
	BLUMOTE_DEBUG = 0xFF	/* specialized debug command whose functionality may change any time */
};

enum command_return_codes {
	BLUMOTE_ACK = 0x06,
	BLUMOTE_NAK = 0x15
};

enum component_codes {
	BLUMOTE_HW,
	BLUMOTE_FW,
	BLUMOTE_SW
};

/*
 */
void blumote_main();

/* Initialize the RN-42 hardware with the BluMote configuration.
 * Return Code: true - initialization was successful
 *              false - initialization failed
 */
void init_rn42();

#endif /*BLUMOTE_H*/
