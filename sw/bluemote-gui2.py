#!/usr/bin/env python
# Copyright (c) 2011 Woelfware

import sys, os
import gtk
from bluemote_remote import *
import bluetooth

class Panel(gtk.Table):
	def __init__(self, panel_type, rows, columns, homogenous = True):
		super(Panel, self).__init__(rows, columns, homogenous)
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
		super(TV_panel, self).__init__("TV", 7, 3)

		for i in range(10):
			self.buttons["%d" % i] = gtk.Button("%d" % i)
		self.buttons["select"] = gtk.Button("Select")
		self.buttons["channel-last"] = gtk.Button("Prev Ch")
		self.buttons["menu"] = gtk.Button("Menu")
		self.button_with_pic("pwr", sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/actions/system-shutdown.svg")
		self.buttons["pwr"].modify_bg(gtk.STATE_NORMAL, gtk.gdk.color_parse("red"))
		self.buttons["pwr"].modify_bg(gtk.STATE_ACTIVE, gtk.gdk.Color("#D00000"))
		self.buttons["pwr"].modify_bg(gtk.STATE_PRELIGHT, gtk.gdk.color_parse("red"))
		self.button_with_pic("volume-up", sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/status/audio-volume-high.svg")
		self.button_with_pic("volume-down", sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/status/audio-volume-low.svg")
		self.button_with_pic("volume-mute", sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/status/audio-volume-muted.svg")
		self.button_with_pic("channel-up", sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/actions/go-up.svg")
		self.button_with_pic("channel-down", sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/actions/go-down.svg")
		#self.pixbufs["pwr"] = gtk.gdk.pixbuf_new_from_file_at_size(sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/actions/system-shutdown.svg", 200, 200)
		#self.images["pwr"].set_from_pixbuf(self.pixbufs["pwr"].scale_simple(4, 4, gtk.gdk.INTERP_BILINEAR))
		#self.images["pwr"].set_from_pixbuf(self.pixbufs["pwr"].scale_simple(350, 350, gtk.gdk.INTERP_BILINEAR))

		self.attach(self.buttons["pwr"], 0,1, 0, 1, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		for row in range(1, 4):
			for col in range(3):
				self.attach(self.buttons["%d" % ((row - 1) * 3 + col + 1)], col, col + 1, row, row + 1, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.attach(self.buttons["0"], 0, 1, 4, 5, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.attach(self.buttons["volume-up"], 2, 3, 5, 6, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.attach(self.buttons["volume-down"], 0, 1, 5, 6, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.attach(self.buttons["volume-mute"], 2, 3, 4, 5, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.attach(self.buttons["channel-up"], 1, 2, 4, 5, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.attach(self.buttons["channel-down"], 1, 2, 6, 7, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.attach(self.buttons["select"], 1, 2, 5, 6, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.attach(self.buttons["channel-last"], 2, 3, 6, 7, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)
		self.attach(self.buttons["menu"], 0, 1, 6, 7, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0)

class VCR_panel(Panel):
	def __init__(self):
		super(VCR_panel, self).__init__("VCR", 1, 1)

		self.buttons["play"] = gtk.Button()

		self.pixbufs["play"] = gtk.gdk.pixbuf_new_from_file(sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/actions/media-playback-start.svg")
		self.images["play"] = gtk.Image()
		self.images["play"].set_from_pixbuf(self.pixbufs["play"])
		self.buttons["play"] = gtk.Button()
		self.buttons["play"].set_image(self.images["play"])

		self.attach(self.buttons["play"], 0, 1, 0, 1, gtk.EXPAND | gtk.FILL, yoptions = gtk.EXPAND | gtk.FILL, xpadding = 0, ypadding = 0) 

class Menu(gtk.VBox):
	def __init__(self, window):
		super(Menu, self).__init__(False, 2)

		mb = gtk.MenuBar()

		mb.append(self._bluemote_menu(window))
		mb.append(self._mode_menu(window))

		self.pack_start(mb, False, False, 0)

	def _bluemote_menu(self, window):
		bluemote_menu = gtk.Menu()
		bluemote = gtk.MenuItem("_Bluemote")
		bluemote.set_submenu(bluemote_menu)

		self.connect = gtk.MenuItem("_Connect")
		bluemote_menu.append(self.connect)

		self.learn = gtk.MenuItem("_Learn")
		bluemote_menu.append(self.learn)

		bluemote_menu.append(gtk.SeparatorMenuItem())

		agr = gtk.AccelGroup()
		window.add_accel_group(agr)

		exit = gtk.ImageMenuItem(gtk.STOCK_QUIT, agr)
		key, mod = gtk.accelerator_parse("<Control>Q")
		exit.add_accelerator("activate", agr, key, mod, gtk.ACCEL_VISIBLE)

		exit.connect("activate", gtk.main_quit)

		bluemote_menu.append(exit)

		return bluemote

	def _mode_menu(self, window):
		mode_menu = gtk.Menu()
		mode = gtk.MenuItem("_Mode")
		mode.set_submenu(mode_menu)
		
		tv_mode = gtk.RadioMenuItem(None, "_TV", True)
		tv_mode.set_active(True)
		mode_menu.append(tv_mode)
		vcr_mode = gtk.RadioMenuItem(tv_mode, "_VCR", True)
		mode_menu.append(vcr_mode)

		return mode

class Bluemote_GUI(gtk.Window):
	def delete_event(self, widget, event, data = None):
		# If you return FALSE in the "delete_event" signal handler, GTK
		# will emit the "destroy" signal. Returning TRUE means you don't
		# want the window to be destroyed.  This is useful for popping
		# up 'are you sure you want to quit?' type dialogs.
		#print "delete event occurred"

		# Change FALSE to TRUE and the main window will not be destroyed
		# with a "delete_event".
		return False

	def destroy(self, widget, data = None):
		gtk.mainquit()

	def __init__(self):
		super(Bluemote_GUI, self).__init__()

		self.bm_client = Bluemote_Client()

		self.set_resizable(False)
		self.set_border_width(10)
		self.set_icon_from_file(sys.path[0] + "/data/bluemote_Icon72.png")
		self.set_title("Bluemote")
		self.set_position(gtk.WIN_POS_CENTER)

		menu = Menu(self)

		self.panels = {"TV" : TV_panel(),
			"VCR" : VCR_panel()}
		self.panel = self.panels["TV"]

		self.statusbar = gtk.Statusbar()
		self.statusbar.set_has_resize_grip(False)
		self.statusbar.push(0, "Welcome to Bluemote!")

		self.connect("delete_event", self.delete_event)
		self.connect("destroy", self.destroy)
		self.connect("key-press-event", self.on_window_key_press_event, self.panel.buttons)
		self.connect_buttons()

		menu.connect.connect("activate", self.connect_to_pod)

		vbox = gtk.VBox()
		vbox.add(menu)
		vbox.add(self.panel)
		vbox.add(self.statusbar)
		self.add(vbox)

		self.show_all()
		settings = self.get_settings()
		settings.set_long_property("gtk-menu-images", True, "")
		settings.set_long_property("gtk-button-images", True, "")

	def connect_buttons(self):
		if self.panel.type == "TV":
			self.panel.buttons["pwr"].connect("clicked", self.button_pwr_cb)
			for i in range(10):
				self.panel.buttons["%d" % i].connect("clicked", self.button_cb)
		elif self.panel.type == "VCR":
			self.panel.buttons["play"].connect("clicked", self.button_play_cb)

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
		self.statusbar.pop(0)
		self.statusbar.push(0, "Got button press %s" % (widget.get_label()))
		#print "got button press", widget.get_label()

	def button_pwr_cb(self, widget, data = None):
		self.statusbar.pop(0)
		self.statusbar.push(0, "Got pwr button press")
		#print "got pwr button press"

	def button_play_cb(self, widget, data = None):
		print "got play button press"

	def connect_to_pod(self, widget, data = None):
		podlist = bluemote_pod_list()
		#bm_pods = self.bm_client.find_bluemote_pods()
		#self.bm_client.connect_to_bluemote_pod(bm_pods[0])

	def main(self):
		# All PyGTK applications must have a gtk.main(). Control ends here
		# and waits for an event to occur (like a key press or mouse event).
		gtk.main()

class bluemote_pod_list(gtk.Dialog):
	def __init__(self):
		super(bluemote_pod_list, self).__init__()

		self.set_size_request(450, 250)
		self.set_position(gtk.WIN_POS_CENTER)

		self.connect("destroy", gtk.main_quit)
		self.set_title("Pod View")

		sw = gtk.ScrolledWindow()
		sw.set_shadow_type(gtk.SHADOW_ETCHED_IN)
		sw.set_policy(gtk.POLICY_AUTOMATIC, gtk.POLICY_AUTOMATIC)

		self.vbox.pack_start(sw, True, True, 0)

		self.show_all()

		store = self.create_pods()

		treeView = gtk.TreeView(store)
		treeView.connect("row-activated", self.on_activated)
		treeView.set_rules_hint(True)
		sw.add(treeView)

		self.create_columns(treeView)

		self.show_all()

	def create_pods(self):
		store = gtk.ListStore(str, str, str, str, str, str, str, str, str)

		bluetooth_devices =  bluetooth.find_service()
		for bt_dev in bluetooth_devices:
			if bt_dev["name"] == "Bluemote":
				store.append([bt_dev["name"], bt_dev["host"], bt_dev["description"], bt_dev["provider"], bt_dev["protocol"], bt_dev["port"], bt_dev["service-classes"], bt_dev["profiles"], bt_dev["service-id"]])

		return store

	def create_columns(self, treeView):
		columns = ["name", "host", "description", "provider", "protocol", "port", "service-classes", "profiles", "service-id"]

		i = 0
		for column_name in columns:
			rendererText = gtk.CellRendererText()
			column = gtk.TreeViewColumn(column_name, rendererText, text = i)
			column.set_sort_column_id(i)
			treeView.append_column(column)
			i += 1

	def on_activated(self, widget, row, col):
		model = widget.get_model()
		text = model[row][0]
		for i in range(1, 10):
			text += ", " + model[row][i]
		self.statusbar.push(0, text)

# If the program is run directly or passed as an argument to the python
# interpreter then create a Bluemote_GUI instance and show it
if __name__ == "__main__":
	gui = Bluemote_GUI()
	gui.main()
