/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef HW_H
#define HW_H

#include <stdint.h>

/* enable/disable USCI_A0 RX interrupt
 * which is the bluetooth interface
 */
#define ENABLE_BT_RX_INT()	do {IE2 |= UCA0RXIE;} while (0)
#define DISABLE_BT_RX_INT()	do {IE2 &= ~UCA0RXIE;} while (0)

int_fast32_t get_us();

int_fast32_t get_sys_tick();

void init_hw();

void reset_rn42();

#endif /*HW_H*/
