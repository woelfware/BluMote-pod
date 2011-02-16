/*
 * Copyright (c) 2011 Woelfware
 */

#include "config.h"
#include "bluemote.h"
#include <stdlib.h>
#include <glib.h>
#include <glib/gprintf.h>

int main(int argc, char *argv[])
{
	struct bluemote_server server;
	sdp_session_t *session;

	bm_server_init(&server);
	bm_allocate_socket(&server);
	if (bm_bind_socket(&server)) {
		g_printerr("Failed to bind to a socket.\n");
		exit(EXIT_FAILURE);
	}
	session = bm_register_service(server.loc_addr.rc_channel);
	bm_listen(&server);
	g_print("accepted connection from %s\n", server.buf);
	memset(server.buf, 0, sizeof(server.buf));
	do {
		bm_read_data(&server);
		if (server.bytes_read > 0) {
			g_print("received [%s]\n", server.buf);
		}
	} while (server.bytes_read);
	bm_close(&server);

	return EXIT_SUCCESS;
}

