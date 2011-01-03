#!/usr/bin/env sh
#This is an auto generated BOM Maker file for use with Project Generator.
#Project Generator v 3.9.3 - Kiwi PCB
#Copyright 2010, www.KiwiPCB.com
#This release is for general use.
# Created on 12/26/10 at 11:37:08

/usr/bin/gnetlist -g partslist1 -l "/home/ken/Documents/woelfware/bluemote/hardware_pod/local_gafrc"    "/home/ken/Documents/woelfware/bluemote/hardware_pod/schematics/bluemote_pod.sch"  -o "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/tmp_sequential_single_bluemote_pod.txt"

/usr/bin/gnetlist -g partslist3 -l "/home/ken/Documents/woelfware/bluemote/hardware_pod/local_gafrc"   "/home/ken/Documents/woelfware/bluemote/hardware_pod/schematics/bluemote_pod.sch"  -o "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/tmp_grouped_single_bluemote_pod.txt"

cat "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/bom_sequential_single_bluemote_pod.txt" "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/tmp_sequential_single_bluemote_pod.txt" >> "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/bom_sequential_single_bluemote_pod.txt"

cat "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/bom_grouped_single_bluemote_pod.txt" "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/tmp_grouped_single_bluemote_pod.txt" >> "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/bom_grouped_single_bluemote_pod.txt"

rm -f "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/tmp_sequential_single_bluemote_pod.txt" "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/tmp_grouped_single_bluemote_pod.txt"

/usr/bin/gnumeric "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/bom_sequential_single_bluemote_pod.txt" "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/bom_grouped_single_bluemote_pod.txt"
