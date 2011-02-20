/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef BLUEMOTE_H
#define BLUEMOTE_H

#include <glib.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/rfcomm.h>
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>

enum COMMAND_CODES {
	BM_DISCONNECT,
	BM_RENAME_DEVICE,
	BM_LEARN,
	BM_GET_VERSION,
	BM_IR_TRANSMIT,
	BM_DEBUG = 0x7F,
	BM_NONE = -1
};

enum COMMAND_RETURN_CODES {
	BM_NOT_INITTED	= 0x00,
	BM_ACK		= 0x06,
	BM_NAK		= 0x15
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

struct sdp_description {
	gchar name[16],
	      dsc[16],
	      prov[16];
};

struct bluemote_server {
	struct version version;
	struct sockaddr_rc loc_addr,
			   rem_addr;
	struct sdp_description sdp; 
	socklen_t opt;
	gint s,
	     client,
	     bytes_read,
	     bytes_written;
	guint8 buf[1024],
	       out_buf[1024];
};

void bm_server_init(struct bluemote_server *server);
void bm_allocate_socket(struct bluemote_server *server);
gint bm_bind_socket(struct bluemote_server *server);
void bm_listen(struct bluemote_server *server);
void bm_read_data(struct bluemote_server *server);
gssize bm_write_data(struct bluemote_server *server);
void bm_close(struct bluemote_server *server);
sdp_session_t *bm_register_service(struct bluemote_server *server);
enum COMMAND_CODES bm_get_command(struct bluemote_server *server);
void bm_rename(struct bluemote_server *server);

#endif

