#!/usr/bin/env python
# Copyright © 2010 Woelfware

from bluetooth import *

server_sock = BluetoothSocket(RFCOMM)
server_sock.bind(("", PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

advertise_service(server_sock,
	"BlueMote",
	service_classes = [SERIAL_PORT_CLASS],
	profiles = [SERIAL_PORT_PROFILE])
				   
print "Waiting for connection on RFCOMM channel %d" % port

client_sock, client_info = server_sock.accept()
print "Accepted connection from ", client_info

try:
	while True:
		data = client_sock.recv(1024)
		if len(data) == 0:
			break
		if data == "1":
			client_sock.send("Hello")
		elif data == "2":
			client_sock.send(", world!\n")
		else:
			print "received [%s]" % data
			client_sock.send("ACK")
except IOError:
	pass
finally:
	print "disconnected"
	client_sock.close()
	server_sock.close()
	print "all done"
