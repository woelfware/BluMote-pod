package com.woelfware.droidmote;

import java.util.HashMap;
import java.util.Iterator;

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
    private Button btn_input;
    private Button led_btn;
    private Button btn_pwr;
    private Button power2_btn;
    private Button back_skip_btn;
    private Button back_btn;
    private Button forward_btn;
    private Button skip_forward_btn;
    private Button record_btn;
    private Button stop_btn;
    private Button play_btn;
    private Button pause_btn;
    private Button eject_btn;
    private Button disc_btn;
    private Button left_btn;
    private Button mute_btn;
    private Button enter_btn;
    private Button down_btn;
    private Button info_btn;
    private Button right_btn;
    private Button return_btn;
    private Button pgup_btn;
    private Button pgdn_btn;
    private Button guide_btn;
    private Button exit_btn;
    private Button btn_up;
    private Button move_left_btn;
    private Button move_right_btn;
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
    private Button btn_home;
    
    // Gesture for fling event
    private GestureDetector gestureDetector;
    
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
    private static int LONG_DELAY_TIME = 750;
    
    // Flag that tells us if we are holding our finger on a buttona and should loop
    private static boolean LOOP_KEY = false;
    
    // last button pushed, used in handler, prevents firing wrong click event
    private static int last_button = 0;
    
    // sets mode to learn mode for button action handerls
    private static boolean LEARN_MODE = false;
    
    // for fling gesture recognition
    private static final int LARGE_MOVE = 60;
    
    // keeps track of # of times the MESSAGE_PRESSED has been called, creator/consumer idea
    // prevents user from double tapping a button and creating double messages in queue
    private int NUM_MESSAGES = 0;
    
    // Hash map to keep track of all the buttons on the interface and associated properties
    HashMap<Integer,Object[]> button_map;
    
    // keep track of what the active page of buttons is
    public enum Pages {
        MAIN,NUMBERS 
    }
    private Pages page = Pages.MAIN;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
  
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
//        gestureDetector = new GestureDetector(this, new SimpleOnGestureListener() {
//        	@Override
//        	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//        		if (e1.getY() - e2.getY() > LARGE_MOVE) {
//        			Toast.makeText(getApplicationContext(), "Flight detected!", Toast.LENGTH_SHORT).show();
//        			return true;
//        		}
//        		else if (e2.getY() - e1.getY() > LARGE_MOVE) {
//        			Toast.makeText(getApplicationContext(), "Flight detected!", Toast.LENGTH_LONG).show();
//        			return true;
//        		}
//        		else if (e1.getX() - e2.getX() > LARGE_MOVE) {
//        			Toast.makeText(getApplicationContext(), "Flight detected!", Toast.LENGTH_LONG).show();
//        			return true;
//        		}
//        		else if (e2.getX() - e1.getX() > LARGE_MOVE) {
//        			Toast.makeText(getApplicationContext(), "Flight detected!", Toast.LENGTH_LONG).show();
//        			return true;
//        		}
//        		return false;
//        	}        	
//        });
        // get SQL database class 
        device_data = new MyDB(this);
        device_data.open();
                        
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

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the session
        } else {
            if (mChatService == null) {
            	// Initialize the BluetoothChatService to perform bluetooth connections
                mChatService = new BluetoothChatService(this, mHandler);
            	setupDefaultButtons();
            }
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

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
        
        // close sqlite database connection
        device_data.close();
                     
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
                
    }
    
    // this is called after resume from another full-screen activity
    @Override
	protected void onRestart() {
		super.onRestart();
		
		device_data.open();
	}

    // for the fling event
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//    	return gestureDetector.onTouchEvent(event);
//    }
    
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
    
    // Called when a user pushes a button down for the first time
    private void touchButton(int rbutton) {
    	Button button = null; // if we can't find one in button_map, set to null
    	Object[] payload = null;    	
    	    	    	  	    	
    	payload = button_map.get(rbutton);

    	if (payload != null) {
    		button = (Button)payload[0];
    		
    		button.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    		
	    	// check if the navigation move_left was pushed
	    	if (button == move_left_btn) {
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
	    	// check if the navigation move_left was pushed
	    	if (button == move_right_btn) {
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
	    	
	    	// if we got here it means we are a regular button
	    	
	    	// turn on LED!
			led_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.led));
			
			if (last_button != rbutton) { // if we didn't just click the same button again
				NUM_MESSAGES = 1; // set to 1
			}
			else { NUM_MESSAGES++; } // we repeated the button click
			
    		Message msg = new Message();            
    		msg.what = MESSAGE_KEY_PRESSED;

    		button.setBackgroundDrawable(getResources().getDrawable((Integer)payload[3]));
    		buttonSend((String)payload[1]);
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
    		if (!LEARN_MODE) { 
    			if (e.getAction() == MotionEvent.ACTION_DOWN) {  
    	    		LOOP_KEY = true; // start looping until we lift finger off key
    	    		
    				// if our up/down counter is at 0, then fire a new key press
   // 				if (NUM_MESSAGES == 0) {
    					touchButton(v.getId());
   // 				}
    				return true;  // because we consumed the event
    			}   
    			else if (e.getAction() == MotionEvent.ACTION_UP) {
    				// turn off LED
    				led_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.led_off));
    				
 //   				NUM_MESSAGES--;
 //   				last_button = 0; // reset last button
    				
    				LOOP_KEY = false;	// reset loop key global
    				// is this heavy to reset all buttons? 
    				// otherwise do switch/case
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
    	    	
    	    	if (NUM_MESSAGES == 0 && LOOP_KEY) {
    	    		NUM_MESSAGES++;
            		mHandler.sendMessageDelayed(msg, DELAY_TIME);
    	    	}
				      		
        	}
        }
    };
    
    // this function iterates through buttons and sets them to default un-pushed graphic view
    private void resetButtons() {
    	Object[] payload;    	
    	Button btn;
    	
    	// need iterate through button_map and reset images to unpressed state image
    	Iterator itr = button_map.keySet().iterator();
    	while (itr.hasNext()) {
    		Integer key = (Integer)itr.next();
    		payload = button_map.get(key);
    		btn = (Button)payload[0];
    		btn.setBackgroundDrawable(getResources().getDrawable((Integer)payload[2]));    		    
    	}
    	payload = button_map.get(BUTTON_ID);
    }
    
    // called to setup the buttons on the screen
    private void setupDefaultButtons() {
    	
    	setupSpinner();
        
        // Initialize the buttons with a listener for click and touch events
        btn_volume_up = (Button) findViewById(R.id.btn_volume_up);
        btn_volume_down = (Button) findViewById(R.id.btn_volume_down);
        btn_channel_up = (Button) findViewById(R.id.btn_channel_up);
        btn_channel_down = (Button) findViewById(R.id.btn_channel_down);
        btn_input = (Button) findViewById(R.id.btn_input);
        led_btn = (Button) findViewById(R.id.led_btn);
        btn_pwr = (Button) findViewById(R.id.power_btn);
        power2_btn = (Button) findViewById(R.id.power2_btn);
        back_skip_btn = (Button) findViewById(R.id.back_skip_btn);
        back_btn = (Button) findViewById(R.id.back_btn);
        forward_btn = (Button) findViewById(R.id.forward_btn);
        skip_forward_btn = (Button) findViewById(R.id.skip_forward_btn); 
        record_btn = (Button) findViewById(R.id.record_btn);
        stop_btn = (Button) findViewById(R.id.stop_btn);
    	play_btn = (Button) findViewById(R.id.play_btn);
    	eject_btn = (Button) findViewById(R.id.eject_btn);
    	disc_btn = (Button) findViewById(R.id.disc_btn);
    	left_btn = (Button) findViewById(R.id.left_btn);
    	mute_btn = (Button) findViewById(R.id.mute_btn);
    	enter_btn = (Button) findViewById(R.id.enter_btn);
    	down_btn = (Button) findViewById(R.id.down_btn);
    	info_btn = (Button) findViewById(R.id.info_btn);
    	right_btn = (Button) findViewById(R.id.right_btn);
    	return_btn = (Button) findViewById(R.id.return_btn);
    	pgup_btn = (Button) findViewById(R.id.pgup_btn);
    	pgdn_btn = (Button) findViewById(R.id.pgdn_btn);
    	guide_btn = (Button) findViewById(R.id.guide_btn);
    	exit_btn = (Button) findViewById(R.id.exit_btn);
    	move_right_btn = (Button) findViewById(R.id.move_right_btn);
    	move_left_btn = (Button) findViewById(R.id.move_left_btn);
    	pause_btn = (Button) findViewById(R.id.pause_btn);
    	btn_up = (Button) findViewById(R.id.btn_up);
    	
        //btn_volume_up.setOnLongClickListener(l);
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
        left_btn.setOnClickListener(buttonClick);
        left_btn.setOnTouchListener(buttonTouch);
        mute_btn.setOnClickListener(buttonClick);
        mute_btn.setOnTouchListener(buttonTouch);
        btn_up.setOnClickListener(buttonClick);
        btn_up.setOnTouchListener(buttonTouch);
        enter_btn.setOnClickListener(buttonClick);
        enter_btn.setOnTouchListener(buttonTouch);
        down_btn.setOnClickListener(buttonClick);
        down_btn.setOnTouchListener(buttonTouch);
        info_btn.setOnClickListener(buttonClick);
        info_btn.setOnTouchListener(buttonTouch);
        right_btn.setOnClickListener(buttonClick);
        right_btn.setOnTouchListener(buttonTouch);
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
        
        //set bundle of associated button properties
        // order is : Button name, Constant that represents btn, graphic for unpressed, graphic for pushed 
        Object[] btn_1 = {btn_volume_up, Constants.VOLUME_UP, R.drawable.vol_up, R.drawable.vol_up};
        Object[] btn_2 = {btn_volume_down, Constants.VOLUME_DOWN, R.drawable.vol_down, R.drawable.vol_down};
        Object[] btn_3 = {btn_channel_up, Constants.CHANNEL_UP, R.drawable.ch_up, R.drawable.ch_up};
        Object[] btn_4 = {btn_channel_down, Constants.CHANNEL_DOWN, R.drawable.ch_down, R.drawable.ch_down};
        Object[] btn_5 = {btn_input, null, R.drawable.av_input, R.drawable.av_input};
        Object[] btn_6 = {btn_pwr, null, R.drawable.power_48_gray, R.drawable.power_48_gray};
        Object[] btn_7 = {power2_btn, null, R.drawable.power_green, R.drawable.power_green};
        Object[] btn_8 = {back_skip_btn, null, R.drawable.back_skip, R.drawable.back_skip};
        Object[] btn_9 = {back_btn, null, R.drawable.back, R.drawable.back};
        Object[] btn_10 = {forward_btn, null, R.drawable.forward, R.drawable.forward};
        Object[] btn_11 = {skip_forward_btn, null, R.drawable.forward_skip, R.drawable.forward_skip};
        Object[] btn_12 = {record_btn, null, R.drawable.record_square, R.drawable.record_square};
        Object[] btn_13 = {stop_btn, null, R.drawable.stop, R.drawable.stop};
        Object[] btn_14 = {play_btn, null, R.drawable.play, R.drawable.play};
        Object[] btn_15 = {pause_btn, null, R.drawable.pause, R.drawable.pause};
        Object[] btn_16 = {eject_btn, null, R.drawable.eject_square, R.drawable.eject_square};
        Object[] btn_17 = {disc_btn, null, R.drawable.disc_menu, R.drawable.disc_menu};
        Object[] btn_18 = {left_btn, null, R.drawable.left, R.drawable.left};
        Object[] btn_19 = {mute_btn, null, R.drawable.mute, R.drawable.mute};
        Object[] btn_20 = {btn_up, null, R.drawable.up, R.drawable.up};
        Object[] btn_21 = {enter_btn, null, R.drawable.enter, R.drawable.enter};
        Object[] btn_22 = {down_btn, null, R.drawable.down, R.drawable.down};
        Object[] btn_23 = {info_btn, null, R.drawable.info_display, R.drawable.info_display};
        Object[] btn_24 = {right_btn, null, R.drawable.right, R.drawable.right};
        Object[] btn_25 = {return_btn, null, R.drawable.return_back, R.drawable.return_back};
        Object[] btn_26 = {pgup_btn, null, R.drawable.pgup, R.drawable.pgup};
        Object[] btn_27 = {pgdn_btn, null, R.drawable.pgdn, R.drawable.pgdn};
        Object[] btn_28 = {guide_btn, null, R.drawable.guide, R.drawable.guide};
        Object[] btn_29 = {exit_btn, null, R.drawable.exit, R.drawable.exit};
        Object[] btn_30 = {move_right_btn, null, R.drawable.move_right, R.drawable.move_right};
        Object[] btn_31 = {move_left_btn, null, R.drawable.move_left, R.drawable.move_left};
        
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
        button_map.put(R.id.left_btn, btn_18);
        button_map.put(R.id.mute_btn,btn_19);
        button_map.put(R.id.btn_up,btn_20);
        button_map.put(R.id.enter_btn,btn_21);
        button_map.put(R.id.down_btn,btn_22);
        button_map.put(R.id.info_btn,btn_23);
        button_map.put(R.id.right_btn,btn_24);
        button_map.put(R.id.return_btn,btn_25);
        button_map.put(R.id.pgup_btn,btn_26);
        button_map.put(R.id.pgdn_btn,btn_27);
        button_map.put(R.id.guide_btn,btn_28);
        button_map.put(R.id.exit_btn,btn_29);
        button_map.put(R.id.move_right_btn,btn_30);
        button_map.put(R.id.move_left_btn,btn_31);
        
//        // Initialize the BluetoothChatService to perform bluetooth connections
//        mChatService = new BluetoothChatService(this, mHandler);
                
    }
    
    // Same as SetupButtons but called when user goes into number selection view
    private void setupNumbers() {
        led_btn = (Button) findViewById(R.id.led_btn);
    	move_right_btn = (Button) findViewById(R.id.move_right_btn);
    	move_left_btn = (Button) findViewById(R.id.move_left_btn);
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
    	btn_last = (Button) findViewById(R.id.btn_last);
    	btn_home = (Button) findViewById(R.id.btn_home);
    	
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
        btn_last.setOnTouchListener(buttonTouch);
        btn_last.setOnClickListener(buttonClick);
        btn_home.setOnTouchListener(buttonTouch);
        btn_home.setOnClickListener(buttonClick);
        move_left_btn.setOnClickListener(buttonClick);
        move_left_btn.setOnTouchListener(buttonTouch);
        move_right_btn.setOnClickListener(buttonClick);
        move_right_btn.setOnTouchListener(buttonTouch);
        
    	//set bundle of associated button properties
        // order is : Button name, Constant that represents btn, graphic for unpressed, graphic for pushed 
        Object[] btn_1 = {btn_n0, null, R.drawable.n0, R.drawable.n0};
        Object[] btn_2 = {btn_n1, null, R.drawable.n1, R.drawable.n1};
        Object[] btn_3 = {btn_n2, null, R.drawable.n2, R.drawable.n2};
        Object[] btn_4 = {btn_n3, null, R.drawable.n3, R.drawable.n3};
        Object[] btn_5 = {btn_n4, null, R.drawable.n4, R.drawable.n4};
        Object[] btn_6 = {btn_n5, null, R.drawable.n5, R.drawable.n5};
        Object[] btn_7 = {btn_n6, null, R.drawable.n6, R.drawable.n6};
        Object[] btn_8 = {btn_n7, null, R.drawable.n7, R.drawable.n7};
        Object[] btn_9 = {btn_n8, null, R.drawable.n8, R.drawable.n8};
        Object[] btn_10 = {btn_n9, null, R.drawable.n9, R.drawable.n9};
        Object[] btn_11 = {btn_dash, null, R.drawable.dash, R.drawable.dash};
        Object[] btn_12 = {btn_enter, null, R.drawable.enter_square, R.drawable.enter_square};
        Object[] btn_13 = {btn_exit, null, R.drawable.exit, R.drawable.exit};
        Object[] btn_14 = {btn_last, null, R.drawable.last, R.drawable.last};
        Object[] btn_15 = {btn_home, null, R.drawable.home, R.drawable.home};
        Object[] btn_16 = {move_right_btn, null, R.drawable.move_right, R.drawable.move_right};
        Object[] btn_17 = {move_left_btn, null, R.drawable.move_left, R.drawable.move_left};
        
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
        button_map.put(R.id.btn_last, btn_14);
        button_map.put(R.id.btn_home, btn_15);
        button_map.put(R.id.move_right_btn, btn_16);
        button_map.put(R.id.move_left_btn, btn_17);
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
    	prefs = getSharedPreferences("droidMoteSettings", MODE_PRIVATE);
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

        // Check that there's actually something to send
        if (message.length > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message;
            mChatService.write(send);
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
            	if ((int)msg.arg1 == last_button && LOOP_KEY) {
            		
 //           		if (LOOP_KEY) {
            			// prevent sending button click if new button has been pushed in mean-time

            			Button toclick;
            			Object[] payload = button_map.get(msg.arg1);
            			if (payload != null) {
            				toclick = (Button)payload[0];
            				toclick.performClick();
            			}
 //           		}            		        		

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
        // this is to manage the button configurations in database
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
        	Toast.makeText(this, "Select button to train", Toast.LENGTH_LONG).show();
        	LEARN_MODE = true;
        	return true;
        case R.id.stop_learn:        
        	Toast.makeText(this, "Stopped Learning", Toast.LENGTH_LONG).show();
        	LEARN_MODE = false;
        	// reset all images to unpressed state
        	resetButtons();
        	return true;
        case R.id.exit_menu:                	        	
        	finish();
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

		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
    // this function updates the current table with what is selected in drop-down
	// it then grabs the button keys from that table into local devices Cursor
    public void fetchButtons() {
    	// first update the cur_table from spinner
    	if (device_spinner.getCount() > 0) {
    		Object table = device_spinner.getSelectedItem();
    		if (table != null) {
    			cur_table = table.toString();
    			devices = device_data.getKeys(cur_table);
    		}
    	}
    }
    
    // this function sends the code to the pod based on the button that was selected
    public void buttonSend(String buttonCode) {
    	String column;    	
    	devices.moveToFirst();
    	for (int i=0; i< devices.getCount(); i++) {
    		column = devices.getString(1);
    		if (column.equals(buttonCode)) {
    			byte[] code = devices.getBlob(2);    			
    			byte command = (byte)(Codes.Commands.IR_TRANSMIT);
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
    		toSend[0] = (byte)toSend[0];
    		toSend[1] = 0x00; // Reserved
    		STATE = Codes.Commands.LEARN;
    		sendMessage(toSend); 
    		break;
    	case Codes.Commands.ABORT_LEARN:
    		toSend = new byte[2];
    		toSend[0] = Codes.Commands.ABORT_LEARN;
    		toSend[0] = (byte)toSend[0];
    		toSend[1] = 0x00; // Reserved
    		STATE = Codes.Commands.ABORT_LEARN;
    		sendMessage(toSend);
    		break;
    	case Codes.Commands.GET_VERSION:
    		STATE = Codes.Commands.GET_VERSION;
    		toSend = new byte[2];
    		toSend[0] = Codes.Commands.GET_VERSION;
    		toSend[0] = (byte)toSend[0];
    		toSend[1] = 0x00; // Reserved
    		sendMessage(toSend);
    		break;
    	case Codes.Commands.RENAME_DEVICE:
    		STATE = Codes.Commands.RENAME_DEVICE;
    		toSend = new byte[Codes.new_name.length()+2];
    		toSend[0] = Codes.Commands.RENAME_DEVICE;
    		toSend[0] = (byte)toSend[0];
    		toSend[1] = 0x00; // Reserved
    		// load new names to the toSend byte[]
    		byte[] new_name = Codes.new_name.getBytes();
    		System.arraycopy(new_name, 0, toSend, 2, new_name.length);
    		
    		sendMessage(toSend);
    		break;
    	}
    }
    
    // 	this method should be called whenever we receive a byte[] from the pod
    public void interpretResponse(byte[] response, int bytes) {
    	
    	switch (STATE) {
    	case Codes.Commands.LEARN:
    		if (response[0] == Codes.Return.ACK) {
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
    		if (response[0] == Codes.Return.ACK) {
    			STATE = 0x00; // reset state
    			// convert data into a String
    			Codes.pod_data = response;
    			// need to launch window to dump the data to
    			showDialog(DIALOG_SHOW_INFO);
    		}
    		break;
    	case Codes.Commands.ABORT_LEARN:
    		if (response[0] == Codes.Return.ACK) {
    			STATE = 0x00; // reset state
    		}
    		break;
    	case Codes.Commands.RENAME_DEVICE:
    		if (response[0] == Codes.Return.ACK) {
    			STATE = 0x00; // reset state
    		}
    		break;
    	case Codes.Commands.IR_TRANSMIT:
    		if (response[0] == Codes.Return.ACK) {
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
