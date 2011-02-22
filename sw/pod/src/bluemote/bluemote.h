/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef BLUEMOTE_H
#define BLUEMOTE_H

#include "bbluetooth.h"
#include "ir.h"
#include "types.h"
#include <stdint.h>

struct version {
	uint8_t	major,
		minor,
		revision;
};

struct bserver {
	struct version version;
	bbluetooth_t bluetooth;
};

struct bserver *bserver_create();
void bserver_destroy(struct bserver *server);
bool blisten(struct bserver *server);
int bread(struct bserver *server, uint8_t **rdbuf);
ssize_t bwrite(struct bserver *server, uint8_t *wrbuf, size_t len);
void bclose(struct bserver *server);
enum COMMAND_CODES bget_command(struct bserver *server, uint8_t *buf, size_t len);
void bget_version(struct bserver *server);
void brename(struct bserver *server,
		uint8_t *name,
		uint8_t *description,
		uint8_t *provider);
void blearn(struct bserver *server);

#endif

