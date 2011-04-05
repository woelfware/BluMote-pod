/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef BLUETOOTH_H_
#define BLUETOOTH_H_

#include <stdbool.h>

#ifndef EOF
#define EOF	(-1)
#endif

/*
 * The character read is returned as an int value.
 * If a reading error happens, the function returns EOF.
 */
int bluetooth_getchar();

/*
 * If there are no errors, the same character that has been written is returned.
 * If an error occurs, EOF is returned and the error indicator is set.
 */
int bluetooth_putchar(int character);

bool bluetooth_main();

#endif /*BLUETOOTH_H_*/
