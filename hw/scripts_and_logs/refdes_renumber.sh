#!/usr/bin/env sh
#This is an auto generated Refdes Renumber Maker file for use with Project Generator.
#Project Generator v 3.9.3 - Kiwi PCB
#Copyright 2010, www.KiwiPCB.com
#This release is for general use.
# Created on 12/26/10 at 11:36:55

cp "/home/ken/Documents/woelfware/bluemote/hardware_pod/schematics/bluemote_pod.sch" "/home/ken/Documents/woelfware/bluemote/hardware_pod/schematics/backup_schematics/12_26_10__11_36_55/"

refdes_renum --verbose  --pgskip  --force "/home/ken/Documents/woelfware/bluemote/hardware_pod/schematics/bluemote_pod.sch"  >> "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/refdes_renumber_results_bluemote_pod.txt"

/usr/bin/gedit "/home/ken/Documents/woelfware/bluemote/hardware_pod/scripts_and_logs/refdes_renumber_results_bluemote_pod.txt"
