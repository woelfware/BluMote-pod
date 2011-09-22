/*
 * Copyright (c) 2011 Woelfware
 */

#include "bluetooth.h"
#include "blumote.h"
#include "ir.h"
#include "hw.h"

#define N_ELEMENTS(arr)	(sizeof(arr)/sizeof((arr)[0]))

typedef bool (*task)(int_fast32_t us);

void main()
{
	int_fast32_t us;
	int i;
	task const tasks[] = {
		bluetooth_main,
		blumote_main};
	bool run_again;

	init_hw();

	(void)get_us();
	do {
		us = get_us();
		run_again = init_blumote(us);
		while (bluetooth_main(us) == true);
	} while (run_again == true);

	(void)get_us();
	do {
		us = get_us();
		run_again = false;
		for (i = 0; i < N_ELEMENTS(tasks); i++) {
			if ((*tasks[i])(us)) {
				run_again = true;
			}
		}
		
		if (tx_ir_code) {
			(void)get_us();
			while (ir_main(get_us()));
			tx_ir_code = false;
			(void)get_us();
			run_again = true;
		} else if (learn_ir_code) {
			gp_buf_owner = gp_buf_owner_ir;
			buf_clear(&gp_rx_tx);
			(void)get_us();
			ENABLE_IR_LEARN();
			while (learn_ir_code);
			(void)get_us();
			while (tx_learned_code()) {
				(void)bluetooth_main(get_us());
			}
			run_again = true;
		}

		if (run_again == false) {
			_BIS_SR(LPM4_bits + GIE);
		}
	} while (1);
}

