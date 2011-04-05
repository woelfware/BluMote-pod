#ifndef BLUMOTE_H_
#define BLUMOTE_H_

#include <stdbool.h>

enum command_codes {
	BLUMOTE_DISCONNECT,
	BLUMOTE_RENAME_DEVICE,
	BLUMOTE_LEARN,
	BLUMOTE_GET_VERSION,
	BLUMOTE_IR_TRANSMIT,
	BLUMOTE_DEBUG = 0xFF,	/* specialized debug command whose functionality may change any time */
	BLUMOTE_NONE = -1
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

bool init_blumote();

bool blumote_main();

#endif /*BLUMOTE_H_*/
