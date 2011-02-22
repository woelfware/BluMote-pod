#!/usr/bin/env sh
# This Refdes renumber script was created by:  Project Manager v 4.0.0 - Circuit Dashboard
# The Project Manager is copyright 2011, www.CircuitDashboard.com
# This release is for general use.
# Created on 02/20/11 at 16:18:51   For Kenneth Hutchins  < kenneth.hutchins@gmail.com > 

cp "/home/ken/bluemote/hw/schematics/bluemote_pod.sch" "/home/ken/bluemote/hw/schematics/backup_schematics/02_20_11__16_18_51/"
cp "/home/ken/bluemote/hw/schematics/bluemote_pod_p2.sch" "/home/ken/bluemote/hw/schematics/backup_schematics/02_20_11__16_18_51/"

refdes_renum --verbose  --pgskip  --force "/home/ken/bluemote/hw/schematics/bluemote_pod.sch" "/home/ken/bluemote/hw/schematics/bluemote_pod_p2.sch"  >> "/home/ken/bluemote/hw/project_scripts/Refdes_Renumber_Schematics_results_bluemote_pod.txt"

/usr/bin/gedit "/home/ken/bluemote/hw/project_scripts/Refdes_Renumber_Schematics_results_bluemote_pod.txt"
