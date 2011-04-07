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
	(void)add_task(bluetooth_main);
	(void)add_task(ir_main);
	(void)add_task(blumote_main);
}

void main()
{
	bool run_again;

	init_hw();
	init_tasks();
	do {
		run_again = init_blumote(sys_tick);
		while (bluetooth_main(sys_tick) == true);
	} while (run_again == true);

	while (1) {
		run_again = run_tasks(sys_tick);
		if (run_again == false) {
			_BIS_SR(LPM4_bits + GIE);
		}
	}
}
