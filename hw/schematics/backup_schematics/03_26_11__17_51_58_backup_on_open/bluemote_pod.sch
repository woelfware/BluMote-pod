v 20100214 2
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
N 48500 44900 50500 44900 4
{
T 49700 44900 5 8 1 1 0 0 1
netname=IR_IN
}
C 62200 46300 1 0 0 led3.sym
{
T 62700 46500 5 8 1 1 0 4 1
refdes=D101
T 61800 48600 5 8 0 0 0 1 1
device=device
T 61800 48800 5 8 0 0 0 1 1
value=none
T 61800 49200 5 8 0 0 0 1 1
footprint=led_t1_75.fp
T 61800 50000 5 8 0 0 0 1 1
symversion=1.0
}
C 60000 42900 1 0 0 resistor.sym
{
T 61125 44800 5 8 1 1 0 4 1
refdes=R101
T 61100 46200 5 8 0 0 0 1 1
device=resistor
T 61500 44800 5 8 1 1 0 1 1
value=1K
T 61100 46800 5 8 0 0 0 1 1
footprint=0805_resistor.fp
T 61100 47600 5 8 0 0 0 1 1
symversion=1.1
}
C 60600 47000 1 270 0 resistor.sym
{
T 62625 45900 5 8 1 1 0 4 1
refdes=R102
T 63900 45900 5 8 0 0 270 1 1
device=resistor
T 62400 45700 5 8 1 1 0 1 1
value=100
T 64500 45900 5 8 0 0 270 1 1
footprint=0805_resistor.fp
T 65300 45900 5 8 0 0 270 1 1
symversion=1.1
}
N 59400 44600 60800 44600 4
{
T 59600 44600 5 8 1 1 0 0 1
netname=IR_OUT1
}
C 56900 46800 1 0 0 resistor.sym
{
T 58025 48700 5 8 1 1 0 4 1
refdes=R103
T 58000 50100 5 8 0 0 0 1 1
device=resistor
T 58400 48700 5 8 1 1 0 1 1
value=1K
T 58000 50700 5 8 0 0 0 1 1
footprint=0805_resistor.fp
T 58000 51500 5 8 0 0 0 1 1
symversion=1.1
}
N 59400 47800 59400 48100 4
N 56800 48500 57700 48500 4
{
T 57200 48300 5 8 1 1 0 0 1
netname=IR_OUT2
}
C 59000 48100 1 0 0 2N7002.sym
{
T 59625 48725 5 8 1 1 0 4 1
refdes=Q102
T 59300 50100 5 8 0 0 0 1 1
device=NMOS
T 59300 50300 5 8 0 0 0 1 1
value=none
T 59300 50700 5 8 0 0 0 1 1
footprint=sot_23.fp
T 59300 51500 5 8 0 0 0 1 1
symversion=1.1
}
C 59200 50300 1 0 0 resistor.sym
{
T 60275 52400 5 8 1 1 180 4 1
refdes=R104
T 60300 53600 5 8 0 0 0 1 1
device=resistor
T 60100 52200 5 8 1 1 0 1 1
value=100
T 60300 54200 5 8 0 0 0 1 1
footprint=0805_resistor.fp
T 60300 55000 5 8 0 0 0 1 1
symversion=1.1
}
C 59200 49400 1 0 0 resistor.sym
{
T 60325 51500 5 8 1 1 0 4 1
refdes=R105
T 60300 52700 5 8 0 0 0 1 1
device=resistor
T 60100 51300 5 8 1 1 0 1 1
value=100
T 60300 53300 5 8 0 0 0 1 1
footprint=0805_resistor.fp
T 60300 54100 5 8 0 0 0 1 1
symversion=1.1
}
C 59200 48500 1 0 0 resistor.sym
{
T 60275 50600 5 8 1 1 180 4 1
refdes=R106
T 60300 51800 5 8 0 0 0 1 1
device=resistor
T 60100 50400 5 8 1 1 0 1 1
value=100
T 60300 52400 5 8 0 0 0 1 1
footprint=0805_resistor.fp
T 60300 53200 5 8 0 0 0 1 1
symversion=1.1
}
C 59200 47600 1 0 0 resistor.sym
{
T 60275 49700 5 8 1 1 180 4 1
refdes=R107
T 60300 50900 5 8 0 0 0 1 1
device=resistor
T 60100 49500 5 8 1 1 0 1 1
value=100
T 60300 51500 5 8 0 0 0 1 1
footprint=0805_resistor.fp
T 60300 52300 5 8 0 0 0 1 1
symversion=1.1
}
N 59400 48900 59400 52900 4
C 7200 2300 0 0 0 title_8_5x11_full.sym
{
T 47250 54850 15 8 0 0 0 0 1
device=none
}
C 51600 44000 1 0 1 usb_micro_b.sym
{
T 51500 45300 5 8 1 1 0 7 1
refdes=J101
T 51300 46600 5 8 0 0 0 7 1
device=device
T 51300 46800 5 8 0 0 0 7 1
value=none
T 51300 47200 5 8 0 0 0 7 1
footprint=tyco_1981568_usb_micro_b.fp
T 51300 48000 5 8 0 0 0 7 1
symversion=1.1
}
C 63800 47000 1 0 1 audio_mono.sym
{
T 62200 49600 5 8 1 1 0 7 1
refdes=J102
T 62700 50700 5 8 0 0 0 7 1
device=device
T 62700 50900 5 8 0 0 0 7 1
value=none
T 62700 51300 5 8 0 0 0 7 1
footprint=mj-3536ng.fp
T 62700 52100 5 8 0 0 0 7 1
symversion=1.1
}
C 63800 47900 1 0 1 audio_mono.sym
{
T 62200 50500 5 8 1 1 0 7 1
refdes=J103
T 62700 51600 5 8 0 0 0 7 1
device=device
T 62700 51800 5 8 0 0 0 7 1
value=none
T 62700 52200 5 8 0 0 0 7 1
footprint=mj-3536ng.fp
T 62700 53000 5 8 0 0 0 7 1
symversion=1.1
}
C 63800 48800 1 0 1 audio_mono.sym
{
T 62200 51400 5 8 1 1 0 7 1
refdes=J104
T 62700 52500 5 8 0 0 0 7 1
device=device
T 62700 52700 5 8 0 0 0 7 1
value=none
T 62700 53100 5 8 0 0 0 7 1
footprint=mj-3536ng.fp
T 62700 53900 5 8 0 0 0 7 1
symversion=1.1
}
C 63800 49700 1 0 1 audio_mono.sym
{
T 62200 52300 5 8 1 1 0 7 1
refdes=J105
T 62700 53400 5 8 0 0 0 7 1
device=device
T 62700 53600 5 8 0 0 0 7 1
value=none
T 62700 54000 5 8 0 0 0 7 1
footprint=mj-3536ng.fp
T 62700 54800 5 8 0 0 0 7 1
symversion=1.1
}
C 63800 50600 1 0 1 audio_mono.sym
{
T 62200 53200 5 8 1 1 0 7 1
refdes=J106
T 62700 54300 5 8 0 0 0 7 1
device=device
T 62700 54500 5 8 0 0 0 7 1
value=none
T 62700 54900 5 8 0 0 0 7 1
footprint=mj-3536ng.fp
T 62700 55700 5 8 0 0 0 7 1
symversion=1.1
}
N 61800 44600 61900 44600 4
N 62300 45000 62300 45200 4
N 62300 44000 62300 44200 4
N 62300 46300 62300 46200 4
C 59200 51200 1 0 0 resistor.sym
{
T 60225 53300 5 8 1 1 0 4 1
refdes=R108
T 60300 54500 5 8 0 0 0 1 1
device=resistor
T 60200 53100 5 8 1 1 0 1 1
value=100
T 60300 55100 5 8 0 0 0 1 1
footprint=0805_resistor.fp
T 60300 55900 5 8 0 0 0 1 1
symversion=1.1
}
N 61300 53600 61300 48900 4
N 59400 52900 60000 52900 4
N 59400 49300 60000 49300 4
N 60000 50200 59400 50200 4
N 60000 51100 59400 51100 4
N 60000 52000 59400 52000 4
N 59000 48500 58700 48500 4
B 57700 47500 5300 6600 3 0 0 0 -1 -1 0 -1 -1 -1 -1 -1
T 58200 53900 9 8 1 0 0 0 1
External IR Emitter Control
T 58200 53700 9 8 1 0 0 0 1
~20mA per IR LED
T 58200 53500 9 8 1 0 0 0 1
VF of LEDs should be less than 2.5V
B 59500 43500 3500 4000 3 0 0 0 -1 -1 0 -1 -1 -1 -1 -1
T 59700 47300 9 8 1 0 0 0 1
Internal IR Emitter Control
C 51800 45300 1 0 0 5V.sym
{
T 52100 47000 5 8 0 0 0 1 1
device=net_symbol
T 52100 47400 5 8 0 0 0 1 1
footprint=none
T 52100 48200 5 8 0 0 0 1 1
symversion=1.1
T 51800 45300 5 8 0 0 0 0 1
netname=5V
}
N 51600 44200 51900 44200 4
N 51900 44200 51900 43800 4
N 51900 45300 51900 45000 4
N 51600 45000 53700 45000 4
N 62300 46900 62300 46800 4
B 47400 43500 2900 2800 3 0 0 0 -1 -1 0 -1 -1 -1 -1 -1
T 47500 46000 9 8 1 0 0 0 1
IR Reciever
C 53700 44100 1 0 0 mcp1603t.sym
{
T 54300 45300 5 8 1 1 0 1 1
refdes=U102
T 54000 46500 5 8 0 0 0 1 1
device=device
T 54000 46700 5 8 0 0 0 1 1
value=none
T 54000 47100 5 8 0 0 0 1 1
footprint=mcp1603t_tsot.fp
T 54000 47900 5 8 0 0 0 1 1
symversion=1.1
}
N 56000 45000 55800 45000 4
N 53400 45000 53400 44600 4
N 53400 44600 53700 44600 4
N 54700 43800 54700 44100 4
C 56000 45000 1 0 0 inductor.sym
{
T 56325 45200 5 8 1 1 0 4 1
refdes=L101
T 56310 46650 5 8 0 0 0 1 1
device=inductor
T 56310 44850 5 8 1 1 0 1 1
value=4.7uH
T 56310 47250 5 8 0 0 0 1 1
footprint=we_tpc_type_s.fp
T 56310 48050 5 8 0 0 0 1 1
symversion=1.1
}
C 52800 44100 1 0 0 capacitor.sym
{
T 53100 44600 5 8 1 1 0 4 1
refdes=C101
T 52400 46400 5 8 0 0 0 1 1
device=device
T 52900 44200 5 8 1 1 0 1 1
value=0.1uF
T 52400 47000 5 8 0 0 0 1 1
footprint=cap_0805.fp
T 52400 47800 5 8 0 0 0 1 1
symversion=1.0
}
N 52900 43800 52900 44100 4
N 52900 44600 52900 45000 4
C 57400 44100 1 0 0 capacitor.sym
{
T 57700 44600 5 8 1 1 0 4 1
refdes=C102
T 57000 46400 5 8 0 0 0 1 1
device=device
T 57500 44200 5 8 1 1 0 1 1
value=0.1uF
T 57000 47000 5 8 0 0 0 1 1
footprint=cap_0805.fp
T 57000 47800 5 8 0 0 0 1 1
symversion=1.0
}
N 55800 44600 57200 44600 4
N 57200 44600 57200 45000 4
N 57000 45000 58500 45000 4
N 57500 44100 57500 43800 4
N 57500 44600 57500 45000 4
N 58500 44600 58500 45300 4
B 50300 43500 9200 2300 3 0 0 0 -1 -1 0 -1 -1 -1 -1 -1
T 55200 43800 9 8 1 0 0 0 2
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
C 62200 46900 1 0 0 3_3V.sym
{
T 62500 48600 5 8 0 0 0 1 1
device=net_symbol
T 62500 49000 5 8 0 0 0 1 1
footprint=none
T 62500 49800 5 8 0 0 0 1 1
symversion=1.1
T 62200 46900 5 8 0 1 0 0 1
netname=3.3V
}
C 61200 53600 1 0 0 3_3V.sym
{
T 61500 55300 5 8 0 0 0 1 1
device=net_symbol
T 61500 55700 5 8 0 0 0 1 1
footprint=none
T 61500 56500 5 8 0 0 0 1 1
symversion=1.1
T 61200 53600 5 8 0 1 0 0 1
netname=3.3V
}
C 58400 45300 1 0 0 3_3V.sym
{
T 58700 47000 5 8 0 0 0 1 1
device=net_symbol
T 58700 47400 5 8 0 0 0 1 1
footprint=none
T 58700 48200 5 8 0 0 0 1 1
symversion=1.1
T 58400 45300 5 8 0 1 0 0 1
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
C 62200 43800 1 0 0 gnd.sym
{
T 62500 45500 5 8 0 0 0 1 1
device=net_symbol
T 62500 45900 5 8 0 0 0 1 1
footprint=none
T 62500 46700 5 8 0 0 0 1 1
symversion=1.1
T 62200 43800 5 8 0 0 0 0 1
netname=GND
}
C 59300 47600 1 0 0 gnd.sym
{
T 59600 49300 5 8 0 0 0 1 1
device=net_symbol
T 59600 49700 5 8 0 0 0 1 1
footprint=none
T 59600 50500 5 8 0 0 0 1 1
symversion=1.1
T 59300 47600 5 8 0 0 0 0 1
netname=GND
}
C 57400 43600 1 0 0 gnd.sym
{
T 57700 45300 5 8 0 0 0 1 1
device=net_symbol
T 57700 45700 5 8 0 0 0 1 1
footprint=none
T 57700 46500 5 8 0 0 0 1 1
symversion=1.1
T 57400 43600 5 8 0 0 0 0 1
netname=GND
}
C 54600 43600 1 0 0 gnd.sym
{
T 54900 45300 5 8 0 0 0 1 1
device=net_symbol
T 54900 45700 5 8 0 0 0 1 1
footprint=none
T 54900 46500 5 8 0 0 0 1 1
symversion=1.1
T 54600 43600 5 8 0 0 0 0 1
netname=GND
}
C 52800 43600 1 0 0 gnd.sym
{
T 53100 45300 5 8 0 0 0 1 1
device=net_symbol
T 53100 45700 5 8 0 0 0 1 1
footprint=none
T 53100 46500 5 8 0 0 0 1 1
symversion=1.1
T 52800 43600 5 8 0 0 0 0 1
netname=GND
}
C 51800 43600 1 0 0 gnd.sym
{
T 52100 45300 5 8 0 0 0 1 1
device=net_symbol
T 52100 45700 5 8 0 0 0 1 1
footprint=none
T 52100 46500 5 8 0 0 0 1 1
symversion=1.1
T 51800 43600 5 8 0 0 0 0 1
netname=GND
}
N 61600 48900 61300 48900 4
N 61600 49800 61300 49800 4
N 61600 50700 61300 50700 4
N 61600 51600 61300 51600 4
N 61600 52500 61300 52500 4
N 61000 52900 61600 52900 4
N 61000 52000 61600 52000 4
N 61600 51100 61000 51100 4
N 61000 50200 61600 50200 4
N 61000 49300 61600 49300 4
C 52100 43900 1 0 0 capacitor_tantalum.sym
{
T 52600 44600 5 8 1 1 0 4 1
refdes=C103
T 51900 46400 5 8 0 0 0 1 1
device=device
T 52400 44200 5 8 1 1 0 1 1
value=10uF
T 51900 47000 5 8 0 0 0 1 1
footprint=cap_pol_0805.fp
T 51900 47800 5 8 0 0 0 1 1
symversion=1.0
}
C 52300 43600 1 0 0 gnd.sym
{
T 52600 45300 5 8 0 0 0 1 1
device=net_symbol
T 52600 45700 5 8 0 0 0 1 1
footprint=none
T 52600 46500 5 8 0 0 0 1 1
symversion=1.1
T 52300 43600 5 8 0 0 0 0 1
netname=GND
}
N 52400 43800 52400 44100 4
N 52400 44600 52400 45000 4
C 58200 43900 1 0 0 capacitor_tantalum.sym
{
T 58700 44600 5 8 1 1 0 4 1
refdes=C104
T 58000 46400 5 8 0 0 0 1 1
device=device
T 58500 44200 5 8 1 1 0 1 1
value=10uF
T 58000 47000 5 8 0 0 0 1 1
footprint=cap_pol_0805.fp
T 58000 47800 5 8 0 0 0 1 1
symversion=1.0
}
C 58400 43600 1 0 0 gnd.sym
{
T 58700 45300 5 8 0 0 0 1 1
device=net_symbol
T 58700 45700 5 8 0 0 0 1 1
footprint=none
T 58700 46500 5 8 0 0 0 1 1
symversion=1.1
T 58400 43600 5 8 0 0 0 0 1
netname=GND
}
N 58500 44100 58500 43800 4
N 55050 53300 55950 53300 4
{
T 54650 53250 5 8 1 1 0 0 1
netname=P4.0
}
N 55050 53100 55950 53100 4
{
T 54650 53050 5 8 1 1 0 0 1
netname=P4.1
}
C 55950 51700 1 0 0 header_8pin.sym
{
T 56050 53600 5 8 1 1 0 1 1
refdes=J107
T 56250 54900 5 8 0 0 0 1 1
device=generic_header
T 56250 55100 5 8 0 0 0 1 1
value=none
T 56250 55500 5 8 0 0 0 1 1
footprint=header_1x8_pitch_0.100.fp
T 56250 56300 5 8 0 0 0 1 1
symversion=1.2
}
N 55050 52900 55950 52900 4
{
T 54650 52850 5 8 1 1 0 0 1
netname=P4.2
}
N 55050 52700 55950 52700 4
{
T 54650 52650 5 8 1 1 0 0 1
netname=P4.3
}
N 55050 52500 55950 52500 4
{
T 54650 52450 5 8 1 1 0 0 1
netname=P4.4
}
N 55050 52300 55950 52300 4
{
T 54650 52250 5 8 1 1 0 0 1
netname=P4.5
}
N 55050 52100 55950 52100 4
{
T 54650 52050 5 8 1 1 0 0 1
netname=P4.6
}
N 55050 51900 55950 51900 4
{
T 54650 51850 5 8 1 1 0 0 1
netname=P4.7
}
N 53000 53300 53900 53300 4
{
T 52500 53250 5 8 1 1 0 0 1
netname=Reset
}
N 53000 53100 53900 53100 4
{
T 52200 53050 5 8 1 1 0 0 1
netname=Baud_Rate
}
N 53000 52900 53900 52900 4
{
T 52600 52850 5 8 1 1 0 0 1
netname=P3.2
}
N 53000 52700 53900 52700 4
{
T 52600 52650 5 8 1 1 0 0 1
netname=P3.3
}
N 53000 52500 53900 52500 4
{
T 52600 52450 5 8 1 1 0 0 1
netname=P3.4
}
N 53000 52300 53900 52300 4
{
T 52600 52250 5 8 1 1 0 0 1
netname=P3.5
}
N 53000 52100 53900 52100 4
{
T 52600 52050 5 8 1 1 0 0 1
netname=P3.6
}
N 53000 51900 53900 51900 4
{
T 52600 51850 5 8 1 1 0 0 1
netname=P3.7
}
C 53900 51700 1 0 0 header_8pin.sym
{
T 54000 53600 5 8 1 1 0 1 1
refdes=J108
T 54200 54900 5 8 0 0 0 1 1
device=generic_header
T 54200 55100 5 8 0 0 0 1 1
value=none
T 54200 55500 5 8 0 0 0 1 1
footprint=header_1x8_pitch_0.100.fp
T 54200 56300 5 8 0 0 0 1 1
symversion=1.2
}
C 49500 51700 1 0 0 header_8pin.sym
{
T 49600 53600 5 8 1 1 0 1 1
refdes=J109
T 49800 54900 5 8 0 0 0 1 1
device=generic_header
T 49800 55100 5 8 0 0 0 1 1
value=none
T 49800 55500 5 8 0 0 0 1 1
footprint=header_1x8_pitch_0.100.fp
T 49800 56300 5 8 0 0 0 1 1
symversion=1.2
}
N 48600 51900 49500 51900 4
{
T 48200 51850 5 8 1 1 0 0 1
netname=P1.7
}
N 48600 52100 49500 52100 4
{
T 48200 52050 5 8 1 1 0 0 1
netname=P1.6
}
N 48600 53300 49500 53300 4
{
T 48200 53250 5 8 1 1 0 0 1
netname=P1.0
}
N 48600 53100 49500 53100 4
{
T 48200 53050 5 8 1 1 0 0 1
netname=P1.1
}
N 48600 52900 49500 52900 4
{
T 48200 52850 5 8 1 1 0 0 1
netname=P1.2
}
N 48600 52700 49500 52700 4
{
T 48100 52650 5 8 1 1 0 0 1
netname=IR_IN
}
N 48600 52500 49500 52500 4
{
T 47900 52450 5 8 1 1 0 0 1
netname=IR_OUT1
}
N 48600 52300 49500 52300 4
{
T 47900 52250 5 8 1 1 0 0 1
netname=IR_OUT2
}
N 50700 53300 51600 53300 4
{
T 50300 53250 5 8 1 1 0 0 1
netname=P2.0
}
N 50700 53100 51600 53100 4
{
T 50300 53050 5 8 1 1 0 0 1
netname=P2.1
}
N 50700 52900 51600 52900 4
{
T 50300 52850 5 8 1 1 0 0 1
netname=P2.2
}
N 50700 52700 51600 52700 4
{
T 50300 52650 5 8 1 1 0 0 1
netname=P2.3
}
N 50700 52500 51600 52500 4
{
T 50300 52450 5 8 1 1 0 0 1
netname=P2.4
}
N 50700 52300 51600 52300 4
{
T 50300 52250 5 8 1 1 0 0 1
netname=P2.5
}
N 50700 52100 51600 52100 4
{
T 50300 52050 5 8 1 1 0 0 1
netname=P2.6
}
N 50700 51900 51600 51900 4
{
T 50300 51850 5 8 1 1 0 0 1
netname=P2.7
}
C 51600 51700 1 0 0 header_8pin.sym
{
T 51700 53600 5 8 1 1 0 1 1
refdes=J110
T 51900 54900 5 8 0 0 0 1 1
device=generic_header
T 51900 55100 5 8 0 0 0 1 1
value=none
T 51900 55500 5 8 0 0 0 1 1
footprint=header_1x8_pitch_0.100.fp
T 51900 56300 5 8 0 0 0 1 1
symversion=1.2
}
C 61900 44200 1 0 0 2N7002.sym
{
T 62525 44825 5 8 1 1 0 4 1
refdes=Q101
T 62200 46200 5 8 0 0 0 1 1
device=NMOS
T 62200 46400 5 8 0 0 0 1 1
value=none
T 62200 46800 5 8 0 0 0 1 1
footprint=sot_23.fp
T 62200 47600 5 8 0 0 0 1 1
symversion=1.1
}
C 48300 50200 1 0 0 header_2pin.sym
{
T 48500 50700 5 8 1 1 0 7 1
refdes=J111
T 48400 52100 5 8 0 0 0 1 1
device=generic_header
T 48400 52300 5 8 0 0 0 1 1
value=none
T 48400 52700 5 8 0 0 0 1 1
footprint=header_1x2_pitch_0.100.fp
T 48400 53500 5 8 0 0 0 1 1
symversion=1.3
}
C 48300 49200 1 0 0 header_2pin.sym
{
T 48500 49700 5 8 1 1 0 7 1
refdes=J112
T 48400 51100 5 8 0 0 0 1 1
device=generic_header
T 48400 51300 5 8 0 0 0 1 1
value=none
T 48400 51700 5 8 0 0 0 1 1
footprint=header_1x2_pitch_0.100.fp
T 48400 52500 5 8 0 0 0 1 1
symversion=1.3
}
C 49000 50800 1 0 0 3_3V.sym
{
T 49300 52500 5 8 0 0 0 1 1
device=net_symbol
T 49300 52900 5 8 0 0 0 1 1
footprint=none
T 49300 53700 5 8 0 0 0 1 1
symversion=1.1
T 49000 50800 5 8 0 1 0 0 1
netname=3.3V
}
C 49000 48900 1 0 0 gnd.sym
{
T 49300 50600 5 8 0 0 0 1 1
device=net_symbol
T 49300 51000 5 8 0 0 0 1 1
footprint=none
T 49300 51800 5 8 0 0 0 1 1
symversion=1.1
T 49000 48900 5 8 0 0 0 0 1
netname=GND
}
N 48700 49300 49100 49300 4
N 49100 49100 49100 49500 4
N 48700 49500 49100 49500 4
N 48700 50300 49100 50300 4
N 49100 50300 49100 50800 4
N 48700 50500 49100 50500 4
T 60000 42800 9 8 1 0 0 0 1
1 of 2
T 56600 43100 9 8 1 0 0 0 1
BluMote
T 56600 42800 9 8 1 0 0 0 1
bluemote_pod.sch
T 56600 42500 9 8 1 0 0 0 1
KH
T 61600 42500 9 8 1 0 0 0 1
2/27/2011


