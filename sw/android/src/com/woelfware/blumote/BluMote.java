package com.woelfware.blumote;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.woelfware.database.MyDB;

public class BluMote extends Activity implements OnClickListener,OnItemClickListener,OnItemSelectedListener
{
	// Debugging
	// private static final String TAG = "BlueMote";
	public static final boolean D = true;

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_MANAGE_DEVICE = 3;
	// private static final int REQUEST_RENAME_POD = 4;
	public static final int ACTIVITY_ADD = 5;
	private static final int ACTIVITY_RENAME = 6;
	private static final int MISC_RENAME = 7;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	// Message time for repeat-key-delay
	public static final int MESSAGE_KEY_PRESSED = 6;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// button payload indexes
	private static final int BTN_NAME = 0;
	private static final int BTN_TEXT = 1;

	// Dialog menu constants
	private static final int DIALOG_SHOW_INFO = 0;

	// Layout Views
	private TextView mTitle;
	// private ImageButton led_btn;
	
	private ViewFlipper flip;

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
	public MyDB device_data;
	// Shared preferences class - for storing config settings between runs
	public SharedPreferences prefs;

	// This is the device database results for each button on grid
	public Cursor devices;

	// currently selected device (table of DB)
	public String cur_table;

	// context of screen view, using this for categorizing in database
	// hardcoding this for now, will need to adjust for new contexts
	private String cur_context = "tv-dvd";

	// Currently selected button resource id (for training mode operations)
	private int BUTTON_ID = 0;

	// current State of the pod communication
	private Codes.PROGRAM_STATE STATE = Codes.PROGRAM_STATE.IDLE;

	// Sets the delay in-between repeated sending of the keys on the interface
	// (in ms)
	private static int DELAY_TIME = 750;
	private static int LONG_DELAY_TIME = 750;

	// Flag that tells us if we are holding our finger on a buttona and should
	// loop
	private static boolean LOOP_KEY = false;

	// last button pushed, used in handler, prevents firing wrong click event
	private static int last_button = 0;
	
	// arraylist position of activity that we want to rename
	private static int activity_rename;
	
	// misc button id that we want to rename
	private static String misc_button;

	// keeps track of # of times the MESSAGE_PRESSED has been called,
	// creator/consumer idea
	// prevents user from double tapping a button and creating double messages
	// in queue
	private static int NUM_MESSAGES = 0;

	// producer/consumer variable for messages sent versus ACK'd
	private static int PKTS_SENT = 0;

	// Hash map to keep track of all the buttons on the interface and associated
	// properties
	HashMap<Integer, Object[]> button_map;

	// These are used for activities display window
	private static final int ID_DELETE = 0;
	private static final int ID_RENAME = 1;
	ListView activitiesListView;

	// keep track of what the active page of buttons is
	public enum Pages {
		MAIN, NUMBERS, ACTIVITIES
	}

	private Pages page = Pages.MAIN;

	private MainInterface mainScreen = null;
	
	// viewflipper animations
	private Animation slide_right_anim;
	private Animation slide_left_anim;
	private Animation slide_right_out_anim;
	private Animation slide_left_out_anim;
	
