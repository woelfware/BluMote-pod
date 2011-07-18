package com.woelfware.blumote;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

/**
 * This class offloads several of the functions associated with dealing 
 * with the interface and buttons/dropdowns/lists
 * @author keusej
 *
 */
public class MainInterface {
	
	private  ImageButton fav_btn;
    private  ImageButton btn_volume_up;
    private  ImageButton btn_volume_down;
    private  ImageButton btn_channel_up;
    private  ImageButton btn_channel_down;
//    private  Spinner device_spinner;
//    private  ImageButton led_btn;
    private  ImageButton btn_pwr;
    private  ImageButton power2_btn;
    private  ImageButton back_skip_btn;
    private  ImageButton back_btn;
    private  ImageButton forward_btn;
    private  ImageButton skip_forward_btn;
    private  ImageButton record_btn;
    private  ImageButton stop_btn;
    private  ImageButton play_btn;
    private  ImageButton pause_btn;
    private  ImageButton eject_btn;
    private  Button disc_btn;
    private  ImageButton mute_btn;
    private  Button info_btn;
    private  ImageButton return_btn;
    private  ImageButton pgup_btn;
    private  ImageButton pgdn_btn;
    private  Button guide_btn;
    private  Button exit_btn;
    private  ImageButton move_left_btn;
    private  ImageButton move_right_btn;
    private  Button btn_input;
    private  Button btn_last;

    private  ImageButton move_left_n_btn;
    private  ImageButton move_right_n_btn;
	private Spinner device_spinner;
	private ImageButton left_btn;
	private ImageButton down_btn;
	private ImageButton right_btn;
	private ImageButton btn_up;
	private Button btn_n0;
	private Button btn_n1;
	private Button btn_n2;
	private Button btn_n3;
	private Button btn_n4;
	private Button btn_n5;
	private Button btn_n6;
	private Button btn_n7;
	private Button btn_n8;
	private Button btn_n9;
	private Button btn_dash;
	private Button btn_enter;
	private Button btn_exit;
	private ImageButton btn_home;
	private Button btn_misc1;
	private Button btn_misc2;
	private Button btn_misc3;
	private Button btn_misc4;
	private Button btn_misc5;
	private Button btn_misc6;
	private Button btn_misc7;
	private Button btn_misc8;
	
    private  ImageButton move_left_a_btn;
    private  ImageButton move_right_a_btn;

    // this has to match the number of misc buttons in the interface
    // we use this for the renaming of misc buttons logic
    private static final int NUM_MISC_BTNS = 8;
    
    private Button add_activity_btn;
    
    // array adapter for the drop-down spinner
	private ArrayAdapter<String> spinnerAdapter;
    
    ListView activitiesListView;
    
    private  BluMote blumote;
    
    HashMap<Integer,String> button_map = null;
    
    private Activities activities;
    
    /**
     * public constructor
     * @param d the BluMote object to work with
     */
	public MainInterface(BluMote d) {
		blumote = d;
	}
	
	// prefix to put before a new activity to prefs file to find it easier
	// when searching through keys
	static final String ACTIVITY_PREFIX = "(A)_";
	static final String ACTIVITY_PREFIX_SPACE = "(A) ";
	
