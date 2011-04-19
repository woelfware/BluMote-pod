#ifndef CONFIG_H_
#define CONFIG_H_

#define BLUMOTE_NAME	"BluMote"
#define VERSION_MAJOR	0
#define VERSION_MINOR	1
#define VERSION_REV	0

#define SYS_CLK	(16)	/* MHz */

/* IR */
#define IR_CARRIER_FREQ		(40)	/* KHz */
#define US_PER_IR_TICK		(8)
#define MAX_SPACE_WAIT_TIME	(20000)	/* us */
#define IR_LEARN_CODE_TIMEOUT	(10000000)	/* us */

/* Buffer Sizes
 * Note: must be powers of 2
 */
#define BLUMOTE_RX_BUF_SIZE	(128)
#define IR_BUF_SIZE		(128)
#define UART_RX_BUF_SIZE	(32)
#define UART_TX_BUF_SIZE	(64)

#ifndef EOF
#define EOF	(-1)
#endif

#endif /*CONFIG_H_*/
