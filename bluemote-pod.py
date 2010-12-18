#!/usr/bin/env python
# file: rfcomm-server.py
# auth: Albert Huang <albert@csail.mit.edu>
# desc: simple demonstration of a server application that uses RFCOMM sockets
#
# $Id: rfcomm-server.py 518 2007-08-10 07:20:07Z albert $

from bluetooth import *

server_sock = BluetoothSocket(RFCOMM)
server_sock.bind(("", PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "1518d772-83b7-4fe4-9ed4-66fb348a9274"        

advertise_service(server_sock,
	"BlueMote",
	service_id = uuid,
	service_classes = [uuid, SERIAL_PORT_CLASS],
	profiles = [SERIAL_PORT_PROFILE], 
#				   protocols = [OBEX_UUID] 
	)
				   
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

print "disconnected"

client_sock.close()
server_sock.close()
print "all done"
