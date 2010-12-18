#!/usr/bin/env python
# file: rfcomm-client.py
# auth: Albert Huang <albert@csail.mit.edu>
# desc: simple demonstration of a client application that uses RFCOMM sockets
#	   intended for use with rfcomm-server
#
# $Id: rfcomm-client.py 424 2006-08-24 03:35:54Z albert $

from bluetooth import *
import sys

addr = None

if len(sys.argv) < 2:
	print "no device specified.  Searching all nearby bluetooth devices for"
	print "the BlueMote service"
else:
	addr = sys.argv[1]
	print "Searching for BlueMote on %s" % addr

# search for the BlueMote service
uuid = "1518d772-83b7-4fe4-9ed4-66fb348a9274"        
service_matches = find_service(uuid = uuid, address = addr)

if len(service_matches) == 0:
	print "couldn't find the BlueMote service =("
	sys.exit(0)

first_match = service_matches[0]
port = first_match["port"]
name = first_match["name"]
host = first_match["host"]

print "connecting to \"%s\" on %s" % (name, host)

# Create the client socket
sock = BluetoothSocket(RFCOMM)
sock.connect((host, port))

print "connected.  type stuff"
while True:
	data = raw_input()
	if len(data) == 0:
		break
	sock.send(data)
	print sock.recv(1024)

sock.close()
