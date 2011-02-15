/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef BLUEMOTE_H
#define BLUEMOTE_H

#include <glib.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/rfcomm.h>

enum COMMAND_CODES {
	BM_INIT,
	BM_RENAME_DEVICE,
	BM_LEARN,
	BM_GET_VERSION,
	BM_IR_TRANSMIT,
	BM_DEBUG = 0x7F
};

enum COMMAND_RETURN_CODES {
	BM_NOT_INITTED	= 0x00,
	BM_ACK		= 0x06,
	BM_NACK		= 0x15
};

enum COMPONENT_CODES {
	BM_HARDWARE,
	BM_FIRMWARE,
	BM_SOFTWARE
};

struct version {
	guint8	major,
		minor,
		revision;
};

struct bluemote_server {
	struct version version;
	struct sockaddr_rc loc_addr,
			   rem_addr;
	socklen_t opt;
	gint s,
	     client,
	     bytes_read;
	guint8 pkt_cnt,
	       buf[1024];
};

void bm_server_init(struct bluemote_server *server);
void bm_allocate_socket(struct bluemote_server *server);
void bm_bind_socket(struct bluemote_server *server);
void bm_listen(struct bluemote_server *server);
void bm_read_data(struct bluemote_server *server);
void bm_close(struct bluemote_server *server);

#endif

