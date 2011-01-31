#!/usr/bin/env python
# Copyright (c) 2011 Woelfware

import sys, os
import gtk

class Panel():
	def __init__(self, panel_type, rows, columns, homogenous = True):
		self.table = gtk.Table(rows, columns, homogenous)
		self.buttons = {}
		self.pixbufs = {}
		self.images = {}
		self.type = panel_type

	def button_with_pic(self, button_name, image_file):
		self.pixbufs[button_name] = gtk.gdk.pixbuf_new_from_file(image_file)
		self.images[button_name] = gtk.Image()
		self.images[button_name].set_from_pixbuf(self.pixbufs[button_name])
		self.buttons[button_name] = gtk.Button()
		self.buttons[button_name].set_image(self.images[button_name])

class TV_panel(Panel):
	def __init__(self):
		Panel.__init__(self, "TV", 7, 3)

		for i in range(10):
			self.buttons["%d" % i] = gtk.Button("%d" % i)
		self.buttons["select"] = gtk.Button("Select")
		self.buttons["channel-last"] = gtk.Button("Prev Ch")
		self.buttons["menu"] = gtk.Button("Menu")
		self.button_with_pic("pwr", sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/actions/system-shutdown.svg")
		self.button_with_pic("volume-up", sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/status/audio-volume-high.svg")
		self.button_with_pic("volume-down", sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/status/audio-volume-low.svg")
		self.button_with_pic("volume-mute", sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/status/audio-volume-muted.svg")
		self.button_with_pic("channel-up", sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/actions/go-up.svg")
		self.button_with_pic("channel-down", sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/actions/go-down.svg")
		#self.pixbufs["pwr"] = gtk.gdk.pixbuf_new_from_file_at_size(sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/actions/system-shutdown.svg", 200, 200)
		#self.images["pwr"].set_from_pixbuf(self.pixbufs["pwr"].scale_simple(4, 4, gtk.gdk.INTERP_BILINEAR))
		#self.images["pwr"].set_from_pixbuf(self.pixbufs["pwr"].scale_simple(350, 350, gtk.gdk.INTERP_BILINEAR))

		# pack widgets
		self.table.attach(self.buttons["pwr"], 0,1, 0, 1, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		for row in range(1, 4):
			for col in range(3):
				self.table.attach(self.buttons["%d" % ((row - 1) * 3 + col + 1)], col, col + 1, row, row + 1, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.table.attach(self.buttons["0"], 0, 1, 4, 5, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.table.attach(self.buttons["volume-up"], 2, 3, 5, 6, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.table.attach(self.buttons["volume-down"], 0, 1, 5, 6, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.table.attach(self.buttons["volume-mute"], 2, 3, 4, 5, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.table.attach(self.buttons["channel-up"], 1, 2, 4, 5, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.table.attach(self.buttons["channel-down"], 1, 2, 6, 7, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.table.attach(self.buttons["select"], 1, 2, 5, 6, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.table.attach(self.buttons["channel-last"], 2, 3, 6, 7, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.table.attach(self.buttons["menu"], 0, 1, 6, 7, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)

class VCR_panel(Panel):
	def __init__(self):
		Panel.__init__(self, "VCR", 1, 1)

		self.buttons["play"] = gtk.Button()

		self.pixbufs["play"] = gtk.gdk.pixbuf_new_from_file(sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/actions/media-playback-start.svg")
		self.images["play"] = gtk.Image()
		self.images["play"].set_from_pixbuf(self.pixbufs["play"])
		self.buttons["play"] = gtk.Button()
		self.buttons["play"].set_image(self.images["play"])

		# pack widgets
		self.table.attach(self.buttons["play"], 0, 1, 0, 1, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0) 

class Bluemote_GUI:
	def delete_event(self, widget, event, data=None):
		# If you return FALSE in the "delete_event" signal handler,
		# GTK will emit the "destroy" signal. Returning TRUE means
		# you don't want the window to be destroyed.
		# This is useful for popping up 'are you sure you want to quit?'
		# type dialogs.
		print "delete event occurred"

		# Change FALSE to TRUE and the main window will not be destroyed
		# with a "delete_event".
		return False

	def destroy(self, widget, data = None):
		gtk.mainquit()

	def __init__(self):
		# create widgets
		self.window = gtk.Window(gtk.WINDOW_TOPLEVEL)
		self.panels = {}
		self.panels["TV"] = TV_panel()
		self.panels["VCR"] = VCR_panel()
		self.panel = self.panels["TV"]

		# connect signals
		self.window.connect("delete_event", self.delete_event)
		self.window.connect("destroy", self.destroy)
		self.window.connect("key-press-event", self.on_window_key_press_event, self.panel.buttons)
		self.connect_buttons()

		# additional configs
		self.window.set_resizable(False)
		self.window.set_border_width(10)
		self.window.set_icon_from_file(sys.path[0] + "/data/bluemote_Icon72.png")
		self.window.set_title("Bluemote - %s Mode" % ("TV"))

		# pack widgets
		# button pad setup
		self.window.add(self.panel.table)

		self.window.show_all()

	def connect_buttons(self):
		if self.panel.type == "TV":
			self.panel.buttons["pwr"].connect("clicked", self.button_pwr_cb)
			for i in range(10):
				self.panel.buttons["%d" % i].connect("clicked", self.button_cb)
		elif self.panel.type == "VCR":
			self.panel.buttons["play"].connect("clicked", self.button_play_cb)

	def resize_image(self, widget, event, button):
		print "resizing image"

	def on_window_key_press_event(self, widget, event, data = None):
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
			data[keyval_name[-1]].emit("clicked")

	def button_cb(self, widget, data = None):
		print "got button press", widget.get_label()

	def button_pwr_cb(self, widget, data = None):
		print "got pwr button press"

	def button_play_cb(self, widget, data = None):
		print "got play button press"

	def main(self):
		# All PyGTK applications must have a gtk.main(). Control ends here
		# and waits for an event to occur (like a key press or mouse event).
		gtk.main()

# If the program is run directly or passed as an argument to the python
# interpreter then create a Bluemote_GUI instance and show it
if __name__ == "__main__":
	gui = Bluemote_GUI()
	gui.main()
