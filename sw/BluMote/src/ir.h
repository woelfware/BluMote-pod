/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef IR_H
#define IR_H

#include <stdbool.h>

/* true - timeout
 * false - exited normally
 */
bool ir_learn();

/* true - received an abort cmd
 * false - exited normally
 */
bool ir_tx(volatile struct buf *abort);

#endif /*IR_H*/
