package com.woelfware.blumote;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.woelfware.blumote.Codes.Pod;
import com.woelfware.database.DeviceDB;
import com.woelfware.database.Constants.CATEGORIES;

/**
 * Primary class for the project.
 * Implements the callbacks for the buttons/etc on the interface.
 * @author keusej
 *
 */
public class BluMote extends Activity implements OnClickListener,OnItemClickListener,OnItemSelectedListener
{
	// Debugging
	@SuppressWarnings("unused")
	private static final String TAG = "BlueMote";
	static final boolean DEBUG = false;
	
	// set false for using the emulator for testing UI
	static final boolean ENABLE_BT = true;

	// Preferences file for this application
	static final String PREFS_FILE = "BluMoteSettings";
	
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_MANAGE_DEVICE = 3;
	// private static final int REQUEST_RENAME_POD = 4;
	static final int ACTIVITY_ADD = 5;	
	private static final int ACTIVITY_RENAME = 6;
	private static final int MISC_RENAME = 7;
	static final int ACTIVITY_INIT_EDIT = 8;
	private static final int PREFERENCES = 9;
	
	// Message types sent from the BluetoothChatService Handler
	static final int MESSAGE_STATE_CHANGE = 1;
	static final int MESSAGE_READ = 2;
	static final int MESSAGE_WRITE = 3;
	static final int MESSAGE_DEVICE_NAME = 4;
	static final int MESSAGE_TOAST = 5;
	// Message time for repeat-key-delay
	static final int MESSAGE_KEY_PRESSED = 6;

	// Key names received from the BluetoothChatService Handler
	static final String DEVICE_NAME = "device_name";
	static final String TOAST = "toast";

	// Dialog menu constants
	private static final int DIALOG_SHOW_INFO = 0;
	private static final int DIALOG_INIT_DELAY = 1;
	static final int DIALOG_INIT_PROGRESS = 2; 
	private static final int DIALOG_ABOUT = 3;
	private static final int DIALOG_LEARN_WAIT = 4;

	// Layout Views
	private TextView mTitle;
	// private ImageButton led_btn;
	
	// helps change interface pages
	private ViewFlipper flip;
	
	// pager is for keeping track of what page we are on
	ImageView pager;

	// Name of the connected device
	private String mConnectedDeviceName = null;

	// connecting device name - temp storage
	private String connectingDevice;
	// connecting device MAC address - temp storage
	private String connectingMAC;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;	

	// SQL database class
	DeviceDB device_data;
	// Shared preferences class - for storing config settings between runs
	SharedPreferences prefs;

	// This is the device database results for each button on grid
	// Note that when we are working with an activity then this structure
	// is updated for the button mappings but must be refreshed with getActivityButtons()
	// whenever a modification is performed to the activity button mappings
	ButtonData[] buttons;

	// Data associated with the power_off button of an activity
	ButtonData[] activityPowerOffData = null;
	
	// currently selected device
	String cur_device;
	
	// context of screen view, using this for categorizing in database
	// hardcoding this for now, will need to adjust for new contexts
	private String cur_context = CATEGORIES.TV_DVD.getValue();

	// Currently selected button resource id (for training mode operations)
	private int BUTTON_ID = 0;

	// current State of the pod bluetooth communication
	Codes.BT_STATE BT_STATE = Codes.BT_STATE.IDLE;
	
	// current interface State of program
	Codes.INTERFACE_STATE INTERFACE_STATE = Codes.INTERFACE_STATE.MAIN;
	
	// these are all in ms (milli-seconds)
	private static int LOCK_RELEASE_TIME = 5000; // timeout to release IR transmit lock if pod doesn't send us an ACK
	static int DELAY_TIME = 200; // repeat button click delay (after button held)
	private static int LONG_DELAY_TIME = 1000; // starts repeated button clicks

	// number of times that pod should repeat when button held down
	private static final byte REPEAT_IR_LONG = (byte) 150;
	
	// Flag that tells us if we are holding our finger on a button and should loop
	private static boolean BUTTON_LOOPING = false;
	
	// arraylist position of activity that we want to rename
	private static int activity_rename;
	
	// misc button id that we want to rename
	private static String misc_button;

	// a unique integer code for each time a button is pushed, used for preventing
	// a user from double pushing a button and the long timer accidentally activates
	private static int buttonPushID = 0;

	// Hash map to keep track of all the buttons on the interface and associated
	// properties
	HashMap<Integer, String> button_map;	
	
	// for holding the activity init sequence while it's being built
	ArrayList<String> activityInit = new ArrayList<String>();

	// used to convert device/activity names into IDs that do not change
	InterfaceLookup lookup;
	
	// These are used for activities display window
	private static final int ID_DELETE = 0;
	private static final int ID_RENAME = 1;
	private static final int ID_MANAGE = 2;
	ListView activitiesListView;
	
	// Menu object - initalized in onCreateOptionsMenu()
	Menu myMenu;
	
	// keep track of what the active page of buttons is
	enum Pages {
		MAIN, NUMBERS, ACTIVITIES
	}

	private Pages page = Pages.MAIN;

	private MainInterface mainScreen = null;
	private Activities activities = null;
	
	// viewflipper animations
	private Animation slide_right_anim;
	private Animation slide_left_anim;
	private Animation slide_right_out_anim;
	private Animation slide_left_out_anim;
	
	GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    
    // Flag to indicate that the next keypress is a new activity button association
    private boolean captureButton = false;
    // keeps track of what the actiivty button was that we clicked on that wanted to be associated
    // with the device button
    private String activityButton = "";

    // if the button has been pushed down recently, this prevents another button press which could overflow the
    // pod with too much button data
	private boolean buttonLock = false;   
    
    // these variables are all used in the gesture listener logic
	private static boolean isButtonTimerStarted = false;
    private static View lastView;
    private static boolean isButtonPushed;
    
    // preference : haptic button feedback
    private boolean hapticFeedback = true;
        
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		
		super.onCreate(savedInstanceState);

