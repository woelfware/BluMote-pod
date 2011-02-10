#!/usr/bin/env sh
# This symbol checker script was created by:  Symbol Maker v 3.9.5 - Kiwi PCB
# The Symbol Maker is copyright 2011, www.KiwiPCB.com
# This release is for general use.
# Created on 01/30/11 at 21:13:14   For Kenneth Hutchins  ( kenneth.hutchins@gmail.com ) personal use

echo "This standard symbol design rule check was created by:  Symbol Maker v 3.9.5 - Kiwi PCB" > /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results.txt
echo "The Symbol Maker is copyright 2011, www.KiwiPCB.com" >> /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results.txt
echo "This release is for general use." >> /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results.txt
echo "Created on 01/30/11 at 21:13:14   For Kenneth Hutchins  ( kenneth.hutchins@gmail.com ) personal use" >> /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results.txt
echo "" >> /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results.txt

echo "This enhanced symbol design rule check was created by:  Symbol Maker v 3.9.5 - Kiwi PCB" > /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results_verbose.txt
echo "The Symbol Maker is copyright 2011, www.KiwiPCB.com" >> /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results_verbose.txt
echo "This release is for general use." >> /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results.txt
echo "Created on 01/30/11 at 21:13:14   For Kenneth Hutchins  ( kenneth.hutchins@gmail.com ) personal use" >> /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results_verbose.txt
echo "" >> /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results_verbose.txt

/usr/bin/gsymcheck -v /home/ken/bluemote/hw/project_symbols/all_new_symbols/usb_micro_b.sym >> /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results.txt
/usr/bin/gsymcheck -vv /home/ken/bluemote/hw/project_symbols/all_new_symbols/usb_micro_b.sym >> /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results_verbose.txt
/usr/bin/gedit /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results.txt /home/ken/bluemote/hw/project_symbols/all_new_symbols/backups/gsymcheck_results_verbose.txt
