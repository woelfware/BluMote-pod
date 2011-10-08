/*
 * Copyright (c) 2011 Woelfware
 */

#include "btime.h"
#include "hw.h"

void wait_us(int_fast32_t ttl)
{
	(void)get_sys_tick();
	while (ttl > 0) {
		ttl -= get_us();
	}
}
