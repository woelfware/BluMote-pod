#!/usr/bin/env python
# Copyright (c) 2011 Woelfware

from bluetooth import *
import bluemote
import os
import time

class Bluemote_Client(bluemote.Services):
	def __init__(self):
		bluemote.Services.__init__(self)
		self.addr = None

	def find_bluemote_pods(self, pod_name = None):
		
		if pod_name is None:
			pod_name = self.service["name"]
		print "Searching for \"%s\" service..." % (pod_name)
		return find_service(name = pod_name)

	def connect_to_bluemote_pod(self, addr):
		# Create the client socket
		self.client_sock = BluetoothSocket(RFCOMM)
		self.client_sock.connect((addr, 1))

	def transport_tx(self, cmd, msg):
		full_msg = struct.pack("B", cmd)
		full_msg += msg
		self.client_sock.send(full_msg)

	def rename_device(self, name):
		self.transport_tx(self.cmd_codes.rename_device, name)
		pass

	def _learn_unpack_msg(self, msg):
		return_msg = msg
		pkt_nbr = 0
		i = 0
		print 'pkt %i len %i' % (pkt_nbr, len(msg))
		print 'ack/nak:', hex(ord(msg[i]))
		i += 1
		if len(msg) <= i:
			msg = self.client_sock.recv(256)
			return_msg += msg
			i = 0
			pkt_nbr += 1
			print 'pkt %i len %i' % (pkt_nbr, len(msg))
		print 'reserved:', hex(ord(msg[i]))
		i += 1
		if len(msg) <= i:
			msg = self.client_sock.recv(256)
			return_msg += msg
			i = 0
			pkt_nbr += 1
			print 'pkt %i len %i' % (pkt_nbr, len(msg))
		code_len = int(ord(msg[i]))
		print 'length:', code_len
		i += 1
		while code_len > 0:
			while i + 1 < len(msg):
				print int(ord(msg[i]) * 256 + ord(msg[i + 1]))
				i += 2
				code_len -= 2
			if i < len(msg):
				code_len -= 1
			if code_len:
				if code_len & 1 == 1:
					tmp = msg[i]
				msg = self.client_sock.recv(256)
				return_msg += msg
				pkt_nbr += 1
				print 'pkt %i len %i' % (pkt_nbr, len(msg))
				if code_len & 1 == 1:
					msg = tmp + msg
				i = 0
				code_len += 1

		return return_msg[1:]	# strip the ack

	def learn(self):
		self.transport_tx(self.cmd_codes.learn, "")
		msg = self.client_sock.recv(1024)
		return self._learn_unpack_msg(msg)

	def _get_version_unpack_msg(self, msg):
		version = []
		full_msg = struct.unpack(len(msg) * "B", msg)
		flags = full_msg[0]
		versions = full_msg[1:]

		i = 0
		while i < len(versions):
			for cc in dir(self.component_codes):
				if getattr(self.component_codes, cc) == versions[i]:
					version.append((cc,
							"%u.%u.%u" % \
								(versions[i + 1], \
								 versions[i + 2], \
								 versions[i + 3])))
			i += 4

		return version

	def get_version(self):
		self.transport_tx(self.cmd_codes.get_version, "")
		msg = self.client_sock.recv(256)
		return self._get_version_unpack_msg(msg)

	def ir_transmit(self, msg):
		self.transport_tx(self.cmd_codes.ir_transmit, msg)
		pass

	def _debug_unpack_msg(self, msg):
		return self._learn_unpack_msg(msg)

	def debug(self, msg = ""):
		self.transport_tx(self.cmd_codes.debug, msg)
		msg = self.client_sock.recv(256)
		return self._debug_unpack_msg(msg)

if __name__ == "__main__":
	bm_remote = Bluemote_Client()

	try:
		found = False
		while found == False:
			try:
				nearby_devices = discover_devices(lookup_names = True)
			except:
				nearby_devices = ()
			print 'found %d devices' % len(nearby_devices)
			for addr, name in nearby_devices:
				if name[:len('BluMote')] == 'BluMote':
					print 'connecting to', addr, name
					bm_remote.connect_to_bluemote_pod(addr)
					found = True
					break

		print 'getting version info'
		version = bm_remote.get_version()
		for component in version:
			print "%s version: %s" % component

		print "Please push a button on your remote."
		key_code = bm_remote.learn()

		for i in range(5):
			print 'transmitting the button code.'
			bm_remote.ir_transmit(key_code)
			time.sleep(2)

		bm_remote.client_sock.close()
	except IOError:
		pass
	finally:
		try:
			bm_remote.client_sock.close()
		except:
			pass
