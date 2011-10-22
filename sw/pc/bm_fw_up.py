#!/usr/bin/env python
# Copyright (c) 2011 Woelfware

import sys
import time
import bluetooth

def find_blumotes():
	found = False

	print 'Searching for BluMote devices...'

	while not found:
		try:
			nearby_devices = bluetooth.discover_devices(lookup_names = True)
			blumote_devices = [dev for dev in nearby_devices if dev[1].startswith('BluMote-')]
			if len(blumote_devices):
				found = True
		except IOError:
			# no Bluetooth devices found
			pass

	return blumote_devices

def get_target_addr(blumote_devices):
	valid_selection = False
	while not valid_selection:
		print '\nSelect the BluMote you would like to update:'
		print '\t0) Cancel'
		for i in xrange(len(blumote_devices)):
			print '\t%i) %s [%s]' % (i + 1, blumote_devices[i][1], blumote_devices[i][0])
		target = int(raw_input()) - 1

		if target < 0:
			return None
		elif target < len(blumote_devices):
			valid_selection = True
		else:
			print 'Invalid selection!  Try again...'
			time.sleep(1)

	return blumote_devices[target][0]

class BluMote(bluetooth.BluetoothSocket):
	def __init__(self, protocol = bluetooth.RFCOMM):
		bluetooth.BluetoothSocket.__init__(self, protocol)
		
	def __enter__(self):
		return self

	def __exit__(self, *args):
		self.close()

	def enter_bsl(self):
		print 'skipping entering the bsl until the pins are setup correctly on the pod'
		return

		rst = 1 << 0
		test = 1 << 1

		# http://www.ti.com/lit/ug/slau319a/slau319a.pdf
		# rst  ________|------
		# test ___|-|_|--|____
		self.send('S@,%02X%02X' % (rst | test, rst | test))
		self.send('S&,%02X%02X' % (test, test))
		self.send('S&,%02X%02X' % (test, 0))
		self.send('S&,%02X%02X' % (test, test))
		self.send('S&,%02X%02X' % (rst | test, rst | test))
		self.send('S&,%02X%02X' % (rst | test, rst))

	def enter_cmd_mode(self):
		self.send('$$$')
		return self.recv(128)

	def set_baud_9600(self):
		self.send('U,9600,N\r\n')
		return self.recv(128)

if __name__ == '__main__':
	blumotes = find_blumotes()
	addr = get_target_addr(blumotes)

	if addr is None:
		print 'Cancelled... good bye.'
		exit()

	with BluMote() as bm_up:
		bm_up.connect((addr, 1))

		print bm_up.enter_cmd_mode()
		print bm_up.enter_bsl()
		print bm_up.set_baud_9600()

