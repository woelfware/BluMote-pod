package com.woelfware.droidmote;

import static com.woelfware.droidmote.Codes.*;
import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;

import com.woelfware.database.Constants;
import com.woelfware.database.MyDB;

public class Droidmote extends Activity {
	// Debugging
    private static final String TAG = "BlueMote";
    private static final boolean D = true;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_MANAGE_DEVICE = 3;
    
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    // Context menu constants
    private static final int ID_TRAIN = 0;
    private static final int ID_UNTRAIN = 1;
    
    // Layout Views
    private TextView mTitle;
    private ListView mConversationView;
    private Button btn_volume_up;
    private Button btn_volume_down;
    private Button btn_channel_up;
    private Button btn_channel_down;
    private Spinner device_spinner;
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;   
    
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
        device_data.open();
        
        // populate Buttons from DB
		fetchButtons();
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        device_data.close();
        
        // save the last table in preferences for next time we launch
        Editor mEditor =  prefs.edit();
        mEditor.putString("lastDevice",cur_table);
        mEditor.commit();
        
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
                
        if(D) Log.e(TAG, "-- ON STOP --");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
        
    }
    
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);
        
        // Initialize the buttons with a listener for click events
        btn_volume_up = (Button) findViewById(R.id.btn_volume_up);
        btn_volume_down = (Button) findViewById(R.id.btn_volume_down);
        btn_channel_up = (Button) findViewById(R.id.btn_channel_up);
        btn_channel_down = (Button) findViewById(R.id.btn_channel_down);
        
        btn_volume_up.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //DEBUG
            	device_data.insertButton(cur_table, Constants.VOLUME_UP, "Volume Up");
            	fetchButtons(); // just to refresh the local Cursor of devices
                // END DEBUG
            	buttonSend(Constants.VOLUME_UP);
            }
        });
        btn_volume_down.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //DEBUG
            	device_data.insertButton(cur_table, Constants.VOLUME_DOWN, "Volume Down");
            	fetchButtons(); // just to refresh the local Cursor of devices
                // END DEBUG
            	buttonSend(Constants.VOLUME_DOWN);
            }
        });
        btn_channel_up.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //DEBUG
            	device_data.insertButton(cur_table, Constants.CHANNEL_UP, "Channel Up");
            	fetchButtons(); // just to refresh the local Cursor of devices
                // END DEBUG
                buttonSend(Constants.CHANNEL_UP);
            }
        });
        btn_channel_down.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //DEBUG
            	device_data.insertButton(cur_table, Constants.CHANNEL_DOWN, "Channel Down");
            	fetchButtons(); // just to refresh the local Cursor of devices
                // END DEBUG
                buttonSend(Constants.CHANNEL_DOWN);
            }
        });
        
        registerForContextMenu(findViewById(R.id.btn_volume_up));

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
//        mOutStringBuffer = new StringBuffer("");
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
    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    //This is a DEBUG function
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
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
    // The Handler that gets information back from the BluetoothChatService
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
                    mConversationArrayAdapter.clear();
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
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
            	// TODO this readmessage should be a byte[] instead of a String
            	// when we receive a byte, we should call interpretResponse() to see if this is associated
            	// with a learn request or if it is something else
                byte[] readBuf = (byte[]) msg.obj;
                interpretResponse(readBuf);
                // construct a string from the valid bytes in the buffer
                // DEBUG
                String readMessage = new String(readBuf, 0, msg.arg1);
                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
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
                // Attempt to connect to the device
                mChatService.connect(device);
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
		//TODO add all the buttons
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
    // this function populates on-screen buttons from database, also sends init sequence to pod
    public void fetchButtons() {
    	// first update the cur_table from spinner
    	if (device_spinner.getCount() > 0) {
    		Object table = device_spinner.getSelectedItem();
    		// TODO do the init sequence to pod
    		if (table != null) {
    			cur_table = table.toString();
    			devices = device_data.getKeys(cur_table);
    		}
    	}
    	sendCode(Codes.Commands.INIT);
    }
    
    // this function just sends the code to the pod based on the button that was selected
    public void buttonSend(String buttonCode) {
    	String column;    	
    	devices.moveToFirst();
    	for (int i=0; i< devices.getCount(); i++) {
    		column = devices.getString(1);
    		if (column.equals(buttonCode)) {
    			short temp = devices.getShort(2); // we store a short to the database
    			byte code = (byte)(0x00FF & temp); // convert to a byte
    			//code = (byte)((code << 1) | Codes.PKT_COUNT); 
    			byte command = (byte)((Codes.Commands.IR_TRANSMIT << 1) | Codes.PKT_COUNT);
    			byte[] toSend = {command, 0x00, code}; // 0x00 is reserved byte
    			sendMessage(toSend); // send data if matches 
    		}
    	}   
    }
    
    //TODO implement these case statements
    // This function sends the command codes to the pod
    public void sendCode(int code) {
    	byte[] toSend;
    	switch (code) {
    	case Codes.Commands.LEARN:
    		toSend = new byte[2];
    		toSend[0] = Codes.Commands.DEBUG; // DEBUG , should be LEARN
    		toSend[0] = (byte)((toSend[0] << 1) | Codes.PKT_COUNT);
    		toSend[1] = 0x00; // Reserved
    		STATE = Codes.Commands.LEARN;
    		sendMessage(toSend); 
    		break;
    	case Codes.Commands.ABORT_LEARN:
    		toSend = new byte[2];
    		toSend[0] = Codes.Commands.ABORT_LEARN;
    		toSend[0] = (byte)((toSend[0] << 1) | Codes.PKT_COUNT);
    		toSend[1] = 0x00; // Reserved
    		STATE = Codes.Commands.ABORT_LEARN;
    		sendMessage(toSend);
    		break;
    	case Codes.Commands.INIT:
    		toSend = new byte[28];
    		toSend[0] = Codes.Commands.INIT;
    		toSend[0] = (byte)((toSend[0] << 1) | Codes.PKT_COUNT);
    		toSend[1] = 0x00; // Reserved
    		// need to send the following data:
    		// zero : 6B
    		// one : 6B
    		// header : 6B
    		// trailer : 6B
    		STATE = Codes.Commands.INIT;
    		break;
    	case Codes.Commands.GET_VERSION:
    		STATE = Codes.Commands.GET_VERSION;
    		toSend = new byte[2];
    		toSend[0] = Codes.Commands.GET_VERSION;
    		toSend[0] = (byte)((toSend[0] << 1) | Codes.PKT_COUNT);
    		toSend[1] = 0x00; // Reserved
    		sendMessage(toSend);
    		break;    	
    	}
    }
    
    public void interpretResponse(byte[] response) {
    	// this method should be called whenever we receive a byte[] from the pod
    	// NOTE: we should store any state related info in the Codes.java class I think,
    	// thinking the RENAME_DEVICE stuff if we have to do a retry event
    	// NOTE: if we get an ACK then we should increment pkt_count
    	switch (STATE) {
    	case Codes.Commands.LEARN:
    		if (response[0] == Codes.Return.ACK) {
    			Codes.PKT_COUNT = (byte)((Codes.PKT_COUNT + 1) % 2);
    		}
    		break;
    	case Codes.Commands.GET_VERSION:
    		if (response[0] == Codes.Return.ACK) {
    			Codes.PKT_COUNT = (byte)((Codes.PKT_COUNT + 1) % 2);
    		}
    		break;
    	case Codes.Commands.ABORT_LEARN:
    		if (response[0] == Codes.Return.ACK) {
    			Codes.PKT_COUNT = (byte)((Codes.PKT_COUNT + 1) % 2);
    		}
    		break;
    	case Codes.Commands.INIT:
    		if (response[0] == Codes.Return.ACK) {
    			Codes.PKT_COUNT = (byte)((Codes.PKT_COUNT + 1) % 2);
    		}
    		break;
    	case Codes.Commands.RENAME_DEVICE:
    		if (response[0] == Codes.Return.ACK) {
    			Codes.PKT_COUNT = (byte)((Codes.PKT_COUNT + 1) % 2);
    		}
    		break;
    	case Codes.Commands.IR_TRANSMIT:
    		if (response[0] == Codes.Return.ACK) {
    			Codes.PKT_COUNT = (byte)((Codes.PKT_COUNT + 1) % 2);
    		}
    		break;
    	}	
    }
}
