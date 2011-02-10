#!/usr/bin/env sh
# This BOM generation script was created by:  Project Manager v 3.9.5 - Kiwi PCB
# The Project Manager is copyright 2011, www.KiwiPCB.com
# This release is for general use.
# Created on 01/21/11 at 07:41:15   For Kenneth Hutchins  < kenneth.hutchins@gmail.com > 

/usr/bin/gnetlist -g partslist1 -l "/home/ken/bluemote/hw/local_gafrc" "/home/ken/bluemote/hw/schematics/bluemote_pod.sch"  -o "/home/ken/bluemote/hw/project_scripts/tmp_sequential_bluemote_pod.pcb.txt"

/usr/bin/gnetlist -g partslist3 -l "/home/ken/bluemote/hw/local_gafrc" "/home/ken/bluemote/hw/schematics/bluemote_pod.sch"  -o "/home/ken/bluemote/hw/project_scripts/tmp_grouped_bluemote_pod.pcb.txt"

cat "/home/ken/bluemote/hw/project_documentation//bom_sequential_bluemote_pod.pcb.csv" "/home/ken/bluemote/hw/project_scripts/tmp_sequential_bluemote_pod.pcb.txt" >> "/home/ken/bluemote/hw/project_documentation//bom_sequential_bluemote_pod.pcb.csv"

cat "/home/ken/bluemote/hw/project_documentation//bom_grouped_bluemote_pod.pcb.csv" "/home/ken/bluemote/hw/project_scripts/tmp_grouped_bluemote_pod.pcb.txt" >> "/home/ken/bluemote/hw/project_documentation//bom_grouped_bluemote_pod.pcb.csv"

rm -f "/home/ken/bluemote/hw/project_scripts/tmp_sequential_bluemote_pod.pcb.txt" "/home/ken/bluemote/hw/project_scripts/tmp_grouped_bluemote_pod.pcb.txt"

/usr/bin/gnumeric "/home/ken/bluemote/hw/project_documentation//bom_sequential_bluemote_pod.pcb.csv" "/home/ken/bluemote/hw/project_documentation//bom_grouped_bluemote_pod.pcb.csv"
