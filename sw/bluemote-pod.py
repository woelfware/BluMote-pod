#!/usr/bin/env python
# Copyright (c) 2010 Woelfware

from bluetooth import *
import bluemote

class Bluemote_Server(bluemote.Services):
	def __init__(self):
		bluemote.Services.__init__(self)
		self.version = ((self.component_codes.software, 0, 1, 0),)
		self.pkt_cnt = 0
		self.zero = None
		self.one = None
		self.header = None
		self.tailer = None

		self.server_sock = BluetoothSocket(RFCOMM)
		self.server_sock.bind(("", PORT_ANY))
		self.server_sock.listen(1)

		port = self.server_sock.getsockname()[1]

		advertise_service(self.server_sock,
			self.service_name,
			service_classes = [SERIAL_PORT_CLASS],
			profiles = [SERIAL_PORT_PROFILE])
						   
		print "Waiting for connection on RFCOMM channel %d" % port
		self.client_sock, self.client_info = self.server_sock.accept()
		print "Accepted connection from ", self.client_info

	def transport_tx(self, cmd, msg):
		full_msg = struct.pack("B", (cmd << 1) | (self.pkt_cnt & 0x01))
		full_msg += msg
		self.client_sock.send(full_msg)

	def get_command(self):
		full_msg = None
		new_data = False

		while new_data == False:
			full_msg = self.client_sock.recv(1024)

			# duplicate packet detection
			flags = struct.unpack(">B", full_msg[0])[0]
			pkt_cnt = flags & 0x01
			if pkt_cnt == self.pkt_cnt:
				new_data = True
				self.pkt_cnt = (self.pkt_cnt + 1) % 2

		cmd_code = flags >> 1
		try:
			msg = struct.unpack_from(">B", data, 1)
		except:
			msg = None

		# check for valid command code
		for cc in dir(self.cmd_codes):
			if getattr(self.cmd_codes, cc) == cmd_code:
				return (cc, msg) 
		return (None, None)

	def _init_unpack_msg(self, msg):
		self.zero = {}
		self.one = {}
		self.header = {}
		self.tailer = {}

		flags, self.zero["duration"] = struct.unpack_from(">BH", msg, 0)
		if flags & 0x01 == 1:
			self.zero["pulse"] = True
		else:
			self.zero["pulse"] = False
		flags, one["duration"] = struct.unpack_from(">BH", msg, 6)
		if flags & 0x01 == 1:
			self.one["pulse"] = True
		else:
			self.one["pulse"] = False
		flags, header["duration"] = struct.unpack_from(">BH", msg, 12)
		if flags & 0x01 == 1:
			self.header["pulse"] = True
		else:
			self.header["pulse"] = False
		flags, tailer["duration"] = struct.unpack_from(">BH", msg, 18)
		if flags & 0x01 == 1:
			self.tailer["pulse"] = True
		else:
			self.tailer["pulse"] = False

	def init(self, msg):
		self._init_unpack_msg(msg)
		self.transport_tx(self.cmd_rc.ack, "")

	def rename_device(self, msg):
		self.transport_tx(self.cmd_rc.ack, "")

	def train(self, msg):
		self.transport_tx(self.cmd_rc.ack, "")

	def get_version(self, msg):
		return_msg = ""

		for v in self.version:
			return_msg += struct.pack(len(v) * "B", *v)

		self.transport_tx(self.cmd_rc.ack, return_msg)

	def ir_transmit(self, msg):
		self.transport_tx(self.cmd_rc.ack, "")

if __name__ == "__main__":
	bm_pod = Bluemote_Server()

	try:
		while True:
			cmd_code, msg = bm_pod.get_command()
			if cmd_code != None:
				getattr(bm_pod, cmd_code)(msg)
			else:
				print "Invalid Command Code"
	except IOError:
		pass
	finally:
		print "disconnected"
		bm_pod.client_sock.close()
		bm_pod.server_sock.close()
		print "all done"
