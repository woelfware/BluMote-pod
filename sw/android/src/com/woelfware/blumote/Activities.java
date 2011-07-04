package com.woelfware.blumote;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.widget.ArrayAdapter;

import com.woelfware.database.Constants.DB_FIELDS;

public class Activities {
	private MainInterface mainint = null;
	private BluMote blumote = null;
	ArrayAdapter<String> mActivitiesArrayAdapter = null;
	private String workingActivity = null;
	
	// prefix to put before a new activity to prefs file to find it easier
	// when searching through keys
	static final String ACTIVITY_PREFIX = "(A)_";
	private static final String INIT = "INIT";
	
	// public constructor
	// ARGUMENTS:
	// MainInterface mainint : handle to the MainInterface class that constructed this
	// Blumote blumote : handle to the Blumote class that we operate on
	public Activities(BluMote blumote, MainInterface mainint) {
		this.mainint = mainint;
		this.blumote = blumote;
		
		// Initialize array adapter
		mActivitiesArrayAdapter = new ArrayAdapter<String>(blumote,
				R.layout.manage_devices_item);
	}
	
	// updates the arrayadapter parameter with all the activities from the
	// prefs file.  boolean suppressPrefix is used to remove the 
	// ACTIVITIES_PREFIX if that is desired
	void populateActivites(boolean suppressPrefix, ArrayAdapter<String> adapter) {
		Map<String,?> values = blumote.prefs.getAll();
		
		// iterate through these values
		for (String item : values.keySet()) {
			// check if prefix is an activity
			if (item.startsWith(ACTIVITY_PREFIX)) {
				// convert underscores to spaces
				if (suppressPrefix == true) {
					// remove the prefix
					item = item.replace(ACTIVITY_PREFIX, "");
				}			
				item = item.replace("_", " ");
				// add it to arraylist
				adapter.add(item);
			}
		}
	}
	
	// instead of continuously recreating the activity that we are working with
	// this function allows the caller to set it once and then all functions
	// use this handle to perform their actions
	public void setWorkingActivity(String key) 	{
		workingActivity = processActivityKeyForFile(key);
	}
	
	public String getWorkingActivity() {
		return workingActivity;
	}
	
	// this function will convert a regular activity name
	// into a format suitable for using as a prefs file key
	private String processActivityKeyForFile(String key) {
		// convert spaces to underscores
		key = key.replace(" ", "_");
		// prepend prefix if it doesn't already exist
		if (key.startsWith(ACTIVITY_PREFIX)) {
			return key;
		}
		else {
			return ACTIVITY_PREFIX + key;
		}
	}
	
	// delete activity from the arraylist
	public void deleteActivity(int position) {
		String name = mActivitiesArrayAdapter.getItem(position);
		mActivitiesArrayAdapter.remove(name);
				
		name = processActivityKeyForFile(name);
		
		Editor mEditor = blumote.prefs.edit();
		mEditor.remove(name); 
		// TODO implement removing all variations, like INIT, etc, 
		mEditor.remove(name+INIT);
		// iterate through button_map to search for any button name suffixes
		// to clean up and remove from prefs file
		Set<Integer> toIterate = mainint.button_map.keySet();
		String entry;
		for (int i : toIterate) {
			entry = mainint.button_map.get(i);
			mEditor.remove(name+entry);
		}
		mEditor.commit();
		
		// refresh drop-down
		mainint.populateDropDown();
	}
	
	// add a new activity to the arraylist
	public void addActivity(String s) {
		// add to arraylist
		mActivitiesArrayAdapter.add(s);

		s = processActivityKeyForFile(s);
		Editor mEditor = blumote.prefs.edit();
		mEditor.putString(s, null); // key, value
		mEditor.commit();	
		
		// set workingActivity to this
		setWorkingActivity(s);
		
		// replace underscores with spaces for setDropDown()
		s = s.replace("_", " ");

		mainint.populateDropDown(); // always refresh dropdown when adding an activity
		mainint.setDropDown(s); // always set active dropdown item to new activity
		
		// set program state
		blumote.INTERFACE_STATE = Codes.INTERFACE_STATE.ACTIVITY_INIT;
				
		//TODO - launch help window if first time creating activity
			
	}
	
