package com.woelfware.blumote;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.woelfware.database.DeviceDB;


public class ManageDevices extends Activity {
	private DeviceDB device_data;
	private ArrayAdapter<String> mDevicesArrayAdapter;
    private static final int ACTIVITY_ADD=0;
    private static final int ACTIVITY_RENAME=1;
    private static final int ID_DELETE = 0;
    private static final int ID_RENAME = 1;
    private Button add_config_btn;
    
    ListView devicesListView;
	String deviceName; // holds table that we were working on (like when rename is called)

	// used to convert device/activity names into IDs that do not change
	InterfaceLookup lookup;
	
	// Shared preferences class - for storing config settings between runs
	SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.manage_devices);
        
        // get preferences file
		prefs = getSharedPreferences(BluMote.PREFS_FILE, MODE_PRIVATE);
		
        // initialize the InterfaceLookup
		lookup = new InterfaceLookup(prefs);
		
        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.manage_devices_item);
        
        // Find and set up the ListView for paired devices
        devicesListView = (ListView) findViewById(R.id.devices_list);
        devicesListView.setAdapter(mDevicesArrayAdapter);
//        devicesListView.setOnItemClickListener(mDeviceClickListener);
        
        add_config_btn = (Button) findViewById(R.id.add_config_btn);
        add_config_btn.setOnClickListener( new OnClickListener() {
            public void onClick(View v) {
            	// Launch the function to ask for a name for device
            	Intent i = new Intent(getApplicationContext(), EnterDevice.class);
                startActivityForResult(i, ACTIVITY_ADD);
            }
        });
        
        device_data = new DeviceDB(this);
        device_data.open();
        populateDisplay();
        
        registerForContextMenu(findViewById(R.id.devices_list));
        Intent i = getIntent();
        setResult(RESULT_OK,i);       
	}

	private void populateDisplay() {
		String[] devices = device_data.getDevices();
		if (devices != null) {
			mDevicesArrayAdapter.clear(); // clear before adding
			for (int i= 0 ; i< devices.length; i++) {
				mDevicesArrayAdapter.add(devices[i]);
			}
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
        device_data.close();
	}

	@Override
	protected void onResume() {
		super.onResume();
        device_data.open();
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	String return_string;
    	Bundle return_bundle;
    	
    	if (requestCode == ACTIVITY_ADD && resultCode == RESULT_OK) {
    		device_data.open();	
    		// add the new item to the database
    		return_bundle = intent.getExtras();
    		if ( return_bundle != null ) {
    			return_string = return_bundle.getString("returnStr");
    			// spaces don't work for table names, so replace with underscore
    			return_string = return_string.replace(" ", "_");
        		device_data.addDevice(return_string);
        		// create a new lookup ID for this item
        		lookup.addLookupId(return_string);
    		}
        	// refresh the display of items
    		populateDisplay();    		
    	}
    	if (requestCode == ACTIVITY_RENAME && resultCode == RESULT_OK) {
    		device_data.open();	// onActivityResult() is called BEFORE onResume() so need this!
    		return_bundle = intent.getExtras();
    		if ( return_bundle != null ) {
    			return_string = return_bundle.getString("returnStr");
        		device_data.renameDevice(deviceName, return_string);
        		
        		SharedPreferences prefs = getSharedPreferences(BluMote.PREFS_FILE, MODE_PRIVATE);
        		// rename all MISC buttons stored
        		MainInterface.renameMiscButtons(deviceName, return_string, prefs);
        		// update lookup with new name
        		lookup.updateLookupName(return_string, deviceName);
    		}
        	// refresh the display of items
    		populateDisplay();    		
    	}
    	super.onActivityResult(requestCode, resultCode, intent);        
    }

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch(item.getItemId()) {
		case ID_DELETE:
			// need to remove this table and repopulate list
			deviceName = mDevicesArrayAdapter.getItem((int)(info.id));
			// replace spaces with underscores
			deviceName = deviceName.replace(" ", "_");
			device_data.removeDevice(deviceName);
			lookup.deleteLookupId(deviceName);
			populateDisplay();
			return true;
		case ID_RENAME:
			// need to remove this table and repopulate list
			deviceName = mDevicesArrayAdapter.getItem((int)(info.id));
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
		if (v.getId() == R.id.devices_list) {
//			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle("Menu");
			menu.add(0, ID_DELETE, 0, "Delete");
			menu.add(0, ID_RENAME, 0, "Rename");
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}
    
    

}
