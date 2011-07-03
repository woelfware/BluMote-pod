v 20110115 2
C 56750 46700 1 0 0 msp430x22x2_da.sym
{
T 57850 52400 5 8 1 1 0 4 1
refdes=U201
T 58650 52400 5 8 1 1 0 1 1
device=MSP4302232
T 57850 53800 5 8 0 0 0 1 1
value=none
T 57850 54200 5 8 0 0 0 1 1
footprint=texas_instruments_tssop_38pin.fp
T 57850 55000 5 8 0 0 0 1 1
symversion=1.0
}
C 49150 48100 1 0 0 rn42.sym
{
T 50250 52400 5 8 1 1 0 4 1
refdes=U202
T 52050 52400 5 8 1 1 0 1 1
device=RN_42
T 50250 53800 5 8 0 0 0 1 1
value=none
T 50250 54200 5 8 0 0 0 1 1
footprint=roving_networks_rn42.fp
T 50250 55000 5 8 0 0 0 1 1
symversion=1.0
}
N 59650 51600 60550 51600 4
{
T 60650 51550 5 8 1 1 0 0 1
netname=IR_OUT2
}
C 7200 2300 0 0 0 title_8_5x11_full.sym
{
T 47250 54850 15 8 0 0 0 0 1
device=none
}
C 50650 48350 1 0 0 gnd.sym
{
T 50950 50050 5 8 0 0 0 1 1
device=net_symbol
T 50950 50450 5 8 0 0 0 1 1
footprint=none
T 50950 51250 5 8 0 0 0 1 1
symversion=1.1
T 50650 48350 5 8 0 0 0 0 1
netname=GND
}
N 50750 48550 50750 48900 4
N 48800 48700 50750 48700 4
N 50950 48700 50950 48900 4
N 48800 48700 48800 52000 4
N 48800 49800 49950 49800 4
N 49950 52000 48800 52000 4
N 52950 50000 54200 50000 4
N 54200 50000 54200 47400 4
N 54200 47400 61550 47400 4
N 61550 47400 61550 49600 4
N 61550 49600 59650 49600 4
{
T 60650 49600 5 8 1 1 0 0 1
netname=P3.5
}
N 52950 49800 54000 49800 4
N 54000 49800 54000 47600 4
N 54000 47600 61350 47600 4
N 61350 47600 61350 49400 4
N 61350 49400 59650 49400 4
{
T 60650 49400 5 8 1 1 0 0 1
netname=P3.4
}
N 49950 50000 47800 50000 4
N 47800 49800 47800 50400 4
C 47700 50400 1 0 0 3_3V.sym
{
T 48000 52100 5 8 0 0 0 1 1
device=net_symbol
T 48000 52500 5 8 0 0 0 1 1
footprint=none
T 48000 53300 5 8 0 0 0 1 1
symversion=1.1
T 47700 50400 5 8 0 1 0 0 1
netname=3.3V
}
N 49950 51200 48500 51200 4
{
T 48000 51150 5 8 1 1 0 0 1
netname=Reset
}
N 49950 51400 48500 51400 4
{
T 47650 51350 5 8 1 1 0 0 1
netname=Baud_Rate
}
C 57050 47700 1 0 0 gnd.sym
{
T 57350 49400 5 8 0 0 0 1 1
device=net_symbol
T 57350 49800 5 8 0 0 0 1 1
footprint=none
T 57350 50600 5 8 0 0 0 1 1
symversion=1.1
T 57050 47700 5 8 0 0 0 0 1
netname=GND
}
N 57550 51400 57150 51400 4
N 57150 51400 57150 47900 4
N 57550 49200 57150 49200 4
N 59650 51200 60550 51200 4
{
T 60650 51150 5 8 1 1 0 0 1
netname=IR_IN
}
N 59650 51400 60550 51400 4
{
T 60650 51350 5 8 1 1 0 0 1
netname=IR_OUT1
}
N 52950 51000 54400 51000 4
N 54400 51000 54400 47200 4
N 54400 47200 61750 47200 4
N 61750 47200 61750 49800 4
N 61750 49800 59650 49800 4
{
T 60650 49800 5 8 1 1 0 0 1
netname=P3.6
}
N 52950 51200 54650 51200 4
N 54650 51200 54650 49400 4
N 54650 49400 57550 49400 4
{
T 56200 49400 5 8 1 1 0 0 1
netname=P3.3
}
N 52950 51600 54900 51600 4
N 54900 51600 54900 49600 4
N 54900 49600 57550 49600 4
{
T 56200 49600 5 8 1 1 0 0 1
netname=P3.2
}
N 51350 46950 61950 46950 4
N 61950 46950 61950 50600 4
N 61950 50600 59650 50600 4
{
T 60650 50600 5 8 1 1 0 0 1
netname=P1.0
}
N 51350 46950 51350 48900 4
N 57550 49800 56650 49800 4
{
T 55800 49750 5 8 1 1 0 0 1
netname=Baud_Rate
}
N 57550 50000 56650 50000 4
{
T 56150 49950 5 8 1 1 0 0 1
netname=Reset
}
N 57550 49000 57400 49000 4
N 57400 49000 57400 53700 4
N 57400 51800 57550 51800 4
C 56300 53700 1 0 0 3_3V.sym
{
T 56600 55400 5 8 0 0 0 1 1
device=net_symbol
T 56600 55800 5 8 0 0 0 1 1
footprint=none
T 56600 56600 5 8 0 0 0 1 1
symversion=1.1
T 56300 53700 5 8 0 1 0 0 1
netname=3.3V
}
C 56300 53050 1 0 0 capacitor.sym
{
T 56600 53450 5 8 1 1 0 4 1
refdes=C201
T 55900 55350 5 8 0 0 0 1 1
device=device
T 56500 53150 5 8 1 1 0 1 1
value=0.1uF
T 55900 55950 5 8 0 0 0 1 1
footprint=cap_0805.fp
T 55900 56750 5 8 0 0 0 1 1
symversion=1.0
}
C 56300 52650 1 0 0 gnd.sym
{
T 56600 54350 5 8 0 0 0 1 1
device=net_symbol
T 56600 54750 5 8 0 0 0 1 1
footprint=none
T 56600 55550 5 8 0 0 0 1 1
symversion=1.1
T 56300 52650 5 8 0 0 0 0 1
netname=GND
}
N 55800 53700 57400 53700 4
N 56400 53050 56400 52850 4
C 47700 49300 1 0 0 capacitor.sym
{
T 48000 49700 5 8 1 1 0 4 1
refdes=C202
T 47300 51600 5 8 0 0 0 1 1
device=device
T 47900 49400 5 8 1 1 0 1 1
value=0.1uF
T 47300 52200 5 8 0 0 0 1 1
footprint=cap_0805.fp
T 47300 53000 5 8 0 0 0 1 1
symversion=1.0
}
C 47700 48900 1 0 0 gnd.sym
{
T 48000 50600 5 8 0 0 0 1 1
device=net_symbol
T 48000 51000 5 8 0 0 0 1 1
footprint=none
T 48000 51800 5 8 0 0 0 1 1
symversion=1.1
T 47700 48900 5 8 0 0 0 0 1
netname=GND
}
N 47800 49300 47800 49100 4
C 62900 52400 1 0 1 sbw_programming_header.sym
{
T 62800 53900 5 8 1 1 0 7 1
refdes=J201
T 62900 52300 5 8 0 1 0 7 1
device=Programming Header
T 63000 55400 5 8 0 0 0 7 1
value=none
T 63000 55800 5 8 0 0 0 7 1
footprint=header_1x6_pitch_0.100.fp
T 63000 56600 5 8 0 0 0 7 1
symversion=1.1
}
N 59650 51000 61600 51000 4
{
T 60650 51000 5 8 1 1 0 0 1
netname=P1.2
}
N 61600 51000 61600 52600 4
N 61400 53600 61900 53600 4
N 61400 53600 61400 50800 4
N 61400 50800 59650 50800 4
{
T 60650 50800 5 8 1 1 0 0 1
netname=P1.1
}
N 57150 52000 57150 53200 4
N 57150 52000 57550 52000 4
N 57150 53200 61900 53200 4
N 61900 53000 56900 53000 4
N 56900 50800 56900 53000 4
N 55800 50800 57550 50800 4
C 61700 53700 1 0 0 3_3V.sym
{
T 62000 55400 5 8 0 0 0 1 1
device=net_symbol
T 62000 55800 5 8 0 0 0 1 1
footprint=none
T 62000 56600 5 8 0 0 0 1 1
symversion=1.1
T 61700 53700 5 8 0 1 0 0 1
netname=3.3V
}
N 61900 53400 61800 53400 4
N 61800 53400 61800 53700 4
C 61700 52100 1 0 0 gnd.sym
{
T 62000 53800 5 8 0 0 0 1 1
device=net_symbol
T 62000 54200 5 8 0 0 0 1 1
footprint=none
T 62000 55000 5 8 0 0 0 1 1
symversion=1.1
T 61700 52100 5 8 0 0 0 0 1
netname=GND
}
N 61800 52300 61800 52800 4
N 61900 52600 61600 52600 4
N 61800 52800 61900 52800 4
N 56400 53550 56400 53700 4
N 52950 50200 54200 50200 4
N 52950 50400 54200 50400 4
N 50950 48700 50750 48700 4
N 54200 50200 54200 50400 4
C 51000 51700 1 90 0 resistor.sym
{
T 49525 53250 5 8 1 1 180 4 1
refdes=R201
T 47700 52800 5 8 0 0 90 1 1
device=resistor
T 49600 52750 5 8 1 1 180 1 1
value=1K
T 47100 52800 5 8 0 0 90 1 1
footprint=0805_resistor.fp
T 46300 52800 5 8 0 0 90 1 1
symversion=1.1
}
C 49200 53700 1 0 0 3_3V.sym
{
T 49500 55400 5 8 0 0 0 1 1
device=net_symbol
T 49500 55800 5 8 0 0 0 1 1
footprint=none
T 49500 56600 5 8 0 0 0 1 1
symversion=1.1
T 49200 53700 5 8 0 1 0 0 1
netname=3.3V
}
N 49300 51600 49300 52500 4
N 49950 51600 49300 51600 4
N 49300 53500 49300 53700 4
T 59900 42800 9 8 1 0 0 0 1
2 of 2
T 56500 43100 9 8 1 0 0 0 1
BluMote
T 56500 42800 9 8 1 0 0 0 1
bluemote_pod_p2.sch
T 56500 42500 9 8 1 0 0 0 1
KH
T 61600 42500 9 8 1 0 0 0 1
02/27/2011
C 57500 51000 1 90 0 resistor.sym
{
T 56125 52550 5 8 1 1 180 4 1
refdes=R202
T 54200 52100 5 8 0 0 90 1 1
device=resistor
T 56200 52050 5 8 1 1 180 1 1
value=4.7K
T 53600 52100 5 8 0 0 90 1 1
footprint=0805_resistor.fp
T 52800 52100 5 8 0 0 90 1 1
symversion=1.1
}
N 55800 51800 55800 50800 4
N 55800 52800 55800 53700 4
T 49200 42500 9 8 1 0 0 0 5
Removed JP201-JP203
Shorted U202 pins 15 and 16
Removed J202
Removed Unused nets
Added R202
T 61500 43100 9 8 1 0 0 0 1
Version 0.2


