
# Remember that the units for all of the values shown below are in 1/100th of a mil.
# That is 100 * the value in mils, or 3937* the value in mm, or 100,000 times the value in inches.


Element [ 0x0 "CON__CUI_MJ-3523-SMT"  ""  ""  20000  20000  0  0   0   100   0x00 ] 
	(

	#Now adding Pins(Vias)
	#Pin [rX  rY  Annular-Ring  Copper-Clearance  Soldermask-Clearance  Drill-Size  "Name" "Number" SFlags]
	Pin [ 0 	0 	400 	1600 	400 	6700 	""  	"" 	 0x00 ]
	Pin [ -27600 	0 	400 	1600 	400 	6700 	""  	"" 	 0x00 ]

	#Now adding Pads
	#Pad [rX1 rY1 rX2 rY2 Thickness Clearance Mask "Name" "Number" SFlags]

	Pad [ 4400 	-15750 	4400 	-13450 	8700 	1600 	9100 	"1"  	"1" 	 0x0100 ]
	Pad [ -32700 	-14600 	-32700 	-14600 	11000 	1600 	11400 	"2"  	"2" 	 0x0100 ]
	Pad [ -48800 	3000 	-48800 	3000 	11000 	1600 	11400 	"3"  	"3" 	 0x0100 ]

	#Now adding Silkscreen lines
	#ElementLine [X1 Y1 X2 Y2 Thickness] 

	ElementLine [ 13800 		9850 		23600 		9850 		1000 ]
	ElementLine [ 13800 		-9850 		23600 		-9850 		1000 ]
	ElementLine [ 23600 		9850 		23600 		-9850 		1000 ]
	ElementLine [ 13800 		9850 		13800 		11800 		1000 ]
	ElementLine [ 13800 		-9850 		13800 		-11800 		1000 ]
	ElementLine [ -43300 		11800 		13800 		11800 		1000 ]
	ElementLine [ 10800 		-11800 		13800 		-11800 		1000 ]
	ElementLine [ -2000 		-11800 		-25600 		-11800 		1000 ]
	ElementLine [ -43300 		9850 		-43300 		11800 		1000 ]
	ElementLine [ -43300 		-11800 		-43300 		-4500 		1000 ]
	ElementLine [ -43300 		-11800		-40000 		-11800 		1000 ]

)
