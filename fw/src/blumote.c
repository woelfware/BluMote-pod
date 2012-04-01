/*
 * Copyright (c) 2011 Woelfware
 */

#include <msp430.h>
#include <string.h>
#include "bluetooth.h"
#include "blumote.h"
#include "btime.h"
#include "buffer.h"
#include "config.h"
#include "hw.h"
#include "ir.h"

static void get_calibration(
	volatile struct buf * const bluetooth_rx_buf,
	uint16_t addr,
	uint8_t length);
static void get_name(struct buf * const bluetooth_rx_buf);
static void get_rn42_data(
	volatile struct buf * const bluetooth_rx_buf,
	int_fast32_t ttl);
static void ir_xmit();
static void send_ACK();
static void send_cmd(
	struct buf *const bluetooth_rx_buf,
	char const * const cmd,
	int const cmd_len);
static void send_NAK();
static bool set_baudrate(struct buf * const bluetooth_rx_buf);
static bool set_cmd_mode(struct buf * const bluetooth_rx_buf);
static void set_exit_cmd_mode(struct buf * const bluetooth_rx_buf);
static void set_latency(struct buf * const bluetooth_rx_buf);
static void set_low_power(struct buf * const bluetooth_rx_buf);
static void set_name(struct buf * const bluetooth_rx_buf);
static void reset_bluetooth();

static void get_calibration(
	volatile struct buf * const bluetooth_rx_buf,
	uint16_t addr,
	uint8_t length)
{
	int len = (int)length;

	len = len <= bluetooth_rx_buf->buf_size ? len : bluetooth_rx_buf->buf_size;

	for ( ; len > 0; addr++, --len) {
		bluetooth_rx_buf->buf[bluetooth_rx_buf->wr_ptr++] = *(char *)addr;
	}
}

static void get_name(struct buf * const bluetooth_rx_buf)
{
	char const * const str_get_name = "GN\r";

	send_cmd(bluetooth_rx_buf,
		str_get_name,
		strlen(str_get_name));
}

static void get_rn42_data(
	volatile struct buf * const bluetooth_rx_buf,
	int_fast32_t ttl)
{
	int my_wr_ptr = bluetooth_rx_buf->wr_ptr;
 
	(void)get_sys_tick();
	while (ttl > 0) {
		ttl -= get_us();
		if (my_wr_ptr != bluetooth_rx_buf->wr_ptr) {
			my_wr_ptr = bluetooth_rx_buf->wr_ptr;
			ttl = MIN_UART_WAIT_TIME;
			(void)get_sys_tick();
		}
	}
}

static void restore_bt_rx_buf()
{
	uber_buf.rd_ptr = uber_buf.wr_ptr = 0;
	set_bluetooth_rx_buf(&uber_buf);
}

static void ir_xmit()
{
	volatile struct buf bt_rx = {0, 0};
	uint8_t const MIN_IR_XMIT_BYTES = 4;
	uint8_t repeat_cnt,
		data_len,
		buf[2];

	bt_rx.buf = buf;
	bt_rx.buf_size = sizeof(buf);

	set_bluetooth_rx_buf(&bt_rx);
	ENABLE_BT_RX_INT();

	/* verify that we got a good packet */
	if (uber_buf.wr_ptr - uber_buf.rd_ptr < MIN_IR_XMIT_BYTES) {
		restore_bt_rx_buf();
		send_NAK();
		return;
	}

	/* extract the fields */
	repeat_cnt = uber_buf.buf[uber_buf.rd_ptr++];
	if (!repeat_cnt) {
		repeat_cnt = NBR_IR_BURSTS;
	}
	data_len = uber_buf.buf[uber_buf.rd_ptr++];
	set_ir_carrier_frequency(uber_buf.buf[uber_buf.rd_ptr++]);
	uber_buf.rd_ptr++;	/* reserved */

	/* more packet verification */
	if (uber_buf.wr_ptr - uber_buf.rd_ptr != data_len) {
		restore_bt_rx_buf();
		send_NAK();
		return;
	}

	update_ccr0_timing(get_ir_carrier_frequency());

	/* blast the ir code */
	for ( ; repeat_cnt; --repeat_cnt) {
		if (ir_tx(&bt_rx)) {
			break;
		}
	}

	restore_bt_rx_buf();

	send_ACK();
}

