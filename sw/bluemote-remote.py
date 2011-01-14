#!/usr/bin/env python
# Copyright (c) 2011 Woelfware

from bluetooth import *
import bluemote

class Bluemote_Client(bluemote.Services):
	def __init__(self):
		bluemote.Services.__init__(self)
		self.addr = None
		self.pkt_cnt = 0

	def find_bluemote_pods(self):
		print "Searching for \"%s\" service..." % (self.service_name)
		return find_service(name = self.service_name)

	def connect_to_bluemote_pod(self, pod):
		port = pod["port"]
		name = pod["name"]
		host = pod["host"]

		# Create the client socket
		self.client_sock = BluetoothSocket(RFCOMM)
		self.client_sock.connect((host, port))

	def init(self):
		# have to train first or load a config from a database
		pass

	def rename_device(self):
		pass

	def train(self):
		self.client_sock.send(self.cmd_codes.train)
		pass

	def _get_version_unpack_msg(self, msg):
		version = []
		versions = struct.unpack(len(msg) * "B", msg)

		i = 0
		while i < len(msg):
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
		byte = (self.cmd_codes.get_version << 1) | (self.pkt_cnt & 0x01)
		self.pkt_cnt = (self.pkt_cnt + 1) % 2
		msg = struct.pack('B', byte)
		self.client_sock.send(msg)
		msg = self.client_sock.recv(1024)
		return self._get_version_unpack_msg(msg)

	def ir_transmit(self):
		pass

if __name__ == "__main__":
	bm_remote = Bluemote_Client()

	try:
		bm_pods = bm_remote.find_bluemote_pods()
		bm_remote.connect_to_bluemote_pod(bm_pods[0])

		version = bm_remote.get_version()
		for component in version:
			print "%s version: %s" % component
	except IOError:
		pass
	finally:
		bm_remote.client_sock.close()
