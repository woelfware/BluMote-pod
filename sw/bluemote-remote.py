#!/usr/bin/env python
# Copyright (c) 2011 Woelfware

from bluetooth import *
import bluemote
import os

class Bluemote_Client(bluemote.Services):
	def __init__(self):
		bluemote.Services.__init__(self)
		self.addr = None
		self.pkt_cnt = 0

	def find_bluemote_pods(self, pod_name = None):
		if pod_name is None:
			pod_name = self.service_name
		print "Searching for \"%s\" service..." % (pod_name)
		return find_service(name = pod_name)

	def connect_to_bluemote_pod(self, pod):
		port = pod["port"]
		name = pod["name"]
		host = pod["host"]

		# Create the client socket
		self.client_sock = BluetoothSocket(RFCOMM)
		self.client_sock.connect((host, port))

	def transport_tx(self, cmd, msg):
		full_msg = struct.pack("B", (cmd << 1) | (self.pkt_cnt & 0x01))
		self.pkt_cnt = (self.pkt_cnt + 1) % 2
		full_msg += msg
		self.client_sock.send(full_msg)

	def init(self):
		# have to learn the codes first or load a config from a database
		#self.transport_tx(self.cmd_codes.init, "")
		pass

	def rename_device(self, name):
		self.transport_tx(self.cmd_codes.rename_device, name)
		pass

	def _learn_unpack_msg(self, msg):
		key_code = []
		buf = struct.pack(">H", 0)
		full_msg = struct.unpack(len(msg) * "B", msg)
		flags = full_msg[0]
		key_codes = full_msg[1:]

		i = 0
		while i < len(key_codes):
			key_code.append((key_codes[i] << 8) + key_codes[i + 1])
			i += 2

		return key_code

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
		msg = self.client_sock.recv(1024)
		return self._get_version_unpack_msg(msg)

	def ir_transmit(self):
		#self.transport_tx(self.cmd_codes.ir_transmit, "")
		pass

	def _debug_unpack_msg(self, msg):
		return self._learn_unpack_msg(msg)

	def debug(self, msg = ""):
		self.transport_tx(self.cmd_codes.debug, msg)
		msg = self.client_sock.recv(1024)
		return self._debug_unpack_msg(msg)

if __name__ == "__main__":
	bm_remote = Bluemote_Client()

	try:
		bm_pods = bm_remote.find_bluemote_pods()
		bm_remote.connect_to_bluemote_pod(bm_pods[0])

		version = bm_remote.get_version()
		for component in version:
			print "%s version: %s" % component

		print "Please push key \"1\" on your remote."
		key_code = bm_remote.learn()
		print key_code

		print "Trying out debug message to get a precanned button press."
		for i in range(10):
			key_code = bm_remote.debug(struct.pack('B', i))
			print key_code

		new_pod_name = "indigomote"
		bm_remote.rename_device(new_pod_name)
		bm_remote.client_sock.close()
		bm_pods = bm_remote.find_bluemote_pods(new_pod_name)
		bm_remote.connect_to_bluemote_pod(bm_pods[0])
		version = bm_remote.get_version()
		for component in version:
			print "%s version: %s" % component
	except IOError:
		pass
	finally:
		try:
			bm_remote.client_sock.close()
		except:
			pass
