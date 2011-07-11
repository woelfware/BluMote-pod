package com.woelfware.blumote;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.woelfware.database.Constants;

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
	static void populateActivities(boolean suppressPrefix, ArrayAdapter<String> adapter, SharedPreferences prefs) {
		Map<String,?> values = prefs.getAll();
		
		// iterate through these values
		for (String item : values.keySet()) {
			// check if prefix is an activity and this is not an initilialization item
			if (item.startsWith(ACTIVITY_PREFIX) && !item.endsWith(INIT)) {				
				if (suppressPrefix == true) {
					// remove the prefix
					item = item.replace(ACTIVITY_PREFIX, "");
				}			
				// convert underscores to spaces
				item = item.replace("_", " ");
				// add it to arraylist
				adapter.add(item);
			}
		}
	}
	
	void populateActivites(boolean suppressPrefix, ArrayAdapter<String> adapter) { 
		populateActivities(suppressPrefix, adapter, blumote.prefs);
	}	
	
	// instead of continuously recreating the activity that we are working with
	// this function allows the caller to set it once and then all functions
	// use this handle to perform their actions
	public void setWorkingActivity(String key) 	{
		workingActivity = addActivityPrefix(key);
	}
	
	public String getWorkingActivity() {
		return workingActivity;
	}
	
	// this function will make sure the activity prefix is 
	// attached to the key passed in, this format is necessary for
	// saving activity data except activity-INIT data.
	// also ensures that spaces are converted to underscores
	private static String addActivityPrefix(String key) {
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
	
	// this function will make sure the activity prefix is 
	// removed from the key passed in, this format is necessary for
	// saving activity INIT data
	// also ensures that spaces are converted to underscores
	// appends the INIT suffix to the key
	private static String formatActivityNameForInit(String key) {
		// convert spaces to underscores
		key = key.replace(" ", "_");

		if (key.startsWith(ACTIVITY_PREFIX)) {
			// remove the prefix
			key = key.replace(ACTIVITY_PREFIX, "");
		}
		if (key.endsWith(INIT)) {
			return key;
		} else {
			return key + INIT;
		}
	}
	
	
	// delete activity from the arraylist
	public void deleteActivity(int position) {
		String name = mActivitiesArrayAdapter.getItem(position);
		mActivitiesArrayAdapter.remove(name);
				
		name = addActivityPrefix(name);
		
		Editor mEditor = blumote.prefs.edit();
		// delete the activity record as well as it's associated INIT routine
		mEditor.remove(name); 	
		name = formatActivityNameForInit(name);
		mEditor.remove(name);
		
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

		s = addActivityPrefix(s);
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
	public void renameActivity(String newName, int position) { 
		String oldName = mActivitiesArrayAdapter.getItem(position);
		mActivitiesArrayAdapter.remove(oldName);
		mActivitiesArrayAdapter.add(newName);
		
		newName = addActivityPrefix(newName);
		oldName = addActivityPrefix(oldName);
		Editor mEditor = blumote.prefs.edit();
		
		// store the data from old name
		String activity = blumote.prefs.getString(oldName, null);				
		mEditor.remove(oldName); // remove old one
		mEditor.putString(newName, activity); // add new name with old data
		// Now do the same for INIT data
		oldName = formatActivityNameForInit(oldName);
		newName = formatActivityNameForInit(newName);
		String activityInit = blumote.prefs.getString(oldName, null);
		mEditor.remove(oldName); // remove old one
		mEditor.putString(newName, activityInit); // add new name with old data
		
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
	static void addActivityInitSequence(String activityName, List<String> init, SharedPreferences prefs) {
		if (activityName != null) {
			Editor mEditor = prefs.edit();

			activityName = formatActivityNameForInit(activityName);
			// convert List to a compacted csv string
			StringBuilder initItems = new StringBuilder();
			for (Iterator<String> initStep = init.iterator(); initStep.hasNext();) {
				initItems.append(initStep.next()+",");
			}
			mEditor.putString(activityName, initItems.toString()); 

			mEditor.commit();
		}
	}
	
	void addActivityInitSequence(String activityName, List<String> init) {
		addActivityInitSequence(activityName,init, blumote.prefs);
	}
	
	void addActivityInitSequence(List<String> init) {
		addActivityInitSequence(workingActivity,init);
	}
	
	// Retrieve the initialization sequence to be performed by the activity
	// returns a String[] with the elements in order (first to last)
	static String[] getActivityInitSequence(String activityName, SharedPreferences prefs) {
		activityName = formatActivityNameForInit(activityName);
		String initSequence = prefs.getString(activityName, null);
		if (initSequence == null) {
			return null;
		}
		else {
			return initSequence.split(",");
		}
	}
	
	private String[] getActivityInitSequence() {
		return getActivityInitSequence(workingActivity, blumote.prefs);
	}
	
	// this function will extract the init sequence and then execute it.
	// note that while this is running it will pop-up a 'working' dialog
	// that will stay up until the init sequence completes.
	void startActivityInitSequence(String activityName) {
		activityName = formatActivityNameForInit(activityName);
		// call getActivityInitSequence(activityName) to get the list of items to execute
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
				// extract value after the space
				String buttonID = (item.split(" ")[1]); 
				byte[] toSend = null;
				
				// need to determine if this is an activity (A)_ 
				if ( (item.split(" ")[0]).startsWith(Activities.ACTIVITY_PREFIX)) {
					String activityName = (item.split(" ")[0]); // extract value before the space
					// then need to extract lookup to real device button association..
					try {
						// Returns DeviceButton created from activity button and activity name
						DeviceButton realDevice = new DeviceButton(activityName, buttonID);
						toSend = blumote.device_data.getButton(realDevice.getDevice(), realDevice.getButton());
					} catch (Exception e) {
						// failed so don't send anything
					}
					
				}				
				else {
					// 	otherwise we can just use getButton if it is a regular device
					try {
						String device = (item.split(" ")[0]); // extract value before the space
						toSend = blumote.device_data.getButton(device, buttonID);
					} catch (Exception e) {
						// failed so don't send anything
					}
				}
				// execute button code
				if (toSend != null) {
					// always clear out PKTS_SENT since when doing an init sequence
					// we don't need to worry about flooding the pod as much...
					//TODO - do we need some sort of wait to occur if PKTS_SENT is not 0?  
					// problem is that normally PKTS_SENT gets decremented by an ACTION_UP which
					// in this case is not an option....need to either clear it or find some way to wait
					// until the ACK is received but also prevent locking up
					BluMote.PKTS_SENT = 0;
					blumote.sendButton(toSend);
				}
			}				
		} // end while	
		// check if we are done processing, if so dismiss the progress dialog
		if (initItemsIndex >= initItems.length) {
			blumote.dismissDialog(BluMote.DIALOG_INIT_PROGRESS);
		}
	} // end nextActivityInitSequence
	

	// add a new button association for an existing activity
	// ARGUMENTS:
	// String activityName: the key of the activity to associate this init sequence with
	// String btnID : is button on interface
	// String device :is an existing device name which is in the database, the second 
	// String deviceBtn :is a ID for an interface button of that device  
	// Note: when a new keybinding is added it is 'appended' to the existing bindings
	// in the prefs file
	void addActivityKeyBinding(String activityName, String btnID, String device, String deviceBtn) {
		if (activityName != null) {
			activityName = addActivityPrefix(activityName);
			String record = blumote.prefs.getString(activityName, null);
			
			// formatting of record is : btnID device deviceBtn, etc
			if (record != null) {
				// supress leading comma if null record (empty)
				record = record + ",";
			} else {
				record = "";
			}
			
			// append the new data to the existing record
			record = record + btnID + " " + device + " " + deviceBtn;
			
			// save new record into NV memory
			Editor mEditor = blumote.prefs.edit();
			mEditor.putString(activityName, record);
			mEditor.commit();
			
			// update buttons on interface
			mainint.fetchButtons();
		}
	}
	
	void addActivityKeyBinding(String btnID, String device, String deviceBtn) {
		addActivityKeyBinding(workingActivity, btnID, device, deviceBtn);
	}
	
	// removes a binding from an activity
	// ARGUMENTS:
	// String activityName : the key of the activity to be modified
	// String buttonID : the button-id of the button we want un-bound 
	void removeActivityKeyBinding(String activityName, String buttonID) {
		activityName = addActivityPrefix(activityName);
		
		String record = blumote.prefs.getString(activityName, null);
		
		if (record != null) {
			// split up the items by commas
			String[] entries = record.split(",");
			
			// after we remove the button we will have data of one less item
			String[] newEntries = new String[entries.length - 1];
			int newEntriesIndex = 0;
			
			// formatting of record is : btnID device deviceBtn, etc
			String[] buttonMap = new String[3];
			try {
				for (int i= 0; i< entries.length; i++) {
					buttonMap = entries[i].split(" ");
					if (buttonMap[2].equals(buttonID)) {					
						continue; // skip this item
					} else {
						newEntries[newEntriesIndex] = entries[i];
						newEntriesIndex++;
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				// if we get this then we tried to put too many things into newEntries[]
				// which probably means we did not find the item to delete.
				// in this case we are just going to not edit the record so we will 
				// define newEntries as equivalent to entries[]
				newEntries = entries;
			}
				
			// now that we have our newEntries[] we need to convert it into a flattened csv string
			// convert List to a compacted csv string
			StringBuilder newRecord = new StringBuilder();
			for (int i= 0 ; i < newEntries.length; i++) {
				newRecord.append(newEntries[i]+",");
			}
			
			// save new record into NV memory
			Editor mEditor = blumote.prefs.edit();
			mEditor.putString(activityName, newRecord.toString());
			mEditor.commit();
			
			// refresh interface buttons
			mainint.fetchButtons();
		} // end if						
	}	
	
	void removeActivityKeyBinding(String binding) {
		removeActivityKeyBinding(workingActivity, binding);
	}
	
	// this function will return an array of ButtonData objects
	// that hold the button codes pulled from the actual device database
	ButtonData[] getActivityButtons(String activityName) {
		activityName = addActivityPrefix(activityName);
		
		// need to loop through the activity 'record' that defines the button mappings
		// and then add each item in the proper columns of the ButtonData[] object
		String record = blumote.prefs.getString(activityName, null);
		ButtonData[] deviceButtons = null;
		if (record != null) {
			// split up the items by commas		
			String[] entries = record.split(",");

			deviceButtons = new ButtonData[entries.length];
			ActivityButton activityButton;

			for (int index = 0; index < entries.length; index++) {
				activityButton = new ActivityButton(activityName, entries[index]);
				try {
					// try to insert data from database if it exists
					deviceButtons[index] = new ButtonData(
							0, activityButton.getActivityButton(), 
							blumote.device_data.getButton(activityButton.getDeviceName(), activityButton.getDeviceButton()),
							Constants.CATEGORIES.ACTIVITY.getValue() );
				} catch (Exception e) {
					// if the call the getButtion() failed then lets just create a button with null for data
					deviceButtons[index] = new ButtonData(
							0, activityButton.getActivityButton(), 
							null,
							Constants.CATEGORIES.ACTIVITY.getValue() );
				}
				
			}
		}
		return deviceButtons;
	}	
	
	ButtonData[] getActivityButtons() {
		return getActivityButtons(workingActivity);
	}

	void setWorkingActivity(int position) {
		// uses ListView index to set the working activity
		String name = mActivitiesArrayAdapter.getItem(position);
		setWorkingActivity(name);
	}
	
	private class ActivityButton {
		private String deviceName;
		private String activityName;
		private String activityButton;
		private String deviceButton;
		
		// formatting of record is : btnID device deviceBtn, etc
		ActivityButton(String activityName, String record) {
			String[] items = record.split(" ");
			this.activityName = activityName;
			this.activityButton = items[0];
			this.deviceName = items[1];
			this.deviceButton = items[2];
		}
		
		String getActivityButton() {
			return activityButton;
		}
		
		String getDeviceButton() {
			return deviceButton;
		}
		
		String getDeviceName() {
			return deviceName;
		}
		
		@SuppressWarnings("unused")
		String getActivityName() {
			return activityName;
		}	
	}
	
	// instantiates a DeviceButton based on an activity and button
	// sets internal fields to 'null' if an error occurs during translation
	private class DeviceButton {
		private String deviceName;
		private String deviceButton;
		
		// takes an activity and activity button and converts to a device and button
		DeviceButton(String activityName, String activityButton) {
			String record = null;
			Map<String,?> values = blumote.prefs.getAll();
			
			// iterate through these values
			for (String item : values.keySet()) {
				// check if prefix is an activity
				if (item.startsWith(ACTIVITY_PREFIX)) {									
					// need to see if this is the activityName we are seeking
					if (item.equals(activityName)) {
						record = (String)values.get(item);
						break; // get out of for loop
					}
				}
			}
			
			if (record != null) {
				// convert activity buttons record to appropriate device buttons
				// split up the items by commas		
				String[] entries = record.split(",");

				ActivityButton activityBtnItem;
				for (int index = 0; index < entries.length; index++) {
					activityBtnItem = new ActivityButton(activityName, entries[index]);

					// check if this activityBtnItem matches the activityButton we are interested in
					if (activityBtnItem.getActivityButton().equals(activityButton)) {
						this.deviceButton = activityBtnItem.getDeviceButton();
						this.deviceName = activityBtnItem.getDeviceName();
						break;
					}

				}
			} // end if
			else {
				this.deviceButton = null;
				this.deviceName = null;
			}
		}
		
		String getDevice() {
			return deviceName;
		}
		
		String getButton() {
			return deviceButton;
		}
	}
}