static void reset_bluetooth()
{
	reset_rn42();
	wait_us(1000000);
}

static void send_ACK()
{
	uber_buf.rd_ptr = uber_buf.wr_ptr = 0;
	uber_buf.buf[uber_buf.wr_ptr++] = BLUMOTE_ACK;
	bluetooth_tx(&uber_buf);
}

static void send_cmd(
	struct buf *const bluetooth_rx_buf,
	char const * const cmd,
	int const cmd_len)
{
	/* init buffers */
	memcpy((char *)uber_buf.buf, cmd, cmd_len);
	uber_buf.rd_ptr = 0;
	uber_buf.wr_ptr = cmd_len;

	/* tx cmd */
	bluetooth_tx(&uber_buf);

	bluetooth_rx_buf->rd_ptr = bluetooth_rx_buf->wr_ptr = 0;

	/* rx cmd */
	get_rn42_data(bluetooth_rx_buf, MAX_UART_WAIT_TIME);
}

static void send_NAK()
{
	uber_buf.rd_ptr = uber_buf.wr_ptr = 0;
	uber_buf.buf[uber_buf.wr_ptr++] = BLUMOTE_NAK;
	bluetooth_tx(&uber_buf);
}

static bool set_baudrate(struct buf * const bluetooth_rx_buf)
{
	char const * const str_set_baudrate = "SU,11\r\n",	/* 115,200 baud */
		* const str_rx_set_baudrate_aok = "AOK\r\n",
		* const str_rx_set_baudrate_err = "ERR\r\n";
	bool rc = true;

	send_cmd(bluetooth_rx_buf,
		str_set_baudrate,
		strlen(str_set_baudrate));

	/* analyze the result */
	if (memcmp((const void *)bluetooth_rx_buf->buf,
			str_rx_set_baudrate_aok,
			strlen(str_rx_set_baudrate_aok))) {
		if (memcmp((const void *)bluetooth_rx_buf->buf,
				str_rx_set_baudrate_err,
				strlen(str_rx_set_baudrate_err))) {
			/* get an err... at least we're talking.
			 * retry setting the rn-42 to 115,200 baud.
			 */
			send_cmd(bluetooth_rx_buf,
				str_set_baudrate,
				strlen(str_set_baudrate));
			if (memcmp((const void *)bluetooth_rx_buf->buf,
					str_rx_set_baudrate_aok,
					strlen(str_rx_set_baudrate_aok))) {
				rc = false;
			}
		}
	}

	return rc;
}

static bool set_cmd_mode(struct buf * const bluetooth_rx_buf)
{
	char const * const str_set_cmd_mode = "$$$",
		* const str_rx_cmd_mode = "CMD\r\n";
	bool rc = true;

	send_cmd(bluetooth_rx_buf,
		str_set_cmd_mode,
		strlen(str_set_cmd_mode));

	/* analyze the result */
	if (memcmp((const void *)bluetooth_rx_buf->buf,
			str_rx_cmd_mode,
			strlen(str_rx_cmd_mode))) {
		rc = false;
	}

	return rc;
}

static void set_exit_cmd_mode(struct buf * const bluetooth_rx_buf)
{
	char const *str_exit_cmd_mode = "---\r\n";
	
	send_cmd(bluetooth_rx_buf,
		str_exit_cmd_mode,
		strlen(str_exit_cmd_mode));
}

static void set_latency(struct buf * const bluetooth_rx_buf)
{
	char const * const str_set_latency = "SQ,16\r\n";

	send_cmd(bluetooth_rx_buf,
		str_set_latency,
		strlen(str_set_latency));
}

static void set_low_power(struct buf * const bluetooth_rx_buf)
{
	char const * const str_set_low_power = "SW,8050\r\n";

	send_cmd(bluetooth_rx_buf,
		str_set_low_power,
		strlen(str_set_low_power));
}

static void set_name(struct buf * const bluetooth_rx_buf)
{
	char const * const str_set_name = "S-," BLUMOTE_NAME "\r\n";

	send_cmd(bluetooth_rx_buf,
		str_set_name,
		strlen(str_set_name));
}

