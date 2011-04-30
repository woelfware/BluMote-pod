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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.woelfware.database.MyDB;

public class Droidmote extends Activity {
	// Debugging
    //private static final String TAG = "BlueMote";
    public static final boolean D = true;
    
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_MANAGE_DEVICE = 3;
    //private static final int REQUEST_RENAME_POD = 4;
    
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
    
    // Context menu constants
    private static final int ID_TRAIN = 0;
    private static final int ID_UNTRAIN = 1;
    
    // Dialog menu constants
    private static final int DIALOG_SHOW_INFO = 0;
    
    // Layout Views
    private TextView mTitle;
    private ImageButton fav_btn;
    private ImageButton btn_volume_up;
    private ImageButton btn_volume_down;
    private ImageButton btn_channel_up;
    private ImageButton btn_channel_down;
    private Spinner device_spinner;
    private Button btn_input;
//    private ImageButton led_btn;
    private ImageButton btn_pwr;
    private ImageButton power2_btn;
    private ImageButton back_skip_btn;
    private ImageButton back_btn;
    private ImageButton forward_btn;
    private ImageButton skip_forward_btn;
    private ImageButton record_btn;
    private ImageButton stop_btn;
    private ImageButton play_btn;
    private ImageButton pause_btn;
    private ImageButton eject_btn;
    private Button disc_btn;
    private ImageButton left_btn;
    private ImageButton mute_btn;
    private ImageButton down_btn;
    private Button info_btn;
    private ImageButton right_btn;
    private ImageButton return_btn;
    private ImageButton pgup_btn;
    private ImageButton pgdn_btn;
    private Button guide_btn;
    private Button exit_btn;
    private ImageButton btn_up;
    private ImageButton move_left_btn;
    private ImageButton move_right_btn;
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
    private Button btn_last;
    private ImageButton btn_home;
    private Button btn_misc1;
    private Button btn_misc2;
    private Button btn_misc3;
    private Button btn_misc4;
    private Button btn_misc5;
    private Button btn_misc6;
    private Button btn_misc7;
    private Button btn_misc8;
    
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
    
    // context of screen view, using this for categorizing in database
    // hardcoding this for now, will need to adjust for new contexts
    private String cur_context = "tv-dvd"; 
    
    // Currently selected button resource id (for training mode operations) 
    private int BUTTON_ID = 0; 
    
    // current State of the pod communication
    private byte STATE = Codes.IDLE;
    
    // Sets the delay in-between repeated sending of the keys on the interface
    private static int DELAY_TIME = 250; 
    private static int LONG_DELAY_TIME = 750;
    
    // Flag that tells us if we are holding our finger on a buttona and should loop
    private static boolean LOOP_KEY = false;
    
    // last button pushed, used in handler, prevents firing wrong click event
    private static int last_button = 0;  
    
    // keeps track of # of times the MESSAGE_PRESSED has been called, creator/consumer idea
    // prevents user from double tapping a button and creating double messages in queue
    private static int NUM_MESSAGES = 0;    
    
    // Hash map to keep track of all the buttons on the interface and associated properties
    HashMap<Integer,Object[]> button_map;
            
    // These are used for activities display window
	private ArrayAdapter<String> mActivitiesArrayAdapter;
    private static final int ACTIVITY_ADD=5;
    private static final int ACTIVITY_RENAME=6;
    private static final int ID_DELETE = 0;
    private static final int ID_RENAME = 1;
    private Button add_activity_btn;
    ListView activitiesListView;
    
