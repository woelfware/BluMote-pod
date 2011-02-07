package com.woelfware.droidmote;

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
import android.util.Log;
import android.view.ContextMenu;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.woelfware.database.Constants;
import com.woelfware.database.MyDB;
import com.woelfware.droidmote.Codes.Commands;

public class Droidmote extends Activity {
	// Debugging
    private static final String TAG = "BlueMote";
    private static final boolean D = true;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_MANAGE_DEVICE = 3;
    private static final int REQUEST_RENAME_POD = 4;
    
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
    
    // Context menu constants
    private static final int ID_TRAIN = 0;
    private static final int ID_UNTRAIN = 1;
    
    // Dialog menu constants
    private static final int DIALOG_SHOW_INFO = 0;
    
    // Layout Views
    private TextView mTitle;
    private Button btn_volume_up;
    private Button btn_volume_down;
    private Button btn_channel_up;
    private Button btn_channel_down;
    private Spinner device_spinner;
    
    // Name of the connected device
    private String mConnectedDeviceName = null;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    
    // array adapter for the drop-down spinner
    private ArrayAdapter<String> mAdapter;
 
    // SQL database class
    private MyDB device_data;
    //Shared preferences class - for storing config settings between runs
    private SharedPreferences prefs;
    
    // This is the device database results for each button on grid
    private Cursor devices;
    
    // currently selected device (table of DB)
    private String cur_table;
    
    // Currently selected button resource id (for training mode operations) 
    private int BUTTON_ID = 0; 
    // current State of the pod communication
    private byte STATE = 0x00;
    
    // Sets the delay in-between repeated sending of the keys on the interface
    private static int DELAY_TIME = 250; 
    
    // Flag that tells us if we are holding our finger on a buttona and should loop
    private static boolean LOOP_KEY = false;
    // sets mode to learn mode for button action handerls
    private static boolean LEARN_MODE = false;
    // keeps track of # of times the MESSAGE_PRESSED has been called, creator/consumer idea
    // prevents user from double tapping a button and creating double messages in queue
    private int NUM_MESSAGES = 0;
    
    // Hash map to keep track of all the buttons on the interface and associated properties
    HashMap<Integer,Object[]> button_map;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
  
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
        // get SQL database class 
        device_data = new MyDB(this);
        device_data.open();
        
        // setup spinner
        device_spinner = (Spinner) findViewById(R.id.device_spinner);
        mAdapter = new ArrayAdapter<String>(this, R.layout.spinner_entry);
        mAdapter.setDropDownViewResource(R.layout.spinner_entry);
        device_spinner.setAdapter(mAdapter);
        device_spinner.setOnItemSelectedListener(new MyOnItemSelectedListener(this));
        populateDropDown();
        
        // set spinner to default from last session if possible
        prefs = getSharedPreferences("droidMoteSettings", MODE_PRIVATE);
        String prefs_table = prefs.getString("lastDevice", null);
        if (prefs_table != null) {
        	for(int i=0; i<device_spinner.getCount(); i++) {
        		if (prefs_table.equals(device_spinner.getItemAtPosition(i))) {
        			device_spinner.setSelection(i);
        		}
        	}        
        }
                
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
        
    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
        
        // populate Buttons from DB
		fetchButtons();
		
