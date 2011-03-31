/*
 * Copyright (c) 2011 Woelfware
 */

#include "bluetooth.h"
#include "blumote.h"
#include "ir.h"
#include "hw.h"
#include "task.h"

void init_tasks()
{
	add_task(bluetooth_main);
	add_task(ir_main);
}

void main()
{
	init_hw();

	while (1) {
		if (!run_tasks()) {
			_BIS_SR(LPM4_bits + GIE);
		}
	}
}
