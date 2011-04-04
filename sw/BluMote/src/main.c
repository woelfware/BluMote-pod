/*
 * Copyright (c) 2011 Woelfware
 */

#include "bluetooth.h"
#include "blumote.h"
#include "ir.h"
#include "hw.h"
#include "task.h"

static void init_tasks()
{
	add_task(bluetooth_main);
	add_task(ir_main);
	add_task(blumote_main);
}

void main()
{
	init_hw();
	init_tasks();

	while (1) {
		if (!run_tasks()) {
			_BIS_SR(LPM4_bits + GIE);
		}
	}
}
