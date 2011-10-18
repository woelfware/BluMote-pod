/*
 * Copyright (c) 2011 Woelfware
 */

#ifndef CONFIG_H
#define CONFIG_H

#define BLUMOTE_NAME	"BluMote"
#define VERSION_MAJOR	0
#define VERSION_MINOR	1
#define VERSION_REV	0

#define SYS_CLK	(16)	/* MHz */
#define US_PER_SYS_TICK	(4)

#define IR_LEARN_CODE_TIMEOUT	(10000000)	/* us */
#define MAX_FILTERED_SPACE_TIME	(300)	/* us */
#define NBR_IR_BURSTS	(3)

#define MAX_UART_WAIT_TIME	(50000)	/* us - time to wait for the first char to arrive */
#define MIN_UART_WAIT_TIME	(500) /* us - time to wait after the first char has been rx'ed */

#define BLUETOOTH_RESET_HOLD_TIME	(10000)	/* us */
#define BLUETOOTH_STARTUP_TIME	(10000)	/* us */

#define UBER_BUF_SIZE	0x100

#endif /*CONFIG_H*/
