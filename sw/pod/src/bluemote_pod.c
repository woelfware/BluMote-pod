/*
 * Copyright (c) 2011 Woelfware
 */

#include "bluemote.h"
#include "config.h"
#include <glib.h>
#include <sys/socket.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/rfcomm.h>
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>

static void set_version(struct version *version)
{
	g_assert(version != NULL);

	version->major = VERSION_MAJOR;
	version->minor = VERSION_MINOR;
	version->revision = VERSION_REVISION;
}

void bm_server_init(struct bluemote_server *server)
{
	g_assert(server != NULL);

	memset(server, 0, sizeof(struct bluemote_server));

	set_version(&server->version);
	server->opt = (socklen_t)sizeof(server->rem_addr);
}

void bm_allocate_socket(struct bluemote_server *server)
{
	g_assert(server != NULL);

	server->s = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
}

gint bm_bind_socket(struct bluemote_server *server)
{
	gint err;

	g_assert(server != NULL);

	/* bind socket to the first available port on the first available local
	 * bluetooth adapter
	 */
	server->loc_addr.rc_family	= AF_BLUETOOTH;
	server->loc_addr.rc_bdaddr	= *BDADDR_ANY;
	for (server->loc_addr.rc_channel = 1;
			server->loc_addr.rc_channel <= 31;
			server->loc_addr.rc_channel++) {
		err = bind(server->s,
				(struct sockaddr *)&server->loc_addr,
				sizeof(server->loc_addr));
		if (!err)
			break;
	}

	return err;
}

void bm_listen(struct bluemote_server *server)
{
	g_assert(server != NULL);

	listen(server->s, 1);
	server->client = accept(server->s, (struct sockaddr *)&server->rem_addr, &server->opt);

	ba2str(&server->rem_addr.rc_bdaddr, server->buf);
}

void bm_read_data(struct bluemote_server *server)
{
	g_assert(server != NULL);

	server->bytes_read = read(server->client, server->buf, sizeof(server->buf));
}

gssize bm_write_data(struct bluemote_server *server)
{
	g_assert(server != NULL);

	return write(server->client, server->out_buf, server->bytes_written);
}

void bm_close(struct bluemote_server *server)
{
	g_assert(server != NULL);

	close(server->client);
	close(server->s);
}

sdp_session_t *bm_register_service(guint8 rfcomm_channel)
{
	gchar const *service_name = SERVICE_NAME,
	      *service_dsc = SERVICE_DSC,
	      *service_prov = SERVICE_PROV;
	uuid_t root_uuid,
	       l2cap_uuid,
	       rfcomm_uuid,
	       svc_uuid;
	sdp_list_t *l2cap_list, 
	       *rfcomm_list,
	       *root_list,
	       *proto_list,
	       *access_proto_list;
	sdp_data_t *channel;
	sdp_record_t *record = sdp_record_alloc();

	/* make the service record publicly browsable */
	sdp_uuid16_create(&root_uuid, PUBLIC_BROWSE_GROUP);
	root_list = sdp_list_append(0, &root_uuid);
	sdp_set_browse_groups(record, root_list);

	/* set l2cap information */
	sdp_uuid16_create(&l2cap_uuid, L2CAP_UUID);
	l2cap_list = sdp_list_append(0, &l2cap_uuid);
	proto_list = sdp_list_append(0, l2cap_list);

	/* set rfcomm information */
	sdp_uuid16_create(&rfcomm_uuid, RFCOMM_UUID);
	channel = sdp_data_alloc(SDP_UINT8, &rfcomm_channel);
	rfcomm_list = sdp_list_append(0, &rfcomm_uuid);
	sdp_list_append(rfcomm_list, channel);
	sdp_list_append(proto_list, rfcomm_list);

	/* attach protocol information to service record */
	access_proto_list = sdp_list_append(0, proto_list);
	sdp_set_access_protos(record, access_proto_list);

	/* set the name, provider, and description */
	sdp_set_info_attr(record, service_name, service_prov, service_dsc);
	gint err = 0;
	sdp_session_t *session = 0;

	/* connect to the local SDP server, register the service record, and 
	 * disconnect
	 */
	session = sdp_connect(BDADDR_ANY, BDADDR_LOCAL, SDP_RETRY_IF_BUSY);
	err = sdp_record_register(session, record, 0);

	/* cleanup */
	sdp_data_free(channel);
	sdp_list_free(l2cap_list, 0);
	sdp_list_free(rfcomm_list, 0);
	sdp_list_free(root_list, 0);
	sdp_list_free(access_proto_list, 0);

	return session;
}

enum COMMAND_CODES bm_get_command(struct bluemote_server *server)
{
	guint8 cmd_code;

	g_assert(server != NULL);

	if (server->bytes_read < 1) {
		return BM_NONE;
	}

	if (server->pkt_cnt & 0x01 != server->buf[0] & 0x01) {
		/* duplicate packet detected - resend last packet */
		bm_write_data(server);
		return BM_NONE;
	}

	server->pkt_cnt = (server->pkt_cnt + 1) & 0x01;

	cmd_code = (server->buf[0] >> 1) & 0x7F;
	switch (cmd_code) {
	case BM_INIT :
	case BM_RENAME_DEVICE :
	case BM_LEARN :
	case BM_GET_VERSION :
	case BM_IR_TRANSMIT :
	case BM_DEBUG :
		return cmd_code;

	default :
	{
		server->out_buf[0] = (((guint8)BM_NAK << 1) & 0xFE)
			| (server->pkt_cnt & 0x01);
		server->bytes_written = 1;
		bm_write_data(server);
		return BM_NONE;
	}
	}
}

void bm_get_version(struct bluemote_server *server)
{
	g_assert(server != NULL);

	server->out_buf[0] = (((guint8)BM_ACK << 1) & 0xFE)
		| (server->pkt_cnt & 0x01);
	server->bytes_written = 1;
	server->out_buf[1] = (guint8)BM_FIRMWARE;
	server->bytes_written++;
	memcpy(&server->out_buf[1], &server->version, sizeof(server->version));
	server->bytes_written += sizeof(server->version);
	write(server->client, server->out_buf, server->bytes_written);
}

