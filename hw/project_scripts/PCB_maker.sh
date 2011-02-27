#!/usr/bin/env sh
# This PCB generation script was created by:  Project Manager v 4.0.0 - Circuit Dashboard
# The Project Manager is copyright 2011, www.CircuitDashboard.com
# This release is for general use.
# Created on 02/26/11 at 22:18:15   For Kenneth Hutchins  < kenneth.hutchins@gmail.com > 

mv "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm" "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm.tmp"
cp  "/home/ken/.CircuitDashboard/pcb_layers/Double_Sided_PCB.scm" "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm"

mkdir -p "/home/ken/bluemote/hw/pcbs/backup_pcbs/02_26_11__22_18_15"
cp "/home/ken/bluemote/hw/pcbs/bluemote_pod.pcb" "/home/ken/bluemote/hw/pcbs/backup_pcbs/02_26_11__22_18_15"
mv "/home/ken/bluemote/hw/pcbs/bluemote_pod.pcb-" "/home/ken/bluemote/hw/pcbs/backup_pcbs/02_26_11__22_18_15"
mv "/home/ken/bluemote/hw/pcbs/bluemote_pod.pcb.new.pcb" "/home/ken/bluemote/hw/pcbs/backup_pcbs/02_26_11__22_18_15"
mv "/home/ken/bluemote/hw/pcbs/bluemote_pod.pcb.net" "/home/ken/bluemote/hw/pcbs/backup_pcbs/02_26_11__22_18_15"
mv "/home/ken/bluemote/hw/pcbs/bluemote_pod.pcb.cmd" "/home/ken/bluemote/hw/pcbs/backup_pcbs/02_26_11__22_18_15"
mv "/home/ken/bluemote/hw/pcbs/bluemote_pod.pcb.pcb.bak" "/home/ken/bluemote/hw/pcbs/backup_pcbs/02_26_11__22_18_15"

/usr/bin/gsch2pcb -v -s -q "/home/ken/bluemote/hw/schematics/bluemote_pod.sch" "/home/ken/bluemote/hw/schematics/bluemote_pod_p2.sch"  -d "/home/ken/bluemote/hw/project_footprints" -o "/home/ken/bluemote/hw/pcbs/bluemote_pod" >> "/home/ken/bluemote/hw/project_documentation/PCB_creation_for_bluemote_pod.pcb.txt"

/bin/mv -f "/home/ken/.gEDA/gafrc_orig_hold" "/home/ken/.gEDA/gafrc"
/usr/bin/gedit "/home/ken/bluemote/hw/project_documentation/PCB_creation_for_bluemote_pod.pcb.txt"

mv "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm.tmp" "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm"

