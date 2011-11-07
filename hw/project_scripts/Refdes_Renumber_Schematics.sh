#!/usr/bin/env sh
# This Refdes renumber script was created by:  Project Manager v 4.0.0 - Circuit Dashboard
# The Project Manager is copyright 2011 by Andrew Miner, www.CircuitDashboard.com
# This release is for general use.
# Created on 11/06/11 at 19:00:58   For Kenneth Hutchins  < kenneth.hutchins@gmail.com > 

cp "/home/ken/Workspace/blumote/hw/schematics/bluemote_pod.sch" "/home/ken/Workspace/blumote/hw/schematics/backup_schematics/11_06_11__19_00_58/"
cp "/home/ken/Workspace/blumote/hw/schematics/bluemote_pod_p2.sch" "/home/ken/Workspace/blumote/hw/schematics/backup_schematics/11_06_11__19_00_58/"

refdes_renum --verbose  --pgskip "/home/ken/Workspace/blumote/hw/schematics/bluemote_pod.sch" "/home/ken/Workspace/blumote/hw/schematics/bluemote_pod_p2.sch"  >> "/home/ken/Workspace/blumote/hw/project_scripts/Refdes_Renumber_Schematics_results_bluemote_pod.txt"

/usr/bin/gedit "/home/ken/Workspace/blumote/hw/project_scripts/Refdes_Renumber_Schematics_results_bluemote_pod.txt"
