#!/usr/bin/env sh
# This PCB generation script was created by:  Project Manager v 4.0.0 - Circuit Dashboard
# The Project Manager is copyright 2011 by Andrew Miner, www.CircuitDashboard.com
# This release is for general use.
# Created on 11/14/11 at 22:02:40   For Kenneth Hutchins  < kenneth.hutchins@gmail.com > 

mv "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm" "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm.tmp"
cp  "/home/ken/.CircuitDashboard/pcb_layers/Double_Sided_PCB.scm" "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm"

mkdir -p "/home/ken/Workspace/blumote/hw/pcbs/backup_pcbs/11_14_11__22_02_40"
cp "/home/ken/Workspace/blumote/hw/pcbs/bluemote_pod.pcb" "/home/ken/Workspace/blumote/hw/pcbs/backup_pcbs/11_14_11__22_02_40"
mv "/home/ken/Workspace/blumote/hw/pcbs/bluemote_pod.pcb-" "/home/ken/Workspace/blumote/hw/pcbs/backup_pcbs/11_14_11__22_02_40"
mv "/home/ken/Workspace/blumote/hw/pcbs/bluemote_pod.pcb.new.pcb" "/home/ken/Workspace/blumote/hw/pcbs/backup_pcbs/11_14_11__22_02_40"
mv "/home/ken/Workspace/blumote/hw/pcbs/bluemote_pod.pcb.net" "/home/ken/Workspace/blumote/hw/pcbs/backup_pcbs/11_14_11__22_02_40"
mv "/home/ken/Workspace/blumote/hw/pcbs/bluemote_pod.pcb.cmd" "/home/ken/Workspace/blumote/hw/pcbs/backup_pcbs/11_14_11__22_02_40"
mv "/home/ken/Workspace/blumote/hw/pcbs/bluemote_pod.pcb.pcb.bak" "/home/ken/Workspace/blumote/hw/pcbs/backup_pcbs/11_14_11__22_02_40"

/usr/bin/gsch2pcb -v -s -q "/home/ken/Workspace/blumote/hw/schematics/bluemote_pod.sch" "/home/ken/Workspace/blumote/hw/schematics/bluemote_pod_p2.sch"  -d "/home/ken/Workspace/blumote/hw/project_footprints" -o "/home/ken/Workspace/blumote/hw/pcbs/bluemote_pod" >> "/home/ken/Workspace/blumote/hw/project_documentation/PCB_creation_for_bluemote_pod.pcb.txt"

/bin/mv -f "/home/ken/.gEDA/gafrc_orig_hold" "/home/ken/.gEDA/gafrc"
/usr/bin/gedit "/home/ken/Workspace/blumote/hw/project_documentation/PCB_creation_for_bluemote_pod.pcb.txt"

mv "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm.tmp" "/usr/share/gEDA/scheme/gnet-gsch2pcb.scm"

