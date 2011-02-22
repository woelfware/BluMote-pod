/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef BLUEMOTE_H
#define BLUEMOTE_H

#include "config.h"
#include <bluemote/bbluetooth.h>
#include <bluemote/ir.h>
#include <bluemote/pod.h>
#include <bluemote/types.h>

struct version {
	uint8_t	major,
		minor,
		revision;
};

struct bserver {
	struct version version;
	struct bserver bluetooth;
};

#endif

