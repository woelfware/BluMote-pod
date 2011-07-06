package com.woelfware.blumote;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.woelfware.database.Constants.DB_FIELDS;

public class Activities {
	protected static boolean timerReady = true;
	private final String TAG = "Activities";
	private MainInterface mainint = null;
	private BluMote blumote = null;
	ArrayAdapter<String> mActivitiesArrayAdapter = null;
	private String workingActivity = null;
	
	// prefix to put before a new activity to prefs file to find it easier
	// when searching through keys
	static final String ACTIVITY_PREFIX = "(A)_";
	private static final String INIT = "INIT";
	
	// these member variables will deal with executing all the 
	// initialization steps
	private int initItemsIndex = 0;	
	private String[] initItems = null;
	
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
				initItems.append(initStep.next()+",");
			}
			mEditor.putString(key + INIT, initItems.toString()); 

			mEditor.commit();
		}
	}
	
	void addActivityInitSequence(List<String> init) {
		addActivityInitSequence(workingActivity,init);
	}
	
	// Retrieve the initialization sequence to be performed by the activity
	// returns a String[] with the elements in order (first to last)
	private String[] getActivityInitSequence(String key) {		
		key = processActivityKeyForFile(key);
		
		String initSequence = blumote.prefs.getString(key + INIT, null);
		
		return initSequence.split(","); 
	}
	
	private String[] getActivityInitSequence() {
		return getActivityInitSequence(workingActivity);
	}
	
	// this function will extract the init sequence and then execute it.
	// note that while this is running it will pop-up a 'working' dialog
	// that will stay up until the init sequence completes.
	void startActivityInitSequence(String key) {
		key = processActivityKeyForFile(key);
		// call getActivityInitSequence(key) to get the list of items to execute
		initItems = getActivityInitSequence();
		initItemsIndex = 0; // reset index
		
		// show progress dialog
		blumote.showDialog(BluMote.DIALOG_INIT_PROGRESS);
		
		nextActivityInitSequence();
	}

	void startActivityInitSequence() {
		startActivityInitSequence(workingActivity);
	}
	
	// this should be called after startActivityInitSequence has completed
	// this function will execute the next item in the Init sequence, if
	// no more items are available it will dismiss the progress dialog
	void nextActivityInitSequence() {
		String item;
		while (initItemsIndex < initItems.length) {
			// use initItemIndex to deal with getting through all the items
			// if run into a delay item then need to spawn CountDownTimer and then CountDownTimer
			// will call this method after it finishes...			
			item = initItems[initItemsIndex];
			initItemsIndex++;
			if (item == "") {
				// log error and continue to next item
				Log.e(TAG, "initialization item was null - malformed item");
			}
			// check if item is null and is a DELAY xx item
			else if (item.startsWith("DELAY")) {
				// extract value after the space
				String delay = (item.split(" ")[1]);
				try {
					int delayTime = Integer.parseInt(delay);
					//need to start a wait timer for this period of time
					new CountDownTimer(delayTime, delayTime) {
						public void onTick(long millisUntilFinished) {
							// no need to use this function
						}

						public void onFinish() {
							// called when timer expired
							nextActivityInitSequence(); // continue on the quest to finish the initItems
						}
					}.start();					
					break; // exit while loop while we are waiting for Delay to finish
				} catch (Exception e) {
					// failed, skip and go to next item in this case.
					Log.e(TAG, "Failed to execute an initialization delay!");
				}
			}			
			// else if the item is a button in format "Device Button"
			else {
				// extract value before the space
				String device = (item.split(" ")[0]); 
				// extract value after the space
				String buttonID = (item.split(" ")[1]); 
				byte[] toSend = blumote.device_data.getButton(device, buttonID);

				// execute button code
				blumote.sendButton(toSend);
			}				
		} // end while	
		// check if we are done processing, if so dismiss the progress dialog
		if (initItemsIndex == (initItems.length - 1)) {
			blumote.dismissDialog(BluMote.DIALOG_INIT_PROGRESS);
		}
	} // end nextActivityInitSequence
	
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
