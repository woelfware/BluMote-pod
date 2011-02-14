/*
 * Copyright (c) 2011 Woelfware
 */

#include "bluemote.h"
#include "config.h"
#include <stdio.h>
#include <glib.h>
#include <sys/socket.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/rfcomm.h>

static gchar pod_name[] = "Bluemote",
	     provider[] = "Woelfware",
	     description[] = "IR XPDR";

static void set_version(struct version *version)
{
	version->major = VERSION_MAJOR;
	version->minor = VERSION_MINOR;
	version->revision = VERSION_REVISION;
}

void create_bluemote_server(struct bluemote_server *server)
{
	g_assert(server != NULL);

	set_version(&server->version);
	memset(&server->loc_addr, 0, sizeof(server->loc_addr));
	memset(&server->rem_addr, 0, sizeof(server->rem_addr));
	server->opt = (socklen_t)sizeof(server->rem_addr);
	server->s = 0;
	server->client = 0;
	server->bytes_read = 0;
	server->pkt_cnt = 0;
	memset(&server->buf, 0, sizeof(server->buf));
}

void bm_allocate_socket(struct bluemote_server *server)
{
	g_assert(server != NULL);

	server->s = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
}

void bm_bind_socket(struct bluemote_server *server)
{
    /*
     * bind socket to port 1 of the first available local bluetooth adapter
     */
    server->loc_addr.rc_family	= AF_BLUETOOTH;
    server->loc_addr.rc_bdaddr	= *BDADDR_ANY;
    server->loc_addr.rc_channel	= (guint8)1;
    bind(server->s, (struct sockaddr *)&server->loc_addr, sizeof(server->loc_addr));
}

void bm_listen(struct bluemote_server *server)
{
	listen(server->s, 1);
	server->client = accept(server->s, (struct sockaddr *)&server->rem_addr, &server->opt);

	ba2str(&server->rem_addr.rc_bdaddr, server->buf);
	fprintf(stderr, "accepted connection from %s\n", server->buf);
	memset(server->buf, 0, sizeof(server->buf));
}

void bm_read_data(struct bluemote_server *server)
{
	server->bytes_read = read(server->client, server->buf, sizeof(server->buf));
	if (server->bytes_read > 0) {
		printf("received [%s]\n", server->buf);
	}
}

void bm_close(struct bluemote_server *server)
{
	close(server->client);
	close(server->s);
}

