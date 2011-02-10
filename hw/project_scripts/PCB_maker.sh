#!/usr/bin/env sh
# This PCB generation script was created by:  Project Manager v 3.9.5 - Kiwi PCB
# The Project Manager is copyright 2011, www.KiwiPCB.com
# This release is for general use.
# Created on 01/21/11 at 07:43:09   For Kenneth Hutchins  < kenneth.hutchins@gmail.com > 

mv "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm" "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm.tmp"
cp  "/usr/share/kiwi_pcb/pcb_layers/Double_Sided_PCB.scm" "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm"

mkdir -p "/home/ken/bluemote/hw/pcbs/backup_pcbs/01_21_11__07_43_09"
mv "/home/ken/bluemote/hw/pcbs/bluemote_pod.pcb" "/home/ken/bluemote/hw/pcbs/backup_pcbs/01_21_11__07_43_09"
mv "/home/ken/bluemote/hw/pcbs/bluemote_pod.pcb-" "/home/ken/bluemote/hw/pcbs/backup_pcbs/01_21_11__07_43_09"
mv "/home/ken/bluemote/hw/pcbs/bluemote_pod.pcb.new.pcb" "/home/ken/bluemote/hw/pcbs/backup_pcbs/01_21_11__07_43_09"
mv "/home/ken/bluemote/hw/pcbs/bluemote_pod.pcb.net" "/home/ken/bluemote/hw/pcbs/backup_pcbs/01_21_11__07_43_09"
mv "/home/ken/bluemote/hw/pcbs/bluemote_pod.pcb.cmd" "/home/ken/bluemote/hw/pcbs/backup_pcbs/01_21_11__07_43_09"
mv "/home/ken/bluemote/hw/pcbs/bluemote_pod.pcb.pcb.bak" "/home/ken/bluemote/hw/pcbs/backup_pcbs/01_21_11__07_43_09"

/usr/bin/gsch2pcb -v -s -q "/home/ken/bluemote/hw/schematics/bluemote_pod.sch"  -d "/home/ken/bluemote/hw/project_footprints" -o "/home/ken/bluemote/hw/pcbs/bluemote_pod" >> "/home/ken/bluemote/hw/project_documentation/PCB_creation_for_bluemote_pod.pcb.txt"

/bin/mv -f "/home/ken/.gEDA/gafrc_orig_hold" "/home/ken/.gEDA/gafrc"
/usr/bin/gedit "/home/ken/bluemote/hw/project_documentation/PCB_creation_for_bluemote_pod.pcb.txt"

mv "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm.tmp" "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm"
