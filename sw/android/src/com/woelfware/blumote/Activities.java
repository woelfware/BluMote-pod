package com.woelfware.blumote;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.woelfware.database.Constants;
/**
 * This class handles activities which are created to 
 * allow combining devices on the interface - like "watch a DVD"
 * and also allows for initialization sequences to be sent in a sequence
 * The data is persistend in the SharedPreferences object 
 * @author keusej
 *
 */
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
	// SUFFIX for initialization items
	private static final String INIT = "INIT";
	// SUFFIX for power off codes to store
	private static final String OFF = "_OFF";
	
	// these member variables will deal with executing all the 
	// initialization steps
	private int initItemsIndex = 0;	
	private String[] initItems = null;
	
	// these member variables will deal with sending the power off codes for an activity
	private ButtonData[] powerOffData = null;
	private int powerOffDataIndex = 0;
	
	/**
	 * 
	 * @param blumote main BluMote object to reference
	 * @param mainint MainInterface object to reference
	 */
	public Activities(BluMote blumote, MainInterface mainint) {
		this.mainint = mainint;
		this.blumote = blumote;
		
		// Initialize array adapter
		mActivitiesArrayAdapter = new ArrayAdapter<String>(blumote,
				R.layout.manage_devices_item);
	}
		
	/**
	 * updates the arrayadapter parameter with all the activities from the
	 * prefs file.  boolean suppressPrefix is used to remove the
	 * ACTIVITIES_PREFIX if that is desired
	 * @param suppressPrefix true if we want to remove the activity prefix ACTIVITY_PREFIX, 
	 * false if we don't want to suppress when adding to the ArrayAdapter
	 * @param adapter the ArrayAdapter that we want to add activities to
	 * @param prefs the SharedPreferences object that we used to store activities in
	 * 
	 */
	static void populateActivities(boolean suppressPrefix, ArrayAdapter<String> adapter, SharedPreferences prefs) {
		Map<String,?> values = prefs.getAll();
		
		// iterate through these values
		for (String item : values.keySet()) {
			// check if prefix is an activity and this is not an initilialization item
			if (item.startsWith(ACTIVITY_PREFIX)) {
				// make sure none of the suffixes are present
				if (item.endsWith(INIT) ) {
					 continue;
				}
				if (item.endsWith(OFF)) {
					continue;
				}
				boolean foundIt = false;
				for (int i=0; i< MainInterface.NUM_MISC_BTNS; i++) {
					if (item.endsWith("btn_misc" + Integer.toString(i))) {
						foundIt = true;
						break;
					}
				}
				if (foundIt) {
					continue;
				}
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
	
	/**
	 * updates the arrayadapter parameter with all the activities from the
	 * prefs file.  boolean suppressPrefix is used to remove the
	 * ACTIVITIES_PREFIX if that is desired
	 * @param suppressPrefix true if we want to remove the activity prefix ACTIVITY_PREFIX, 
	 * false if we don't want to suppress when adding to the ArrayAdapter
	 * @param adapter the ArrayAdapter that we want to add activities to
	 */
	void populateActivites(boolean suppressPrefix, ArrayAdapter<String> adapter) { 
		populateActivities(suppressPrefix, adapter, blumote.prefs);
	}	
	
	// instead of continuously recreating the activity that we are working with
	// this function allows the caller to set it once and then all functions
	// use this handle to perform their actions
	/**
	 * sets the working activity.  The Activities class depends on this being set prior to usage
	 * of any member functions.
	 * @param key The name of the activity that we want to work with
	 */
	public void setWorkingActivity(String key) 	{
		workingActivity = addActivityPrefix(key);
	}
	
	/**
	 * Receive the working activity that we set with setWorkingActivity()
	 * @return The working activity is returned
	 */
	public String getWorkingActivity() {
		return workingActivity;
	}
	
	 
	/**
	 * this function will make sure the activity prefix is 
	 * attached to the key passed in, this format is necessary for
	 * saving activity data except activity-INIT data.
	 * @param key the name of the activity that we want to operate on
	 * @return the processed key
	 *  
	 */
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
	
	/**
	 * this function will make sure the activity prefix is 
	 * removed from the key passed in, this format is necessary for
	 * saving some activity data that needs to be hidden from the user.
	 * @param key the name of the activity that we want to operate on
	 * @return the processed key
	 *  
	 */
	@SuppressWarnings("unused")
	private static String removeActivityPrefix(String key) {
		// convert spaces to underscores
		key = key.replace(" ", "_");
		// remove prefix if it doesn't already exist
		if (key.startsWith(ACTIVITY_PREFIX)) {
			return key.replace(ACTIVITY_PREFIX, "");
		}
		else {
			return key;
		}
	}

	/**
	 * This function will make sure the activity prefix is 
	 * removed from the key passed in and add the INIT suffix
	 * @param key the name of the activity we want to operate on
	 * @return the processed activity name that we passed in
	 */
	private static String formatActivityInitSuffix(String key) {
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
	
	/**
	 * This function will make sure the activity prefix is 
	 * removed from the key passed in, and add the OFF suffix
	 * @param key the name of the activity we want to operate on
	 * @return the processed activity name that we passed in
	 */
	private static String formatActivityOffSuffix(String key) {
		// convert spaces to underscores
		key = key.replace(" ", "_");

		if (key.startsWith(ACTIVITY_PREFIX)) {
			// remove the prefix
			key = key.replace(ACTIVITY_PREFIX, "");
		}
		if (key.endsWith(OFF)) {
			return key;
		} else {
			return key + OFF;
		}
	}
	
	/**
	 * delete an activity from the arraylist on the interface.  This function is usually
	 * invoked on a context menu on the arraylist that displays all the activities to the user
	 * 
	 * @param position the position in the arraylist to delete
	 */
	public void deleteActivity(int position) {
		String name = mActivitiesArrayAdapter.getItem(position);
		mActivitiesArrayAdapter.remove(name);
				
		name = addActivityPrefix(name);
		
		Editor mEditor = blumote.prefs.edit();
		// delete the activity record as well as it's associated INIT routine
		mEditor.remove(name); 	
		name = formatActivityInitSuffix(name);
		mEditor.remove(name);
		
		// iterate through button_map to search for any button name suffixes
		// to clean up and remove from prefs file
//		Set<Integer> toIterate = mainint.button_map.keySet();
//		String entry;
//		for (int i : toIterate) {
//			entry = mainint.button_map.get(i);
//			mEditor.remove(name+entry);
//		}
		mEditor.commit();
		
		// refresh drop-down
		mainint.populateDropDown();
	}
	
	/**
	 * add a new activity to the arraylist on the interface
	 * @param s the name of the activity to add to the arraylist
	 */
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
	
	/**
	 * rename an activity on the interface, pass in new name and position in arraylist.
	 * This function will update the preferences file as well as update the interface arraylist.
	 * @param newName the new name
	 * @param position the position of the old name in the arraylist
	 */
	public void renameActivity(String newName, int position) { 
		String oldName = mActivitiesArrayAdapter.getItem(position);
		mActivitiesArrayAdapter.remove(oldName);
		mActivitiesArrayAdapter.add(newName);
				
		newName = addActivityPrefix(newName);
		oldName = addActivityPrefix(oldName);
		Editor mEditor = blumote.prefs.edit();
		
		// store the data from old name
		String activity = blumote.prefs.getString(oldName, null);				
		if (activity != null) {
			mEditor.remove(oldName); // remove old one
			mEditor.putString(newName, activity); // add new name with old data
		}
		// rename all MISC buttons stored
		MainInterface.renameMiscButtons(oldName, newName, blumote.prefs);
		
		// rename all OFF data stored
		String newOFF = formatActivityOffSuffix(newName);
		String oldOFF = formatActivityOffSuffix(oldName);
		String offData = blumote.prefs.getString(oldOFF, null);
		if (offData != null) {
			mEditor.remove(oldOFF);
			mEditor.putString(oldOFF, newOFF);
		}
		
		// Now rename INIT data
		String oldINIT = formatActivityInitSuffix(oldName);
		String newINIT = formatActivityInitSuffix(newName);
		String activityInit = blumote.prefs.getString(oldINIT, null);
		mEditor.remove(oldINIT); // remove old one
		mEditor.putString(newINIT, activityInit); // add new name with old data		
		mEditor.commit();
		
		mainint.populateDropDown(); // always refresh dropdown when renaming an activity
	}		
	
	/**
	 * add a new initialization sequence to an existing activity....
	 * 	
	 * @param activityName the name of the activity we want to add init sequence to
	 * @param init the list of Strings that we want to add to the initialization int the following two formats:
	 * "Delay X" : delay of X milli-seconds
	 * "Device button" : 'device' represents one of the known devices in the database,
	 * 'button' represents the button ID on the device's interface
	 * @param prefs The shared preferences object that the data is persisted in 
	 */
	static void addActivityInitSequence(String activityName, List<String> init, SharedPreferences prefs) {
		if (activityName != null) {
			Editor mEditor = prefs.edit();
			
			// convert List to a compacted csv string
			StringBuilder initItems = new StringBuilder();
			String currentItem;
			String[] curItems;
			ArrayList<String> powerOffItems = new ArrayList<String>();
			for (Iterator<String> initStep = init.iterator(); initStep.hasNext();) {
				currentItem = initStep.next();
				initItems.append(currentItem+",");
				//check if the item is a power button, if so then need to add it to the activity power off button
				curItems = currentItem.split(" "); // split on a space delimeter
				if (curItems.length == 2) { // should have 2 elements
					// second item is the button name (if not a delay)
					if(!curItems[0].equals("DELAY")) {
						if (curItems[1].equals("power_on_btn")) { 
							powerOffItems.add(curItems[0]); // save just the device name
						}
					}
				}
			}
			
			// add the collected power off buttons to the interface
			savePowerOffButton(activityName, powerOffItems, prefs);
			
			// add INIT suffix before storing initItems to prefs file
			activityName = formatActivityInitSuffix(activityName);
			
			mEditor.putString(activityName, initItems.toString()); 

			mEditor.commit();
		}
	}
	
	/**
	 * add a new initialization sequence to an existing activity....
	 * 	
	 * @param activityName the name of the activity we want to add init sequence to
	 * @param init the list of Strings that we want to add to the initialization int the following two formats:
	 * "Delay X" : delay of X milli-seconds
	 * "Device button" : 'device' represents one of the known devices in the database,
	 * 'button' represents the button ID on the device's interface
	 */
	void addActivityInitSequence(String activityName, List<String> init) {
		addActivityInitSequence(activityName,init, blumote.prefs);
	}
	
	/**
	 * add a new initialization sequence to an existing activity....the activity to operate on
	 * is assumed to be set by setWorkingActivity()
	 * 	
	 * @param init the list of Strings that we want to add to the initialization int the following two formats:
	 * "Delay X" : delay of X milli-seconds
	 * "Device button" : 'device' represents one of the known devices in the database,
	 * 'button' represents the button ID on the device's interface
	 */
	void addActivityInitSequence(List<String> init) {
		addActivityInitSequence(workingActivity,init);
	}
	
	/**
	 * Retrieve the initialization sequence to be performed by the activity
	 * @param activityName the name of activity to retrieve init sequence of
	 * @param prefs the Shared preferences object that has the init data
	 * @return Strin[] with the elements in order (first to last)
	 */
	static String[] getActivityInitSequence(String activityName, SharedPreferences prefs) {
		activityName = formatActivityInitSuffix(activityName);
		String initSequence = prefs.getString(activityName, null);
		if (initSequence==null) {
			return null;
		}
		else {
			return initSequence.split(",");
		}
	}
	
	/**
	 * Retrieve the initialization sequence to be performed by the activity.
	 * This function assumes setWorkingActivity() was called and that the default blumote prefs file is used
	 * @return Strin[] with the elements in order (first to last)
	 */
	private String[] getActivityInitSequence() {
		return getActivityInitSequence(workingActivity, blumote.prefs);
	}
	
	/**
	 * this function will extract the init sequence and then execute it.
	 * note that while this is running it will pop-up a 'working' dialog
	 * that will stay up until the init sequence completes.
	 * @param activityName The name of the activity to execute init sequence of
	 */
	void startActivityInitSequence(String activityName) {
		activityName = formatActivityInitSuffix(activityName);
		// call getActivityInitSequence(activityName) to get the list of items to execute
		initItems = getActivityInitSequence();
		initItemsIndex = 0; // reset index
		
		// show progress dialog
		blumote.showDialog(BluMote.DIALOG_INIT_PROGRESS);
		
		nextActivityInitSequence();
	}

	/**
	 * this function will extract the init sequence and then execute it.
	 * note that while this is running it will pop-up a 'working' dialog
	 * that will stay up until the init sequence completes.
	 * The activity to use is assumed to be set by setWorkingActivity() previously.
	 */
	void startActivityInitSequence() {
		startActivityInitSequence(workingActivity);
	}
	
	/**
	 * this should be called after startActivityInitSequence has completed
	 * this function will execute the next item in the Init sequence, if
	 * no more items are available it will dismiss the progress dialog
	 */
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
	

	/**
	 * sends the power off codes with a proper delay between sends to prevent buffer overflow on pod
	 * @param powerOff
	 */
	public void sendPowerOffData(ButtonData[] powerOff) {
		
		powerOffData = powerOff;
		powerOffDataIndex = 0;
		
		nextPowerOffData();
	}
	
	public void nextPowerOffData() {
		if (powerOffDataIndex < powerOffData.length) {
			blumote.sendButton(powerOffData[powerOffDataIndex].getButtonData());
			powerOffDataIndex++;

			if (powerOffDataIndex < powerOffData.length) {
				//need to start a wait timer for this period of time
				new CountDownTimer(BluMote.MEDIUM_DELAY_TIME, BluMote.MEDIUM_DELAY_TIME) {
					public void onTick(long millisUntilFinished) {
						// no need to use this function
					}

					public void onFinish() {
						// called when timer expired			
						nextPowerOffData(); // continue on the quest to finish
					}
				}.start();					
			}

		} // end while	
	}
	
	/**
	 * add a new button association for an existing activity
	 * Note: when a new keybinding is added it is 'appended' to the existing bindings
	 * @param activityname the key of the activity to associate this init sequence with
	 * @param btnID the button id of the activity screen button that we want to bind
	 * @param device is an existing device name which is in the database
	 * @param deviceBtn is the ID for an interface button of the device that we want bound
	 */
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
	
	/**
	 * add a new button association for an existing activity
	 * Note: when a new keybinding is added it is 'appended' to the existing bindings
	 * This function assumes setWorkingActivity() was previously called
	 * @param btnID the button id of the activity screen button that we want to bind
	 * @param device is an existing device name which is in the database
	 * @param deviceBtn is the ID for an interface button of the device that we want bound
	 */
	void addActivityKeyBinding(String btnID, String device, String deviceBtn) {
		addActivityKeyBinding(workingActivity, btnID, device, deviceBtn);
	}
	
	/** 
	 * Removes a key binding from an activity button to a device button.
	 * @param activityname the activity name we want to work with
	 * @param buttonID the button id of the activity button to remove binding of
	 */
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
	
	/** 
	 * Removes a key binding from an activity button to a device button.
	 * This function assumes setWorkingActivity() was called previously.
	 * @param buttonID the button id of the activity button to remove binding of
	 */
	void removeActivityKeyBinding(String binding) {
		removeActivityKeyBinding(workingActivity, binding);
	}
	
	/**
	 * Returns the activity buttons in a ButtonData[] structure
	 * @param activityName the activity we want to return data from
	 * @return The full array of button associations
	 */
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
	
	/**
	 * Returns the activity buttons in a ButtonData[] structure
	 * This function assumes setWorkingActivity() was previously called.
	 * @return The full array of button associations
	 */
	ButtonData[] getActivityButtons() {
		return getActivityButtons(workingActivity);
	}

	/**
	 * This function sets the working activity but uses the position in the 
	 * interface arraylist to do so.
	 * @param position Position in the array list
	 */
	void setWorkingActivity(int position) {
		// uses ListView index to set the working activity
		String name = mActivitiesArrayAdapter.getItem(position);
		setWorkingActivity(name);
	}
	
	/**
	 * Checks if the button ID is valid for an initialization sequence of an activity
	 * @param buttonId the resource ID of the interface button
	 * @return true if it is valid, false if not valid
	 */
	static boolean isValidActivityButton(int buttonId) {		 
		switch (buttonId) {
		case R.id.power_off_btn:
			return false;
		
//		case R.id.move_left_a_btn:
//			return false;
//		
//		case R.id.move_right_a_btn:
//			return false;
			
//		case R.id.move_left_n_btn:
//			return false;
//			
//		case R.id.move_right_n_btn:
//			return false;
			
		default:
			return true;
		}
	}
	
	/**
	 * Takes a list of devices that have had their power buttons pushed when creating the init list for an activity
	 * and stores them for the power off button of an activity.
	 * @param activityName the name of the activity
	 * @param powerOnDevices a list of power buttons that were pushed during init sequence recording
	 * @param prefs the preferences file
	 */
	static void savePowerOffButton(String activityName, ArrayList<String> powerOnDevices, SharedPreferences prefs) {
		// convert the items to comma separated values
		StringBuilder builder = new StringBuilder();
		
		Iterator<String> iterator = powerOnDevices.iterator();
		
		while (iterator.hasNext()) {
			builder.append(iterator.next()+",");
		}
		
		Editor mEditor = prefs.edit();
		activityName = formatActivityOffSuffix(activityName);		
		mEditor.putString(activityName, builder.toString()); 			
		mEditor.commit();
	}
	
	/**
	 * Takes a list of devices that have had their power buttons pushed when creating the init list for an activity
	 * and stores them for the power off button of an activity.
	 * @param powerOnDevices a list of power buttons that were pushed during init sequence recording
	 */
	void savePowerOffButton(ArrayList<String> powerOnDevices) {
		savePowerOffButton(workingActivity, powerOnDevices, blumote.prefs);
	}	
	
	/**
	 * This function returns the power off data associated with an activities 'power off' button.
	 * This is automatically generated when setting up an activities initialization sequence and the user 
	 * pushes a power on command for a device.
	 * @param activityName the name of the activity we want to get the power off codes for
	 * @return the power off codes as an array of ButtonData objects
	 */
	ButtonData[] getPowerOffButtonData(String activityName) {
		// pull power off / toggle codes from prefs file
		// populate blumote's field with this data
		// BluMote needs to look for the power off button push and execute this data
		// this function loads 'null' if no data is stored
		try {
			activityName = formatActivityOffSuffix(activityName);
			String powerOffCodes = blumote.prefs.getString(activityName, null);
			// powerOffCodes is csv
			String[] devices = powerOffCodes.split(",");
			byte[] buttonData;
			if (devices != null) {				
				ButtonData[] returnData = new ButtonData[devices.length];
				// each token is a device name that we should issue the power command for			
				for (int i=0; i < devices.length; i++) {
					try {
						// try to insert data from database if it exists
//						String deviceName = devices[i].split(" ");
						buttonData = blumote.device_data.getButton(devices[i], blumote.button_map.get(R.id.power_on_btn));
						returnData[i] = new ButtonData( R.id.power_on_btn, blumote.button_map.get(R.id.power_on_btn),
								buttonData,	Constants.CATEGORIES.TV_DVD.getValue() );
					} catch (Exception e) {
						// if the call the getButtion() failed then lets just create a button with null for data
						returnData[i] = new ButtonData(
								0, blumote.button_map.get(R.id.power_on_btn), 
								null, Constants.CATEGORIES.TV_DVD.getValue() );
					}
				}
				
				return returnData;
			} else { // no devices in the list, so return null
				return null;
			}
		} catch (Exception e) {
			// just exit function and set blumote power-off data to null
			return null;
		}
	}
	
	/**
	 * Get the list of all the devices that have power-on commands associated with the activities'
	 * initialization list.  
	 * @param activityName the name of the activity
	 * @return the array of data or null if nothing was found
 	 */
	String[] getPowerOffDevices(String activityName) {
		// pull power off / toggle codes from prefs file
		// populate blumote's field with this data
		// BluMote needs to look for the power off button push and execute this data
		// this function loads 'null' if no data is stored
		try {
			String powerOffCodes = blumote.prefs.getString(formatActivityOffSuffix(getWorkingActivity()), null);
			// powerOffCodes is csv
			String[] devices = powerOffCodes.split(",");
			return devices;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * This is a helper class to encapsulate all the parameters of a
	 * activity button which is associated with a real device button.
	 * @author keusej
	 *
	 */
	private class ActivityButton {
		private String deviceName;
		private String activityName;
		private String activityButton;
		private String deviceButton;
		
		/**
		 * Constructor for the class
		 * @param activityName the name of the activity
		 * @param record The data that is extracted from the SharedPreferences for a particular activity
		 */
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
	
	/**
	 * Helper class to encapsulate the data associated with a real device button
	 */
	private class DeviceButton {
		private String deviceName;
		private String deviceButton;
		
		/**
		 * Takes an activity and activity button and converts to a device and button.
		 * 
		 * @param activityName the name of the activity
		 * @param activityButton the button name on the activity interface
		 */
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
