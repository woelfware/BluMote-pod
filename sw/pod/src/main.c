/*
 * Copyright (c) 2011 Woelfware
 */

#include "config.h"
#include "bluemote.h"
#include <stdlib.h>
#include <stdio.h>

int main(int argc, char *argv[])
{
	struct bluemote_server server;

	create_bluemote_server(&server);
	bm_allocate_socket(&server);
	bm_bind_socket(&server);
	bm_listen(&server);
	bm_read_data(&server);
	bm_close(&server);

	return EXIT_SUCCESS;
}

