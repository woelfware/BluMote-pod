#ifndef CONFIG_H_
#define CONFIG_H_

#define BLUMOTE_NAME	"BluMote"
#define VERSION_MAJOR	0
#define VERSION_MINOR	1
#define VERSION_REV	0

#define SYS_CLK	(16)	/* MHz */

/* IR */
#define IR_CARRIER_FREQ		(38)	/* KHz */
#define US_PER_SYS_TICK		(8)
#define MAX_SPACE_WAIT_TIME	(20000)	/* us */
#define IR_LEARN_CODE_TIMEOUT	(10000000)	/* us */

/* Buffer Sizes
 * Note: must be powers of 2
 */
#define GP_BUF_SIZE	(256)	/* blumote, ir */
#define UART_RX_BUF_SIZE	(32)
#define UART_TX_BUF_SIZE	(32)

#ifndef EOF
#define EOF	(-1)
#endif

#endif /*CONFIG_H_*/