	/**
	 * This method will initialize the button elements of the interface
	 * as well as dropdowns/lists and set up the button_map hashmap in blumote
	 */
	public void initialize(Activities a) {
		// create a handle to the Activities class for working with the activities framework
		activities = a;
		
		// if button_map is null then need to set it up, only do this once
		if (button_map == null) {
			/////////////////////////////////////
			////// MAIN SCREEN
			// Initialize the buttons with a listener for click and touch events
			btn_volume_up = (ImageButton) blumote.findViewById(R.id.btn_volume_up);
			btn_volume_down = (ImageButton) blumote.findViewById(R.id.btn_volume_down);
			btn_channel_up = (ImageButton) blumote.findViewById(R.id.btn_channel_up);
			btn_channel_down = (ImageButton) blumote.findViewById(R.id.btn_channel_down);
			btn_input = (Button) blumote.findViewById(R.id.btn_input);
			//        led_btn = (ImageButton) blumote.findViewById(R.id.led_btn);
			btn_pwr = (ImageButton) blumote.findViewById(R.id.power_btn);
			power2_btn = (ImageButton) blumote.findViewById(R.id.power2_btn);
			back_skip_btn = (ImageButton) blumote.findViewById(R.id.back_skip_btn);
			back_btn = (ImageButton) blumote.findViewById(R.id.back_btn);
			forward_btn = (ImageButton) blumote.findViewById(R.id.forward_btn);
			skip_forward_btn = (ImageButton) blumote.findViewById(R.id.skip_forward_btn); 
			record_btn = (ImageButton) blumote.findViewById(R.id.record_btn);
			stop_btn = (ImageButton) blumote.findViewById(R.id.stop_btn);
			play_btn = (ImageButton) blumote.findViewById(R.id.play_btn);
			eject_btn = (ImageButton) blumote.findViewById(R.id.eject_btn);
			disc_btn = (Button) blumote.findViewById(R.id.disc_btn);
			mute_btn = (ImageButton) blumote.findViewById(R.id.mute_btn);
			info_btn = (Button) blumote.findViewById(R.id.info_btn);
			return_btn = (ImageButton) blumote.findViewById(R.id.return_btn);
			pgup_btn = (ImageButton) blumote.findViewById(R.id.pgup_btn);
			pgdn_btn = (ImageButton) blumote.findViewById(R.id.pgdn_btn);
			guide_btn = (Button) blumote.findViewById(R.id.guide_btn);
			exit_btn = (Button) blumote.findViewById(R.id.exit_btn);
			move_right_btn = (ImageButton) blumote.findViewById(R.id.move_right_btn);
			move_left_btn = (ImageButton) blumote.findViewById(R.id.move_left_btn);
			pause_btn = (ImageButton) blumote.findViewById(R.id.pause_btn);
			fav_btn = (ImageButton) blumote.findViewById(R.id.fav_btn);
			btn_last = (Button) blumote.findViewById(R.id.btn_last);
			btn_volume_up.setOnTouchListener(blumote.gestureListener);
			btn_volume_up.setOnClickListener(blumote);
			btn_volume_down.setOnTouchListener(blumote.gestureListener);
			btn_volume_down.setOnClickListener(blumote);
			btn_channel_up.setOnTouchListener(blumote.gestureListener);
			btn_channel_up.setOnClickListener(blumote);
			btn_channel_down.setOnTouchListener(blumote.gestureListener);
			btn_channel_down.setOnClickListener(blumote);
			btn_input.setOnClickListener(blumote);
			btn_input.setOnTouchListener(blumote.gestureListener);
			btn_pwr.setOnClickListener(blumote);
			btn_pwr.setOnTouchListener(blumote.gestureListener);
			power2_btn.setOnClickListener(blumote);
			power2_btn.setOnTouchListener(blumote.gestureListener);
			back_skip_btn.setOnClickListener(blumote);
			back_skip_btn.setOnTouchListener(blumote.gestureListener);
			back_btn.setOnClickListener(blumote);
			back_btn.setOnTouchListener(blumote.gestureListener);
			forward_btn.setOnClickListener(blumote);
			forward_btn.setOnTouchListener(blumote.gestureListener);
			skip_forward_btn.setOnClickListener(blumote);
			skip_forward_btn.setOnTouchListener(blumote.gestureListener);
			record_btn.setOnClickListener(blumote);
			record_btn.setOnTouchListener(blumote.gestureListener);
			stop_btn.setOnClickListener(blumote);
			stop_btn.setOnTouchListener(blumote.gestureListener);
			play_btn.setOnClickListener(blumote);
			play_btn.setOnTouchListener(blumote.gestureListener);
			eject_btn.setOnClickListener(blumote);
			eject_btn.setOnTouchListener(blumote.gestureListener);
			disc_btn.setOnClickListener(blumote);
			disc_btn.setOnTouchListener(blumote.gestureListener);        
			mute_btn.setOnClickListener(blumote);
			mute_btn.setOnTouchListener(blumote.gestureListener);;
			info_btn.setOnClickListener(blumote);
			info_btn.setOnTouchListener(blumote.gestureListener);
			return_btn.setOnClickListener(blumote);
			return_btn.setOnTouchListener(blumote.gestureListener);
			pgup_btn.setOnClickListener(blumote);
			pgup_btn.setOnTouchListener(blumote.gestureListener);
			pgdn_btn.setOnClickListener(blumote);
			pgdn_btn.setOnTouchListener(blumote.gestureListener);
			guide_btn.setOnClickListener(blumote);
			guide_btn.setOnTouchListener(blumote.gestureListener);
			exit_btn.setOnClickListener(blumote);
			exit_btn.setOnTouchListener(blumote.gestureListener);
			move_left_btn.setOnClickListener(blumote);
			move_left_btn.setOnTouchListener(blumote.gestureListener);
			move_right_btn.setOnClickListener(blumote);
			move_right_btn.setOnTouchListener(blumote.gestureListener);
			pause_btn.setOnClickListener(blumote);
			pause_btn.setOnTouchListener(blumote.gestureListener);
			fav_btn.setOnTouchListener(blumote.gestureListener);
			fav_btn.setOnClickListener(blumote);
			btn_last.setOnTouchListener(blumote.gestureListener);
			btn_last.setOnClickListener(blumote);

			//set bundle of associated button properties
			// order is : Button name, database string identifier for btn      
			button_map = new HashMap<Integer,String>();
			button_map.put(R.id.btn_volume_up, "btn_volume_up");
			button_map.put(R.id.btn_volume_down, "btn_volume_down");
			button_map.put(R.id.btn_channel_up, "btn_channel_up");
			button_map.put(R.id.btn_channel_down, "btn_channel_down");
			button_map.put(R.id.btn_input, "btn_input");
			button_map.put(R.id.power_btn, "btn_pwr");
			button_map.put(R.id.power2_btn, "power2_btn");
			button_map.put(R.id.back_skip_btn, "back_skip_btn");
			button_map.put(R.id.back_btn, "back_btn");
			button_map.put(R.id.forward_btn, "forward_btn");
			button_map.put(R.id.skip_forward_btn, "skip_forward_btn");
			button_map.put(R.id.record_btn, "record_btn");
			button_map.put(R.id.stop_btn, "stop_btn");
			button_map.put(R.id.play_btn, "play_btn");
			button_map.put(R.id.pause_btn, "pause_btn");
			button_map.put(R.id.eject_btn, "eject_btn");
			button_map.put(R.id.disc_btn, "disc_btn");
			button_map.put(R.id.mute_btn, "mute_btn");
			button_map.put(R.id.info_btn, "info_btn");
			button_map.put(R.id.return_btn, "return_btn");
			button_map.put(R.id.pgup_btn, "pgup_btn");
			button_map.put(R.id.pgdn_btn, "pgdn_btn");
			button_map.put(R.id.guide_btn, "guide_btn");
			button_map.put(R.id.exit_btn, "exit_btn");
			button_map.put(R.id.move_right_btn, "move_right_btn");
			button_map.put(R.id.move_left_btn, "move_left_btn");
			button_map.put(R.id.fav_btn, "fav_btn");
			button_map.put(R.id.btn_last, "btn_last");
			///////////////////////////////////////////////////////////
			// NUMBERS SCREEN
			move_right_n_btn = (ImageButton) blumote.findViewById(R.id.move_right_n_btn);
			move_left_n_btn = (ImageButton) blumote.findViewById(R.id.move_left_n_btn);
			btn_n0 = (Button) blumote.findViewById(R.id.btn_n0);
			btn_n1 = (Button) blumote.findViewById(R.id.btn_n1);
			btn_n2 = (Button) blumote.findViewById(R.id.btn_n2);
			btn_n3 = (Button) blumote.findViewById(R.id.btn_n3);
			btn_n4 = (Button) blumote.findViewById(R.id.btn_n4);
			btn_n5 = (Button) blumote.findViewById(R.id.btn_n5);
			btn_n6 = (Button) blumote.findViewById(R.id.btn_n6);
			btn_n7 = (Button) blumote.findViewById(R.id.btn_n7);
			btn_n8 = (Button) blumote.findViewById(R.id.btn_n8);
			btn_n9 = (Button) blumote.findViewById(R.id.btn_n9);
			btn_dash = (Button) blumote.findViewById(R.id.btn_dash);
			btn_enter = (Button) blumote.findViewById(R.id.btn_enter);
			btn_exit = (Button) blumote.findViewById(R.id.btn_exit);
			btn_home = (ImageButton) blumote.findViewById(R.id.btn_home);
			left_btn = (ImageButton) blumote.findViewById(R.id.left_btn);
			right_btn = (ImageButton) blumote.findViewById(R.id.right_btn);
			btn_up = (ImageButton) blumote.findViewById(R.id.btn_up);
			down_btn = (ImageButton) blumote.findViewById(R.id.down_btn);
			btn_misc1 = (Button) blumote.findViewById(R.id.btn_misc1);
			btn_misc2 = (Button) blumote.findViewById(R.id.btn_misc2);
			btn_misc3 = (Button) blumote.findViewById(R.id.btn_misc3);
			btn_misc4 = (Button) blumote.findViewById(R.id.btn_misc4);
			btn_misc5 = (Button) blumote.findViewById(R.id.btn_misc5);
			btn_misc6 = (Button) blumote.findViewById(R.id.btn_misc6);
			btn_misc7 = (Button) blumote.findViewById(R.id.btn_misc7);
			btn_misc8 = (Button) blumote.findViewById(R.id.btn_misc8);

			// action listeners
			btn_n0.setOnTouchListener(blumote.gestureListener);
			btn_n0.setOnClickListener(blumote);
			btn_n1.setOnTouchListener(blumote.gestureListener);
			btn_n1.setOnClickListener(blumote);
			btn_n2.setOnTouchListener(blumote.gestureListener);
			btn_n2.setOnClickListener(blumote);
			btn_n3.setOnTouchListener(blumote.gestureListener);
			btn_n3.setOnClickListener(blumote);
			btn_n4.setOnTouchListener(blumote.gestureListener);
			btn_n4.setOnClickListener(blumote);
			btn_n5.setOnTouchListener(blumote.gestureListener);
			btn_n5.setOnClickListener(blumote);
			btn_n6.setOnTouchListener(blumote.gestureListener);
			btn_n6.setOnClickListener(blumote);
			btn_n7.setOnTouchListener(blumote.gestureListener);
			btn_n7.setOnClickListener(blumote);
			btn_n8.setOnTouchListener(blumote.gestureListener);
			btn_n8.setOnClickListener(blumote);
			btn_n9.setOnTouchListener(blumote.gestureListener);
			btn_n9.setOnClickListener(blumote);
			btn_dash.setOnTouchListener(blumote.gestureListener);
			btn_dash.setOnClickListener(blumote);
			btn_enter.setOnTouchListener(blumote.gestureListener);
			btn_enter.setOnClickListener(blumote);
			btn_exit.setOnTouchListener(blumote.gestureListener);
			btn_exit.setOnClickListener(blumote);
			btn_home.setOnTouchListener(blumote.gestureListener);
			btn_home.setOnClickListener(blumote);
			move_left_n_btn.setOnClickListener(blumote);
			move_left_n_btn.setOnTouchListener(blumote.gestureListener);
			move_right_n_btn.setOnClickListener(blumote);
			move_right_n_btn.setOnTouchListener(blumote.gestureListener);
			move_right_btn.setOnClickListener(blumote);
			move_right_btn.setOnTouchListener(blumote.gestureListener);
			left_btn.setOnClickListener(blumote);
			left_btn.setOnTouchListener(blumote.gestureListener);
			right_btn.setOnClickListener(blumote);
			right_btn.setOnTouchListener(blumote.gestureListener);
			btn_up.setOnClickListener(blumote);
			btn_up.setOnTouchListener(blumote.gestureListener);
			down_btn.setOnClickListener(blumote);
			down_btn.setOnTouchListener(blumote.gestureListener);
			btn_misc1.setOnClickListener(blumote);
			btn_misc1.setOnTouchListener(blumote.gestureListener);
			btn_misc2.setOnClickListener(blumote);
			btn_misc2.setOnTouchListener(blumote.gestureListener);
			btn_misc3.setOnClickListener(blumote);
			btn_misc3.setOnTouchListener(blumote.gestureListener);
			btn_misc4.setOnClickListener(blumote);
			btn_misc4.setOnTouchListener(blumote.gestureListener);
			btn_misc5.setOnClickListener(blumote);
			btn_misc5.setOnTouchListener(blumote.gestureListener);
			btn_misc6.setOnClickListener(blumote);
			btn_misc6.setOnTouchListener(blumote.gestureListener);
			btn_misc7.setOnClickListener(blumote);
			btn_misc7.setOnTouchListener(blumote.gestureListener);
			btn_misc8.setOnClickListener(blumote);
			btn_misc8.setOnTouchListener(blumote.gestureListener);

			// set bundle of associated button properties
			// order is : Button name, String database id for btn, graphic for
			// unpressed, graphic for pushed

			// bundle all the button data into a big hashtable
			button_map.put(R.id.btn_n0, "btn_n0");
			button_map.put(R.id.btn_n1, "btn_n1");
			button_map.put(R.id.btn_n2, "btn_n2");
			button_map.put(R.id.btn_n3, "btn_n3");
			button_map.put(R.id.btn_n4, "btn_n4");
			button_map.put(R.id.btn_n5, "btn_n5");
			button_map.put(R.id.btn_n6, "btn_n6");
			button_map.put(R.id.btn_n7, "btn_n7");
			button_map.put(R.id.btn_n8, "btn_n8");
			button_map.put(R.id.btn_n9, "btn_n9");
			button_map.put(R.id.btn_dash, "btn_dash");
			button_map.put(R.id.btn_enter, "btn_enter");
			button_map.put(R.id.btn_exit, "btn_exit");
			button_map.put(R.id.btn_home, "btn_home");
			button_map.put(R.id.move_right_n_btn, "move_right_btn");
			button_map.put(R.id.move_left_n_btn, "move_left_btn");
			button_map.put(R.id.left_btn, "left_btn");
			button_map.put(R.id.right_btn, "right_btn");
			button_map.put(R.id.btn_up, "btn_up");
			button_map.put(R.id.down_btn, "down_btn");
			button_map.put(R.id.btn_misc1, "btn_misc1");
			button_map.put(R.id.btn_misc2, "btn_misc2");
			button_map.put(R.id.btn_misc3, "btn_misc3");
			button_map.put(R.id.btn_misc4, "btn_misc4");
			button_map.put(R.id.btn_misc5, "btn_misc5");
			button_map.put(R.id.btn_misc6, "btn_misc6");
			button_map.put(R.id.btn_misc7, "btn_misc7");
			button_map.put(R.id.btn_misc8, "btn_misc8");
			/////////////////////////////////////////////////////////
			// ACTIVITY setup
			// initialize buttons
			move_right_a_btn = (ImageButton) blumote.findViewById(R.id.move_right_a_btn);
			move_left_a_btn = (ImageButton) blumote.findViewById(R.id.move_left_a_btn);
			move_left_a_btn.setOnClickListener(blumote);
			move_left_a_btn.setOnTouchListener(blumote.gestureListener);
			move_right_a_btn.setOnClickListener(blumote);
			move_right_a_btn.setOnTouchListener(blumote.gestureListener);
			
			button_map.put(R.id.move_right_a_btn, "move_right_btn");
			button_map.put(R.id.move_left_a_btn, "move_left_btn");

			

			// Find and set up the ListView
			activitiesListView = (ListView) blumote.findViewById(R.id.activities_list);
			activitiesListView.setAdapter(activities.mActivitiesArrayAdapter);
			activitiesListView.setOnItemClickListener(blumote);
			activitiesListView.setOnTouchListener(blumote.gestureListener);
	        blumote.registerForContextMenu(blumote.findViewById(R.id.activities_list));
	        
			// setup activity add button
			add_activity_btn = (Button) blumote.findViewById(R.id.add_activity_btn);
			add_activity_btn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// first clear the arraylist that keeps track if initialization entries
					blumote.activityInit.clear();
					// Launch the function to ask for a name for device
					Intent i = new Intent(blumote, EnterDevice.class);
					blumote.startActivityForResult(i, BluMote.ACTIVITY_ADD);
				}
			});
			
			// populate activites arraylist with initial items
			// need to pass in the arrayadapter we want to populate
			activities.populateActivites(true, activities.mActivitiesArrayAdapter); 
			
			///////////////////////////////////////////////
			// SPINNER setup
			// 
			device_spinner = (Spinner) blumote.findViewById(R.id.device_spinner);
			spinnerAdapter = new ArrayAdapter<String>(blumote, R.layout.spinner_entry);
			spinnerAdapter.setDropDownViewResource(R.layout.spinner_entry);
			device_spinner.setAdapter(spinnerAdapter);
			device_spinner.setOnItemSelectedListener(blumote);		
			restoreSpinner(); // restore selection from last program invocation
		}
		refreshMiscBtns(); // refresh misc button text from prefs file
	}
	
	/*
	 * called first time program initializes, just determies what the last used
	 * device was and then sets the drop-down to that item
	 */
	private void restoreSpinner() {
		populateDropDown();
		// set spinner to default from last session if possible		
		String prefs_table = blumote.prefs.getString("lastDevice", null);
		if (prefs_table != null) {
			prefs_table = prefs_table.replaceAll("_", " ");		
			for (int i = 0; i < device_spinner.getCount(); i++) {
				if (prefs_table.equals(device_spinner.getItemAtPosition(i))) {
					device_spinner.setSelection(i);
				}
			}
		}
		// always fetch buttons after we populate the drop down
		fetchButtons();
	}
	
	/**
	 * Get the button hashmap 
	 * @return the button_map hashmap of all buttons on the interface
	 */
	public HashMap<Integer,String> getButtonMap() {
		return button_map;
	}		
	
	/**
	 * sets up the drop-down list, pulls rows from DB to populate
	 */
	void populateDropDown() {
		String[] devices = blumote.device_data.getDevices();
		if (devices != null) {
			spinnerAdapter.clear(); // clear before adding
			for (int i= 0 ; i< devices.length; i++) {
				spinnerAdapter.add(devices[i]);
			}

			//put activities into drop-down
			activities.populateActivites(false, spinnerAdapter);

			// always fetch buttons after we populate the drop down
			fetchButtons();
		}
	}
	
	/**
	 * Turns the visibility of the drop-down selection on or off.
	 * Default state at initialization is for it to be on.
	 */
	void toggleDropDownVis() {
		// toggles the drop-down visibility
		if (device_spinner.getVisibility() == View.VISIBLE) {
			device_spinner.setVisibility(View.INVISIBLE);
		}
		else {
			device_spinner.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * sets dropdown to the item indicated by string parameter
	 * @param s the item that we want to set the drop-down to
	 */
	void setDropDown(String s) {		
		s = s.replace("_", " "); // need this so displays right
		for (int i = 0; i < device_spinner.getCount(); i++) {
			if (s.equals((String)device_spinner.getItemAtPosition(i))) {
				device_spinner.setSelection(i);
				break;
			}
		}
		
		s = s.replace(" ", "_"); // need this so activity prefix startsWith works
		blumote.cur_device = s; // set device to this
		
		// if we are in ACTIVITY or MAIN modes then toggle between them when
		// changing devices in the drop-down
		if (blumote.INTERFACE_STATE == Codes.INTERFACE_STATE.ACTIVITY ||
				blumote.INTERFACE_STATE == Codes.INTERFACE_STATE.MAIN) {
			if (s.startsWith(ACTIVITY_PREFIX)) { 			
				// if it is an activity then set program state to activities mode
				blumote.INTERFACE_STATE = Codes.INTERFACE_STATE.ACTIVITY;						
			}
			else { // must be regular device
				blumote.INTERFACE_STATE = Codes.INTERFACE_STATE.MAIN;			
			}
		}
		// otherwise scan if it is in the database of device configs and set it that way
		
		// update buttons
		fetchButtons();
	}
	
	/**
	 * The currently selected drop down item is returned
	 * @return the currently selected drop down
	 */
	String currentDropDown() {
		return (String)device_spinner.getSelectedItem();		
	}
	
	/**
	 * This function updates the data associated with buttons on the interface, needs 
	 * to be called anytime that data is changed.
	 */
	void fetchButtons() {
		// first update the cur_table from spinner
		if (device_spinner.getCount() > 0) {
			//DEBUG TODO
			Object spinnerItem;
			try {
				spinnerItem = device_spinner.getSelectedItem();
			} catch (Exception e) {
				spinnerItem = null;				
				setSpinnerErrorState();
			}
			if (spinnerItem != null) {
				// replace spaces with underscores and then set cur_table to
				// that				
				String table_s = spinnerItem.toString().replace(" ", "_");
				blumote.cur_device = table_s;
				// if we are in ACTIVITY or MAIN modes then toggle between them when
				// changing devices in the drop-down
				if (blumote.INTERFACE_STATE == Codes.INTERFACE_STATE.ACTIVITY ||
						blumote.INTERFACE_STATE == Codes.INTERFACE_STATE.MAIN) {
					// check if activity or a device
					if (table_s.startsWith(ACTIVITY_PREFIX)) {
						blumote.INTERFACE_STATE = Codes.INTERFACE_STATE.ACTIVITY;
						activities.setWorkingActivity(currentDropDown());
						blumote.buttons = activities.getActivityButtons(table_s);								
					}
					else { // must be a device
						blumote.INTERFACE_STATE = Codes.INTERFACE_STATE.MAIN;					
						blumote.buttons = blumote.device_data.getButtons(blumote.cur_device);					
					}
				}
				// store in NV memory so next program invocation has this
				// set as default
				Editor mEditor = blumote.prefs.edit();
				mEditor.putString("lastDevice", blumote.cur_device);
				mEditor.commit();
			} else {
				setSpinnerErrorState();								
			}
		} else {
			setSpinnerErrorState();
		}
	}		
	/**
	 * sets spinner to a default error condition
	 */
	void setSpinnerErrorState() {
		device_spinner.setSelection(0);
		blumote.buttons = null;
		blumote.INTERFACE_STATE = Codes.INTERFACE_STATE.MAIN; 
	}
	
	/**
	 * Updates the text associated with a Misc button on the interface
	 * TODO - should associate the data with the particular drop-down item selected
	 * @param return_string is the new name of the Misc button
	 * @param misc_button The misc_button name to be operated on
	 */
	void renameMisc(String return_string, String misc_button) {
		// this function should rename the misc button
		// work with the preferences file for this
		// after update preferences then refresh the misc buttons
		Editor mEditor = blumote.prefs.edit();
		mEditor.putString(misc_button, return_string); // key, value
		mEditor.commit();
		refreshMiscBtns(); // update interface with new label
	}	
	
	/**
	 * This function retrieves the underlying android resource ID associated 
	 * with a View based on the name of the item as defined in the interface.
	 * @param name the name of the View
	 * @return the integer id of that View
	 */
	int getResourceFromString(String name) {
		return blumote.getResources().getIdentifier(name,"id",blumote.getPackageName());		
	}
	
	/**
	 * Refresh the misc buttons on the interface, pulls in any modified text labels that may have been set
	 * TODO - should call this everytime the drop-down is changed in order for customized misc buttons per device to work
	 */
	void refreshMiscBtns() {
		// this stub is for refreshing the misc buttons from the preferences file
		// call this when the program first is launched and after renaming any of them
		for (int i=1; i<= NUM_MISC_BTNS; i++) {
			String misc_btn = blumote.prefs.getString(
					"btn_misc"+Integer.toString(i), null);
			if (misc_btn != null) {
				// update btn on interface with text value from prefs file
				try {
					Button btn = (Button)blumote.findViewById(
							getResourceFromString("btn_misc"+Integer.toString(i)));					
					btn.setText(misc_btn);
				} catch (Exception e) {
					// oops something didn't work, oh well
				}
			}							
				//button_map.get(misc_btn)
		}		
	}	

	/**
	 * Returns an array that contains the devices that are currently setup
	 * Note that the return value excludes activities, only devices are returned
	 * @return the CharSequence[] representing all the devices currently known
	 */
	public CharSequence[] getDropDownDevices() {
		ArrayList<String> list = new ArrayList<String>();
		if (device_spinner.getCount() > 0) {
			for (int i=0; i < device_spinner.getCount(); i++) {
				String s = (String)device_spinner.getItemAtPosition(i);
				if (!s.startsWith(ACTIVITY_PREFIX_SPACE)) { 
					// if not an activity then add to the stringbuilder
					list.add(s);
				}
			}
		}
		CharSequence[] returnData = list.toArray(new CharSequence[list.size()]);
		
		return returnData;
	}	
}
