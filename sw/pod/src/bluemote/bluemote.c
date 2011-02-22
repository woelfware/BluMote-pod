/*
 * Copyright (c) 2011 Woelfware
 */

#include "bluemote.h"
#include "config.h"
#include <assert.h>
#include <stdlib.h>

static void set_version(struct version *version)
{
	assert(version != NULL);

	version->major = VERSION_MAJOR;
	version->minor = VERSION_MINOR;
	version->revision = VERSION_REVISION;
}

struct bserver *bserver_create()
{
	struct bserver *server = calloc(1, sizeof(struct bserver));

	if (server) {
		set_version(&server->version);
		server->bluetooth = bbluetooth_create();
		if (!server->bluetooth) {
			free(server);
			server = NULL;
		}
	}

	return server;
}

bool blisten(struct bserver *server)
{
	assert(server);

	return bbluetooth_listen(server->bluetooth);
}

void bserver_destroy(struct bserver *server)
{
	assert(server);

	bbluetooth_destroy(server->bluetooth);
	free(server);
}

int bread(struct bserver *server, uint8_t **rdbuf)
{
	assert(server != NULL);

	return bbluetooth_read(server->bluetooth, rdbuf);
}

ssize_t bwrite(struct bserver *server, uint8_t *wrbuf, size_t len)
{
	assert(server != NULL);

	return bbluetooth_write(server->bluetooth, wrbuf, len);
}

void bclose(struct bserver *server)
{
	assert(server != NULL);

	bbluetooth_close(server->bluetooth);
}

enum COMMAND_CODES bget_command(struct bserver *server, uint8_t *buf, size_t len)
{
	uint8_t cmd_code;

	assert(buf);

	if (len < 0) {
		return B_DISCONNECT;
	}

	if (len == 0) {
		uint8_t msg[] = {B_NAK};
		(void)bbluetooth_write(server->bluetooth, msg, sizeof(msg));
		return B_NONE;
	}

	cmd_code = buf[0];
	switch (cmd_code) {
	case B_RENAME_DEVICE :
	case B_LEARN :
	case B_GET_VERSION :
	case B_IR_TRANSMIT :
	case B_DISCONNECT :
	case B_DEBUG :
		return cmd_code;

	default :
	{
		uint8_t msg[] = {B_NAK};
		(void)bbluetooth_write(server->bluetooth, msg, sizeof(msg));
		return B_NONE;
	}
	}
}

void bget_version(struct bserver *server)
{
	uint8_t msg[] = {B_ACK,
		B_FIRMWARE,
		server->version.major,
		server->version.minor,
		server->version.revision};

	assert(server != NULL);

	bbluetooth_write(server->bluetooth, msg, sizeof(msg));
}

void brename(struct bserver *server,
		uint8_t *name,
		uint8_t *description,
		uint8_t *provider)
{
	assert(server != NULL);

	bbluetooth_set_description(server->bluetooth, name, description, provider);
}

void blearn(struct bserver *server)
{
	assert(server);
#if 0
	enum STATE {
		GET_PKT_1,
		GET_PKT_1_TAILER,
		GET_PKT_2
	} state = GET_PKT_1;
	gboolean scan_codes = TRUE;

	do {
		struct ir_input ir;

		bir_read(&ir)

		switch (state) {
		case GET_PKT_1 :
			if (ir.type == IR_SPACE
					&& ir.value > 20000) {
				state = GET_PKT_1_TAILER;
			}
			break;

		case GET_PKT_1_TAILER :
			if (ir.type == IR_SPACE
					&& ir.value > 20000) {
				state = GET_PKT_2
			}
			break;

		case GET_PKT_2 :
			if (ir.type == IR_SPACE
					&& ir.value > 20000) {
				scan_codes = FALSE;
			} else {
				uint16_t value = htons(ir.value);
				memcpy(&server->out_buf[struct->bytes_written], &value, sizeof(value));
				struct->bytes_written += sizeof(value);
			}
			break;
		}
	} while (scan_codes);
#endif
}