	public GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get preferences file
		prefs = getSharedPreferences("droidMoteSettings", MODE_PRIVATE);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
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
                if (gestureDetector.onTouchEvent(e)) {
                    return true;
                }
                else 
                {                	
                	// non fling event
                	if (e.getAction() == MotionEvent.ACTION_DOWN) {
            			// buzz
            			v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            		}
                	
                	// create object[] for button
                	Object[] payload = null;
            		payload = button_map.get(v.getId());

            		if (payload != null) {            			
            			if (e.getAction() == MotionEvent.ACTION_DOWN) {
            				// check if this is a misc rename
                			if (STATE == Codes.PROGRAM_STATE.RENAME_STATE) {
                				// TODO store the button that we want to update if it is a valid misc key
                				// if it isn't then exit and Toast user, change state back to idle
                				if (((String)payload[BTN_TEXT]).startsWith("btn_misc")) {
                					// if compare works then we can go ahead and implement the rename
                					misc_button = (String)payload[BTN_TEXT];
                    				// launch window to get new name to use
                    				Intent i = new Intent(BluMote.this, EnterDevice.class);
                    				startActivityForResult(i, MISC_RENAME);
                				}
                				else {
                					Toast.makeText(BluMote.this, "Not a valid Misc button, canceling", 
                							Toast.LENGTH_SHORT).show();
                				}
                				
                				STATE = Codes.PROGRAM_STATE.IDLE; // reset state in any case
                				return false; 
                			}
                			
                			// check if it is a navigational button
                			// checkNavigation returns true if it consumed the button
            				if (checkNavigation(payload)) {
            					return false;  
            				}
            			}
            			// should I attempt to delay recognition to allow
                    	// fling to register but not button press??
                    	
                    	// only execute this one time and only if not in learn mode
                		// if we don't have !LOOP_KEY you can hit button multiple times
                		// and hold finger on button and you'll get duplicates

                		if (STATE != Codes.PROGRAM_STATE.LEARN && STATE != Codes.PROGRAM_STATE.ACTIVITY) {
                			// don't want to execute touchButton when in learn mode or
                			// activity mode
                			if (e.getAction() == MotionEvent.ACTION_DOWN) {
                				LOOP_KEY = true; // start looping until we lift finger off
                									// key

                				touchButton(payload, v.getId());
                			} else if (e.getAction() == MotionEvent.ACTION_UP) {
                				LOOP_KEY = false; // reset loop key global
                				if (PKTS_SENT > 0) { // decrement producer/consumer for
                										// button sends
                					PKTS_SENT--; // if we lose our response from pod, need
                									// to decrement by 1
                				} // so that we can eventually be able to send a packet out
                				  // again
                			}
                		}
            		}                	
            		return false; // allows XML to consume
                }
            }
        };
        
        flip.setOnTouchListener(gestureListener);
        
		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		// get SQL database class
		device_data = new MyDB(this);
		device_data.open();
	}	
	
	@Override
	public void onStart() {
		super.onStart();
		if (mChatService == null) { // then first time this was called
			// Initialize the BluetoothChatService to perform bluetooth
			// connections
			mChatService = new BluetoothChatService(this, mHandler);

			// instantiate button screen helper classes
			mainScreen = new MainInterface(this);
			// setup interface
			setupInterface();
			flip.showNext(); // start out one screen to the right (main)
			// context menu on array list
			registerForContextMenu(findViewById(R.id.activities_list));
		}
	}

	@Override
	public synchronized void onResume() {
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
				mChatService.start();
			}
		}

		// setup spinner (need this in case we removed the spinner item from a
		// call to managedevices)
		mainScreen.fetchButtons(); // update buttons from DB

		// See if the bluetooth device is connected, if not try to connect
		if (mBluetoothAdapter.isEnabled()) {
			if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
				String address = prefs.getString("lastPod", null);
				// Get the BLuetoothDevice object
				if (address != null) {
					BluetoothDevice device = mBluetoothAdapter
							.getRemoteDevice(address);
					// Attempt to connect to the device
					mChatService.connect(device);
				}
			}
		}
	}

	@Override
	public synchronized void onPause() {
		super.onPause();

	}

	@Override
	public void onStop() {
		super.onStop();

		// if (mChatService != null) mChatService.stop();
		// close sqlite database connection
		device_data.close();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		// device_data.close();
		// save the last table in preferences for next time we launch
		// Editor mEditor = prefs.edit();
		// mEditor.putString("lastDevice",cur_table);
		// mEditor.commit();

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

	// move screen to the left	
	private void moveLeft() {
		LOOP_KEY = false;
		
		// setup flipper animations
		flip.setInAnimation(slide_right_anim); // -100 -> 0
		flip.setOutAnimation(slide_left_out_anim); // 0 -> 100
		
		switch (page) {
		case MAIN:
			//setContentView(R.layout.activities);								
			flip.showPrevious();
			page = Pages.ACTIVITIES;
			//setupActivities();
			return;

		case ACTIVITIES:
			return;

		case NUMBERS:
			//setContentView(R.layout.main);
			flip.showPrevious();
			page = Pages.MAIN;
			//setupDefaultButtons();
			return;
		}
	}
	
	// move screen to the right
	private void moveRight() {
		LOOP_KEY = false;
		
		// setup flipper animations
		flip.setInAnimation(slide_left_anim); // 100 -> 0
		flip.setOutAnimation(slide_right_out_anim); // 0 -> -100
		//flip.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_right));
		
		switch (page) {
		case MAIN:
			//setContentView(R.layout.number_screen);
			flip.showNext();
			page = Pages.NUMBERS;
			//setupNumbers();
			return;

		case ACTIVITIES:
			//setContentView(R.layout.main);
			flip.showNext();
			page = Pages.MAIN;
			//setupDefaultButtons();
			return;

		case NUMBERS:
			return;
		}
	}
	
	private boolean checkNavigation(Object[] payload) {
		if (payload != null) {
			try {
				// see if we have a navigation page move command....
				if (payload[BTN_TEXT] == "move_left_btn") {
					moveLeft();
					return true;
				}
				// check if the navigation move_right was pushed
				// this only works when we are in main screen
				if (payload[BTN_TEXT] == "move_right_btn") {
					moveRight();
					return true;
				}
			} catch (Exception e) {
				// do nothing, this is fine
				return false;
			}
		}
		return false;
	}
	
	// Called when a user pushes a non-navigation button down for the first time
	private void touchButton(Object[] payload, int rbutton) {		
		if (payload != null) {
			// if we got here it means we are a regular button, not move_left or
			// move_right

			NUM_MESSAGES++;

			Message msg = new Message();
			msg.what = MESSAGE_KEY_PRESSED;

			buttonSend((String) payload[BTN_TEXT]);
			msg.arg1 = rbutton;
			last_button = rbutton;
			mHandler.sendMessageDelayed(msg, LONG_DELAY_TIME);		
		}
	}
	
	// interface implementation
	public void onClick(View v) {
		BUTTON_ID = v.getId();

		if (STATE == Codes.PROGRAM_STATE.ACTIVITY) {
			// TODO implement appending/saving button to the activity list
			// item....
		} else if (STATE == Codes.PROGRAM_STATE.LEARN) {
			sendCode(Codes.Pod.LEARN);
		} else { // skip this handler if we are in learn button mode
			// send message to handler after the delay expires, allows for
			// repeating event
			if (NUM_MESSAGES == 0 && LOOP_KEY) {
				Message msg = new Message();
				msg.what = MESSAGE_KEY_PRESSED;

				Object[] payload = null;
				payload = button_map.get(BUTTON_ID);

				if (payload != null) {
					msg.arg1 = BUTTON_ID;
					buttonSend((String) payload[BTN_TEXT]);
				}

				NUM_MESSAGES++;
				mHandler.sendMessageDelayed(msg, DELAY_TIME);
			}
		}
	}			

	public void setButtonMap(HashMap<Integer, Object[]> map) {
		button_map = map;
	}

	// called to setup the buttons on the main screen
	private void setupInterface() {
		mainScreen.initialize();
		button_map = mainScreen.getButtonMap();		
	}

	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	private void sendMessage(byte[] message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		// check that we have a context selected and available
		if (devices != null) {
			// Check that there's actually something to send
			if (message.length > 0) {
				// Get the message bytes and tell the BluetoothChatService to
				// write
				byte[] send = message;
				mChatService.write(send);
			}
		}
	}

	// The Handler that gets information back from other activities/classes
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
						prefs = getSharedPreferences("droidMoteSettings",
								MODE_PRIVATE);
						String prefs_table = prefs.getString("knownDevices",
								null);

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
								if (device.matches(connectingDevice)) {
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

				case BluetoothChatService.STATE_LISTEN:
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
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;

			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;

			case MESSAGE_KEY_PRESSED:
				// called when a touch event is registered on a button
				// check if button is still depressed, if so, then generate a
				// touch event
				NUM_MESSAGES--;
				if ((int) msg.arg1 == last_button && LOOP_KEY) {
					Object[] payload = button_map.get(msg.arg1);
					if (payload != null) {
						try {
							Button toclick;
							toclick = (Button) payload[BTN_NAME];
							toclick.performClick();
						} catch (Exception e) {
							// must be an imagebutton then
							ImageButton toclick;
							toclick = (ImageButton) payload[BTN_NAME];
							toclick.performClick();
						}
					}
				}
				break;
			}
		}
	};

	// called when activities finish running and return to this activity
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// if already connected, break connection
				if (mChatService.getState() != BluetoothChatService.STATE_NONE) {
					mChatService.stop();
				}
				// Get the device MAC address
				connectingMAC = data.getExtras().getString(
						PodListActivity.EXTRA_DEVICE_ADDRESS);

				// save device for future use - no need to re-scan
				connectingDevice = (data.getExtras()
						.getString(PodListActivity.EXTRA_DEVICE_NAME));
				// .replaceAll("\\\\", "blah"); //for some reason strings get
				// double backslash when pulling out

				// Store the address of device to preferences for connect in
				// onResume()
				Editor mEditor = prefs.edit();
				mEditor.putString("lastPod", connectingMAC);
				mEditor.commit();

				// Attempt to connect to the device
				// device.getName(); // grab the friendly name, a rename command
				// can change this
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
				// do nothing as of now
			}
			break;

		case ACTIVITY_ADD:
			if (resultCode == Activity.RESULT_OK) {
				// add the new item to the database
				Bundle return_bundle = data.getExtras();
				if (return_bundle != null) {
					String return_string = return_bundle.getString("returnStr");
					
					// Add item to list
					mainScreen.addActivity(return_string);
				}				
			}
			break;
			
		case ACTIVITY_RENAME:
			if (resultCode == Activity.RESULT_OK) {
				Bundle return_bundle = data.getExtras();
				if (return_bundle != null) {
					String return_string = return_bundle.getString("returnStr");

					mainScreen.renameActivity(return_string, activity_rename);
				}				
			}
			break;
		
		case MISC_RENAME:
			if (resultCode == Activity.RESULT_OK) {
				Bundle return_bundle = data.getExtras();
				if (return_bundle != null) {
					String return_string = return_bundle.getString("returnStr");
					// TODO test that this works
					mainScreen.renameMisc(return_string, misc_button);
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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

		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;

			// this is to manage the button configurations in database
		case R.id.manage_devices:
			// need to launch the manage devices view now
			Intent i = new Intent(this, ManageDevices.class);
			startActivityForResult(i, REQUEST_MANAGE_DEVICE);
			return true;

		case R.id.get_info:
			sendCode(Codes.Pod.GET_VERSION);
			return true;

		case R.id.learn_button:
			Toast.makeText(this, "Select button to train", Toast.LENGTH_SHORT)
					.show();
			STATE = Codes.PROGRAM_STATE.LEARN;
			return true;

		case R.id.stop_learn:
			Toast.makeText(this, "Stopped Learning", Toast.LENGTH_SHORT).show();
			sendCode(Codes.Pod.ABORT_LEARN);
			STATE = Codes.PROGRAM_STATE.ABORT_LEARN;
			return true;
			
		case R.id.rename_misc:
			Toast.makeText(this, "Select Misc Button to rename", Toast.LENGTH_SHORT).show();
			STATE = Codes.PROGRAM_STATE.RENAME_STATE;
		}
		return false;
	}

	// OnItemSelectedListener interface definition
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

		// populate the keys from database
		mainScreen.fetchButtons();
		Editor mEditor = prefs.edit();
		mEditor.putString("lastDevice", cur_table);
		mEditor.commit();
	}
	// OnItemSelectedListener interface definition
	public void onNothingSelected(AdapterView<?> parent) {
		// Do nothing.
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case ID_DELETE:
			mainScreen.deleteActivity((int)info.id);
			return true;
		case ID_RENAME:
			// store ID of the item to be renamed
			activity_rename = (int)info.id;
			//launch window to get new name to use
			Intent i = new Intent(this, EnterDevice.class);
			startActivityForResult(i, ACTIVITY_RENAME);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.activities_list) {
			// AdapterView.AdapterContextMenuInfo info =
			// (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle("Menu");
			menu.add(0, ID_DELETE, 0, "Delete");
			menu.add(0, ID_RENAME, 0, "Rename");
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	// The on-click listener for all devices in the ListViews
	public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
		// TODO implement execution of the activity that was selected
	}

	// this function sends the code to the pod based on the button that was
	// selected
	public void buttonSend(String buttonCode) {
		String column;
		if (devices != null) {
			devices.moveToFirst();
			for (int i = 0; i < devices.getCount(); i++) {
				column = devices.getString(1);
				if (column.equals(buttonCode)) {
					byte[] code = devices.getBlob(2);
					byte command = (byte) (Codes.Pod.IR_TRANSMIT);
					byte[] toSend = new byte[code.length + 1]; // 1 extra bytes
																// for command
																// byte
					toSend[0] = command;
					for (int j = 1; j < toSend.length; j++) {
						toSend[j] = code[j - 1];
					}
					// {command, 0x00, code}; // 0x00 is reserved byte
					STATE = Codes.PROGRAM_STATE.IR_TRANSMIT;

					// increment producer/consumer, ACK received will consume,
					// removing finger from button clears
					if (PKTS_SENT == 0) {
						sendMessage(toSend); // send data if matches
						PKTS_SENT = PKTS_SENT + 2;
					}

					return;
				}
				// move to next button
				devices.moveToNext();
			}
		}
	}

	// This function sends the command codes to the pod
	public void sendCode(int code) {
		byte[] toSend;
		switch (code) {
		case Codes.Pod.LEARN:
			toSend = new byte[1];
			toSend[0] = Codes.Pod.LEARN;
			toSend[0] = (byte) toSend[0];
			STATE = Codes.PROGRAM_STATE.LEARN;
			sendMessage(toSend);
			break;

		case Codes.Pod.ABORT_LEARN:
			toSend = new byte[1];
			toSend[0] = Codes.Pod.ABORT_LEARN;
			toSend[0] = (byte) toSend[0];
			STATE = Codes.PROGRAM_STATE.ABORT_LEARN;
			sendMessage(toSend);
			break;

		case Codes.Pod.GET_VERSION:
			STATE = Codes.PROGRAM_STATE.GET_VERSION;
			toSend = new byte[1];
			toSend[0] = Codes.Pod.GET_VERSION;
			toSend[0] = (byte) toSend[0];
			sendMessage(toSend);
			break;
		}
	}

	// called after learn mode is finished and has data to store
	public void storeButton() {
		Object[] payload = null;
		payload = button_map.get(BUTTON_ID);

		if (payload != null) {
			device_data.insertButton(cur_table, (String) payload[BTN_TEXT],
					cur_context, Codes.pod_data);
		}

		// refresh the local Cursor with new database updates
		mainScreen.fetchButtons();
		STATE = Codes.PROGRAM_STATE.IDLE; // reset state, drop out of learn mode
		Codes.learn_state = Codes.LEARN_STATE.IDLE; // ready to start a new
													// learn command now
		Toast.makeText(this, "Button Learned", Toast.LENGTH_SHORT).show();
	}

	// returns false if the data to be inserted is more bytes than array is
	// setup for,
	// this should not happen unless pod screwed up
	public boolean checkPodDataBounds(int bytes) {
		if (bytes > (Codes.pod_data.length - Codes.data_index)) {
			return false;
		}
		return true;
	}

	// an error happened, Toast user and reset state machines
	// if argument = 1 then signal learn mode error,
	// if argument = 2 then signal get info error
	public void signalError(int code) {
		if (code == 1) {
			Toast.makeText(this, "Error occured, exiting learn mode!",
					Toast.LENGTH_SHORT).show();
			STATE = Codes.PROGRAM_STATE.IDLE;
			Codes.learn_state = Codes.LEARN_STATE.IDLE;
		} else if (code == 2) {
			STATE = Codes.PROGRAM_STATE.IDLE;
			Codes.info_state = Codes.INFO_STATE.IDLE;
		}
	}

	// this method should be called whenever we receive a byte[] from the pod
	// the bytes argument tells us how many bytes were received and stored in
	// response[]
	// note that the response array is a circular buffer, the starting index
	// head is 'index'
	public void interpretResponse(byte[] response, int bytes, int index) {
		switch (STATE) {
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
							Codes.pod_data[Codes.data_index++] = 0;
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
									// gracefully
				Toast.makeText(this, "Communication error, exiting learn mode",
						Toast.LENGTH_SHORT).show();
				STATE = Codes.PROGRAM_STATE.IDLE;
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
					STATE = Codes.PROGRAM_STATE.IDLE;
					return;
				}

				// need to launch window to dump the data to
				showDialog(DIALOG_SHOW_INFO);
			}
			break;

		case ABORT_LEARN:
			STATE = Codes.PROGRAM_STATE.IDLE; // reset state
			if (response[index] == Codes.Pod.ACK) {

			}
			break;

		case IR_TRANSMIT:
			STATE = Codes.PROGRAM_STATE.IDLE;
			// decrement PKTS_SENT - we need to prevent user from flooding
			// pod with data
			if (PKTS_SENT > 1) {
				PKTS_SENT = PKTS_SENT - 2;
			} else if (PKTS_SENT > 0) {
				PKTS_SENT--;
			}
			if (response[index] == Codes.Pod.ACK) {
				// right now we don't do anything differently for ACK/NACK
			}
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		AlertDialog alert;
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
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// .setCancelable(false)
			builder.setMessage(podData).setTitle("Pod Information");
			alert = builder.create();
			break;
		default:
			alert = null;
		}
		return alert;
	}
	
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
}
