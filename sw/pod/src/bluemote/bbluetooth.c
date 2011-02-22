/*
 * Copyright (c) 2011 Woelfware
 */

#include "bbluetooth.h"
#include <assert.h>
#include <stdlib.h>

#if defined(linux)
#	include <sys/socket.h>
#	include <sys/select.h>
#	include <bluetooth/bluetooth.h>
#	include <bluetooth/rfcomm.h>
#	include <bluetooth/sdp.h>
#	include <bluetooth/sdp_lib.h>
#elif defined(MSP430)
#else
#	error "Bluemote is not supported on this platform."
#endif

struct sdp_description {
	uint8_t name[16],
		dsc[16],
		prov[16];
};

struct bbluetooth {
	struct sockaddr_rc loc_addr,
			   rem_addr;
	struct sdp_description sdp; 
	sdp_session_t *session;
	socklen_t opt;
	int sockfd,
	    client,
	    bytes_read;
	uint8_t rdbuf[1024];
};

static int allocate_socket()
{
	return socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
}

static int bbind_socket(struct sockaddr_rc *address, int *socket)
{
	int err;

	assert(address);
	assert(socket);

	/* bind socket to the first available port on the first available local
	 * bluetooth adapter
	 */
	address->rc_family	= AF_BLUETOOTH;
	address->rc_bdaddr	= *BDADDR_ANY;
	for (address->rc_channel = 1;
			address->rc_channel <= 31;
			address->rc_channel++) {
		err = bind(*socket,
				(struct sockaddr *)address,
				sizeof(struct sockaddr_rc));
		if (!err) {
			break;
		}
	}

	return err;
}

static sdp_session_t *bregister_service(bbluetooth_t server)
{
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
	sdp_record_t *record;

	record = sdp_record_alloc();

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
	channel = sdp_data_alloc(SDP_UINT8, &server->loc_addr.rc_channel);
	rfcomm_list = sdp_list_append(0, &rfcomm_uuid);
	sdp_list_append(rfcomm_list, channel);
	sdp_list_append(proto_list, rfcomm_list);

	/* attach protocol information to service record */
	access_proto_list = sdp_list_append(0, proto_list);
	sdp_set_access_protos(record, access_proto_list);

	/* set the name, provider, and description */
	sdp_set_info_attr(record,
			server->sdp.name,
			server->sdp.prov,
			server->sdp.dsc);
	int err = 0;
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

static void blisten(bbluetooth_t server)
{
	assert(server);

	listen(server->sockfd, 1);
	server->client = accept(server->sockfd,
			(struct sockaddr *)&server->rem_addr,
			&server->opt);

	ba2str(&server->loc_addr.rc_bdaddr, server->rdbuf);
	memset(server->rdbuf, 0, sizeof(server->rdbuf));
}

bbluetooth_t bbluetooth_create()
{
	bbluetooth_t bluetooth = calloc(1, sizeof(struct bbluetooth));

	bluetooth->opt = sizeof(bluetooth->rem_addr);

	return bluetooth;
}

void bbluetooth_destroy(bbluetooth_t server)
{
	free(server);
}

void bbluetooth_set_description(bbluetooth_t server,
		uint8_t *name,
		uint8_t *description,
		uint8_t *provider)
{
	assert(server);

	if (name) {
		strncpy(server->sdp.name, name, sizeof(server->sdp.name));
	}
	if (description) {
		strncpy(server->sdp.dsc, description, sizeof(server->sdp.dsc));
	}
	if (provider) {
		strncpy(server->sdp.prov, provider, sizeof(server->sdp.prov));
	}
}

bool bbluetooth_listen(bbluetooth_t server)
{
	assert(server);

	server->sockfd = allocate_socket();
	if (bbind_socket(&server->loc_addr, &server->sockfd)) {
		return false;
	}
	server->session = bregister_service(server);

	blisten(server);

	return true;
}

int bbluetooth_read(bbluetooth_t server, uint8_t **rdbuf)
{
	fd_set rfds;

	assert(server != NULL);

	FD_ZERO(&rfds);
	FD_SET(server->client, &rfds);

	if (select(server->client + 1, &rfds, NULL, NULL, NULL)) {
		server->bytes_read = read(server->client,
				server->rdbuf,
				sizeof(server->rdbuf));
	} else {
		server->bytes_read = -1;
	}

	*rdbuf = server->rdbuf;
	return server->bytes_read;
}

ssize_t bbluetooth_write(bbluetooth_t server, uint8_t *wrbuf, size_t len)
{
	return write(server->client,
			wrbuf,
			len);
}

void bbluetooth_close(bbluetooth_t server)
{
	assert(server != NULL);

	close(server->client);
	close(server->sockfd);
}