		// get preferences file
		prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);

		// initialize the InterfaceLookup
		lookup = new InterfaceLookup(prefs);
		
		// Get local Bluetooth adapter
		if (ENABLE_BT) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		// If the adapter is null, then Bluetooth is not supported
		if (ENABLE_BT && mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (ENABLE_BT && !mBluetoothAdapter.isEnabled() ) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the session
		}

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main_interface);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		flip=(ViewFlipper)findViewById(R.id.flipper); // flips between our screens
		
		slide_right_anim = AnimationUtils.loadAnimation(this, R.anim.slide_right);
		slide_left_anim = AnimationUtils.loadAnimation(this, R.anim.slide_left);
		slide_left_out_anim = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
		slide_right_out_anim = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
		
		// the gesture class is used to handle fling events
		gestureDetector = new GestureDetector(new MyGestureDetector());
		// the gesture listener will listen for any touch event
		gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent e) {            	
                if (gestureDetector.onTouchEvent(e)) { // check if it is a fling event
                	isButtonPushed = false;
                    return true;
                }                
                else // non fling event
                {                     	
                	if (button_map.get(v.getId()) != null) {
                		// if this is a valid button press then execute the button logic
                		if (e.getAction() == MotionEvent.ACTION_DOWN) {
                			isButtonPushed = true;
                			buttonPushID++;
                			// check if buttonTimerStarted is not started
                			if (!isButtonTimerStarted) {
                				isButtonTimerStarted = true;
                				lastView = v; // save the last view 
                				// provide haptic feedback on button click if that preference is set
                				if (hapticFeedback) {
                					lastView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                				}
                				// fire off count down timer before executing button press (ms)
                				new ButtonCountDown(LONG_DELAY_TIME, buttonPushID) {
                					public void onFinish() {
                						// called when timer expired
                						isButtonTimerStarted = false;
                						if ((getButtonID() == buttonPushID) && isButtonPushed) {
                							// if this was the original button push we started with
                							// and button is still being pushed then execute the long button push
                							executeButtonLongDown();        
                						}
                					}
                				}.start();			
                			} 
                		}  // END if (e.getAction() == MotionEvent.ACTION_DOWN

                		// only execute this one time and only if not in learn mode
                		// if we don't have !LOOP_KEY you can hit button multiple times
                		// and hold finger on button and you'll get duplicates
                		if (e.getAction() == MotionEvent.ACTION_UP) {
                			isButtonTimerStarted = false;
                			isButtonPushed = false; 
                			// if we were doing a long press, 
							// make sure that we exit repeat mode
                			if (BUTTON_LOOPING) {  	
                				sendCode(Pod.ABORT_TRANSMIT); 
                				BUTTON_LOOPING = false;
                			}
                		}
                	}
                } // END else
                return false; // allows XML to consume
            } // END onTouch(View v, MotionEvent e)
		}; // END gestureListener

		flip.setOnTouchListener(gestureListener);

		pager = (ImageView)findViewById(R.id.pager);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		// get SQL database class
		device_data = new DeviceDB(this);
		device_data.open();
		
		// refresh the haptic feedback pref
		SharedPreferences myprefs = PreferenceManager.getDefaultSharedPreferences(this);
		hapticFeedback = myprefs.getBoolean("hapticPREF", true);
		
		// Load the last pod that we connected to, onResume() will try to connect to this
		connectingMAC = prefs.getString("lastPod", null);
	}	
	
	/**
	 * Execute a button long press event.  This is called after a timer expires.
	 * The button will repeat for as long as the user keeps their finger on the button.
	 */
	protected void executeButtonLongDown() {		
		
		// This method entered if the long key press is triggered from countdown timer,
		// also if we re-launch the method after a short delay time which is done if 
		// the user is still holding the button down.
		if (isButtonPushed) { // check if user moved finger off button before firing button press
			// indicate to onClick() that we are in repeat key mode, prevents double click of final release
			BUTTON_LOOPING = true;
			// check if it is a navigational button
			// checkNavigation returns true if it is a navigation button
			if (mainScreen.isNavigationButton(lastView.getId())) {
				// execute navigation
				executeNavigationButton(lastView.getId());
			} else if (INTERFACE_STATE == Codes.INTERFACE_STATE.ACTIVITY || 
					INTERFACE_STATE == Codes.INTERFACE_STATE.MAIN) {				
				sendButton(lastView.getId());
				// start a new shorter timer that will call this method
				buttonPushID++; // increment global button push id
				new ButtonCountDown(DELAY_TIME, buttonPushID) {
					public void onFinish() {
						// called when timer expired
						if (getButtonID() == buttonPushID) {
							// if same push ID then execute this function again
							executeButtonLongDown();
						}
					}
				}.start();
			}		
		} else {
			BUTTON_LOOPING = false;
			// send abort IR transmit command if button not being held any longer
			sendCode(Pod.ABORT_TRANSMIT);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (mChatService == null) { // then first time this was called
			// Initialize the BluetoothChatService to perform bluetooth
			// connections
			mChatService = new BluetoothChatService(this, mHandler);						
			
			// instantiate button screen helper classes
			mainScreen = new MainInterface(this);
			// instantiate activities helper class
			activities = new Activities(this, mainScreen);
			
			// setup interface
			mainScreen.initialize(activities);		
			button_map = mainScreen.getButtonMap();
			
			flip.showNext(); // start out one screen to the right (main)
			// context menu on array list
			registerForContextMenu(findViewById(R.id.activities_list));
		}
	}
	
	@Override
	protected synchronized void onResume() {
		super.onResume();
		device_data.open(); // make sure database open

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				if (ENABLE_BT) {
					mChatService.start();
				}
			}
		}

		// setup spinner (need this in case we removed the spinner item from a
		// call to managedevices)
		mainScreen.fetchButtons(); // update buttons from DB

		if (ENABLE_BT) {
			// See if the bluetooth device is connected, if not try to connect
			if (mBluetoothAdapter.isEnabled()) {
				if ( (mChatService.getState() != BluetoothChatService.STATE_CONNECTING) &&
						(mChatService.getState() != BluetoothChatService.STATE_CONNECTED)) {
					// Get the BLuetoothDevice object
					if (connectingMAC != null) {
						BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(connectingMAC);
						// Attempt to connect to the device
						mChatService.connect(device);
					}
				}
			}
		}
	}

	@Override
	protected synchronized void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();

		// if (mChatService != null) mChatService.stop();
		// close sqlite database connection
		device_data.close();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
	}

	// this is called after resume from another full-screen activity
	@Override
	protected void onRestart() {
		super.onRestart();

		device_data.open();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	/**
	 * move screen to the left	
	 */
	private void moveLeft() {
		BUTTON_LOOPING = false;
		
		// setup flipper animations
		flip.setInAnimation(slide_right_anim); // -100 -> 0
		flip.setOutAnimation(slide_left_out_anim); // 0 -> 100
		
		switch (page) {
		case MAIN:
			flip.showPrevious();
			page = Pages.ACTIVITIES;
			// set pager to left
			pager.setImageDrawable(getResources().getDrawable(R.drawable.left_circle));
			return;

		case ACTIVITIES:
			return;

		case NUMBERS:
			flip.showPrevious();
			page = Pages.MAIN;
			// set pager to center
			pager.setImageDrawable(getResources().getDrawable(R.drawable.middle_circle));
			return;
		}
	}
	
	/**
	 * move screen to the right
	 */
	private void moveRight() {
		BUTTON_LOOPING = false;
		
		// setup flipper animations
		flip.setInAnimation(slide_left_anim); // 100 -> 0
		flip.setOutAnimation(slide_right_out_anim); // 0 -> -100
		//flip.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_right));
		
		switch (page) {
		case MAIN:
			flip.showNext();
			page = Pages.NUMBERS;
			// set pager to right
			pager.setImageDrawable(getResources().getDrawable(R.drawable.right_circle));
			return;

		case ACTIVITIES:
			flip.showNext();
			page = Pages.MAIN;
			// set pager to middle
			pager.setImageDrawable(getResources().getDrawable(R.drawable.middle_circle));
			return;

		case NUMBERS:
			return;
		}
	}
	
	
	/**
	 * Execute the movement of a navigational button
	 * THIS IS CURRENTLY DEPRACATED SINCE NAVIGATION BUTTONS WERE REMOVED FROM INTERFACE
	 * @param buttonID the button name
	 */
	public void executeNavigationButton(int buttonID) {
		String buttonName = button_map.get(buttonID);
		if (buttonName != null) {
			try {
				// see if we have a navigation page move command....
				if (buttonName == "move_left_btn") {
					moveLeft();
					return;
				}
				// check if the navigation move_right was pushed
				// this only works when we are in main screen
				if (buttonName == "move_right_btn") {
					moveRight();
					return;
				}
			} catch (Exception e) {
				// do nothing				
			}
		}
		return;
	}	
	
	// interface implementation for buttons
	public void onClick(View v) {
		BUTTON_ID = v.getId(); // save Button ID - besides this function, also referenced in storeButton()
								// when a new button is learned	
		String buttonName = null;
		buttonName = button_map.get(BUTTON_ID); // convert ID to button name
		if (mainScreen.isNavigationButton(BUTTON_ID)) {
			return;  // navigation buttons don't need an onClick handler
		}
		if (INTERFACE_STATE == Codes.INTERFACE_STATE.RENAME_STATE) {
			// store the button that we want to update if it is a valid misc key
			// if it isn't then exit and Toast user, change state back to idle
			if (buttonName.startsWith(MainInterface.BTN_MISC)) {
				// if compare works then we can go ahead and implement the rename
				misc_button = buttonName;
				// launch window to get new name to use
				Intent i = new Intent(BluMote.this, EnterDevice.class);
				startActivityForResult(i, MISC_RENAME);
			}
			else {
				Toast.makeText(BluMote.this, "Not a valid Misc button, canceling", 
						Toast.LENGTH_SHORT).show();
			}

			INTERFACE_STATE = Codes.INTERFACE_STATE.MAIN; // reset state in any case
		} else if (INTERFACE_STATE == Codes.INTERFACE_STATE.ACTIVITY_EDIT) {
			if (Activities.isValidActivityButton(BUTTON_ID)) {
				if (captureButton) {
					// if we are in this mode then what we want to do is to associate the button that was
					// pushed (from a device) with the original activity button

					// activityButton holds the original activity button we want to associate to
					//addActivityKeyBinding(String btnID, String device, String deviceBtn)
					activities.addActivityKeyBinding(activityButton, cur_device, button_map.get(BUTTON_ID));
					captureButton = false; 
					// make sure to jump back to original activity and then re-show the drop-down				
					mainScreen.setDropDown(activities.getWorkingActivity());				
					mainScreen.toggleDropDownVis();	
					Toast.makeText(this, "Button associated with device",
							Toast.LENGTH_SHORT).show();
				}
				else {
					// else we want to associate a new button on the activity interface
					final CharSequence[] items = mainScreen.getDropDownDevices();

					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Pick an existing device");
					builder.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							// items[item] is the name of the item that was selected
							// need to set the drop-down to this and then hide the drop-down and switch to 
							// this device for the next step.  Also set the global flags for the button that
							// we are working on and the flag that indicates we are waiting for a keypress
							captureButton = true;
							activityButton = button_map.get(BUTTON_ID);		
							activities.setWorkingActivity(mainScreen.getCurrentDropDown());
							// switch to the selected device
							INTERFACE_STATE = Codes.INTERFACE_STATE.MAIN; // setting drop-down only works in ACTIVITY/MAIN modes
							mainScreen.setDropDown(items[item].toString());
							INTERFACE_STATE = Codes.INTERFACE_STATE.ACTIVITY_EDIT;
							mainScreen.toggleDropDownVis();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
				}
			} else {
				Toast.makeText(this, "Sorry you can't use that button",
						Toast.LENGTH_SHORT).show();
			}
		} else if (INTERFACE_STATE == Codes.INTERFACE_STATE.ACTIVITY_INIT) {
			// store init entries by device, button-id
			if (Activities.isValidActivityButton(BUTTON_ID)) {
				activityInit.add(cur_device+" "+button_map.get(BUTTON_ID));
				Toast.makeText(this, "Button press added to initialization list!",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Invalid button!",	Toast.LENGTH_SHORT).show();
			}
		} else if (INTERFACE_STATE == Codes.INTERFACE_STATE.LEARN) {
			sendCode(Codes.Pod.LEARN);
			showDialog(DIALOG_LEARN_WAIT);
			
		} else { 
			if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {				
				if (BUTTON_LOOPING == false) {
					sendButton(BUTTON_ID);
				}
			} else {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
				.show();
			}
		}
	}			

	/**
	 * Set the current button_map that contains all the data for the buttons on the interface 
	 * to a new map.
	 * @param map the new map to use
	 */
	protected void setButtonMap(HashMap<Integer, String> map) {
		button_map = map;
	}

	/**
	 * Ensure that the bluetooth device is discoverable, if it is not it requests it
	 */
	@SuppressWarnings("unused")
	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends the byte[] to the currently connected bluetooth device
	 * @param message the byte[] to send
	 */
	private void sendMessage(byte[] message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		
		// Check that there's actually something to send
		if (message.length > 0) {
			// Get the message bytes and tell the BluetoothChatService to
			// write
			byte[] send = message;
			mChatService.write(send);
		}		
	}

	/**
	 * The Handler that gets information back from other activities/classes
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					// Store the address of connecting device to preferences for
					// re-connect on resume
					// this global gets setup in onActivityResult()
					Editor mEditor = prefs.edit();
					mEditor.putString("lastPod", connectingMAC);
					
					// first need to pull current known devices list so we can
					// append to it
					if (connectingDevice != null) {
//						prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
						String prefs_table = prefs.getString("knownDevices",null);

						// then pull name of device off and append
						if (prefs_table == null) {
							prefs_table = connectingDevice; // '\t' is the
															// delimeter between
															// items
						} else {
							// make sure isn't already in the list
							String devices[] = prefs_table.split("\t");
							boolean foundIt = false;
							for (String device : devices) {
								if (device.equals(connectingDevice)) {
									foundIt = true;
									break;
								}
							}
							if (foundIt == false) {
								prefs_table = prefs_table + "\t"
										+ connectingDevice; // '\t' is the
															// delimeter between
															// items
							}
						}
						mEditor.putString("knownDevices", prefs_table);
						// commit changes to the database
						mEditor.commit();
					}
					break;

				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;

//				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;

			case MESSAGE_WRITE:
				break;

			case MESSAGE_READ:
				// arg1 is the # of bytes read
				byte[] readBuf = (byte[]) msg.obj;
				interpretResponse(readBuf, msg.arg1, msg.arg2);
				break;

			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				// see if there is a user-defined name attached to this device name
				mConnectedDeviceName = PodListActivity.translatePodName(
						mConnectedDeviceName, prefs);
				break;

			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;			
			}
		}
	};	
	
	// called when activities finish running and return to this activity
	// strangely this is called BEFORE the onResume() function
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// when returning from activity, make sure database is opened again
		device_data.open();
		
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// check if we are requesting to connect to a different pod
//				String curAddress = prefs.getString("lastPod", null);
				// Get the device MAC address
				connectingMAC = data.getExtras().getString(
						PodListActivity.EXTRA_DEVICE_ADDRESS);
				connectingDevice = data.getExtras().getString(
						PodListActivity.EXTRA_DEVICE_NAME);
				// the onResume() function will connect to the "lastPod" item,
				// it is called after this function completes.
//				if (curAddress == null) {
//					// if null we know we have not connected to anything before
//					connectNewPod(data);
//				}
//				else if ( !curAddress.equals(connectingMAC) ) {
//					// if doesn't match from what we were connected to before
//					connectNewPod(data);
//				} 
			}
			break;

		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled
				//setupDefaultButtons();
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;

		case REQUEST_MANAGE_DEVICE:
			// when the manage devices activity returns
			if (resultCode == Activity.RESULT_OK) {
				// refresh drop-down items				
				mainScreen.populateDropDown();
				// refresh InterfaceLookup
				lookup.refreshLookup();
			}
			break;

		case ACTIVITY_ADD:
			if (resultCode == Activity.RESULT_OK) {
				// add the new item to the database
				Bundle return_bundle = data.getExtras();
				if (return_bundle != null) {
					String return_string = return_bundle.getString("returnStr");
					
					// Add item to list
					activities.addActivity(return_string);							
				}				
			}
			break;
			
		case ACTIVITY_RENAME:
			if (resultCode == Activity.RESULT_OK) {
				Bundle return_bundle = data.getExtras();
				if (return_bundle != null) {
					String return_string = return_bundle.getString("returnStr");
					activities.renameActivity(return_string, activity_rename);					
				}				
			}
			break;
		
		case MISC_RENAME:
			if (resultCode == Activity.RESULT_OK) {
				Bundle return_bundle = data.getExtras();
				if (return_bundle != null) {
					String return_string = return_bundle.getString("returnStr");
					mainScreen.renameMisc(return_string, misc_button);
				}
			}
			break;
			
		case ACTIVITY_INIT_EDIT:
			if (resultCode == Activity.RESULT_OK) {
				Bundle return_bundle = data.getExtras();
				if (return_bundle != null) {
					String request = return_bundle.getString("returnStr");				
					if (request.equals(ActivityInitEdit.REDO)) {
						// check if the "REDO" was requested, if so re-enter ACTIVITY_INIT mode
						activityInit.clear();
						INTERFACE_STATE = Codes.INTERFACE_STATE.ACTIVITY_INIT;
					}
					else if (request.equals(ActivityInitEdit.APPEND) ) {
						// append to end of existing init items
						activityInit.clear();						
						// get list of activity init items into local data structure
						// assumption here is setWorkingActivity was called prior 
						// to launching the intent that got us here
						String[] newInitItems = 
							Activities.getActivityInitSequence(activities.getWorkingActivity(), prefs);		
						if (newInitItems != null) {
							for (int i=0; i< newInitItems.length; i++) {
								activityInit.add(newInitItems[i]);
							}
						}
						// enter ACTIVITY_INIT mode to begin adding more items
						INTERFACE_STATE = Codes.INTERFACE_STATE.ACTIVITY_INIT;
					}
					// otherwise just go back into normal activity mode
				}								
			}
			break;
			
		case PREFERENCES:
			// refresh the haptic feedback pref
			SharedPreferences myprefs = PreferenceManager.getDefaultSharedPreferences(this);
			hapticFeedback = myprefs.getBoolean("hapticPREF", true);			
			break;
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// save the menu so we can change it depending on context
		myMenu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// called everytime menu is shown
		menu.clear();
		
		if (INTERFACE_STATE == Codes.INTERFACE_STATE.ACTIVITY_EDIT) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.activity_edit_menu, menu);
		}
		else if (INTERFACE_STATE == Codes.INTERFACE_STATE.ACTIVITY_INIT) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.initialization_menu, menu);
		}
		else {	
			if (INTERFACE_STATE == Codes.INTERFACE_STATE.ACTIVITY) {
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.activities_menu, menu);
			} else { // use default menu (MAIN menu)
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.options_menu, menu);
				if (BT_STATE == Codes.BT_STATE.LEARN) {
					// if we are currently in learn mode, then offer up the 'cancel learn' item
					menu.findItem(R.id.stop_learn).setVisible(true);
					menu.findItem(R.id.learn_mode).setVisible(false);
				} else {
					// else hide stop learn and show learn
					menu.findItem(R.id.stop_learn).setVisible(false);
					menu.findItem(R.id.learn_mode).setVisible(true);;															
				}
			}			
			if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
				menu.findItem(R.id.disconnect).setVisible(true);
				menu.findItem(R.id.scan).setVisible(false);
			} else {
				menu.findItem(R.id.disconnect).setVisible(false);
				menu.findItem(R.id.scan).setVisible(true);
			}	
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i; 
		switch (item.getItemId()) {
		case R.id.scan:
			// first stop any connecting process if it is running
			if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
				mChatService.stop();
			}
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, PodListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		
		case R.id.disconnect:
			// disconnect the bluetooth link
			mChatService.stop();
			return true;
			
		case R.id.about:
			showDialog(DIALOG_ABOUT);
			return true;
			
		case R.id.preferences:
			// display the preferences
			Intent prefsIntent = new Intent(this,MyPreferences.class);
			startActivityForResult(prefsIntent, PREFERENCES);
			return true;
			
		case R.id.backup:
			// start the backup to the SD card	    
			ExportDatabaseFileTask backupStuff = new ExportDatabaseFileTask(this);
			backupStuff.execute("");
			// now backup prefs file
			exportPreferences();
			return true;

		case R.id.restore:
			// restores a backup from the SD card of databases and prefs file
			if (device_data.restore() && importPreferences() ) {
				Toast.makeText(this, "Successfully restored!", Toast.LENGTH_SHORT).show();
				// get preferences file
				prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);

				// initialize the InterfaceLookup
				lookup = new InterfaceLookup(prefs);	
				
				// populate activities arraylist with initial items
				// need to pass in the arrayadapter we want to populate
				activities.populateActivites(true, activities.mActivitiesArrayAdapter); 
			} else {
				Toast.makeText(this, "Import failed!", Toast.LENGTH_SHORT).show();
			}
			mainScreen.populateDropDown();
			return true;
			
		/*	This function removed except for debug cases
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		*/
		
		// this is to manage the button configurations in database
		case R.id.manage_devices:
			// need to launch the manage devices view now
			i = new Intent(this, ManageDevices.class);
			startActivityForResult(i, REQUEST_MANAGE_DEVICE);
			return true;

		case R.id.get_info:
			sendCode(Codes.Pod.GET_VERSION);
			return true;

		case R.id.learn_mode:
			// TODO need to make sure that user has a device selected in the drop down before
			// entering learn mode
			if (mainScreen.getCurrentDropDown() != null) {
				Toast.makeText(this, "Select button to train", Toast.LENGTH_SHORT).show();
				BT_STATE = Codes.BT_STATE.LEARN;
				INTERFACE_STATE = Codes.INTERFACE_STATE.LEARN;
			} else {
				Toast.makeText(this, "You must have a device selected in the drop-down menu!", Toast.LENGTH_SHORT).show();
			}
			return true;

		case R.id.stop_learn:
			Toast.makeText(this, "Stopped Learning", Toast.LENGTH_SHORT).show();
			sendCode(Codes.Pod.ABORT_LEARN);
			BT_STATE = Codes.BT_STATE.ABORT_LEARN;
			INTERFACE_STATE = Codes.INTERFACE_STATE.MAIN;
			return true;
			
		case R.id.rename_misc:
			Toast.makeText(this, "Select Misc Button to rename", Toast.LENGTH_SHORT).show();
			INTERFACE_STATE = Codes.INTERFACE_STATE.RENAME_STATE;
			return true;
			
		case R.id.activity_edit_init:
			// save menu item that was selected, if a "REDO" is requested
			// then need this information to set the currently selected activity
			activities.setWorkingActivity(mainScreen.getCurrentDropDown());
			startActivityEdit();
			return true;
			
		case R.id.activity_associate_btn:
			INTERFACE_STATE = Codes.INTERFACE_STATE.ACTIVITY_EDIT;
			Toast.makeText(this, "Press a button to associate with a device...", Toast.LENGTH_SHORT).show();
			return true;
			
		case R.id.insert_delay:
			// launch a selector window to ask for how long of a delay
			// then store this result in the activityInit List
			// else we want to associate a new button on the activity interface
			showDialog(DIALOG_INIT_DELAY);
			return true;
			
		case R.id.end_init:
			INTERFACE_STATE = Codes.INTERFACE_STATE.ACTIVITY_EDIT; // next step is associating buttons
			// when ending the activity init then we should store the init sequence to the prefs file
			activities.addActivityInitSequence(activityInit);
			// after we add it then we need to clear out the activityInit
			activityInit.clear();
			Toast.makeText(this, "Press a button to associate with a device...", Toast.LENGTH_SHORT).show();
			// now put us back into the original activity for the drop-down
			mainScreen.setDropDown(activities.getWorkingActivity());
			return true;
			
		case R.id.end_activity_edit:
			INTERFACE_STATE = Codes.INTERFACE_STATE.ACTIVITY; // go to activity mode (to use new activity)
			Toast.makeText(this, "Done editing activity", Toast.LENGTH_SHORT).show();
			// now put us back into the original activity for the drop-down
			mainScreen.setDropDown(activities.getWorkingActivity());
		}

		return false;
	}

	private String getDataDir() { 
		try { 
			PackageInfo packageInfo = 
				getPackageManager().getPackageInfo(getPackageName(), 0); 
			if (packageInfo == null) return null; 
			ApplicationInfo applicationInfo = 
				packageInfo.applicationInfo; 
			if (applicationInfo == null) return null; 
			if (applicationInfo.dataDir == null) return null; 
			return applicationInfo.dataDir; 
		} catch (NameNotFoundException ex) { 
			return null; 
		} 
	} 

	private boolean importPreferences() {   
        
		File sd = Environment.getExternalStorageDirectory();
		File currentDB = new File(getDataDir(),"/shared_prefs/"+PREFS_FILE+".xml");
        File backupDB = new File(sd, BluMote.PREFS_FILE+".bak");

        if (backupDB.exists()) {
        	try {
        		Utilities.FileUtils.copyFile(backupDB, currentDB);	       
        	} catch (IOException e) {
        		Log.e("IMPORT",e.getMessage(),e);
        		Toast.makeText(this, "Import failed", Toast.LENGTH_SHORT).show();
        		return false;
        	}
	    }
	    return true;
	}
	
	private boolean exportPreferences() {
		// backup the preferences file for activities/etc
		File sd = Environment.getExternalStorageDirectory();
        File prefsFile = new File(getDataDir(),"/shared_prefs/"+PREFS_FILE+".xml");
        File backupDB = new File(sd,BluMote.PREFS_FILE+".bak");
        
        if (prefsFile.exists()) {
        	try {
            	backupDB.createNewFile();
            	Utilities.FileUtils.copyFile(prefsFile, backupDB);
            } catch (IOException e) {
            	Log.e("BACKUP",e.getMessage(),e);
            	Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        		return false;  
            }
        }
        return true;
	}
	
	void startActivityEdit() {
		Intent i = new Intent(this, ActivityInitEdit.class);
		// tack on data to tell the activity what the "activities" item is
		i.putExtra(ActivityInitEdit.ACTIVITY_NAME, activities.getWorkingActivity());
		startActivityForResult(i, ACTIVITY_INIT_EDIT);
		
		// set selected drop-down item to the item being managed
		mainScreen.setDropDown(activities.getWorkingActivity());
	}
	
	/**
	 * OnItemSelectedListener interface definition
	 * called when user selects an item in the drop-down
	 * @param parent
	 * @param view the view of the arraylist
	 * @param pos the position in the arraylist that was selected
	 * @param id the resource-ID of the arraylist that was operated on
	 */
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {		
		// setup interface buttons appropriately		
		mainScreen.fetchButtons();
	}	

	/**
	 * Part of the OnItemSelectedListener interface, not used for this appliaction
	 */
	public void onNothingSelected(AdapterView<?> parent) {
		// Do nothing.
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case ID_DELETE:
			activities.deleteActivity((int)info.id);
			mainScreen.populateDropDown();
			return true;
			
		case ID_RENAME:
			// store ID of the item to be renamed
			activity_rename = (int)info.id;
			//launch window to get new name to use
			Intent i = new Intent(this, EnterDevice.class);
			startActivityForResult(i, ACTIVITY_RENAME);			
			return true;
			
		case ID_MANAGE:					
			// save menu item that was selected, if a "REDO" is requested
			// then need this information to set the currently selected activity
			activities.setWorkingActivity((int)info.id);
			startActivityEdit();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	// context menu is for activities list
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.activities_list) {
			// AdapterView.AdapterContextMenuInfo info =
			// (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle("Menu");
			menu.add(0, ID_DELETE, 0, "Delete Activity");
			menu.add(0, ID_RENAME, 0, "Rename Activity");
			menu.add(0, ID_MANAGE, 0, "Change Startup");
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/**
	 * The on-click listener for all devices in the activities ListViews
	 * @param av
	 * @param v The View object of the listview
	 * @param position the position that was clicked in the listview
	 * @param id the resource-id of the listview
	 */
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {		
		
		// Change to activities state after init is run
		INTERFACE_STATE = Codes.INTERFACE_STATE.ACTIVITY;
		
		// extract the name of activity that was selected
		String activity = ((TextView)v).getText().toString();
		
		// set the working activity before using any activities functions
		activities.setWorkingActivity(activity);
		
		// begin executing init sequence
		activities.startActivityInitSequence(activity);
		
		activity = MainInterface.ACTIVITY_PREFIX + activity;
		mainScreen.setDropDown(activity); // set drop down to selected item
		
	}

	/**
	 * This function sends the code to the pod based on the button 
	 * that was selected.
	 * @param buttonID The resource ID of the button that was pushed
	 */
	protected void sendButton(int buttonID) {		
		boolean foundIt = false;
		if (buttonID == R.id.power_off_btn) {
			// if this is an activity power off button, then treat differently
			ButtonData[] powerOff = activities.getPowerOffButtonData(mainScreen.getCurrentDropDown());
			// send all the data that we retrieved
			if (powerOff != null && powerOff.length > 0) {
				activities.sendPowerOffData(powerOff);
			}
		}
		else if (buttons != null && buttons.length > 0) {
			String buttonName = button_map.get(buttonID);
			if (buttonName != null) {
				for (int i=0; i < buttons.length; i++) {
					if ( buttonName.equals(buttons[i].getButtonName()) ) {
						// then extract the button data out to be sent, check if data is non-null
						byte[] code = buttons[i].getButtonData();
						if (code != null) {
							foundIt = true;
							sendButtonCode(code);
						}
					}				 
				}
			}
			if (!foundIt) {
				Toast.makeText(this, "No IR code found!", Toast.LENGTH_SHORT).show();
			}
		} else {
			// if not looping let user know button not setup
			if (!BUTTON_LOOPING) {
				Toast.makeText(this, "Button not setup!", Toast.LENGTH_SHORT).show();
			}
		}		
	}
	
	/**
	 * this function sends the byte[] for a button to the pod
	 * @param code the IR code data to send
	 */
	protected void sendButtonCode(byte[] code) {
		if (!buttonLock && code != null) { // make sure we have not recently sent a button
			buttonLock = true;
			//create a new timer to avoid flooding pod with button data
			new CountDownTimer(LOCK_RELEASE_TIME, LOCK_RELEASE_TIME) {
				public void onTick(long millisUntilFinished) {
					// no need to use this function
				}
				public void onFinish() {
					// called when timer expired
					buttonLock = false; // release lock
				}
			}.start();	
			
			byte command = (byte) (Codes.Pod.IR_TRANSMIT);
			byte[] toSend = new byte[code.length + 1]; // 1 extra byte for command byte
			toSend[0] = command;
			for (int j = 1; j < toSend.length; j++) {
				// insert different repeat flags based on if this
				// is a long press or a short press of the button
				if (BUTTON_LOOPING && j == 1) {
					toSend[j] = REPEAT_IR_LONG; // long press
				} else if (!BUTTON_LOOPING && j == 1) {
					toSend[j] = 0; // short press
				} else {
					toSend[j] = code[j - 1];
				}
			}
			// {command, 0x00, code}; // 0x00 is reserved byte
			BT_STATE = Codes.BT_STATE.IR_TRANSMIT;

			sendMessage(toSend); // send data if matches
		}
	}
	
	/**
	 * This function sends the command codes to the pod, it is used for everything except 
	 * the button data which uses {@link sendButton()}
	 * @param code
	 */
	protected void sendCode(int code) {
		byte[] toSend;
		switch (code) {
		case Codes.Pod.LEARN:
			toSend = new byte[1];
			toSend[0] = (byte)Codes.Pod.LEARN;
			BT_STATE = Codes.BT_STATE.LEARN;
			sendMessage(toSend);
			break;

		case Codes.Pod.ABORT_LEARN:
			toSend = new byte[1];
			toSend[0] = (byte)Codes.Pod.ABORT_LEARN;
			BT_STATE = Codes.BT_STATE.ABORT_LEARN;
			sendMessage(toSend);
			break;

		case Codes.Pod.GET_VERSION:
			BT_STATE = Codes.BT_STATE.GET_VERSION;
			toSend = new byte[1];
			toSend[0] = (byte)Codes.Pod.GET_VERSION;
			sendMessage(toSend);
			break;

		case Codes.Pod.ABORT_TRANSMIT:
			BT_STATE = Codes.BT_STATE.ABORT_TRANSMIT;
			toSend = new byte[1];
			toSend[0] = (byte)Codes.Pod.ABORT_TRANSMIT;
			sendMessage(toSend);
			break;
		} // end switch
	}

	/**
	 * called after learn mode is finished and has data to store
	 */
	protected void storeButton() {
		String buttonName = null;
		buttonName = button_map.get(BUTTON_ID);

		// make sure payload is not null and make sure we are in learn mode
		if (buttonName != null && INTERFACE_STATE == Codes.INTERFACE_STATE.LEARN) {
			device_data.insertButton(cur_device, buttonName,
					cur_context, Codes.pod_data);
		}
		// should we not drop out of learn mode?  Would reduce menu activity
		BT_STATE = Codes.BT_STATE.IDLE; // reset state, drop out of learn mode
		INTERFACE_STATE = Codes.INTERFACE_STATE.MAIN;
		Codes.learn_state = Codes.LEARN_STATE.IDLE; // ready to start a new
													// learn command now
		dismissDialog(DIALOG_LEARN_WAIT);
		Toast.makeText(this, "Button Code Received", Toast.LENGTH_SHORT).show();
		mainScreen.fetchButtons();
	}

	/**
	 * Determines if more bytes are being read that is available in the local data structure. This
	 * function should be called whenever a new set of data is COLLECTING in interpretResponse()
	 * @param bytes the number of bytes received
	 * @return false if the data is outside of the local storage space available and true if there is no error.
	 */
	protected boolean checkPodDataBounds(int bytes) {
		if (bytes > (Codes.pod_data.length - Codes.data_index)) {
			return false;
		}
		return true;
	}

	/**
	 * A state machine error happened while receiving data over bluetooth
	 * @param code 1 is for errors while in LEARN_MODE and 2 is for errors
	 * while in GET_INFO mode, affects the usage of Toast
	 */
	protected void signalError(int code) {
		if (code == 1) {
			Toast.makeText(this, "Error occured, exiting learn mode!",
					Toast.LENGTH_SHORT).show();
			try {
				dismissDialog(DIALOG_LEARN_WAIT);
			} catch (Exception e) {
				// if dialog had not been shown it throws an error, ignore
			}
			BT_STATE = Codes.BT_STATE.IDLE;
			Codes.learn_state = Codes.LEARN_STATE.IDLE;
		} else if (code == 2) {
			BT_STATE = Codes.BT_STATE.IDLE;
			Codes.info_state = Codes.INFO_STATE.IDLE;
		}
	}

	/**
	 * This method should be called whenever we receive a byte[] from the pod.
	 * @param response the circular buffer that contains the data that was received over BT
	 * @param bytes how many bytes were received and stored in response[] on the call to this method
	 * @param index the starting index into the circular data buffer that should be read
	 */
	protected void interpretResponse(byte[] response, int bytes, int index) {
		switch (BT_STATE) {
		case LEARN:
			try { // catch any unforseen state machine errors.....
				// learn data may not come all together, so need to process data
				// in chunks
				while (bytes > 0) {
					switch (Codes.learn_state) {
					case IDLE:
						if (response[index] == Codes.Pod.ACK) {
							Codes.learn_state = Codes.LEARN_STATE.BYTE1;
							index = (index + 1)
									% (BluetoothChatService.buffer_size - 1);
							bytes--;
							Codes.data_index = 0;
						} else {
							signalError(1);
							return;
						}
						break;

					case BYTE1:
						// first byte after ACK should be a zero
						if (response[index] == 0) {
							Codes.learn_state = Codes.LEARN_STATE.INITIALIZED;
							bytes--;
							index = (index + 1)
									% (BluetoothChatService.buffer_size - 1);
						} else {
							signalError(1);
							return;
						}
						break;

					case INITIALIZED:
						// if we got here then we are on the third byte of data
						if (Utilities.isGreaterThanUnsignedByte(
								response[index], 0)) {
							Codes.pod_data = new byte[(0x00FF & response[index]) + 2]; 
							// store length of data as first two bytes (used in
							// transmitting back)
							Codes.pod_data[Codes.data_index++] = 0; // default to 0
							Codes.pod_data[Codes.data_index++] = response[index];
							bytes--;
							index = (index + 1)
									% (BluetoothChatService.buffer_size - 1);
							Codes.learn_state = Codes.LEARN_STATE.COLLECTING;
						} else {
							signalError(1);
							return;
						}
						break;

					case COLLECTING:
						if (checkPodDataBounds(bytes)) {
							Codes.pod_data[Codes.data_index++] = response[index];
							// first check to see if this is the last byte
							if (Utilities.isGreaterThanUnsignedByte(
									Codes.data_index, Codes.pod_data[1] + 1)) { 
								// if we got here then we are done, pod_data[1]
								// is the expected message length
								storeButton();
								return;
							}
							bytes--;
							index = (index + 1)
									% (BluetoothChatService.buffer_size - 1);
						} else {
							signalError(1);
							return;
						}
						break;
					} // end switch/case
				} // end while loop
			} catch (Exception e) { // something unexpected occurred....exit
				Toast.makeText(this, "Communication error, exiting learn mode",
						Toast.LENGTH_SHORT).show();
				BT_STATE = Codes.BT_STATE.IDLE;
				return;
			}
			break;

		case GET_VERSION:
			if (response[index] == Codes.Pod.ACK) {
				try { // catch any unforseen state machine errors.....
					while (bytes > 0) {
						switch (Codes.info_state) {
						case IDLE:
							if (response[index] == Codes.Pod.ACK) {
								Codes.pod_data = new byte[4];
								Codes.info_state = Codes.INFO_STATE.BYTE0;
								index = (index + 1)
										% (BluetoothChatService.buffer_size - 1);
								bytes--;
								Codes.data_index = 0;
							} else {
								signalError(2);
								return;
							}
							break;

						case BYTE0:
							Codes.pod_data[0] = response[index];
							Codes.info_state = Codes.INFO_STATE.BYTE1;
							bytes--;
							index = (index + 1)
									% (BluetoothChatService.buffer_size - 1);
							break;

						case BYTE1:
							Codes.pod_data[1] = response[index];
							Codes.info_state = Codes.INFO_STATE.BYTE2;
							bytes--;
							index = (index + 1)
									% (BluetoothChatService.buffer_size - 1);
							break;

						case BYTE2:
							Codes.pod_data[2] = response[index];
							Codes.info_state = Codes.INFO_STATE.BYTE3;
							bytes--;
							index = (index + 1)
									% (BluetoothChatService.buffer_size - 1);
							break;

						case BYTE3:
							Codes.pod_data[3] = response[index];
							Codes.info_state = Codes.INFO_STATE.IDLE;
							bytes--;
							index = (index + 1)
									% (BluetoothChatService.buffer_size - 1);
							break;
						}
					}
				} catch (Exception e) {
					Toast.makeText(this,
							"Communication error, exiting learn mode",
							Toast.LENGTH_SHORT).show();
					BT_STATE = Codes.BT_STATE.IDLE;
					return;
				}

				// need to launch window to dump the data to
				showDialog(DIALOG_SHOW_INFO);
			}
			break;

		case ABORT_LEARN:
			BT_STATE = Codes.BT_STATE.IDLE; // reset state
			if (response[index] == Codes.Pod.ACK) {

			}
			break;

		case IR_TRANSMIT:
			BT_STATE = Codes.BT_STATE.IDLE;			
			if (response[index] == Codes.Pod.ACK) {
				// release lock if we get an ACK
				buttonLock = false;
				if (DEBUG) {
					Toast.makeText(this, "ACK received - lock removed", Toast.LENGTH_SHORT).show();
				}
			}
			break;
			
		case ABORT_TRANSMIT:
			BT_STATE = Codes.BT_STATE.IDLE; // reset state
			if (response[index] == Codes.Pod.ACK) {
				Toast.makeText(this, "ACK received for abort transmit", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		AlertDialog alert = null;
		AlertDialog.Builder builder;
		
		switch (id) {
		case DIALOG_SHOW_INFO:
			// define dialog			
			StringBuilder podData = new StringBuilder();
			podData.append("Component ID: ");
			podData.append(Codes.pod_data[0] + "\n");
			podData.append("Major Revision: ");
			podData.append(Codes.pod_data[1] + "\n");
			podData.append("Minor Revision: ");
			podData.append(Codes.pod_data[2] + "\n");
			podData.append("Revision: ");
			podData.append(Codes.pod_data[3]);
			builder = new AlertDialog.Builder(this);
			// .setCancelable(false)
			builder.setMessage(podData).setTitle("Pod Information");
			alert = builder.create();
			return alert;
			
		case DIALOG_INIT_DELAY:
			// create a custom alertdialog using our xml interface for it				
			LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.dialog_init_delay,
			                               (ViewGroup) findViewById(R.id.dialog_init_root));
			final EditText text = (EditText) layout.findViewById(R.id.enter_init_dly);
			builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			builder.setTitle("Enter Delay")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   try {
			        		   if (Integer.parseInt(text.getText().toString()) > 0) {
			        			   activityInit.add("DELAY "+Integer.parseInt(
			        					   text.getText().toString()));
			        			   Toast.makeText(BluMote.this, "Delay added to initialization list!",
			        						Toast.LENGTH_SHORT).show();
			        		   }
			        	   }
			        	   catch (NumberFormatException e) {
			        		   // do nothing, just quit
			        		   Toast.makeText(BluMote.this, "Invalid number, ignoring...",
			       					Toast.LENGTH_SHORT).show();
			        	   }
			        	   dialog.dismiss();
			           }
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			    });
			alert = builder.create();
			return alert;
		
		case DIALOG_INIT_PROGRESS:
			// should create a new progressdialog 
			// the dialog should exit after all the initItems are processed
			ProgressDialog progressDialog = new ProgressDialog(BluMote.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false); // don't allow back button to cancel it
            progressDialog.setMessage("Sending commands, please wait...");
            return progressDialog;
            
		case DIALOG_LEARN_WAIT:
			// should create a new progressdialog 
			// the dialog should exit after all the initItems are processed
			ProgressDialog learnWait = new ProgressDialog(BluMote.this);
			learnWait.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			learnWait.setCancelable(true); // don't allow back button to cancel it
			learnWait.setMessage("Aim the remote control at the pod and push the selected button");
            return learnWait;
			
		case DIALOG_ABOUT:
			// define dialog			
			StringBuilder aboutDialog = new StringBuilder();
			aboutDialog.append(getString(R.string.about_license));
			aboutDialog.append("\nSW Revision: ");
			String versionName = "";
			try {
				versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();				
			}
			aboutDialog.append(versionName + "\n");			
			builder = new AlertDialog.Builder(this);
			// .setCancelable(false)
			builder.setMessage(aboutDialog).setTitle("About BluMote");
			alert = builder.create();
			return alert;
			
		default:
			return alert;
		}
	}
	
	/**
	 * This class will allow Fling events to be captured and processed, if a Fling event is captured
	 * the the appropriate movement function is called to perform that action.
	 * @author keusej
	 *
	 */
	class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    moveRight();
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	moveLeft();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

    }
	
	/**
	 * CountDownTimer extended to support a local ButtonID variable and unused onTick()
	 * @author keusej
	 *
	 */
	abstract class ButtonCountDown extends CountDownTimer{
		int buttonID = 0;
		/**
		 * Public constructor
		 * @param millisInFuture
		 * @param buttonID
		 */
        public ButtonCountDown(long millisInFuture, int buttonID) {
            super(millisInFuture, millisInFuture); 	// instantiate super with onTick and onFinish with
            										// same delay
            this.buttonID = buttonID;
            }
        
        int getButtonID() {
        	return buttonID;
        }
        
        @Override
		public abstract void onFinish();

        @Override
        public void onTick(long millisUntilFinished) {
            // this method unused
        }
    }
}
