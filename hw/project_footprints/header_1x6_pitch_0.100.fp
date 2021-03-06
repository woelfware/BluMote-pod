# This Footprint File was created by:  Footprint Maker 4.0.0 - Circuit Dashboard
# The Footprint Maker is copyright 2011, www.CircuitDashboard.com
# This release is for general use.
# Created on 02/14/11 at 21:30:46   For Kenneth Hutchins  < kenneth.hutchins@gmail.com > personal use

# Remember that the units for all of the values shown below are in 1/100th of a mil.
# That is 100 * the value in mils, or 3937* the value in mm, or 100,000 times the value in inches.


# Comments: 

Element [ 0x0 "header_1x6_pitch_0.100.fp"  "header_1x6_pitch_0.100.fp"  ""  0  0  -14000   -14000   0   100   0x00 ] 
	(

	#Now adding Pins(Vias)
	#Pin [rX  rY  Annular-Ring  Copper-Clearance  Soldermask-Clearance  Drill-Size  "Name" "Number" SFlags]

	Pin [ 0 	0 	8000 	1600 	9000 	4300 	"1"  	"1" 	 0x0100 ]
	Pin [ 0 	10000 	8000 	1600 	9000 	4300 	"2"  	"2" 	 0x00 ]
	Pin [ 0 	20000 	8000 	1600 	9000 	4300 	"3"  	"3" 	 0x00 ]
	Pin [ 0 	30000 	8000 	1600 	9000 	4300 	"4"  	"4" 	 0x00 ]
	Pin [ 0 	40000 	8000 	1600 	9000 	4300 	"5"  	"5" 	 0x00 ]
	Pin [ 0 	50000 	8000 	1600 	9000 	4300 	"6"  	"6" 	 0x00 ]

	#Now adding Silkscreen lines
	#ElementLine [X1 Y1 X2 Y2 Thickness] 

	ElementLine [ -6000 		-6000 		6000 		-6000 		1000 ]
	ElementLine [ -6000 		-6000 		-6000 		56000 		1000 ]
	ElementLine [ 6000 		-6000 		6000 		56000 		1000 ]
	ElementLine [ -6000 		56000 		6000 		56000 		1000 ]

)

# Footprint Maker 4.0.0 - Circuit Dashboard
# The Footprint Maker is copyright 2011, www.CircuitDashboard.com
 # The usage agreement requires that you leave all of this auto generated text intact.

#%Program_version=Footprint Maker 4.0.0 - Circuit Dashboard;  This is PCB Inline Footprint Maker version number
#%date_generated=02/14/11   21:30:46;  The date and time the footprint was generated, it will always be updated
#%part_name=header_1x6_pitch_0.100.fp;  Name of the part/footprint
#%file_path=/home/ken/pcb_projects/footprints/all_new_footprints;  The file path for the footprint
#%show_output=1;  This is used to deal with most values in terms of 0=inch, 1= mils, 2=mm
#%temporary_footprint=0;  This tells us whether we want to save to a file
#%units=1;  This is used to deal with most values in terms of 0=inch, 1= mils, 2=mm
#%pin_one=0;  tells what shape only pin #1 should have in terms of 0=square, 1= round, 2=octagonal
#%other_pins=1;  tells what shape ALL other pins should have in terms of 0=square, 1= round, 2=octagonal
#%numbering=0;  This is used to deal with pin/pad numbering styles 0=CCW, 1=side by side (rows), 2=Columns, 3=QFP
#%rows=6;  Total number of rows in the footprint
#%columns=1;  Total number of columns in the footprint
#%total_pins=6;  Total number of pins in the footprint
#%starting_pin_number=1;  This is the pin number we start counting at
#%soldermask_reveal=5;  How many additional mils (really the unit specified in 'units') that the soldermask is cleared away from the pin/pad
#%copper_clearance=8;  How many additional mils (really the unit specified in 'units') that the copper in a polygon fill will be kept away from the pin/pad
#%pinpad=1;  1=pins are used 2=pads are used
#%column_spacing=;  Spacing between columns
#%row_spacing=100;  Spacing between rows
#%QFP_pin_spacing_columns=;  Spacing between pins in columns for QFPs
#%QFP_pin_spacing_rows=;  Spacing between pins in rows for QFPs
#%use_a_thermal_pad=0;  Are you going to use a thermal pad
#%use_a_thermal_via=0;  Are you going to use a thermal via
#%open_mask=1;  If 0, solder mask will cover the pad
#%top_and_bottom=1;  If 0, pad is only on top
#%thermal_height=;  Height of a thermal pad
#%thermal_width=;  Width of a thermal pad
#%thermal_vertical=;  Vertical location of a thermal pad's center point
#%thermal_horizontal=;  Horizontal location of a thermal pad's center point
#%pin_diameter=37;  The diameter of a pin in a through hole component
#%drill_bit=43.0;  The diameter of the drill for a via
#%annular_ring=80;  The diameter of the copper pad around the via
#%max_ring=92.0000;  The maximum allowed diameter of the copper pad around the via
#%pad_width=;  The width of a copper pad
#%pad_length=;  The length of a copper pad
#%silk_outline_rectangular=1;  0=no silk screen outline, 1=Create a rectangular silkscreen outline
#%silk_outline_round=0;  0=no silk screen outline, 1=Create a round silkscreen outline
#%notch=0;  The notch at the top of the round silkscreen border
#%silk_width=10;  The width of the silkscreen lines
#%refdes_size=100;  This is used to deal with most values in terms of 0=inch, 1= mils, 2=mm
#%silk_diameter=15;  The diameter of the round silkscreen border
#%silk_top=15;  The offset of the silkscreen from the top of the footprint
#%silk_left=15;  The offset of the silkscreen from the top of the footprint
#%silk_right=15;  The offset of the silkscreen from the top of the footprint
#%silk_bottom=15;  The offset of the silkscreen from the top of the footprint
#%silk_object_type=0;  The type of polarity object
#%silk_object_length=30;  The length or size of the polarity object
#%silk_object_width=10;  The width of the silkscreen for the polarity object
#%silk_object_horizontal_offset=100;  The horizontal offset of the polarity object
#%silk_object_vertical_offset=100;  The vertical offset of the polarity object
#%square_notch_value=20;  The square notch value of the footprint
#%diameter_size=100;  The diameter size of a circular silkscreen outline
#%refdes_rotation=0;  The rotation angle for the refdes, 0=Left to Right, 90=top down, 180=inverted right to left, 270=bottom up
#%refdes_vertical_position=1;  The vertical position of the refdes: 0 = center, 1 = top, 2 = bottom
#%refdes_horizontal_position=1;  The horizontal position of the refdes: 0 = center, 1 = left, 2 = right
#%refdes_vertical_offset=100;  The vertical offset of the refdes
#%refdes_horizontal_offset=100;  The horizontal offset of the refdes
#%use_displacement=0;  If set to 1 it will shift or displace the footprint
#%vertical_displacement=0;  The vertical displacement of the footprint
#%horizontal_displacement=0;  The horizontal displacement of the footprint