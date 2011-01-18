#!/usr/bin/env python
# Copyright (c) 2011 Woelfware

from bluetooth import *
import bluemote
import os

# sample key codes of buttons "0" through "9" for use in creating a learning function
sample_key_codes = [[8985, 4475, 564, 569, 539, 1694, 568, 1667, 566, 564, 564, 566, 538, 566, 566, 564, 568, 1668, 570, 1689, 542, 564, 564, 1676, 560, 563, 567, 539, 593, 537, 569, 537, 593, 537, 592, 1644, 588, 544, 588, 542, 561, 1672, 590, 537, 569, 560, 570, 537, 591, 542, 563, 540, 590, 1672, 564, 1670, 590, 537, 569, 1669, 588, 1648, 588, 1672, 564, 1671, 591],
	[9071, 4389, 644, 477, 653, 1592, 648, 1611, 624, 471, 659, 471, 659, 473, 633, 470, 660, 1612, 624, 1609, 650, 469, 633, 1614, 648, 472, 632, 472, 660, 470, 662, 468, 635, 468, 665, 466, 663, 468, 634, 470, 660, 472, 658, 471, 632, 473, 661, 469, 662, 468, 635, 1612, 648, 1585, 651, 1611, 622, 1612, 650, 1586, 650, 1610, 621, 1614, 648, 1586, 652],
	[9087, 4377, 646, 480, 653, 1583, 650, 1584, 622, 499, 661, 477, 655, 477, 626, 478, 655, 1607, 628, 1607, 651, 480, 626, 1609, 653, 475, 628, 475, 656, 474, 657, 476, 625, 480, 651, 479, 650, 480, 629, 475, 654, 476, 655, 475, 630, 473, 657, 475, 653, 479, 624, 1610, 655, 1581, 652, 1608, 625, 1610, 651, 1583, 652, 1612, 624, 1607, 655, 1581, 650],
	[9091, 4372, 650, 477, 626, 1610, 652, 1583, 653, 477, 653, 477, 626, 480, 652, 476, 657, 1578, 656, 1607, 627, 476, 653, 1609, 626, 475, 658, 473, 656, 473, 631, 473, 659, 475, 654, 1580, 652, 480, 655, 473, 630, 473, 657, 473, 659, 474, 630, 473, 655, 477, 655, 475, 626, 1609, 655, 1581, 655, 1605, 599, 1635, 654, 1581, 655, 1607, 627, 1607, 655],
	[9060, 4400, 646, 457, 678, 1585, 646, 1587, 677, 455, 644, 460, 680, 447, 682, 451, 648, 1588, 679, 1556, 675, 455, 677, 1557, 681, 448, 684, 446, 653, 430, 707, 443, 687, 446, 653, 453, 681, 1579, 650, 451, 686, 446, 686, 446, 655, 447, 685, 447, 683, 449, 654, 1579, 684, 422, 679, 1581, 684, 1552, 679, 1581, 650, 1583, 683, 1530, 706, 1576, 654],
	[9057, 4400, 647, 457, 675, 1587, 644, 1592, 670, 458, 648, 455, 675, 455, 677, 455, 649, 1587, 670, 1566, 670, 457, 675, 1561, 674, 456, 679, 453, 646, 457, 673, 460, 677, 452, 647, 1587, 675, 1561, 672, 456, 679, 452, 644, 462, 675, 455, 677, 453, 647, 455, 679, 453, 681, 449, 648, 1588, 670, 1565, 677, 1583, 646, 1579, 687, 1557, 675, 1587, 644],
	[9091, 4391, 682, 448, 682, 1554, 683, 1577, 656, 445, 688, 442, 690, 444, 655, 448, 685, 1576, 659, 1554, 682, 443, 686, 1577, 684, 446, 657, 447, 683, 444, 691, 441, 662, 420, 710, 442, 688, 442, 661, 1577, 683, 447, 657, 424, 710, 442, 688, 442, 661, 443, 690, 1573, 656, 1578, 684, 446, 664, 1572, 685, 1548, 688, 1575, 657, 1576, 690, 1546, 685],
	[9084, 4398, 673, 425, 685, 1561, 699, 1524, 711, 442, 686, 422, 684, 419, 707, 425, 706, 1561, 677, 1561, 681, 413, 714, 1558, 677, 441, 684, 422, 708, 420, 690, 415, 713, 417, 715, 415, 691, 1556, 702, 1534, 699, 420, 710, 419, 691, 413, 715, 415, 713, 419, 689, 1556, 699, 422, 686, 417, 708, 1564, 679, 1556, 702, 1556, 677, 1583, 655, 1557, 703],
	[9087, 4376, 694, 433, 697, 1542, 691, 1568, 666, 442, 688, 440, 695, 455, 648, 433, 697, 1567, 668, 1566, 692, 440, 666, 1590, 672, 433, 673, 432, 698, 433, 697, 433, 668, 437, 693, 1570, 668, 1554, 694, 1553, 694, 456, 672, 438, 666, 439, 693, 437, 695, 433, 671, 435, 695, 435, 697, 433, 670, 1566, 692, 1545, 683, 1575, 668, 1567, 695, 1541, 691],
	[9085, 4376, 668, 450, 678, 1569, 666, 1594, 646, 446, 682, 448, 682, 448, 658, 447, 682, 1590, 650, 1586, 665, 453, 660, 1587, 668, 473, 640, 441, 680, 452, 678, 455, 659, 441, 681, 450, 681, 449, 664, 441, 682, 1590, 653, 441, 677, 455, 676, 452, 664, 439, 683, 1591, 655, 1579, 677, 1558, 677, 442, 682, 1565, 679, 1581, 659, 1574, 681, 1555, 679]]


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

	def transport_tx(self, cmd, msg):
		full_msg = struct.pack("B", (cmd << 1) | (self.pkt_cnt & 0x01))
		self.pkt_cnt = (self.pkt_cnt + 1) % 2
		full_msg += msg
		self.client_sock.send(full_msg)

	def init(self):
		# have to learn the codes first or load a config from a database
		#self.transport_tx(self.cmd_codes.init, "")
		pass

	def rename_device(self):
		#self.transport_tx(self.cmd_codes.rename_device, "")
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
		
	except IOError:
		pass
	finally:
		try:
			bm_remote.client_sock.close()
		except:
			pass
