/*
 * Copyright (c) 2011 Woelfware
 */

#include "bluemote.h"
#include <assert.h>
#include <stdint.h>

static void set_version(struct version *version)
{
	assert(version != NULL);

	version->major = VERSION_MAJOR;
	version->minor = VERSION_MINOR;
	version->revision = VERSION_REVISION;
}

void bpod_init(struct bserver *server)
{
	assert(server != NULL);

	memset(server, 0, sizeof(struct bserver));

	set_version(&server->version);
	server->bluetooth.opt = (socklen_t)sizeof(server->bluetooth.rem_addr);
}

void bread(struct bserver *server)
{
	assert(server != NULL);

	bm_bluetooth_read_data(server);
}

ssize_t bwrite(struct bserver *server)
{
	assert(server != NULL);

	return bm_bluetooth_write(server->client, server->out_buf, server->bytes_written);
}

void bclose(struct bserver *server)
{
	assert(server != NULL);

	bm_bluethooth_close(server);
}

enum COMMAND_CODES bget_command(struct bserver *server)
{
	uint8_t cmd_code;

	assert(server != NULL);

	if (server->bytes_read < 0) {
		return BM_DISCONNECT;
	} else if (server->bytes_read == 0) {
		return BM_NONE;
	}

	cmd_code = server->buf[0];
	switch (cmd_code) {
	case BM_RENAME_DEVICE :
	case BM_LEARN :
	case BM_GET_VERSION :
	case BM_IR_TRANSMIT :
	case BM_DISCONNECT :
	case BM_DEBUG :
		return cmd_code;

	default :
	{
		server->out_buf[0] = (uint8_t)BM_NAK;
		server->bytes_written = 1;
		bm_write_data(server);
		return BM_NONE;
	}
	}
}

void bget_version(struct bserver *server)
{
	assert(server != NULL);

	server->bytes_written = 0;

	server->bluetooth.out_buf[server->bytes_written] = (uint8_t)BM_ACK;
	server->bluetooth.bytes_written++;
	server->bluetooth.out_buf[server->bytes_written] = (uint8_t)BM_FIRMWARE;
	server->bluetooth.bytes_written++;
	memcpy(&server->bluetooth.out_buf[server->bluetooth.bytes_written],
			&server->version,
			sizeof(server->version));
	server->bluetooth.bytes_written += sizeof(server->version);
	bm_bluetooth_write(server);
}

void brename(struct bserver *server)
{
	assert(server != NULL);

	strlcpy(server->sdp.name, &server->buf[1], sizeof(server->sdp.name));
}

void blearn(struct bserver *server)
{
	enum STATE {
		GET_PKT_1,
		GET_PKT_1_TAILER,
		GET_PKT_2
	} state = GET_PKT_1;
	gboolean scan_codes = TRUE;

	struct->bytes_written = 1;

	do {
		struct ir_input ir;

		bm_ir_read(&ir)

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
}