    // keep track of what the active page of buttons is
    public enum Pages {
        MAIN,NUMBERS,ACTIVITIES 
    }
    private Pages page = Pages.MAIN;
    
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
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the session
        } 
        
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
    }
        
    @Override
    public void onStart() {
        super.onStart();
            if (mChatService == null) {
            	// Initialize the BluetoothChatService to perform bluetooth connections
                mChatService = new BluetoothChatService(this, mHandler);
            	setupDefaultButtons();
            }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        device_data.open(); // make sure database open
        
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
        
        // setup spinner (need this in case we removed the spinner item from a call to managedevices)
        if (page == Pages.MAIN) {
            setupSpinner();  // update spinner items
            fetchButtons();  // update buttons from DB
        }
        else if (page == Pages.NUMBERS) {
        	// reserved for future use....
        }
		
		// See if the bluetooth device is connected, if not try to connect
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
        
    }

    @Override
    public void onStop() {
        super.onStop();
        
        //if (mChatService != null) mChatService.stop();
        // close sqlite database connection
        device_data.close();
                     
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        //device_data.close();
        //save the last table in preferences for next time we launch
//        Editor mEditor =  prefs.edit();
//        mEditor.putString("lastDevice",cur_table);
//        mEditor.commit();   
                
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
    				// spaces are removed from table names, converted to underscores,
    				// so convert them back here
    				str1 = str1.replace("_", " ");
    				mAdapter.add(str1);
    			}
    		} while (cursor1.moveToNext());
    	}
    }
    
    // Called when a user pushes a button down for the first time
    private void touchButton(int rbutton) {
    	ImageButton imgbutton = null; // some buttons are image buttons, don't know which until try
    	
    	Object[] payload = null;    	
    	    	    	  	    	
    	payload = button_map.get(rbutton);

    	if (payload != null) {
    		try {
    			// test if button is an image button type
    			imgbutton = (ImageButton)payload[BTN_NAME];
    			
    			// see if we have a navigation page move command....
    			if (imgbutton == move_left_btn) {
    				LOOP_KEY = false;

    				switch (page) {
    				case MAIN:	    			
    					return;
    				case NUMBERS:
    					setContentView(R.layout.main);
    					page = Pages.MAIN;
    					setupDefaultButtons();
    					return;
    				}

    				return;
    			}
    			// check if the navigation move_right was pushed 
    			// this only works when we are in main screen
    			if (imgbutton == move_right_btn) {
    				LOOP_KEY = false;
    				switch (page) {
    				case MAIN:
    					setContentView(R.layout.number_screen);
    					page = Pages.NUMBERS;
    					setupNumbers();
    					return;
    				case NUMBERS:	    			
    					return;
    				}
    				return;
    			}
    		}
    		catch (Exception e) {
    			// guess it wasn't....
    		}    		
    		    		
	    	// if we got here it means we are a regular button, not move_left or move_right
	    	
	    	// turn on LED!
	//		led_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.led));

			NUM_MESSAGES++;
			
    		Message msg = new Message();            
    		msg.what = MESSAGE_KEY_PRESSED;

    		buttonSend((String)payload[BTN_TEXT]);
    		msg.arg1 = rbutton;
    		last_button = rbutton;	
    		mHandler.sendMessageDelayed(msg, LONG_DELAY_TIME);
    		
    	}    	    	    	    
    }

    View.OnTouchListener buttonTouch = new View.OnTouchListener() {
    	public boolean onTouch(View v, MotionEvent e) {
    		//only execute this one time and only if not in learn mode
    		// if we don't have !LOOP_KEY you can hit button multiple times
    		// and hold finger on button and you'll get duplicates    		
    		if (e.getAction() == MotionEvent.ACTION_DOWN) {
    			// even in learn mode should buzz button if action-down happens
    			v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);    
    		}
    		
    		if (STATE != Codes.LEARN && STATE != Codes.ACTIVITY) {     		
        		// don't want to execute touchButton when in learn mode or activity mode
    			if (e.getAction() == MotionEvent.ACTION_DOWN) {  
    	    		LOOP_KEY = true; // start looping until we lift finger off key
    	    		
    				touchButton(v.getId());    		
    			}   
    			else if (e.getAction() == MotionEvent.ACTION_UP) {
    				// turn off LED
  //  				led_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.led_off));

    				LOOP_KEY = false;	// reset loop key global
    			}
    		}
    		return false; // allows XML to consume
    	}
    };
    
    View.OnClickListener buttonClick = new OnClickListener() {
        public void onClick(View v) {
        	BUTTON_ID = v.getId();
        	
        	if (STATE == Codes.ACTIVITY) {
        		//TODO implement appending/saving button to the activity list item.... 
        	}
        	else if (STATE == Codes.LEARN) {
    	    		sendCode(Codes.LEARN);  	    	    	    	    	    	 			
    		}
        	else { // skip this handler if we are in learn button mode
        		// send message to handler after the delay expires, allows for repeating event
        		if (NUM_MESSAGES == 0 && LOOP_KEY) {
        			Message msg = new Message();
        			msg.what = MESSAGE_KEY_PRESSED;

        			Object[] payload = null;    	
        			payload = button_map.get(BUTTON_ID);

        			if (payload != null) {
        				msg.arg1 = BUTTON_ID;
        				buttonSend((String)payload[BTN_TEXT]);
        			}    	    	

        			NUM_MESSAGES++;
        			mHandler.sendMessageDelayed(msg, DELAY_TIME);
        		}

        	}
        }
    };
    
    // called to setup the buttons on the main screen
    private void setupDefaultButtons() {
    	
    	setupSpinner();
        
        // Initialize the buttons with a listener for click and touch events
        btn_volume_up = (ImageButton) findViewById(R.id.btn_volume_up);
        btn_volume_down = (ImageButton) findViewById(R.id.btn_volume_down);
        btn_channel_up = (ImageButton) findViewById(R.id.btn_channel_up);
        btn_channel_down = (ImageButton) findViewById(R.id.btn_channel_down);
        btn_input = (Button) findViewById(R.id.btn_input);
//        led_btn = (ImageButton) findViewById(R.id.led_btn);
        btn_pwr = (ImageButton) findViewById(R.id.power_btn);
        power2_btn = (ImageButton) findViewById(R.id.power2_btn);
        back_skip_btn = (ImageButton) findViewById(R.id.back_skip_btn);
        back_btn = (ImageButton) findViewById(R.id.back_btn);
        forward_btn = (ImageButton) findViewById(R.id.forward_btn);
        skip_forward_btn = (ImageButton) findViewById(R.id.skip_forward_btn); 
        record_btn = (ImageButton) findViewById(R.id.record_btn);
        stop_btn = (ImageButton) findViewById(R.id.stop_btn);
    	play_btn = (ImageButton) findViewById(R.id.play_btn);
    	eject_btn = (ImageButton) findViewById(R.id.eject_btn);
    	disc_btn = (Button) findViewById(R.id.disc_btn);
    	mute_btn = (ImageButton) findViewById(R.id.mute_btn);
    	info_btn = (Button) findViewById(R.id.info_btn);
    	return_btn = (ImageButton) findViewById(R.id.return_btn);
    	pgup_btn = (ImageButton) findViewById(R.id.pgup_btn);
    	pgdn_btn = (ImageButton) findViewById(R.id.pgdn_btn);
    	guide_btn = (Button) findViewById(R.id.guide_btn);
    	exit_btn = (Button) findViewById(R.id.exit_btn);
    	move_right_btn = (ImageButton) findViewById(R.id.move_right_btn);
    	move_left_btn = (ImageButton) findViewById(R.id.move_left_btn);
    	pause_btn = (ImageButton) findViewById(R.id.pause_btn);
    	fav_btn = (ImageButton) findViewById(R.id.fav_btn);
    	btn_last = (Button) findViewById(R.id.btn_last);
        btn_volume_up.setOnTouchListener(buttonTouch);
        btn_volume_up.setOnClickListener(buttonClick);
        btn_volume_down.setOnTouchListener(buttonTouch);
        btn_volume_down.setOnClickListener(buttonClick);
        btn_channel_up.setOnTouchListener(buttonTouch);
        btn_channel_up.setOnClickListener(buttonClick);
        btn_channel_down.setOnTouchListener(buttonTouch);
        btn_channel_down.setOnClickListener(buttonClick);
        btn_input.setOnClickListener(buttonClick);
        btn_input.setOnTouchListener(buttonTouch);
        btn_pwr.setOnClickListener(buttonClick);
        btn_pwr.setOnTouchListener(buttonTouch);
        power2_btn.setOnClickListener(buttonClick);
        power2_btn.setOnTouchListener(buttonTouch);
        back_skip_btn.setOnClickListener(buttonClick);
        back_skip_btn.setOnTouchListener(buttonTouch);
        back_btn.setOnClickListener(buttonClick);
        back_btn.setOnTouchListener(buttonTouch);
        forward_btn.setOnClickListener(buttonClick);
        forward_btn.setOnTouchListener(buttonTouch);
        skip_forward_btn.setOnClickListener(buttonClick);
        skip_forward_btn.setOnTouchListener(buttonTouch);
        record_btn.setOnClickListener(buttonClick);
        record_btn.setOnTouchListener(buttonTouch);
        stop_btn.setOnClickListener(buttonClick);
        stop_btn.setOnTouchListener(buttonTouch);
        play_btn.setOnClickListener(buttonClick);
        play_btn.setOnTouchListener(buttonTouch);
        eject_btn.setOnClickListener(buttonClick);
        eject_btn.setOnTouchListener(buttonTouch);
        disc_btn.setOnClickListener(buttonClick);
        disc_btn.setOnTouchListener(buttonTouch);        
        mute_btn.setOnClickListener(buttonClick);
        mute_btn.setOnTouchListener(buttonTouch);;
        info_btn.setOnClickListener(buttonClick);
        info_btn.setOnTouchListener(buttonTouch);
        return_btn.setOnClickListener(buttonClick);
        return_btn.setOnTouchListener(buttonTouch);
        pgup_btn.setOnClickListener(buttonClick);
        pgup_btn.setOnTouchListener(buttonTouch);
        pgdn_btn.setOnClickListener(buttonClick);
        pgdn_btn.setOnTouchListener(buttonTouch);
        guide_btn.setOnClickListener(buttonClick);
        guide_btn.setOnTouchListener(buttonTouch);
        exit_btn.setOnClickListener(buttonClick);
        exit_btn.setOnTouchListener(buttonTouch);
        move_left_btn.setOnClickListener(buttonClick);
        move_left_btn.setOnTouchListener(buttonTouch);
        move_right_btn.setOnClickListener(buttonClick);
        move_right_btn.setOnTouchListener(buttonTouch);
        pause_btn.setOnClickListener(buttonClick);
        pause_btn.setOnTouchListener(buttonTouch);
        fav_btn.setOnTouchListener(buttonTouch);
    	fav_btn.setOnClickListener(buttonClick);
        btn_last.setOnTouchListener(buttonTouch);
        btn_last.setOnClickListener(buttonClick);
        
        //set bundle of associated button properties
        // order is : Button name, database string identifier for btn, graphic for unpressed, graphic for pushed         
        Object[] btn_1 = {btn_volume_up, "btn_volume_up"};
        Object[] btn_2 = {btn_volume_down, "btn_volume_down"};
        Object[] btn_3 = {btn_channel_up, "btn_channel_up"};
        Object[] btn_4 = {btn_channel_down, "btn_channel_down"};
        Object[] btn_5 = {btn_input, "btn_input"};
        Object[] btn_6 = {btn_pwr, "btn_pwr"};
        Object[] btn_7 = {power2_btn, "power2_btn"};
        Object[] btn_8 = {back_skip_btn, "back_skip_btn"};
        Object[] btn_9 = {back_btn, "back_btn"};
        Object[] btn_10 = {forward_btn, "forward_btn"};
        Object[] btn_11 = {skip_forward_btn, "skip_forward_btn"};
        Object[] btn_12 = {record_btn, "record_btn"};
        Object[] btn_13 = {stop_btn, "stop_btn"};
        Object[] btn_14 = {play_btn, "play_btn"};
        Object[] btn_15 = {pause_btn, "pause_btn"};
        Object[] btn_16 = {eject_btn, "eject_btn"};
        Object[] btn_17 = {disc_btn, "disc_btn"};        
        Object[] btn_19 = {mute_btn, "mute_btn"};
        Object[] btn_23 = {info_btn, "info_btn"};
        Object[] btn_25 = {return_btn, "return_btn"};
        Object[] btn_26 = {pgup_btn, "pgup_btn"};
        Object[] btn_27 = {pgdn_btn, "pgdn_btn"};
        Object[] btn_28 = {guide_btn, "guide_btn"};
        Object[] btn_29 = {exit_btn, "exit_btn"};
        Object[] btn_30 = {move_right_btn, "move_right_btn"};
        Object[] btn_31 = {move_left_btn, "move_left_btn"};
        Object[] btn_32 = {fav_btn, "fav_btn"};
        Object[] btn_33 = {btn_last, "btn_last"};

        
        // bundle all the button data into a big hashtable
        button_map = new HashMap<Integer,Object[]>();
        button_map.put(R.id.btn_volume_up, btn_1);
        button_map.put(R.id.btn_volume_down, btn_2);
        button_map.put(R.id.btn_channel_up, btn_3);
        button_map.put(R.id.btn_channel_down, btn_4);
        button_map.put(R.id.btn_input, btn_5);
        button_map.put(R.id.power_btn, btn_6);
        button_map.put(R.id.power2_btn, btn_7);
        button_map.put(R.id.back_skip_btn, btn_8);
        button_map.put(R.id.back_btn, btn_9);
        button_map.put(R.id.forward_btn, btn_10);
        button_map.put(R.id.skip_forward_btn, btn_11);
        button_map.put(R.id.record_btn, btn_12);
        button_map.put(R.id.stop_btn, btn_13);
        button_map.put(R.id.play_btn, btn_14);
        button_map.put(R.id.pause_btn, btn_15);
        button_map.put(R.id.eject_btn, btn_16);
        button_map.put(R.id.disc_btn, btn_17);
        button_map.put(R.id.mute_btn,btn_19);
        button_map.put(R.id.info_btn,btn_23);
        button_map.put(R.id.return_btn,btn_25);
        button_map.put(R.id.pgup_btn,btn_26);
        button_map.put(R.id.pgdn_btn,btn_27);
        button_map.put(R.id.guide_btn,btn_28);
        button_map.put(R.id.exit_btn,btn_29);
        button_map.put(R.id.move_right_btn,btn_30);
        button_map.put(R.id.move_left_btn,btn_31);
        button_map.put(R.id.fav_btn, btn_32);
        button_map.put(R.id.btn_last, btn_33);
                
    }
    
    // Same as SetupButtons but called when user goes into number selection view
    private void setupNumbers() {
//        led_btn = (ImageButton) findViewById(R.id.led_btn);
    	move_right_btn = (ImageButton) findViewById(R.id.move_right_btn);
    	move_left_btn = (ImageButton) findViewById(R.id.move_left_btn);
    	btn_n0 = (Button) findViewById(R.id.btn_n0);
    	btn_n1 = (Button) findViewById(R.id.btn_n1);
    	btn_n2 = (Button) findViewById(R.id.btn_n2);
    	btn_n3 = (Button) findViewById(R.id.btn_n3);
    	btn_n4 = (Button) findViewById(R.id.btn_n4);
    	btn_n5 = (Button) findViewById(R.id.btn_n5);
    	btn_n6 = (Button) findViewById(R.id.btn_n6);
    	btn_n7 = (Button) findViewById(R.id.btn_n7);
    	btn_n8 = (Button) findViewById(R.id.btn_n8);
    	btn_n9 = (Button) findViewById(R.id.btn_n9);
    	btn_dash = (Button) findViewById(R.id.btn_dash);
    	btn_enter = (Button) findViewById(R.id.btn_enter);
    	btn_exit = (Button) findViewById(R.id.btn_exit);
    	btn_home = (ImageButton) findViewById(R.id.btn_home);
    	left_btn = (ImageButton) findViewById(R.id.left_btn);
    	right_btn = (ImageButton) findViewById(R.id.right_btn);
    	btn_up = (ImageButton) findViewById(R.id.btn_up);
    	down_btn = (ImageButton) findViewById(R.id.down_btn);
    	btn_misc1 = (Button) findViewById(R.id.btn_misc1);
    	btn_misc2 = (Button) findViewById(R.id.btn_misc2);
    	btn_misc3 = (Button) findViewById(R.id.btn_misc3);
    	btn_misc4 = (Button) findViewById(R.id.btn_misc4);
    	btn_misc5 = (Button) findViewById(R.id.btn_misc5);
    	btn_misc6 = (Button) findViewById(R.id.btn_misc6);
    	btn_misc7 = (Button) findViewById(R.id.btn_misc7);
    	btn_misc8 = (Button) findViewById(R.id.btn_misc8);
    	
    	// action listeners
    	btn_n0.setOnTouchListener(buttonTouch);
        btn_n0.setOnClickListener(buttonClick);
        btn_n1.setOnTouchListener(buttonTouch);
        btn_n1.setOnClickListener(buttonClick);
        btn_n2.setOnTouchListener(buttonTouch);
        btn_n2.setOnClickListener(buttonClick);
        btn_n3.setOnTouchListener(buttonTouch);
        btn_n3.setOnClickListener(buttonClick);
        btn_n4.setOnTouchListener(buttonTouch);
        btn_n4.setOnClickListener(buttonClick);
        btn_n5.setOnTouchListener(buttonTouch);
        btn_n5.setOnClickListener(buttonClick);
        btn_n6.setOnTouchListener(buttonTouch);
        btn_n6.setOnClickListener(buttonClick);
        btn_n7.setOnTouchListener(buttonTouch);
        btn_n7.setOnClickListener(buttonClick);
        btn_n8.setOnTouchListener(buttonTouch);
        btn_n8.setOnClickListener(buttonClick);
        btn_n9.setOnTouchListener(buttonTouch);
        btn_n9.setOnClickListener(buttonClick);
        btn_dash.setOnTouchListener(buttonTouch);
        btn_dash.setOnClickListener(buttonClick);
        btn_enter.setOnTouchListener(buttonTouch);
        btn_enter.setOnClickListener(buttonClick);
        btn_exit.setOnTouchListener(buttonTouch);
        btn_exit.setOnClickListener(buttonClick); 
        btn_home.setOnTouchListener(buttonTouch);
        btn_home.setOnClickListener(buttonClick);
        move_left_btn.setOnClickListener(buttonClick);
        move_left_btn.setOnTouchListener(buttonTouch);
        move_right_btn.setOnClickListener(buttonClick);
        move_right_btn.setOnTouchListener(buttonTouch);
        left_btn.setOnClickListener(buttonClick);
        left_btn.setOnTouchListener(buttonTouch);
        right_btn.setOnClickListener(buttonClick);
        right_btn.setOnTouchListener(buttonTouch);
        btn_up.setOnClickListener(buttonClick);
        btn_up.setOnTouchListener(buttonTouch);
        down_btn.setOnClickListener(buttonClick);
        down_btn.setOnTouchListener(buttonTouch);
        btn_misc1.setOnClickListener(buttonClick);
        btn_misc1.setOnTouchListener(buttonTouch);
        btn_misc2.setOnClickListener(buttonClick);
        btn_misc2.setOnTouchListener(buttonTouch);
        btn_misc3.setOnClickListener(buttonClick);
        btn_misc3.setOnTouchListener(buttonTouch);
        btn_misc4.setOnClickListener(buttonClick);
        btn_misc4.setOnTouchListener(buttonTouch);
        btn_misc5.setOnClickListener(buttonClick);
        btn_misc5.setOnTouchListener(buttonTouch);
        btn_misc6.setOnClickListener(buttonClick);
        btn_misc6.setOnTouchListener(buttonTouch);
        btn_misc7.setOnClickListener(buttonClick);
        btn_misc7.setOnTouchListener(buttonTouch);
        btn_misc8.setOnClickListener(buttonClick);
        btn_misc8.setOnTouchListener(buttonTouch);
        
    	//set bundle of associated button properties
        // order is : Button name, String database id for btn, graphic for unpressed, graphic for pushed 
        Object[] btn_1 = {btn_n0, "btn_n0"};
        Object[] btn_2 = {btn_n1, "btn_n1"};
        Object[] btn_3 = {btn_n2, "btn_n2"};
        Object[] btn_4 = {btn_n3, "btn_n3"};
        Object[] btn_5 = {btn_n4, "btn_n4"};
        Object[] btn_6 = {btn_n5, "btn_n5"};
        Object[] btn_7 = {btn_n6, "btn_n6"};
        Object[] btn_8 = {btn_n7, "btn_n7"};
        Object[] btn_9 = {btn_n8, "btn_n8"};
        Object[] btn_10 = {btn_n9, "btn_n9"};
        Object[] btn_11 = {btn_dash, "btn_dash"};
        Object[] btn_12 = {btn_enter, "btn_enter"};
        Object[] btn_13 = {btn_exit, "btn_exit"};
        Object[] btn_15 = {btn_home, "btn_home"};
        Object[] btn_16 = {move_right_btn, "move_right_btn"};
        Object[] btn_17 = {move_left_btn, "move_left_btn"};
        Object[] btn_18 = {left_btn, "left_btn"};
        Object[] btn_19 = {right_btn, "right_btn"};
        Object[] btn_20 = {btn_up, "btn_up"};
        Object[] btn_21 = {down_btn, "down_btn"};
        Object[] btn_22 = {btn_misc1, "btn_misc1"};
        Object[] btn_23 = {btn_misc2, "btn_misc2"};
        Object[] btn_24 = {btn_misc3, "btn_misc3"};
        Object[] btn_25 = {btn_misc4, "btn_misc4"};
        Object[] btn_26 = {btn_misc5, "btn_misc5"};
        Object[] btn_27 = {btn_misc6, "btn_misc6"};
        Object[] btn_28 = {btn_misc7, "btn_misc7"};
        Object[] btn_29 = {btn_misc8, "btn_misc8"};
        
        // bundle all the button data into a big hashtable
        button_map = new HashMap<Integer,Object[]>();
        button_map.put(R.id.btn_n0, btn_1);
        button_map.put(R.id.btn_n1, btn_2);
        button_map.put(R.id.btn_n2, btn_3);
        button_map.put(R.id.btn_n3, btn_4);
        button_map.put(R.id.btn_n4, btn_5);
        button_map.put(R.id.btn_n5, btn_6);
        button_map.put(R.id.btn_n6, btn_7);
        button_map.put(R.id.btn_n7, btn_8);
        button_map.put(R.id.btn_n8, btn_9);
        button_map.put(R.id.btn_n9, btn_10);
        button_map.put(R.id.btn_dash, btn_11);
        button_map.put(R.id.btn_enter, btn_12);
        button_map.put(R.id.btn_exit, btn_13);
        button_map.put(R.id.btn_home, btn_15);
        button_map.put(R.id.move_right_btn, btn_16);
        button_map.put(R.id.move_left_btn, btn_17);
        button_map.put(R.id.left_btn, btn_18);
        button_map.put(R.id.right_btn,btn_19);
        button_map.put(R.id.btn_up,btn_20);
        button_map.put(R.id.down_btn,btn_21);
        button_map.put(R.id.btn_misc1,btn_22);
        button_map.put(R.id.btn_misc2,btn_23);
        button_map.put(R.id.btn_misc3,btn_24);
        button_map.put(R.id.btn_misc4,btn_25);
        button_map.put(R.id.btn_misc5,btn_26);
        button_map.put(R.id.btn_misc6,btn_27);
        button_map.put(R.id.btn_misc7,btn_28);
        button_map.put(R.id.btn_misc8,btn_29);
    }
    
    // This is for the left-most screen - for activities view
    private void setupActivities() {
    	// Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mActivitiesArrayAdapter = new ArrayAdapter<String>(this, R.layout.manage_devices_item);
        
        // Find and set up the ListView for paired devices
        activitiesListView = (ListView) findViewById(R.id.activities_list);
        activitiesListView.setAdapter(mActivitiesArrayAdapter);
        activitiesListView.setOnItemClickListener(mDeviceClickListener);
        
        add_activity_btn = (Button) findViewById(R.id.add_activity_btn);
        add_activity_btn.setOnClickListener( new OnClickListener() {
            public void onClick(View v) {
            	// Launch the function to ask for a name for device
            	Intent i = new Intent(getApplicationContext(), EnterDevice.class);
                startActivityForResult(i, ACTIVITY_ADD);
            }
        });
        
        //TODO this is just a placeholder below, implement working with preferences file
        Cursor cursor1;
        String str1;
        cursor1 = device_data.getTables();
        cursor1.moveToFirst();
        mActivitiesArrayAdapter.clear(); // always clear before adding items
        if (cursor1.getCount() > 0) {
        	do {
        		// need to exclude android_metadata and sqlite_sequence tables from results
        		str1 = cursor1.getString(0);
        		if (!(str1.equals("android_metadata")) 
        				&& !(str1.equals("sqlite_sequence"))) {
        			// convert underscores to spaces
        			str1 = str1.replace("_", " ");
        			mActivitiesArrayAdapter.add(str1);
        		}
        	} while (cursor1.moveToNext());
        }
        
        // context menu on array list
        registerForContextMenu(findViewById(R.id.activities_list));
        
    }
    
    // sets up the spinner at the top of each screen
    // populates it with the last used device view
    private void setupSpinner() {
    	// setup spinner
    	device_spinner = (Spinner) findViewById(R.id.device_spinner);
    	mAdapter = new ArrayAdapter<String>(this, R.layout.spinner_entry);
    	mAdapter.setDropDownViewResource(R.layout.spinner_entry);
    	device_spinner.setAdapter(mAdapter);
    	device_spinner.setOnItemSelectedListener(new MyOnItemSelectedListener(this));
    	populateDropDown();

    	// set spinner to default from last session if possible
 //   	prefs = getSharedPreferences("droidMoteSettings", MODE_PRIVATE);
    	String prefs_table = prefs.getString("lastDevice", null);
    	if (prefs_table != null) {
    		for(int i=0; i<device_spinner.getCount(); i++) {
    			if (prefs_table.equals(device_spinner.getItemAtPosition(i))) {
    				device_spinner.setSelection(i);
    			}
    		}        
    	}
    }

    private void ensureDiscoverable() {
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
        // check that we have a context selected and available
        if (devices != null) {
        	// Check that there's actually something to send
        	if (message.length > 0) {
        		// Get the message bytes and tell the BluetoothChatService to write
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
                    // Store the address of connecting device to preferences for re-connect on resume
                    // this global gets setup in onActivityResult()
                    
                    Editor mEditor =  prefs.edit();
                    mEditor.putString("lastPod",connectingMAC);
                    // first need to pull current known devices list so we can append to it
                    if (connectingDevice != null) { 
                    	prefs = getSharedPreferences("droidMoteSettings", MODE_PRIVATE);
                    	String prefs_table = prefs.getString("knownDevices", null);            	                                          

                    	// then pull name of device off and append
                    	if (prefs_table == null) {
                    		prefs_table = connectingDevice; // '\t' is the delimeter between items
                    	}
                    	else {
                    		// make sure isn't already in the list
                    		String devices[] = prefs_table.split("\t"); 
                    		boolean foundIt = false;
                    		for (String device : devices) {
                    			if (device.matches(connectingDevice) ) {                        	
                    				foundIt = true;
                    				break;
                    			}
                    		} 
                    		if (foundIt == false) {
                    			prefs_table = prefs_table + "\t"+connectingDevice; // '\t' is the delimeter between items
                    		}                    	                    	
                    	}
                    	mEditor.putString("knownDevices",prefs_table);
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
            	if ((int)msg.arg1 == last_button && LOOP_KEY) {            		            			
            			Object[] payload = button_map.get(msg.arg1);
            			if (payload != null) {
            				try {
            					Button toclick;
            					toclick = (Button)payload[BTN_NAME];
            					toclick.performClick();
            				} catch (Exception e) {
            					// must be an imagebutton then
            					ImageButton toclick;
            					toclick = (ImageButton)payload[BTN_NAME];
            					toclick.performClick();
            				}
            			}        		        		
            	}
            	break;
            }
        }
    };

    // called when activities finish running and return to this activity
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
                connectingMAC = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                
                // save device for future use - no need to re-scan
                connectingDevice = (data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME));
                					//.replaceAll("\\\\", "blah"); //for some reason strings get double backslash when pulling out
                                
                // Store the address of device to preferences for connect in onResume()
                Editor mEditor =  prefs.edit();
                mEditor.putString("lastPod",connectingMAC);
                mEditor.commit();

                // Attempt to connect to the device
                //device.getName(); // grab the friendly name, a rename command can change this
            }
            break;
            
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupDefaultButtons();
            } else {
                // User did not enable Bluetooth or an error occured
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
            
        case REQUEST_MANAGE_DEVICE:
        	// when the manage devices activity returns
        	if (resultCode == Activity.RESULT_OK) {
        		// re-populate the drop-down menu and set selection to the first item
        		//device_data.open();	// onActivityResult() is called BEFORE onResume() so need this!
        		//TODO add check to prevent this from crashing us
        		//populateDropDown();
        	}
        	break;
        	
        case ACTIVITY_ADD:
        	//TODO implement working with preferences file
        	if (resultCode == Activity.RESULT_OK) {
        		// add the new item to the database
        		Bundle return_bundle = data.getExtras();
        		if ( return_bundle != null ) {
        			String return_string = return_bundle.getString("returnStr");
        			// spaces don't work for table names, so replace with underscore
        			return_string = return_string.replace(" ", "_");
            		device_data.createTable(return_string);
        		}
            	// refresh the display of items
        		setupActivities(); 
        	}
        	break;
        case ACTIVITY_RENAME:
        	//TODO implement working with prefernces file
        	if (resultCode == Activity.RESULT_OK) {
        		Bundle return_bundle = data.getExtras();
        		if ( return_bundle != null ) {
        			String return_string = return_bundle.getString("returnStr");
            		//device_data.renameTable(table_name, return_string);
            		
        		}
            	// refresh the display of items
        		setupActivities(); 
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
        	// first stop any connecting process if it is running
        	if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
        		mChatService.stop();
        	}
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
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
        	sendCode(Codes.GET_VERSION);
        	return true;
        	
        case R.id.learn_button:
        	Toast.makeText(this, "Select button to train", Toast.LENGTH_SHORT).show();
        	STATE = Codes.LEARN;
        	return true;
        	
        case R.id.stop_learn:        
        	Toast.makeText(this, "Stopped Learning", Toast.LENGTH_SHORT).show();
        	sendCode(Codes.ABORT_LEARN);
        	STATE = Codes.ABORT_LEARN;
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
    		Editor mEditor =  prefs.edit();
            mEditor.putString("lastDevice",cur_table);
            mEditor.commit();
    	}

        public void onNothingSelected(AdapterView<?> parent) {
          // Do nothing.
        }
    }
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		String table_name;
		switch(item.getItemId()) {
		case ID_DELETE:
			//TODO this should delete an activity from the preferences file
//			// need to remove this table and repopulate list
//			table_name = mActivitiesArrayAdapter.getItem((int)(info.id));
//			// replace spaces with underscores
//			table_name = table_name.replace(" ", "_");
//			device_data.removeTable(table_name);
//			setupActivities(); // repopulate the display now
			return true;
		case ID_RENAME:
			//TODO this should delete an activity from the preferences file
//			// need to remove this table and repopulate list
//			table_name = mActivitiesArrayAdapter.getItem((int)(info.id));
//			//launch window to get new name to use
//			Intent i = new Intent(this, EnterDevice.class);
//            startActivityForResult(i, ACTIVITY_RENAME);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.devices_list) {
//			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle("Menu");
			menu.add(0, ID_DELETE, 0, "Delete");
			menu.add(0, ID_RENAME, 0, "Rename");
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}
    
	  // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	//TODO implement execution of the activity that was selected
        }
    };
	
    // this function updates the current table with what is selected in drop-down
	// it then grabs the button keys from that table into local devices Cursor
    public void fetchButtons() {
    	// first update the cur_table from spinner
    	if (device_spinner.getCount() > 0) {
    		Object table = device_spinner.getSelectedItem();
    		if (table != null) {
    			// replace spaces with underscores and then set cur_table to that
    			cur_table = table.toString().replace(" ", "_");
    			devices = device_data.getKeys(cur_table);
    		}
    	}
    }
    
    // this function sends the code to the pod based on the button that was selected
    public void buttonSend(String buttonCode) {
    	String column;
    	if (devices != null) {
    		devices.moveToFirst();
    		for (int i=0; i< devices.getCount(); i++) {
    			column = devices.getString(1);
    			if (column.equals(buttonCode)) {
    				byte[] code = devices.getBlob(2);    			
    				byte command = (byte)(Codes.IR_TRANSMIT);
    				byte[] toSend = new byte[code.length+1]; // 1 extra bytes for command byte
    				toSend[0] = command;
    				for (int j=1; j < toSend.length; j++) {
    					toSend[j] = code[j-1];
    				}
    				//{command, 0x00, code}; // 0x00 is reserved byte
    				STATE = Codes.IR_TRANSMIT;
    				sendMessage(toSend); // send data if matches 
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
    	case Codes.LEARN:
    		toSend = new byte[1];
    		toSend[0] = Codes.LEARN;
    		toSend[0] = (byte)toSend[0];
    		STATE = Codes.LEARN;
    		sendMessage(toSend); 
    		break;
    		
    	case Codes.ABORT_LEARN:
    		toSend = new byte[1];
    		toSend[0] = Codes.ABORT_LEARN;
    		toSend[0] = (byte)toSend[0];    		
    		STATE = Codes.ABORT_LEARN;
    		sendMessage(toSend);
    		break;
    		
    	case Codes.GET_VERSION:
    		STATE = Codes.GET_VERSION;
    		toSend = new byte[1];
    		toSend[0] = Codes.GET_VERSION;
    		toSend[0] = (byte)toSend[0];    		
    		sendMessage(toSend);
    		break;
    	}
    }
    
    // called after learn mode is finished and has data to store
    public void storeButton() {
		Object[] payload = null;
		payload = button_map.get(BUTTON_ID);
		
		if (payload != null) {
			device_data.insertButton(cur_table, (String)payload[BTN_TEXT], cur_context, Codes.pod_data);
		}
	    		    
		// refresh the local Cursor with new database updates
		fetchButtons();
		STATE = Codes.IDLE; // reset state, drop out of learn mode		
		Codes.learn_state = Codes.LEARN_STATE.IDLE; // ready to start a new learn command now
		Toast.makeText(this, "Button Learned", Toast.LENGTH_SHORT).show();
    }
    
    // returns false if the data to be inserted is more bytes than array is setup for, 
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
    		Toast.makeText(this, "Error occured, exiting learn mode!", Toast.LENGTH_SHORT).show();
    		STATE = Codes.IDLE;
    		Codes.learn_state = Codes.LEARN_STATE.IDLE;
    	}
    	else if (code == 2) {
    		STATE = Codes.IDLE;
    		Codes.info_state = Codes.INFO_STATE.IDLE;
    	}
    }       
    
    // this method will perform x > y with what should be unsigned bytes
    public boolean isGreaterThanUnsignedByte(int x, int y) {
    	int xl = 0x00FF & x;
    	int yl = 0x00FF & y;
    	
    	if (xl > yl) { return true; }
    	else { return false; }
    }
    
    // 	this method should be called whenever we receive a byte[] from the pod
    // the bytes argument tells us how many bytes were received and stored in response[]
    // note that the response array is a circular buffer, the starting index head is 'index'
    public void interpretResponse(byte[] response, int bytes, int index) {   
    	switch (STATE) {
    	case Codes.LEARN:  
    		try { // catch any unforseen state machine errors.....
    			// learn data may not come all together, so need to process data in chunks
    			while (bytes > 0) {
    				switch (Codes.learn_state) {
    				case IDLE: 
    					if (response[index] == Codes.ACK) {
    						Codes.learn_state = Codes.LEARN_STATE.BYTE1;
    						index = (index + 1) % (BluetoothChatService.buffer_size - 1);
    						bytes--;
    						Codes.data_index = 0;
    					}
    					else {
    						signalError(1);
    						return;
    					}
    					break;
    					
    				case BYTE1:
    					// first byte after ACK should be a zero
    					if (response[index] == 0) {
    						Codes.learn_state = Codes.LEARN_STATE.INITIALIZED;
    						bytes--;
    						index = (index + 1) % (BluetoothChatService.buffer_size - 1);
    					}
    					else {
    						signalError(1);
    						return;
    					}
    					break;
    					
    				case INITIALIZED:
    					// if we got here then we are on the third byte of data
    					if ( isGreaterThanUnsignedByte(response[index], 0) ) {
    						Codes.pod_data = new byte[(0x00FF & response[index]) + 2];
    						// store length of data as first two bytes (used in transmitting back)
        					Codes.pod_data[Codes.data_index++] = 0;
        					Codes.pod_data[Codes.data_index++] = response[index];
    						bytes--;
        					index = (index + 1) % (BluetoothChatService.buffer_size - 1);
    						Codes.learn_state = Codes.LEARN_STATE.COLLECTING;
    					}
    					else {
    						signalError(1);
    						return;
    					}    					
    					break;
    					
    				case COLLECTING:    					
    					if (checkPodDataBounds(bytes)) {
    						Codes.pod_data[Codes.data_index++] = response[index];
    						// first check to see if this is the last byte
    						if ( isGreaterThanUnsignedByte(Codes.data_index,Codes.pod_data[1]) ) {
    							// if we got here then we are done, pod_data[1] is the expected message length
    							storeButton();    
    							return;
    						}
    						bytes--;
        					index = (index + 1) % (BluetoothChatService.buffer_size - 1);
    					}
    					else {
    						signalError(1);
    						return;
    					}
    					break;
    				} // end switch/case		    				    				
    			} // end while loop
    		} catch (Exception e) {  //something unexpected occurred....exit gracefully
    			Toast.makeText(this, "Communication error, exiting learn mode", Toast.LENGTH_SHORT).show();
    			STATE = Codes.IDLE;
    			return;
    		}
    		break;
    		
    	case Codes.GET_VERSION:
    		if (response[index] == Codes.ACK) {    			
    			// convert data into a String
    			//TODO need to create state machine to gather data like the learn command
    			try { // catch any unforseen state machine errors.....
    				while (bytes > 0) {
    					switch (Codes.info_state) {
    					case IDLE: 
    						if (response[index] == Codes.ACK) {
    							Codes.pod_data = new byte[4];
    							Codes.info_state = Codes.INFO_STATE.BYTE0;
    							index = (index + 1) % (BluetoothChatService.buffer_size - 1);
    							bytes--;
    							Codes.data_index = 0;
    						}
    						else {
    							signalError(2);
    							return;	    						
    						}
    						break;

    					case BYTE0:
    						Codes.pod_data[0] = response[index];
    						Codes.info_state = Codes.INFO_STATE.BYTE1;
    						bytes--;
    						index = (index + 1) % (BluetoothChatService.buffer_size - 1);    					    			
    						break;
    						
    					case BYTE1:
    						Codes.pod_data[1] = response[index];
    						Codes.info_state = Codes.INFO_STATE.BYTE2;
    						bytes--;
    						index = (index + 1) % (BluetoothChatService.buffer_size - 1);    					    			
    						break;
    						
    					case BYTE2:
    						Codes.pod_data[2] = response[index];
    						Codes.info_state = Codes.INFO_STATE.BYTE3;
    						bytes--;
    						index = (index + 1) % (BluetoothChatService.buffer_size - 1);    					    			
    						break;
    						
    					case BYTE3:
    						Codes.pod_data[3] = response[index];
    						Codes.info_state = Codes.INFO_STATE.IDLE;
    						bytes--;
    						index = (index + 1) % (BluetoothChatService.buffer_size - 1);    					    			
    						break;
    					}
    				}
    			}
    			catch (Exception e) {
    				Toast.makeText(this, "Communication error, exiting learn mode", Toast.LENGTH_SHORT).show();
        			STATE = Codes.IDLE;
        			return;
    			}
    					
    			// need to launch window to dump the data to
    			showDialog(DIALOG_SHOW_INFO);
    		}
    		break;
    		
    	case Codes.ABORT_LEARN:
			STATE = Codes.IDLE; // reset state
    		if (response[index] == Codes.ACK) {

    		}
    		break;
    		
    	case Codes.IR_TRANSMIT:
			STATE = Codes.IDLE; // reset state
    		if (response[index] == Codes.ACK) {

    		}
    		break;    		
    	}	
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		AlertDialog alert;
		switch(id) {
		case DIALOG_SHOW_INFO:
			// define dialog
			StringBuilder podData = new StringBuilder();
			podData.append("Component ID: ");
			podData.append(Codes.pod_data[0]+"\n"); 
			podData.append("Major Revision: ");
			podData.append(Codes.pod_data[1]+"\n");
			podData.append("Minor Revision: ");
			podData.append(Codes.pod_data[2]+"\n");
			podData.append("Revision: ");
			podData.append(Codes.pod_data[3]);
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
	
	// returns integer from two bytes
	// a is upper nibble, b is lower nibble	q
//	private int bytesToInt(byte a, byte b) {  
//		int i = 0;
//	    i |= a & 0xFF;
//	    i <<= 8;
//	    i |= b & 0xFF;
//	    return i;  
//	}   
}
