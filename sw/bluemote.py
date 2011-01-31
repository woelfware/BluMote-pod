#!/usr/bin/env python
# Copyright (c) 2011 Woelfware

class Command_Codes():
	def __init__(self):
		self.init          = 0x00
		self.rename_device = 0x01
		self.learn         = 0x02
		self.get_version   = 0x03
		self.ir_transmit   = 0x04
		self.debug         = 0x7F	# specialized debug command whose functionality change whenever

class Command_Return_Codes():
	def __init__(self):
		self.pod_not_initted = 0x00
		self.ack             = 0x06
		self.nak             = 0x15

class Component_Codes():
	def __init__(self):
		self.hardware = 0x00
		self.firmware = 0x01
		self.software = 0x02

class Services():
	def __init__(self):
		self.cmd_codes = Command_Codes()
		self.cmd_rc = Command_Return_Codes()
		self.component_codes = Component_Codes()
		self.service_name = "Bluemote"