		// See if the bluetooth device is connected, if not connect to last stored connection
		// TODO
		if (mBluetoothAdapter.isEnabled()) {
			if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
				String address = prefs.getString("lastPod", null);
				// Get the BLuetoothDevice object
				if (address != null) {
					BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
					// Attempt to connect to the device
					mChatService.connect(device);
				}				
			}
		}
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        
        // close sqlite database connection
        device_data.close();
                     
        if(D) Log.e(TAG, "-- ON STOP --");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        
        // save the last table in preferences for next time we launch
        Editor mEditor =  prefs.edit();
        mEditor.putString("lastDevice",cur_table);
        mEditor.commit();   
        
        if(D) Log.e(TAG, "--- ON DESTROY ---");
        
    }
    
    // this is called after resume from another full-screen activity
    @Override
	protected void onRestart() {
		super.onRestart();
		
		device_data.open();
	}

    // sets up the drop-down list, pulls rows from DB to populate
    private void populateDropDown() {
    	String str1;
    	Cursor cursor1;
    	cursor1 = device_data.getTables();
    	cursor1.moveToFirst();
    	mAdapter.clear(); // clear before adding
    	if (cursor1.getCount() > 0) {
    		do {
    			// need to exclude android_metadata and sqlite_sequence tables from results
    			str1 = cursor1.getString(0);
    			if (!(str1.equals("android_metadata")) 
    					&& !(str1.equals("sqlite_sequence"))) {
    				mAdapter.add(str1);
    			}
    		} while (cursor1.moveToNext());
    	}
    }
    private void touchButton(int rbutton) {
    	Button button = null; // if we can't find one in button_map, set to null
    	Object[] payload = null;    	
    	    	    	  	    	
    	payload = button_map.get(rbutton);

    	if (payload != null) {
    		button = (Button)payload[0];
    		
    		button.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    		LOOP_KEY = true; // start looping until we lift finger off key
    		Message msg = new Message();            
    		msg.what = MESSAGE_KEY_PRESSED;

    		button.setBackgroundDrawable(getResources().getDrawable((Integer)payload[3]));
    		buttonSend((String)payload[1]);
    		msg.arg1 = rbutton;
    			
			NUM_MESSAGES++; //increment on each sendMessage, decrement on each performClick()
    		mHandler.sendMessageDelayed(msg, DELAY_TIME);
    		
    	}    	    	    	    
    }

    View.OnTouchListener buttonTouch = new View.OnTouchListener() {
    	public boolean onTouch(View v, MotionEvent e) {
    		//only execute this one time and only if not in learn mode
    		// if we don't have !LOOP_KEY you can hit button multiple times
    		// and hold finger on button and you'll get duplicates
    		if (!LEARN_MODE) { 
    			if (e.getAction() == MotionEvent.ACTION_DOWN) {    				
    				if (NUM_MESSAGES == 0) {
    					touchButton(v.getId());
    				}
    				return true;  // because we consumed the event
    			}   
    			else if (e.getAction() == MotionEvent.ACTION_UP) {
    				LOOP_KEY = false;	// reset loop key global
    				// is this heavy to reset all buttons? 
    				// otherwise to switch/case
    				resetButtons();
    			}
    		}
    		return false;   		
    	}
    };
    
    View.OnClickListener buttonClick = new OnClickListener() {
        public void onClick(View v) {
        	BUTTON_ID = v.getId();
        	
        	if (LEARN_MODE) {
    			// need to set button to pressed and send code to Pod    			
    			Button button = null; // if we can't find one in button_map, set to null
    	    	Object[] payload = null;    	  	    	
    	    	payload = button_map.get(BUTTON_ID);

    	    	if (payload != null) {
    	    		button = (Button)payload[0];
    	    		button.setBackgroundDrawable(getResources().getDrawable((Integer)payload[3]));
    	    		sendCode(Commands.LEARN);
    	    	}    	    	    	    	    	    	 			
    		}
        	else { // skip this handler if we are in learn button mode
        		// send message to handler after the delay expires, allows for repeating event
        		Message msg = new Message();
        		msg.what = MESSAGE_KEY_PRESSED;
      		
    	    	Object[] payload = null;    	
    	    	payload = button_map.get(BUTTON_ID);
    	    	
    	    	if (payload != null) {
    	    		msg.arg1 = BUTTON_ID;
    	    		buttonSend((String)payload[1]);
    	    	}
	    	
        		//TODO add rest of buttons here
				NUM_MESSAGES++;
        		mHandler.sendMessageDelayed(msg, DELAY_TIME);      		
        	}
        }
    };
    
    
    private void resetButtons() {
		btn_volume_up.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrow_up_volume));
		btn_volume_down.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrow_down_volume));   					    					
		btn_channel_up.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrow_up_ch));   					    					
		btn_channel_down.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrow_down_ch));   					    					
		//TODO add rest of buttons here
    }
    
    private void setupChat() {
        
        // Initialize the buttons with a listener for click and touch events
        btn_volume_up = (Button) findViewById(R.id.btn_volume_up);
        btn_volume_down = (Button) findViewById(R.id.btn_volume_down);
        btn_channel_up = (Button) findViewById(R.id.btn_channel_up);
        btn_channel_down = (Button) findViewById(R.id.btn_channel_down);
        
        //btn_volume_up.setOnLongClickListener(l);
        btn_volume_up.setOnTouchListener(buttonTouch);
        btn_volume_up.setOnClickListener(buttonClick);
        btn_volume_down.setOnTouchListener(buttonTouch);
        btn_volume_down.setOnClickListener(buttonClick);
        btn_channel_up.setOnTouchListener(buttonTouch);
        btn_channel_up.setOnClickListener(buttonClick);
        btn_channel_down.setOnTouchListener(buttonTouch);
        btn_channel_down.setOnClickListener(buttonClick);
        
        //set bundle of associated button properties
        Object[] btn_1 = {btn_volume_up, Constants.VOLUME_UP, R.drawable.arrow_up_volume, R.drawable.arrow_up_volume_pressed};
        Object[] btn_2 = {btn_volume_down, Constants.VOLUME_DOWN, R.drawable.arrow_down_volume, R.drawable.arrow_down_volume_pressed};
        Object[] btn_3 = {btn_channel_up, Constants.CHANNEL_UP, R.drawable.arrow_up_ch, R.drawable.arrow_up_ch_pressed};
        Object[] btn_4 = {btn_channel_down, Constants.CHANNEL_DOWN, R.drawable.arrow_down_ch, R.drawable.arrow_down_ch_pressed};
        // bundle all the button data into a big hashtable
        button_map = new HashMap<Integer,Object[]>();
        button_map.put(R.id.btn_volume_up, btn_1);
        button_map.put(R.id.btn_volume_down, btn_2);
        button_map.put(R.id.btn_channel_up, btn_3);
        button_map.put(R.id.btn_channel_down, btn_4);
        
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
                
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    
    private void sendMessage(byte[] message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message;
            mChatService.write(send);
        }
    }
    
    // The Handler that gets information back
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
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
                interpretResponse(readBuf, msg.arg1);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_KEY_PRESSED:
            	// called when a touch event is registered on a button
            	// check if button is still depressed, if so, then generate a touch event
            	NUM_MESSAGES--;
            	if (LOOP_KEY) {
            		Button toclick;
            		Object[] payload = button_map.get(msg.arg1);
            		if (payload != null) {
            			toclick = (Button)payload[0];
            			toclick.performClick();
            		}        		
            	}
            	break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Store the address of device to preferences for re-connect on resume
                Editor mEditor =  prefs.edit();
                mEditor.putString("lastPod",address);
                mEditor.commit();
                // Attempt to connect to the device
                mChatService.connect(device);
                //device.getName(); // grab the friendly name, a rename command can change this
                // reset PKT_COUNT
                Codes.PKT_COUNT = 0;
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        case REQUEST_MANAGE_DEVICE:
        	// when the manage devices activity returns
        	if (resultCode == Activity.RESULT_OK) {
        		// re-populate the drop-down menu and set selection to the first item
        		device_data.open();	// onActivityResult() is called BEFORE onResume() so need this!
        		populateDropDown();
        	}
        	break;
        case REQUEST_RENAME_POD:
        	// when the rename pod activity returns
        	Bundle return_bundle;
        	if (resultCode == Activity.RESULT_OK) {
        		// add the new item to the database
        		return_bundle = data.getExtras();
        		if ( return_bundle != null ) {
        			Codes.new_name = return_bundle.getString("returnStr");
        			sendCode(Codes.Commands.RENAME_DEVICE);
        		}        		
        	}
        	break;
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
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        case R.id.manage_devices:
        	// need to launch the manage devices view now
    		Intent i = new Intent(this, ManageDevices.class);
            startActivityForResult(i, REQUEST_MANAGE_DEVICE);
        	return true;
        case R.id.rename_pod:
        	// Launch the function to ask for a name for device
        	Intent intent = new Intent(this, EnterDevice.class);
            startActivityForResult(intent, REQUEST_RENAME_POD);
        	return true;
        case R.id.get_info:
        	sendCode(Codes.Commands.GET_VERSION);
        	return true;
        case R.id.learn_button:
        	//TODO implement learn button,
        	// pop up Toast that says to click on a button
        	// set flag that says in learn mode
        	// pick up on this flag in the action handlers
        	// send code to pod on click listener
        	// change button color in click listener
        	// reset key background on return from pod command
        	Toast.makeText(this, "Select button to train", Toast.LENGTH_LONG).show();
        	LEARN_MODE = true;
        	return true;
        case R.id.stop_learn:        
        	Toast.makeText(this, "Stopped Learning", Toast.LENGTH_LONG).show();
        	LEARN_MODE = false;
        	// reset all images to unpressed state
        	resetButtons();
        	return true;
        }
        return false;
    }
    
    public class MyOnItemSelectedListener implements OnItemSelectedListener {
    	Activity _context;
    	
    	public MyOnItemSelectedListener(Activity _c ) {
    		_context = _c;
    	}

    	public void onItemSelected(AdapterView<?> parent,
    			View view, int pos, long id) {

    		// populate the keys from database	  		
    		fetchButtons();
    		
 //   		Toast.makeText(parent.getContext(), "The selection is: " +
 //   				parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
    	}

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case ID_TRAIN:
			BUTTON_ID = item.getGroupId();
			// call function to send to pod that we want to train
			sendCode(Codes.Commands.LEARN);
			return true;
		case ID_UNTRAIN:
			BUTTON_ID = 0; // reset value of button id
			// tell pod to abort train
			sendCode(Codes.Commands.ABORT_LEARN);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.btn_volume_up) {
			menu.setHeaderTitle("Menu");
			menu.add(v.getId(), ID_TRAIN, 0, "Train");
			menu.add(v.getId(), ID_UNTRAIN, 0, "Stop Training");
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
    // this function populates on-screen buttons from database, also sends init sequence to pod
    public void fetchButtons() {
    	// first update the cur_table from spinner
    	if (device_spinner.getCount() > 0) {
    		Object table = device_spinner.getSelectedItem();
    		if (table != null) {
    			cur_table = table.toString();
    			devices = device_data.getKeys(cur_table);
    		}
    	}
//    	sendCode(Codes.Commands.INIT);
    }
    
    // this function just sends the code to the pod based on the button that was selected
    public void buttonSend(String buttonCode) {
    	String column;    	
    	devices.moveToFirst();
    	for (int i=0; i< devices.getCount(); i++) {
    		column = devices.getString(1);
    		if (column.equals(buttonCode)) {
    			//short temp = devices.getShort(2); // we store a short to the database
    			byte[] code = devices.getBlob(2);
    			//byte code = (byte)(0x00FF & temp); // convert to a byte
    			//code = (byte)((code << 1) | Codes.PKT_COUNT); 
    			byte command = (byte)((Codes.Commands.IR_TRANSMIT << 1) | Codes.PKT_COUNT);
    			Codes.PKT_COUNT = (byte)((Codes.PKT_COUNT + 1) % 2);
    			byte[] toSend = new byte[code.length+2]; // 2 extra bytes for command byte and 0x00
    			toSend[0] = command;
    			toSend[1] = 0x00;
    			for (int j=2; j < toSend.length; j++) {
    				toSend[j] = code[j-2];
    			}
    			//{command, 0x00, code}; // 0x00 is reserved byte
    			STATE = Codes.Commands.IR_TRANSMIT;
    			sendMessage(toSend); // send data if matches 
    		}
    		// move to next button
    		devices.moveToNext();
    	}   
    }
    
    // This function sends the command codes to the pod
    public void sendCode(int code) {
    	byte[] toSend;
    	switch (code) {
    	case Codes.Commands.LEARN:
    		toSend = new byte[2];
    		toSend[0] = Codes.Commands.DEBUG; // DEBUG , should be LEARN
    		toSend[0] = (byte)((toSend[0] << 1) | Codes.PKT_COUNT);
    		Codes.PKT_COUNT = (byte)((Codes.PKT_COUNT + 1) % 2);
    		toSend[1] = 0x00; // Reserved
    		STATE = Codes.Commands.LEARN;
    		sendMessage(toSend); 
    		break;
    	case Codes.Commands.ABORT_LEARN:
    		toSend = new byte[2];
    		toSend[0] = Codes.Commands.ABORT_LEARN;
    		toSend[0] = (byte)((toSend[0] << 1) | Codes.PKT_COUNT);
    		Codes.PKT_COUNT = (byte)((Codes.PKT_COUNT + 1) % 2);
    		toSend[1] = 0x00; // Reserved
    		STATE = Codes.Commands.ABORT_LEARN;
    		sendMessage(toSend);
    		break;
    	case Codes.Commands.GET_VERSION:
    		STATE = Codes.Commands.GET_VERSION;
    		toSend = new byte[2];
    		toSend[0] = Codes.Commands.GET_VERSION;
    		toSend[0] = (byte)((toSend[0] << 1) | Codes.PKT_COUNT);
    		Codes.PKT_COUNT = (byte)((Codes.PKT_COUNT + 1) % 2);
    		toSend[1] = 0x00; // Reserved
    		sendMessage(toSend);
    		break;
    	case Codes.Commands.RENAME_DEVICE:
    		STATE = Codes.Commands.RENAME_DEVICE;
    		toSend = new byte[Codes.new_name.length()+2];
    		toSend[0] = Codes.Commands.RENAME_DEVICE;
    		toSend[0] = (byte)((toSend[0] << 1) | Codes.PKT_COUNT);
    		Codes.PKT_COUNT = (byte)((Codes.PKT_COUNT + 1) % 2);
    		toSend[1] = 0x00; // Reserved
    		// load new names to the toSend byte[]
    		byte[] new_name = Codes.new_name.getBytes();
    		System.arraycopy(new_name, 0, toSend, 2, new_name.length);
    		
    		sendMessage(toSend);
    		break;
    	}
    }
    
    public void interpretResponse(byte[] response, int bytes) {
    	// this method should be called whenever we receive a byte[] from the pod
    	switch (STATE) {
    	case Codes.Commands.LEARN:
    		if (response[0] == ((Codes.Return.ACK << 1) | Codes.PKT_COUNT)) {
    			byte[] content = new byte[bytes];
    			for (int i=0; i< bytes; i++) {
    				content[i] = response[i];
    			}
    			Button btn = null;
    			Object[] payload = null;
    			payload = button_map.get(BUTTON_ID);
    			
    			if (payload != null) {
    				btn = (Button)payload[0];
    				btn.setBackgroundDrawable(getResources().getDrawable((Integer)payload[2]));
    				device_data.insertButton(cur_table, (String)payload[1], content);
    			}
    		    		    
    			// refresh the local Cursor with new database updates
    			fetchButtons();
    			STATE = 0x00; // reset state
    			LEARN_MODE = false;
    			Toast.makeText(this, "Button Learned", Toast.LENGTH_LONG).show();
    		}
    		break;
    	case Codes.Commands.GET_VERSION:
    		if (response[0] == ((Codes.Return.ACK << 1) | Codes.PKT_COUNT)) {
    			STATE = 0x00; // reset state
    			// convert data into a String
    			Codes.pod_data = response;
    			// need to launch window to dump the data to
    			showDialog(DIALOG_SHOW_INFO);
    		}
    		break;
    	case Codes.Commands.ABORT_LEARN:
    		if (response[0] == ((Codes.Return.ACK << 1) | Codes.PKT_COUNT)) {
    			STATE = 0x00; // reset state
    		}
    		break;
    	case Codes.Commands.RENAME_DEVICE:
    		if (response[0] == ((Codes.Return.ACK << 1) | Codes.PKT_COUNT)) {
    			STATE = 0x00; // reset state
    		}
    		break;
    	case Codes.Commands.IR_TRANSMIT:
    		if (response[0] == ((Codes.Return.ACK << 1) | Codes.PKT_COUNT)) {
    			STATE = 0x00; // reset state
    		}
    		break;
    	}	
    }

    //showDialog(DIALOG_SHOW_INFO);
	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		AlertDialog alert;
		switch(id) {
		case DIALOG_SHOW_INFO:
			// define dialog
			StringBuilder podData = new StringBuilder();
			podData.append("Component ID: ");
			podData.append(Codes.pod_data[1]+"\n"); // first byte is ACK, throw away
			podData.append("Major Revision: ");
			podData.append(Codes.pod_data[2]+"\n");
			podData.append("Minor Revision: ");
			podData.append(Codes.pod_data[3]+"\n");
			podData.append("Revision: ");
			podData.append(Codes.pod_data[4]);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			//.setCancelable(false)
			builder.setMessage(podData).setTitle("Pod Information");
			alert = builder.create();
			break;
		default:
			alert = null;
		}
		return alert;
	}	
}
