#!/usr/bin/env sh
#This is an auto generated DRC Maker file for use with Project Generator.
#Project Generator v 3.9.3 - Kiwi PCB
#Copyright 2010, www.KiwiPCB.com
#This release is for general use.
# Created on 12/26/10 at 11:37:52

/usr/bin/gnetlist -g drc2  -l "/home/ken/Documents/woelfware/bluemote/hardware_pod/local_gafrc"    "/home/ken/Documents/woelfware/bluemote/hardware_pod/schematics/bluemote_pod.sch"  -o "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/tmp_results_single_bluemote_pod.txt" -v >> "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/tmp_results_verbose_single_bluemote_pod.txt"

cat "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/drc_results_single_bluemote_pod.txt" "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/tmp_results_single_bluemote_pod.txt" >> "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/drc_results_single_bluemote_pod.txt"

cat "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/drc_results_verbose_single_bluemote_pod.txt" "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/tmp_results_verbose_single_bluemote_pod.txt" >> "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/drc_results_verbose_single_bluemote_pod.txt"

rm -f "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/tmp_results_single_bluemote_pod.txt" "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/tmp_results_verbose_single_bluemote_pod.txt"

/usr/bin/gedit "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/drc_results_single_bluemote_pod.txt" "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/drc_results_verbose_single_bluemote_pod.txt"
