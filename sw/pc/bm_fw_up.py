#!/usr/bin/env python
# Copyright (c) 2011 Woelfware

import bluetooth
import struct
import sys
import time

SYNC = 0x80
DATA_ACK = 0x90
DATA_NAK = 0xA0

def find_blumotes():
	found = False

	print 'Searching for BluMote devices...'

	while not found:
		try:
			nearby_devices = bluetooth.discover_devices(lookup_names = True)
			if len(nearby_devices):
				found = True
		except IOError:
			# no Bluetooth devices found
			pass

	return nearby_devices

def get_target_addr(blumote_devices, auto_select = False):
	valid_selection = False
	while not valid_selection:
		if auto_select:
			target = 0
		else:
			print '\nSelect the BluMote you would like to update:'
			print '\t0) Cancel'
			for i in xrange(len(blumote_devices)):
				print '\t%i) %s [%s]' % (i + 1, blumote_devices[i][1], blumote_devices[i][0])
			else:
				target = int(raw_input()) - 1

		if target < 0:
			return None
		elif target < len(blumote_devices):
			valid_selection = True
		else:
			print 'Invalid selection!  Try again...'
			time.sleep(1)

	if auto_select:
		print 'Auto selecting %s [%s].' % (blumote_devices[0][1], blumote_devices[0][0])

	return blumote_devices[target][0]

class BluMote(bluetooth.BluetoothSocket):
	def __init__(self, protocol = bluetooth.RFCOMM):
		bluetooth.BluetoothSocket.__init__(self, protocol)
		
	def __enter__(self):
		return self

	def __exit__(self, type, value, traceback):
		self.close()
		return isinstance(value, IOError)

	def calc_chksum(self, data):
		ckl = ckh = 0
		for i in xrange(len(data)):
			if i % 2:
				ckh ^= data[i]
			else:
				ckl ^= data[i]
		ckl ^= 0xFF
		ckh ^= 0xFF

		return (ckl, ckh)

	def enter_bsl(self):
		test = 1 << 2	# PIO-10
		rst = 1 << 3	# PIO-11

		# http://www.ti.com/lit/ug/slau319a/slau319a.pdf
		# rst  ________|------
		# test ___|-|_|--|____
		self.send('S*,%02X%02X\r\n' % (rst | test, 0))
		self.recv(128)
		self.send('S*,%02X%02X\r\n' % (test, test))
		self.recv(128)
		self.send('S*,%02X%02X\r\n' % (test, 0))
		self.recv(128)
		self.send('S*,%02X%02X\r\n' % (test, test))
		self.recv(128)
		self.send('S*,%02X%02X\r\n' % (rst, rst))
		self.recv(128)
		self.send('S*,%02X%02X\r\n' % (test, 0))
		self.recv(128)

	def enter_cmd_mode(self):
		self.send('$$$')
		return self.recv(128)

	def get_buffer_size(self):
		msg = (SYNC,)
		self.send(struct.pack('B' * len(msg), *msg))
		return self.recv(128)

	def read_mem(self):
		print 'sending rx password...'
		msg = self.rx_password()

		print 'sending rx data block...'
		self.sync()
		msg = (0x80, 0x14, 0x04, 0x04, 0xF0, 0x0F, 0x0E, 0x00, 0x85, 0xE0)
		self.send(struct.pack('B' * len(msg), *msg))
		self.settimeout(1.0)
		try:
			return self.recv(128)
		except:
			raise

	def rx_password(self):
		self.sync()
		msg = [0x80, 0x10, 0x24, 0x24, 0x00, 0x00, 0x00, 0x00]
		passwd = (
			0xFF,	# 0xE0
			0xFF,	# 0xE1
			0xFF,	# 0xE2
			0xFF,	# 0xE3
			0xFF,	# 0xE4
			0xFF,	# 0xE5
			0xFF,	# 0xE6
			0xFF,	# 0xE7
			0xFF,	# 0xE8
			0xFF,	# 0xE9
			0xFF,	# 0xEA
			0xFF,	# 0xEB
			0xFF,	# 0xEC
			0xFF,	# 0xED
			0x78,	# 0xEE
			0xE6,	# 0xEF
			0x56,	# 0xF0
			0xEA,	# 0xF1
			0xE6,	# 0xF2
			0xEA,	# 0xF3
			0xFF,	# 0xF4
			0xFF,	# 0xF5
			0xFF,	# 0xF6
			0xFF,	# 0xF7
			0xFF,	# 0xF8
			0xFF,	# 0xF9
			0xFF,	# 0xFA
			0xFF,	# 0xFB
			0xFF,	# 0xFC
			0xFF,	# 0xFD
			0x26,	# 0xFE
			0xE9)	# 0xFF

		msg.extend(passwd)
		msg.extend(self.calc_chksum(msg))

		self.send(struct.pack('B' * len(msg), *msg))
		return self.recv(128)

	def send_data_frame(self, cmd, addr, data):
		hdr = struct.pack('B', 80)
		cmd = struct.pack('B', cmd)
		addr = struct.pack('<H', addr)
		nbr_of_bytes = struct.pack('<H', len(addr) + len(data))
		nbr_of_pure_data_bytes = struct.pack('<H', len(data))
		msg = ''.join((hdr, cmd, nbr_of_bytes, addr, nbr_of_pure_data_bytes, data))

		# calculate the checksum
		data = struct.unpack('B' * len(msg), msg)
		ckl = ckh = 0
		for i in xrange(len(data)):
			if i % 2:
				ckh ^= data[i]
			else:
				ckl ^= data[i]
		ckl ^= 0xFF
		ckh ^= 0xFF

		msg = ''.join((msg, struct.pack('BB', ckl, ckh)))
		self.send(msg)

	def set_baud_9600(self):
		self.send('U,9600,E\r\n')
		return self.recv(128)

	def set_baud_115k(self):
		#self.send('U,115K,E\r\n')
		self.send('SU,11\r\n')
		return self.recv(128)

	def sync(self):
		self.send(struct.pack('B', SYNC))
		rc = self.recv(128)
		rc = struct.unpack('B' * len(rc), *rc)
		if rc[0] == 0:
			print 'rc is 0'
			rc = self.recv(128)
			rc = struct.unpack('B' * len(rc), *rc)
		if rc[0] == DATA_ACK:
			return True
		else:
			print 'Handshake is unsuccessful.'
			return False

if __name__ == '__main__':
	blumotes = find_blumotes()
	addr = get_target_addr(blumotes, False)

	if addr is None:
		print 'Cancelled... good bye.'
		exit()

	with BluMote() as bm_up:
		bm_up.connect((addr, 1))

		print 'Entering command mode:', bm_up.enter_cmd_mode()
		print 'Entering the BSL...'
		bm_up.enter_bsl()
		print 'Setting the RN-42 UART baud to 9600:', bm_up.set_baud_9600()
		print 'Getting some memory: '
		msg = bm_up.read_mem()
		print struct.unpack('B' * len(msg), msg)
		for i in xrange(10):
			msg = bm_up.recv(128)
			print struct.unpack('B' * len(msg), msg)

