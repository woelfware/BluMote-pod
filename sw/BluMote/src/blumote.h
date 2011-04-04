#ifndef BLUMOTE_H_
#define BLUMOTE_H_

enum command_codes {
	BLUMOTE_DISCONNECT,
	BLUMOTE_RENAME_DEVICE,
	BLUMOTE_LEARN,
	BLUMOTE_GET_VERSION,
	BLUMOTE_IR_TRANSMIT,
	BLUMOTE_DEBUG = 0xFF	/* specialized debug command whose functionality change whenever */
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

int blumote_main();

#endif /*BLUMOTE_H_*/
