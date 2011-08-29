#ifndef CONFIG_H_
#define CONFIG_H_

#define BLUMOTE_NAME	"BluMote"
#define VERSION_MAJOR	0
#define VERSION_MINOR	1
#define VERSION_REV	0

#define SYS_CLK	(16)	/* MHz */

/* IR */
#define IR_CARRIER_FREQ		(38)	/* kHz */
#define US_PER_SYS_TICK		(8)
#define MAX_SPACE_WAIT_TIME	(10000)	/* us */
#define IR_LEARN_CODE_TIMEOUT	(10000000)	/* us */
#define NBR_IR_BURSTS	(3)
#define IR_REPEAT_MASK	(0xFF)

/* Buffer Sizes
 * Note: must be powers of 2
 */
#define GP_BUF_SIZE	(256)	/* blumote, ir */
#define UART_RX_BUF_SIZE	(32)
#define UART_TX_BUF_SIZE	(32)

#define BLUETOOTH_RESET_HOLD_TIME	(10000)	/* us */
#define BLUETOOTH_STARTUP_TIME	(10000)	/* us */

#define MAX_UART_WAIT_TIME	(50000)	/* us - time to wait for the first char to arrive */
#define MIN_UART_WAIT_TIME	(500) /* us - time to wait after the first char has been rx'ed */

#ifndef EOF
#define EOF	(-1)
#endif

#endif /*CONFIG_H_*/

