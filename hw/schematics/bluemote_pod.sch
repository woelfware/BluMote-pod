v 20110115 2
C 49300 42800 1 0 1 pna4612m.sym
{
T 48300 45200 5 8 1 1 0 4 1
refdes=U101
T 48200 46500 5 8 0 0 0 7 1
device=device
T 48200 46700 5 8 0 0 0 7 1
value=none
T 48200 47100 5 8 0 0 0 7 1
footprint=pna4612m.fp
T 48200 47900 5 8 0 0 0 7 1
symversion=1.0
}
N 48500 44700 48900 44700 4
N 48900 44700 48900 44100 4
N 48500 44500 49100 44500 4
N 49100 44500 49100 45400 4
N 48500 44900 50200 44900 4
{
T 49700 44900 5 8 1 1 0 0 1
netname=IR_IN
}
C 56900 42800 1 0 0 resistor.sym
{
T 58025 44700 5 8 1 1 0 4 1
refdes=R103
T 58000 46100 5 8 0 0 0 1 1
device=resistor
T 58400 44700 5 8 1 1 0 1 1
value=100
T 58000 46700 5 8 0 0 0 1 1
footprint=0805_resistor.fp
T 58000 47500 5 8 0 0 0 1 1
symversion=1.1
}
N 59400 43800 59400 44100 4
N 56800 44500 57700 44500 4
{
T 57200 44300 5 8 1 1 0 0 1
netname=IR_OUT2
}
C 59000 44100 1 0 0 2N7002.sym
{
T 59625 44725 5 8 1 1 0 4 1
refdes=Q102
T 59300 46100 5 8 0 0 0 1 1
device=NMOS
T 59300 46300 5 8 0 0 0 1 1
value=none
T 59300 46700 5 8 0 0 0 1 1
footprint=sot_23.fp
T 59300 47500 5 8 0 0 0 1 1
symversion=1.1
}
N 59400 44900 59400 48000 4
C 7200 2300 0 0 0 title_8_5x11_full.sym
{
T 47250 54850 15 8 0 0 0 0 1
device=none
}
C 51600 52300 1 0 1 usb_micro_b.sym
{
T 51500 53600 5 8 1 1 0 7 1
refdes=J101
T 51300 54900 5 8 0 0 0 7 1
device=device
T 51300 55100 5 8 0 0 0 7 1
value=none
T 51300 55500 5 8 0 0 0 7 1
footprint=tyco_1981568_usb_micro_b.fp
T 51300 56300 5 8 0 0 0 7 1
symversion=1.1
}
C 63800 43000 1 0 1 audio_mono.sym
{
T 62200 45600 5 8 1 1 0 7 1
refdes=J102
T 62700 46700 5 8 0 0 0 7 1
device=device
T 62700 46900 5 8 0 0 0 7 1
value=none
T 62700 47300 5 8 0 0 0 7 1
footprint=mj-3536ng.fp
T 62700 48100 5 8 0 0 0 7 1
symversion=1.1
}
C 63800 43900 1 0 1 audio_mono.sym
{
T 62200 46500 5 8 1 1 0 7 1
refdes=J103
T 62700 47600 5 8 0 0 0 7 1
device=device
T 62700 47800 5 8 0 0 0 7 1
value=none
T 62700 48200 5 8 0 0 0 7 1
footprint=mj-3536ng.fp
T 62700 49000 5 8 0 0 0 7 1
symversion=1.1
}
C 63800 44800 1 0 1 audio_mono.sym
{
T 62200 47400 5 8 1 1 0 7 1
refdes=J104
T 62700 48500 5 8 0 0 0 7 1
device=device
T 62700 48700 5 8 0 0 0 7 1
value=none
T 62700 49100 5 8 0 0 0 7 1
footprint=mj-3536ng.fp
T 62700 49900 5 8 0 0 0 7 1
symversion=1.1
}
N 61300 49600 61300 44900 4
N 59400 45300 60000 45300 4
N 60000 46200 59400 46200 4
N 60000 47100 59400 47100 4
N 60000 48000 59400 48000 4
N 59000 44500 58700 44500 4
B 57700 43500 5300 6600 3 0 0 0 -1 -1 0 -1 -1 -1 -1 -1
T 58200 49900 9 8 1 0 0 0 1
External IR Emitter Control
T 58200 49700 9 8 1 0 0 0 1
~20mA per IR LED
T 58200 49500 9 8 1 0 0 0 1
VF of LEDs should be less than 2.5V
T 62500 47900 9 8 1 0 90 0 1
Internal IR Emitter Control
C 51800 53600 1 0 0 5V.sym
{
T 52100 55300 5 8 0 0 0 1 1
device=net_symbol
T 52100 55700 5 8 0 0 0 1 1
footprint=none
T 52100 56500 5 8 0 0 0 1 1
symversion=1.1
T 51800 53600 5 8 0 0 0 0 1
netname=5V
}
N 51600 52500 51900 52500 4
N 51900 52500 51900 52100 4
N 51900 53600 51900 53300 4
N 51600 53300 53700 53300 4
B 47400 43500 2900 2800 3 0 0 0 -1 -1 0 -1 -1 -1 -1 -1
T 47500 46000 9 8 1 0 0 0 1
IR Reciever
C 53700 52400 1 0 0 mcp1603t.sym
{
T 54300 53600 5 8 1 1 0 1 1
refdes=U102
T 54000 54800 5 8 0 0 0 1 1
device=device
T 54000 55000 5 8 0 0 0 1 1
value=none
T 54000 55400 5 8 0 0 0 1 1
footprint=mcp1603t_tsot.fp
T 54000 56200 5 8 0 0 0 1 1
symversion=1.1
}
N 56000 53300 55800 53300 4
N 53400 53300 53400 52900 4
N 53400 52900 53700 52900 4
N 54700 52100 54700 52400 4
C 56000 53300 1 0 0 inductor.sym
{
T 56325 53500 5 8 1 1 0 4 1
refdes=L101
T 56310 54950 5 8 0 0 0 1 1
device=inductor
T 56310 53150 5 8 1 1 0 1 1
value=4.7uH
T 56310 55550 5 8 0 0 0 1 1
footprint=we_tpc_type_s.fp
T 56310 56350 5 8 0 0 0 1 1
symversion=1.1
}
C 52800 52400 1 0 0 capacitor.sym
{
T 53100 52900 5 8 1 1 0 4 1
refdes=C101
T 52400 54700 5 8 0 0 0 1 1
device=device
T 52900 52500 5 8 1 1 0 1 1
value=0.1uF
T 52400 55300 5 8 0 0 0 1 1
footprint=cap_0805.fp
T 52400 56100 5 8 0 0 0 1 1
symversion=1.0
}
N 52900 52100 52900 52400 4
N 52900 52900 52900 53300 4
C 57400 52400 1 0 0 capacitor.sym
{
T 57700 52900 5 8 1 1 0 4 1
refdes=C102
T 57000 54700 5 8 0 0 0 1 1
device=device
T 57500 52500 5 8 1 1 0 1 1
value=0.1uF
T 57000 55300 5 8 0 0 0 1 1
footprint=cap_0805.fp
T 57000 56100 5 8 0 0 0 1 1
symversion=1.0
}
N 55800 52900 57200 52900 4
N 57200 52900 57200 53300 4
N 57000 53300 58500 53300 4
N 57500 52400 57500 52100 4
N 57500 52900 57500 53300 4
N 58500 52900 58500 53600 4
B 50300 51800 9200 2300 3 0 0 0 -1 -1 0 -1 -1 -1 -1 -1
T 55200 52100 9 8 1 0 0 0 2
Incoming voltage (5V) from USB
converted to 3.3V.
C 49000 45400 1 0 0 3_3V.sym
{
T 49300 47100 5 8 0 0 0 1 1
device=net_symbol
T 49300 47500 5 8 0 0 0 1 1
footprint=none
T 49300 48300 5 8 0 0 0 1 1
symversion=1.1
T 49000 45400 5 8 0 1 0 0 1
netname=3.3V
}
C 61200 49600 1 0 0 3_3V.sym
{
T 61500 51300 5 8 0 0 0 1 1
device=net_symbol
T 61500 51700 5 8 0 0 0 1 1
footprint=none
T 61500 52500 5 8 0 0 0 1 1
symversion=1.1
T 61200 49600 5 8 0 1 0 0 1
netname=3.3V
}
C 58400 53600 1 0 0 3_3V.sym
{
T 58700 55300 5 8 0 0 0 1 1
device=net_symbol
T 58700 55700 5 8 0 0 0 1 1
footprint=none
T 58700 56500 5 8 0 0 0 1 1
symversion=1.1
T 58400 53600 5 8 0 1 0 0 1
netname=3.3V
}
C 48800 43900 1 0 0 gnd.sym
{
T 49100 45600 5 8 0 0 0 1 1
device=net_symbol
T 49100 46000 5 8 0 0 0 1 1
footprint=none
T 49100 46800 5 8 0 0 0 1 1
symversion=1.1
T 48800 43900 5 8 0 0 0 0 1
netname=GND
}
C 59300 43600 1 0 0 gnd.sym
{
T 59600 45300 5 8 0 0 0 1 1
device=net_symbol
T 59600 45700 5 8 0 0 0 1 1
footprint=none
T 59600 46500 5 8 0 0 0 1 1
symversion=1.1
T 59300 43600 5 8 0 0 0 0 1
netname=GND
}
C 57400 51900 1 0 0 gnd.sym
{
T 57700 53600 5 8 0 0 0 1 1
device=net_symbol
T 57700 54000 5 8 0 0 0 1 1
footprint=none
T 57700 54800 5 8 0 0 0 1 1
symversion=1.1
T 57400 51900 5 8 0 0 0 0 1
netname=GND
}
C 54600 51900 1 0 0 gnd.sym
{
T 54900 53600 5 8 0 0 0 1 1
device=net_symbol
T 54900 54000 5 8 0 0 0 1 1
footprint=none
T 54900 54800 5 8 0 0 0 1 1
symversion=1.1
T 54600 51900 5 8 0 0 0 0 1
netname=GND
}
C 52800 51900 1 0 0 gnd.sym
{
T 53100 53600 5 8 0 0 0 1 1
device=net_symbol
T 53100 54000 5 8 0 0 0 1 1
footprint=none
T 53100 54800 5 8 0 0 0 1 1
symversion=1.1
T 52800 51900 5 8 0 0 0 0 1
netname=GND
}
C 51800 51900 1 0 0 gnd.sym
{
T 52100 53600 5 8 0 0 0 1 1
device=net_symbol
T 52100 54000 5 8 0 0 0 1 1
footprint=none
T 52100 54800 5 8 0 0 0 1 1
symversion=1.1
T 51800 51900 5 8 0 0 0 0 1
netname=GND
}
N 61600 44900 61300 44900 4
N 61600 45800 61300 45800 4
N 61600 46700 61300 46700 4
N 61600 47100 61000 47100 4
N 61000 46200 61600 46200 4
N 61000 45300 61600 45300 4
C 52100 52200 1 0 0 capacitor_tantalum.sym
{
T 52600 52900 5 8 1 1 0 4 1
refdes=C103
T 51900 54700 5 8 0 0 0 1 1
device=device
T 52400 52500 5 8 1 1 0 1 1
value=10uF
T 51900 55300 5 8 0 0 0 1 1
footprint=cap_pol_0805.fp
T 51900 56100 5 8 0 0 0 1 1
symversion=1.0
}
C 52300 51900 1 0 0 gnd.sym
{
T 52600 53600 5 8 0 0 0 1 1
device=net_symbol
T 52600 54000 5 8 0 0 0 1 1
footprint=none
T 52600 54800 5 8 0 0 0 1 1
symversion=1.1
T 52300 51900 5 8 0 0 0 0 1
netname=GND
}
N 52400 52100 52400 52400 4
N 52400 52900 52400 53300 4
C 58200 52200 1 0 0 capacitor_tantalum.sym
{
T 58700 52900 5 8 1 1 0 4 1
refdes=C104
T 58000 54700 5 8 0 0 0 1 1
device=device
T 58500 52500 5 8 1 1 0 1 1
value=10uF
T 58000 55300 5 8 0 0 0 1 1
footprint=cap_pol_0805.fp
T 58000 56100 5 8 0 0 0 1 1
symversion=1.0
}
C 58400 51900 1 0 0 gnd.sym
{
T 58700 53600 5 8 0 0 0 1 1
device=net_symbol
T 58700 54000 5 8 0 0 0 1 1
footprint=none
T 58700 54800 5 8 0 0 0 1 1
symversion=1.1
T 58400 51900 5 8 0 0 0 0 1
netname=GND
}
N 58500 52400 58500 52100 4
T 59900 42800 9 8 1 0 0 0 1
1 of 2
T 56500 43100 9 8 1 0 0 0 1
BluMote
T 56500 42800 9 8 1 0 0 0 1
bluemote_pod.sch
T 56500 42500 9 8 1 0 0 0 1
KH
T 61600 42500 9 8 1 0 0 0 1
2/27/2011
C 59200 43600 1 0 0 resistor.sym
{
T 60275 45700 5 8 1 1 180 4 1
refdes=R104
T 60300 46900 5 8 0 0 0 1 1
device=resistor
T 60100 45500 5 8 1 1 0 1 1
value=100
T 60300 47500 5 8 0 0 0 1 1
footprint=0805_resistor.fp
T 60300 48300 5 8 0 0 0 1 1
symversion=1.1
}
C 59200 45400 1 0 0 resistor.sym
{
T 60275 47500 5 8 1 1 180 4 1
refdes=R106
T 60300 48700 5 8 0 0 0 1 1
device=resistor
T 60100 47300 5 8 1 1 0 1 1
value=100
T 60300 49300 5 8 0 0 0 1 1
footprint=0805_resistor.fp
T 60300 50100 5 8 0 0 0 1 1
symversion=1.1
}
C 59200 44500 1 0 0 resistor.sym
{
T 60325 46600 5 8 1 1 0 4 1
refdes=R105
T 60300 47800 5 8 0 0 0 1 1
device=resistor
T 60100 46400 5 8 1 1 0 1 1
value=100
T 60300 48400 5 8 0 0 0 1 1
footprint=0805_resistor.fp
T 60300 49200 5 8 0 0 0 1 1
symversion=1.1
}
C 61800 49700 1 180 0 resistor.sym
{
T 60325 48400 5 8 1 1 0 4 1
refdes=R102
T 60700 46400 5 8 0 0 180 1 1
device=resistor
T 60400 48200 5 8 1 1 180 1 1
value=100
T 60700 45800 5 8 0 0 180 1 1
footprint=0805_resistor.fp
T 60700 45000 5 8 0 0 180 1 1
symversion=1.1
}
C 61600 48200 1 0 0 led3.sym
{
T 62100 48400 5 8 1 1 0 4 1
refdes=D102
T 61200 50500 5 8 0 0 0 1 1
device=device
T 61200 50700 5 8 0 0 0 1 1
value=none
T 61200 51100 5 8 0 0 0 1 1
footprint=led_t1_75.fp
T 61200 51900 5 8 0 0 0 1 1
symversion=1.0
}
C 61600 48900 1 0 0 led3.sym
{
T 62100 49100 5 8 1 1 0 4 1
refdes=D101
T 61200 51200 5 8 0 0 0 1 1
device=device
T 61200 51400 5 8 0 0 0 1 1
value=none
T 61200 51800 5 8 0 0 0 1 1
footprint=led_t1_75.fp
T 61200 52600 5 8 0 0 0 1 1
symversion=1.0
}
C 61600 49600 1 0 0 5V.sym
{
T 61900 51300 5 8 0 0 0 1 1
device=net_symbol
T 61900 51700 5 8 0 0 0 1 1
footprint=none
T 61900 52500 5 8 0 0 0 1 1
symversion=1.1
T 61600 49600 5 8 0 0 0 0 1
netname=5V
}
N 61700 48200 61700 48000 4
N 61700 48000 61000 48000 4
N 61700 49600 61700 49400 4
N 61700 48900 61700 48700 4
T 49200 42500 9 8 1 0 0 0 5
Removed J105 - J112
Removed R101, R107, R108
Removed Q101
Changed R103 to 100ohm
Added D102
T 60900 43100 9 8 1 0 0 0 1
Version 0.2
T 50200 49700 9 8 1 0 0 0 4
NOTES:
We may want to add extra cap and a limiting resistor to make sure we don't exceed 100mA from the USB port.
We may want to add diode protection to power.

