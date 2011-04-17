/*
 * Copyright (c) 2011 Woelfware
 */

#include "bluetooth.h"
#include "blumote.h"
#include "ir.h"
#include "hw.h"

#define N_ELEMENTS(arr)	(sizeof(arr)/sizeof((arr)[0]))

typedef bool (*task)(int ms);

static task const tasks[] = {
	bluetooth_main,
	ir_main,
	blumote_main
};

void main()
{
	int ms,
		i;
	bool run_again;

	init_hw();

	(void)get_ms();
	do {
		ms = get_ms();
		run_again = init_blumote(ms);
		while (bluetooth_main(ms) == true);
	} while (run_again == true);

	(void)get_ms();
	do {
		ms = get_ms();
		run_again = false;
		for (i = 0; i < N_ELEMENTS(tasks); i++) {
			if ((*tasks[i])(ms)) {
				run_again = true;
			}
		}
		if (learn_ir_code) {
			(void)get_us();
			while (ir_learn(get_us()));
			learn_ir_code = false;
			(void)get_ms();
			while (run_again = tx_learned_code()) {
				(void)bluetooth_main(get_ms());
			}
			run_again = true;
		}

		if (run_again == false) {
			_BIS_SR(LPM4_bits + GIE);
		}
	} while (1);
}