	// rename an activity , pass in new name and position in arraylist
	public void renameActivity(String s, int position) { 
		String old_name = mActivitiesArrayAdapter.getItem(position);
		mActivitiesArrayAdapter.remove(old_name);
		mActivitiesArrayAdapter.add(s);
		
		s = processActivityKeyForFile(s);
		old_name = processActivityKeyForFile(old_name);

		// store the data from old name
		String activity = blumote.prefs.getString(old_name, null);		
		Editor mEditor = blumote.prefs.edit();
		mEditor.remove(old_name); // remove old one
		mEditor.putString(s, activity); // add new name with old data
		mEditor.commit();
		
		mainint.populateDropDown(); // always refresh dropdown when renaming an activity
	}
	
	// add a new initialization sequence to an existing activity....
	// ARGUMENTS:
	// String key : the key of the activity to associate this init sequence with
	// List<String> init: a List of Strings representing operations to be 
	// performed as part of the activities startup/init sequence.  It is assumed
	// that the items are added to the list in order (first to last). 
	// FORMAT:
	// Delay xx : delay of xx seconds
	// Device button : 'device' represents one of the known devices in the database,
	// 'button' represents the button ID on the interface that should be sent
	void addActivityInitSequence(String key, List<String> init) {
		if (key != null) {
			Editor mEditor = blumote.prefs.edit();

			key = processActivityKeyForFile(key);
			// convert List to a compacted csv string
			StringBuilder initItems = new StringBuilder();
			for (Iterator<String> initStep = init.iterator(); initStep.hasNext();) {
				initItems.append(initStep.next());
			}
			// TODO - was working on this prior to interruptions
			mEditor.putString(key + INIT, initItems.toString()); // key, value

			mEditor.commit();
		}
	}
	
	void addActivityInitSequence(List<String> init) {
		addActivityInitSequence(workingActivity,init);
	}
	
	// Retrieve the initialization sequence to be performed by the activity
	// returns a String[] with the elements in order (first to last)
	String[] getActivityInitSequence(String key) {		
		key = processActivityKeyForFile(key);
		
		String initSequence = blumote.prefs.getString(key + INIT, null);
		
		return initSequence.split(","); 
	}
	
	String[] getActivityInitSequence() {
		return getActivityInitSequence(workingActivity);
	}
	
	// this function will extract the init sequence and then execute it.
	// note that while this is running it will pop-up a 'working' dialog
	// that will stay up until the init sequence completes.
	void executeActivityInitSequence(String key) {
		key = processActivityKeyForFile(key);
		// TODO implement this
		// call getActivityInitSequence(key) to get the list of items to execute
	}

	void executeActivityInitSequence() {
		executeActivityInitSequence(workingActivity);
	}
	
	// add a new button association for an existing activity
	// ARGUMENTS:
	// String key: the key of the activity to associate this init sequence with
	// String btnID : is button on interface
	// String device :is an existing device name which is in the database, the second 
	// String deviceBtn :is a ID for an interface button of that device  
	// Note: when a new keybinding is added it is 'appended' to the existing bindings
	// in the prefs file
	void addActivityKeyBinding(String key, String btnID, String device, String deviceBtn) {
		// TODO implement this function
		if (key != null) {
			key = processActivityKeyForFile(key);
			String test = blumote.prefs.getString(key, null);
			// make sure there is an activity of that name
			if (test != null) {

			}

			mainint.fetchButtons();
		}
	}
	
	void addActivityKeyBinding(String btnID, String device, String deviceBtn) {
		addActivityKeyBinding(workingActivity, btnID, device, deviceBtn);
	}
	
	// removes a binding from an activity
	// ARGUMENTS:
	// String key : the key of the activity to be modified
	// String binding : the button-id of the 
	void removeActivityKeyBinding(String key, String binding) {
		key = processActivityKeyForFile(key);
		// TODO implement this
		mainint.fetchButtons();
	}	
	
	void removeActivityKeyBinding(String binding) {
		removeActivityKeyBinding(workingActivity, binding);
	}
	
	// this function will return a Cursor that represents the button codes stored in the activity profile setting
	Cursor getActivityButtons(String key) {
		key = processActivityKeyForFile(key);
		String[] db_columns = new String[DB_FIELDS.values().length];
		int i = 0;
		for (DB_FIELDS field : DB_FIELDS.values()) {
			db_columns[i++] = field.getValue();
		}
		
		MatrixCursor result = new MatrixCursor(db_columns);
		// now pull data from preferences file and add rows to the cursor
		//TODO implement this
		return result;
	}
	
	Cursor getActivityButtons() {
		return getActivityButtons(workingActivity);
	}
}
