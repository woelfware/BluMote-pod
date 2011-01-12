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

print "Searching all nearby bluetooth devices for the BlueMote service"
service_matches = find_service(name = "BlueMote")

nbr_of_bluemotes = len(service_matches)
if nbr_of_bluemotes == 0:
	print "Couldn't find any BlueMotes"
	sys.exit(0)
elif:
	print "Found a BlueMote"
else:
	print "Found " + str(nbr_of_bluemotes) + " BlueMotes"
	print "Using the first BlueMote"

first_match = service_matches[0]
port = first_match["port"]
name = first_match["name"]
host = first_match["host"]

print "Connecting to \"%s\" on %s" % (name, host)

# Create the client socket
sock = BluetoothSocket(RFCOMM)
sock.connect((host, port))

print "Connected.  type stuff"
while True:
	data = raw_input()
	if len(data) == 0:
		break
	sock.send(data)
	print sock.recv(1024)

sock.close()
