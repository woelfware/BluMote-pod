/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef IR_H
#define IR_H

#include <stdbool.h>
#include "buffer.h"

uint8_t get_ir_carrier_frequency();

/* true - timeout
 * false - exited normally
 */
bool ir_learn();

/* true - received an abort cmd
 * false - exited normally
 */
bool ir_tx(volatile struct buf *abort);

void set_ir_carrier_frequency(uint8_t frequency);

#endif /*IR_H*/