void blumote_main()
{
	uint8_t c;

	get_rn42_data(&uber_buf, MIN_UART_WAIT_TIME);
	DISABLE_BT_RX_INT();
	c = uber_buf.buf[uber_buf.rd_ptr++];
	switch (c) {
	case BLUMOTE_GET_VERSION: {
		uber_buf.rd_ptr = uber_buf.wr_ptr = 0;

		uber_buf.buf[uber_buf.wr_ptr++] = BLUMOTE_ACK;
		uber_buf.buf[uber_buf.wr_ptr++] = BLUMOTE_FW;
		uber_buf.buf[uber_buf.wr_ptr++] = VERSION_MAJOR;
		uber_buf.buf[uber_buf.wr_ptr++] = VERSION_MINOR;
		uber_buf.buf[uber_buf.wr_ptr++] = VERSION_REV;

		bluetooth_tx(&uber_buf);
		}
		break;

	case BLUMOTE_LEARN:
		uber_buf.rd_ptr = uber_buf.wr_ptr = 0;
		uber_buf.buf[uber_buf.wr_ptr++] = BLUMOTE_ACK;	/* return code */
		uber_buf.buf[uber_buf.wr_ptr++] = 0;	/* length */
		uber_buf.buf[uber_buf.wr_ptr++] = 0;	/* ir carrier frequency */
		uber_buf.buf[uber_buf.wr_ptr++] = 0;	/* reserved */

		if (!ir_learn()) {
			uber_buf.buf[1] = uber_buf.wr_ptr - 4;
			uber_buf.buf[2] = get_ir_carrier_frequency();
		} else {
			/* timed out */
			uber_buf.wr_ptr = 0;
			uber_buf.buf[uber_buf.wr_ptr++] = BLUMOTE_NAK;
		}

		bluetooth_tx(&uber_buf);
		break;

	case BLUMOTE_IR_TRANSMIT:
		ir_xmit();
		break;

	case BLUMOTE_RESET_BLUETOOTH:
		/* ACK upon receipt of the reset since the link will be reset
		 * at the end of this function.
		 */
		uber_buf.rd_ptr = uber_buf.wr_ptr = 0;
		uber_buf.buf[uber_buf.wr_ptr++] = BLUMOTE_ACK;
		bluetooth_tx(&uber_buf);

		reset_bluetooth();
		break;

	case BLUMOTE_GET_CALIBRATION: {
		uint16_t addr;
		uint8_t len;

		addr = uber_buf.buf[uber_buf.rd_ptr++];
		addr <<= 8;
		addr |= uber_buf.buf[uber_buf.rd_ptr++];
		len = uber_buf.buf[uber_buf.rd_ptr++];

		uber_buf.rd_ptr = uber_buf.wr_ptr = 0;
		uber_buf.buf[uber_buf.wr_ptr++] = BLUMOTE_ACK;
		get_calibration(&uber_buf, addr, len);
		bluetooth_tx(&uber_buf);
		break;
	}

	default:
		send_NAK();
	}

	ENABLE_BT_RX_INT();
}

void init_rn42()
{
	uint8_t my_buf[16];
	struct buf bluetooth_rx_buf = {0, 0, sizeof(my_buf)};
	
	bluetooth_rx_buf.buf = my_buf;

	reset_bluetooth();
	set_bluetooth_rx_buf(&bluetooth_rx_buf);
try_9600_baud:
	if (!set_cmd_mode(&bluetooth_rx_buf)) {
		reset_bluetooth();
		if (!set_cmd_mode(&bluetooth_rx_buf)) {
			if (baud_rate == 115200) {
				set_baud_9600();
				goto try_9600_baud;
			}

			/* something isn't working - reset */
			volatile int reset = *(int *)0x0000;
		}
	}

	if (baud_rate == 9600) {
		if (set_baudrate(&bluetooth_rx_buf)) {
			set_baud_115200();
		} else {
			/* something isn't working - reset */
			volatile int reset = *(int *)0x0000;
		}
	}

	get_name(&bluetooth_rx_buf);
	if (memcmp((const void *)bluetooth_rx_buf.buf,
			BLUMOTE_NAME,
			strlen(BLUMOTE_NAME))) {
		set_name(&bluetooth_rx_buf);
		set_latency(&bluetooth_rx_buf);
		set_low_power(&bluetooth_rx_buf);
		set_exit_cmd_mode(&bluetooth_rx_buf);
		reset_bluetooth();
	} else {
		set_exit_cmd_mode(&bluetooth_rx_buf);
	}

	set_bluetooth_rx_buf(NULL);
}
