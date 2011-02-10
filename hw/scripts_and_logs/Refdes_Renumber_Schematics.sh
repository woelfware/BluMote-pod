#!/usr/bin/env sh
#This is an auto generated Refdes Renumber Maker file for use with Project Generator.
#Project Generator v 3.9.3 - Kiwi PCB
#Copyright 2010, www.KiwiPCB.com
#This release is for general use.
# Created on 01/11/11 at 22:33:50

cp "/home/ken/bluemote/hw/schematics/bluemote_pod.sch" "/home/ken/bluemote/hw/schematics/backup_schematics/01_11_11__22_33_50/"

refdes_renum --verbose  --pgskip "/home/ken/bluemote/hw/schematics/bluemote_pod.sch"  >> "/home/ken/bluemote/hw/scripts_and_logs/Refdes_Renumber_Schematics_results_bluemote_pod.txt"

/usr/bin/gedit "/home/ken/bluemote/hw/scripts_and_logs/Refdes_Renumber_Schematics_results_bluemote_pod.txt"
