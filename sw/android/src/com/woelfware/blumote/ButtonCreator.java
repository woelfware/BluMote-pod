package com.woelfware.blumote;

import android.widget.ImageButton;

// abstract class that defines a constructor and three member functions
// create an instantiation of this class for each unique screen button configuration
// refer to this new button class instantiation in the ENUM structure for the screen layout
public interface ButtonCreator {
	abstract public ButtonParameters[] getButtons(BluMote blumote);
	abstract public ImageButton getPowerOnBtn();
	abstract public ImageButton getPowerOffBtn();
}