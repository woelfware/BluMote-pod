#!/usr/bin/env python
# Copyright (c) 2011 Woelfware

import sys, os
import time
from threading import Thread
import gobject, gtk
from bluemote_remote import *
import bluetooth
import bluemote

gobject.threads_init()

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

		connect_menu = gtk.Menu()
		connect = gtk.MenuItem("_Connect")
		connect.set_submenu(connect_menu)
		bluemote_menu.append(connect)

		self.connect_last = gtk.MenuItem("<fixme: show last connection>")
		connect_menu.append(self.connect_last)
		self.connect_new = gtk.MenuItem("New")
		connect_menu.append(self.connect_new)

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

class connect_to_pod_thread(Thread):
	def __init__(self, window, bluemote_client):
		super(connect_to_pod_thread, self).__init__()

		self.window = window
		self.bm_client = bluemote_client
		self.start()

	def display_pods(self):
		store = self.__create_pod_store()
		self.__update_gui(store)

	def __update_gui(self, store):
		self.window.label.destroy()

		self.window.set_size_request(450, 150)

		self.window.vbox = gtk.VBox(False, 8)

		sw = gtk.ScrolledWindow()
		sw.set_shadow_type(gtk.SHADOW_ETCHED_IN)
		sw.set_policy(gtk.POLICY_AUTOMATIC, gtk.POLICY_AUTOMATIC)

		self.window.vbox.pack_start(sw, True, True, 0)

		treeView = gtk.TreeView(store)
		treeView.connect("row-activated", self.on_activated)
		treeView.set_rules_hint(True)
		sw.add(treeView)

		self.__create_columns(treeView)

		self.window.add(self.window.vbox)
		self.window.show_all()

	def on_activated(self, widget, row, col):
		model = widget.get_model()
		for pod in self.pods:
			if pod["host"] == model[row][3] \
				and pod["port"] == int(model[row][4]):
				print "connecting to:", pod["host"], pod["port"]
				self.bm_client.connect_to_bluemote_pod(pod)
				print "connected"
				self.window.destroy()
				break

	def __create_columns(self, treeView):
		columns = ["Name", "Description", "Provider", "Host", "Port"]

		i = 0
		for column_name in columns:
			rendererText = gtk.CellRendererText()
			column = gtk.TreeViewColumn(column_name, rendererText, text = i)
			column.set_sort_column_id(i)
			treeView.append_column(column)
			i += 1
		
	def __create_pod_store(self):
		store = gtk.ListStore(str, str, str, str, str)

		bluetooth_devices =  bluetooth.find_service()
		self.pods = []

		for bt_dev in bluetooth_devices:
			if bt_dev["provider"] == bluemote.Services().service["provider"] \
				and bt_dev["description"] == bluemote.Services().service["description"]:
				store.append([bt_dev["name"],
						bt_dev["description"],
						bt_dev["provider"],
						bt_dev["host"],
						bt_dev["port"]])
				self.pods.append(bt_dev)

		return store

	def run(self):
		gobject.idle_add(self.display_pods)

class button_cb_thread(Thread):
	def __init__(self, statusbar, digit):
		super(button_cb_thread, self).__init__()

		self.statusbar = statusbar
		self.digit = digit
		self.start()

	def send_digit(self, statusbar, digit):
		statusbar.pop(0)
		statusbar.push(0, "Got button press %s" % digit)

	def run(self):
		gobject.idle_add(self.send_digit, self.statusbar, self.digit)

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
		gtk.main_quit()

	def __init__(self):
		super(Bluemote_GUI, self).__init__()

		self.bm_client = Bluemote_Client()

		self.set_resizable(False)
		self.set_border_width(10)
		self.set_icon_from_file(sys.path[0] + "/data/bluemote_Icon72.png")
		self.set_title("Bluemote")
		self.set_position(gtk.WIN_POS_CENTER)

		menu = Menu(self)
		menu.connect_new.connect("activate", self.connect_to_pod)

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
		button_cb_thread(self.statusbar, widget.get_label())

	def button_pwr_cb(self, widget, data = None):
		self.statusbar.pop(0)
		self.statusbar.push(0, "Got pwr button press")

	def button_play_cb(self, widget, data = None):
		print "got play button press"

	def connect_to_pod(self, widget, data = None):
		window = self.__create_connect_window()
		connect_to_pod_thread(window, self.bm_client)

	def __create_connect_window(self):
		window = gtk.Window()
		window.set_position(gtk.WIN_POS_CENTER)
		window.set_resizable(False)
		window.set_title("Pod View")
		window.set_icon_from_file(sys.path[0] + "/data/tango-icon-theme-0.8.90/scalable/devices/network-wireless.svg")
		window.set_modal(True)

		window.label = gtk.Label("Searching for Bluemote pods")
		window.add(window.label)

		window.show_all()
		return window

if __name__ == "__main__":
	gui = Bluemote_GUI()
	gtk.main()
