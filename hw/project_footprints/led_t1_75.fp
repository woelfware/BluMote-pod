#Footprint Generator v 3.9.0 - Kiwi PCB 
#Copyright 2010, www.KiwiPCB.com
#By using this software, you are agreeing to its terms of use!  See your serial.txt file for details.
#Generated on 01/03/11 at 17:43:07
#For Kenneth Hutchins at kenneth.hutchins@gmail.com

#Remember that the units for all of the values shown below are in 1/100th of a mil.
#That is 100 * the value in mils, or 3937* the value in mm, or 100,000 times the value in inches.


#Comments: 

Element [ 0x0 "led_t1_75.fp"  "led_t1_75.fp"  ""  0  0  -12400   -12400   0   100   0x00 ] 
	(

	#Now adding Pins(Vias)
	#Pin [rX  rY  Annular-Ring  Copper-Clearance  Soldermask-Clearance  Drill-Size  "Name" "Number" SFlags]

	Pin [ 0 	0 	4800 	1600 	5800 	2799 	"1"  	"1" 	 0x0100 ]
	Pin [ 0 	10000 	4800 	1600 	5800 	2799 	"2"  	"2" 	 0x00 ]

	#Now adding *C* Silkscreen line
	#ElementArc [X Y Width Height StartAngle DeltaAngle Thickness] 

	ElementArc [ 11811 		11811 		1500  		1500  		230 		270 		1000 ]

	#Now adding Silkscreen lines
	#ElementArc [X Y Width Height StartAngle DeltaAngle Thickness] 

	ElementArc [ 0 		5000 		9843  		9843  		275 		360 		1000 ]

)

# Footprint Generator v 3.9.0 - Kiwi PCB
# Copyright 2010, www.KiwiPCB.com
 # The usage agreement requires that you leave all of this auto generated text intact.

#%Program_version=Footprint Generator v 3.9.0 - Kiwi PCB;  This is PCB Inline Footprint Generator version number
#%date_generated=01/03/11   17:43:07;  The date and time the footprint was generated, it will always be updated
#%part_name=led_t1_75.fp;  Name of the part/footprint
#%file_path=/home/ken/pcb_projects/footprints/all_new_footprints;  The file path for the footprint
#%show_output=1;  This is used to deal with most values in terms of 0=inch, 1= mils, 2=mm
#%temporary_footprint=0;  This tells us whether we want to save to a file
#%units=2;  This is used to deal with most values in terms of 0=inch, 1= mils, 2=mm
#%pin_one=0;  tells what shape only pin #1 should have in terms of 0=square, 1= round, 2=octagonal
#%other_pins=1;  tells what shape ALL other pins should have in terms of 0=square, 1= round, 2=octagonal
#%numbering=0;  This is used to deal with pin/pad numbering styles 0=CCW, 1=side by side (rows), 2=Columns, 3=QFP
#%rows=2;  Total number of rows in the footprint
#%columns=1;  Total number of columns in the footprint
#%total_pins=2;  Total number of pins in the footprint
#%starting_pin_number=1;  This is the pin number we start counting at
#%soldermask_reveal=0.1270;  How many additional mils (really the unit specified in 'units') that the soldermask is cleared away from the pin/pad
#%copper_clearance=0.2032;  How many additional mils (really the unit specified in 'units') that the copper in a polygon fill will be kept away from the pin/pad
#%pinpad=1;  1=pins are used 2=pads are used
#%column_spacing=0.0000;  Spacing between columns
#%row_spacing=2.5400;  Spacing between rows
#%QFP_pin_spacing_columns=0.0000;  Spacing between pins in columns for QFPs
#%QFP_pin_spacing_rows=0.0000;  Spacing between pins in rows for QFPs
#%use_a_thermal_pad=0;  Are you going to use a thermal pad
#%use_a_thermal_via=0;  Are you going to use a thermal via
#%open_mask=1;  If 0, solder mask will cover the pad
#%top_and_bottom=1;  If 0, pad is only on top
#%thermal_height=0.0000;  Height of a thermal pad
#%thermal_width=0.0000;  Width of a thermal pad
#%thermal_vertical=0.0000;  Vertical location of a thermal pad's center point
#%thermal_horizontal=0.0000;  Horizontal location of a thermal pad's center point
#%pin_diameter=0.5080;  The diameter of a pin in a through hole component
#%drill_bit=0.711;  The diameter of the drill for a via
#%annular_ring=1.2192;  The diameter of the copper pad around the via
#%max_ring=2.3368;  The maximum allowed diameter of the copper pad around the via
#%pad_width=0.0000;  The width of a copper pad
#%pad_length=0.0000;  The length of a copper pad
#%silk_outline_rectangular=0;  0=no silk screen outline, 1=Create a rectangular silkscreen outline
#%silk_outline_round=1;  0=no silk screen outline, 1=Create a round silkscreen outline
#%notch=0;  The notch at the top of the round silkscreen border
#%silk_width=0.2540;  The width of the silkscreen lines
#%refdes_size=100;  This is used to deal with most values in terms of 0=inch, 1= mils, 2=mm
#%silk_diameter=0.3810;  The diameter of the round silkscreen border
#%silk_top=0.3810;  The offset of the silkscreen from the top of the footprint
#%silk_left=0.3810;  The offset of the silkscreen from the top of the footprint
#%silk_right=0.3810;  The offset of the silkscreen from the top of the footprint
#%silk_bottom=0.3810;  The offset of the silkscreen from the top of the footprint
#%silk_object_type=5;  The type of polarity object
#%silk_object_length=0.7620;  The length or size of the polarity object
#%silk_object_width=0.2540;  The width of the silkscreen for the polarity object
#%silk_object_horizontal_offset=3;  The horizontal offset of the polarity object
#%silk_object_vertical_offset=3;  The vertical offset of the polarity object
#%square_notch_value=0.5080;  The square notch value of the footprint
#%diameter_size=5;  The diameter size of a circular silkscreen outline
#%refdes_rotation=0;  The rotation angle for the refdes, 0=Left to Right, 90=top down, 180=inverted right to left, 270=bottom up
#%refdes_vertical_position=1;  The vertical position of the refdes: 0 = center, 1 = top, 2 = bottom
#%refdes_horizontal_position=1;  The horizontal position of the refdes: 0 = center, 1 = left, 2 = right
#%refdes_vertical_offset=2.5400;  The vertical offset of the refdes
#%refdes_horizontal_offset=2.5400;  The horizontal offset of the refdes
#%use_displacement=0;  If set to 1 it will shift or displace the footprint
#%vertical_displacement=0.0000;  The vertical displacement of the footprint
#%horizontal_displacement=0.0000;  The horizontal displacement of the footprint