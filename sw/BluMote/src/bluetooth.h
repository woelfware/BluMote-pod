/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef BLUETOOTH_H_
#define BLUETOOTH_H_

#include "config.h"
#include <stdbool.h>
#include <stdint.h>

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

/*
 * Convenience function for bluetooth_putchar.
 * Return 1 for success, EOF for error.
 */
int bluetooth_puts(char const *str, int nbr_chars);

/**
 * return bool
 * retval true run again
 * retval false done
 */
bool issue_bluetooth_reset(int_fast32_t us);

bool bluetooth_main(int_fast32_t us);

#endif /*BLUETOOTH_H_*/

