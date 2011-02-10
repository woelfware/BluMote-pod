#!/usr/bin/env sh
# This Refdes renumber script was created by:  Project Manager v 3.9.5 - Kiwi PCB
# The Project Manager is copyright 2011, www.KiwiPCB.com
# This release is for general use.
# Created on 01/21/11 at 07:48:52   For Kenneth Hutchins  < kenneth.hutchins@gmail.com > 

cp "/home/ken/bluemote/hw/schematics/bluemote_pod.sch" "/home/ken/bluemote/hw/schematics/backup_schematics/01_21_11__07_48_52/"

refdes_renum --verbose  --pgskip  --force "/home/ken/bluemote/hw/schematics/bluemote_pod.sch"  >> "/home/ken/bluemote/hw/project_scripts/Refdes_Renumber_Schematics_results_bluemote_pod.txt"

/usr/bin/gedit "/home/ken/bluemote/hw/project_scripts/Refdes_Renumber_Schematics_results_bluemote_pod.txt"
