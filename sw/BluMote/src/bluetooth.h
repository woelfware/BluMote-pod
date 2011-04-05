/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef BLUETOOTH_H_
#define BLUETOOTH_H_

#include <stdbool.h>

#ifndef EOF
#define EOF	(-1)
#endif

int bluetooth_getchar();
int bluetooth_putchar(int character);
bool bluetooth_main();

#endif /*BLUETOOTH_H_*/
