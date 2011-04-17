#ifndef CONFIG_H_
#define CONFIG_H_

#define BLUMOTE_NAME	"BluMote"
#define VERSION_MAJOR	0
#define VERSION_MINOR	1
#define VERSION_REV		0

#define SYS_CLK	(16)	/* MHz */

/* IR */
#define US_PER_IR_TICK		(16)
#define MAX_SPACE_WAIT_TIME	(20000)	/* us */
#define MAX_SPACE_WAIT_TICKS	((MAX_SPACE_WAIT_TIME) / (US_PER_IR_TICK)) 

/* Buffer Sizes
 * Note: must be powers of 2
 */
#define BLUMOTE_RX_BUF_SIZE	(64)
#define IR_BUF_SIZE		(128)
#define UART_RX_BUF_SIZE	(32)
#define UART_TX_BUF_SIZE	(64)

#endif /*CONFIG_H_*/
