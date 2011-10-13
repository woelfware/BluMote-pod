/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef IR_H
#define IR_H

#include <stdbool.h>
#include "buffer.h"

extern uint16_t ccr0_timing;	/* used for the CCR0 timer */

uint8_t get_ir_carrier_frequency();

/* true - timeout
 * false - exited normally
 */
bool ir_learn();

/* true - received an abort cmd
 * false - exited normally
 */
bool ir_tx(volatile struct buf *abort);

void update_ccr0_timing();

void set_ir_carrier_frequency(uint8_t frequency);

#endif /*IR_H*/
