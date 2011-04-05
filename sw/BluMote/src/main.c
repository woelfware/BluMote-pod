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
	bool run_again;

	init_hw();
	init_tasks();
	init_blumote();

	while (1) {
		run_again = run_tasks();
		if (run_again == false) {
			_BIS_SR(LPM4_bits + GIE);
		}
	}
}
