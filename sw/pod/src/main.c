had cre/*
 * Copyright (c) 2011 Woelfware
 */

#include "bluemote.h"
#include "config.h"
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
	struct bserver *server;

	if (!(server = bserver_create())) {
		printf("Failed to create a Bluemote server.\n");
		exit(EXIT_FAILURE);
	}

	brename(server, SERVICE_NAME, SERVICE_DSC, SERVICE_PROV);

	while (1) {
		bool connected = true;

		printf("Listening for incoming bluetooth connections.\n");
		if (!blisten(server)) {
			printf("Failure in accepting client connection.\n");
			exit(EXIT_FAILURE);
		}
		printf("Accepted bluetooth connection.\n");

		while (connected) {
			enum COMMAND_CODES cmd_code;
			uint8_t *rdbuf;
			uint16_t bytes_read;

			bytes_read = bread(server, &rdbuf);
			cmd_code = bget_command(server, rdbuf, bytes_read);
			switch (cmd_code) {
			case B_RENAME_DEVICE :
				brename(server, rdbuf + 1, NULL, NULL);
				connected = false;
				break;

			case B_LEARN :
				break;

			case B_GET_VERSION :
				bget_version(server);
				break;

			case B_IR_TRANSMIT :
				break;

			case B_DEBUG :
				break;

			case B_NONE :
				printf("Received invalid packet.\n");
				break;

			default :
				printf("Unhandled command code: %d\n", cmd_code);
			}
		}

		printf("disconnect\n");
		bclose(server);
	}

	return EXIT_SUCCESS;
}

