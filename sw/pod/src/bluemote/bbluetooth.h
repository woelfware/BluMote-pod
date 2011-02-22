/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef BBLUETOOTH_H
#define BBLUETOOTH_H

#include <stdint.h>
#include <stdbool.h>
#include <unistd.h>

typedef struct bbluetooth *bbluetooth_t;

bbluetooth_t bbluetooth_create();
void bbluetooth_destroy(bbluetooth_t server);
void bbluetooth_set_description(bbluetooth_t server,
		uint8_t *name,
		uint8_t *description,
		uint8_t *provider);
bool bbluetooth_listen(bbluetooth_t server);
int bbluetooth_read(bbluetooth_t server, uint8_t **rdbuf);
ssize_t bbluetooth_write(bbluetooth_t server, uint8_t *wrbuf, size_t len);
void bbluetooth_close(bbluetooth_t server);

#endif

