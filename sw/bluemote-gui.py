#!/usr/bin/env python
# Copyright (c) 2011 Woelfware

import sys
import gtk

class Bluemote_GUI:
	def delete_event(self, widget, event, data = None):
		return gtk.FALSE

	def destroy(self, widget, data = None):
		gtk.mainquit()

	def __init__(self):
		self.window = gtk.Window(gtk.WINDOW_TOPLEVEL)

		self.window.connect("delete_event", self.delete_event)
		self.window.connect("destroy", self.destroy)
		self.window.connect("key-press-event", self.on_window_key_press_event)

		self.window.set_border_width(10)

		key_pad = Bluemote()
		key_pad.panel.reparent(self.window)

		accelgroup = gtk.AccelGroup()
		self.window.add_accel_group(accelgroup)

		self.window.set_icon_from_file("data/bluemote_Icon72.png")
		self.window.set_title("Bluemote - %s Mode" % ("TV"))

	def on_window_key_press_event(self, widget, event):
		keyval_name = gtk.gdk.keyval_name(event.keyval)
		digit_keys = ("0", "KP_0",
			"1", "KP_1",
			"2", "KP_2",
			"3", "KP_3",
			"4", "KP_4",
			"5", "KP_5",
			"6", "KP_6",
			"7", "KP_7",
			"8", "KP_8",
			"9", "KP_9")
		if keyval_name in digit_keys:
			print "got", keyval_name
			#print self.window.emit("clicked")
		else:
			print "fuck if I know"

	def main(self):
		self.window.show()
		gtk.main()

class Bluemote:
	def __init__(self):
		# default values

		# use GtkBuilder to build our interface from the XML file
		try:
			builder = gtk.Builder()
			builder.add_from_file("data/buttons-tv.ui")
		except:
			self.error_message("Failed to load UI XML file: buttons-tv.ui")
			sys.exit(1)

		# get the widgets which will be referenced in callbacks
		self.panel = builder.get_object("button_panel")

		# connect signals
		builder.connect_signals(self)

	def digit_cb(self, widget, data = None):
		print widget.get_label()

if __name__ == "__main__":
	gui = Bluemote_GUI()
	gui.main()
