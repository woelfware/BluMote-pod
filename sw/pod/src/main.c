/*
 * Copyright (c) 2011 Woelfware
 */

#include "config.h"
#include "bluemote.h"
#include <stdlib.h>
#include <glib.h>
#include <glib/gprintf.h>
#include <errno.h>

guint8 hex[] = "0123456789ABCDEF";

int main(int argc, char *argv[])
{
	struct bluemote_server server;
	sdp_session_t *session;
	extern int errno;

	bm_server_init(&server);
	while (1) {
		bm_allocate_socket(&server);
		if (bm_bind_socket(&server)) {
			g_printerr("Failed to bind to a socket.\n");
			exit(EXIT_FAILURE);
		}
		session = bm_register_service(server.loc_addr.rc_channel);
		g_print("listening for client connection\n");
		bm_listen(&server);
		if (server.client < 0) {
			g_printerr("Failure in accepting client connection. (errno: %i)\n", errno);
			exit(EXIT_FAILURE);
		}
		g_print("accepted connection from %s\n", server.buf);
		memset(server.buf, 0, sizeof(server.buf));

		while (1) {
			bm_read_data(&server);
			if (server.bytes_read < 0) {
				break;
			} else {
				gint i = 0;

				g_print("received [0x");
				while (server.bytes_read) {
					g_print("%c%c",
						hex[(server.buf[i] >> 4) & 0x0F],
						hex[server.buf[i] & 0x0F]);
					server.bytes_read--;
					i++;
				}
				g_print("]\n");
			}
		}

		g_print("disconnect\n");
		bm_close(&server);
	}

	return EXIT_SUCCESS;
}

