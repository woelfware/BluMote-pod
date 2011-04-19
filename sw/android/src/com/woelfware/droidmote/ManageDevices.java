package com.woelfware.droidmote;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
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

import com.woelfware.database.MyDB;


public class ManageDevices extends Activity {
	private MyDB device_data;
	private ArrayAdapter<String> mDevicesArrayAdapter;
    private static final int ACTIVITY_ADD=0;
    private static final int ACTIVITY_RENAME=1;
    private static final int ID_DELETE = 0;
    private static final int ID_RENAME = 1;
    private Button add_config_btn;
    
    ListView pairedListView;
	String table_name; // holds table that we were working on (like when rename is called)

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.manage_devices);
        
        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        
        // Find and set up the ListView for paired devices
        pairedListView = (ListView) findViewById(R.id.devices_list);
        pairedListView.setAdapter(mDevicesArrayAdapter);
//        pairedListView.setOnItemClickListener(mDeviceClickListener);
        
        add_config_btn = (Button) findViewById(R.id.add_config_btn);
        add_config_btn.setOnClickListener( new OnClickListener() {
            public void onClick(View v) {
            	// Launch the function to ask for a name for device
            	Intent i = new Intent(getApplicationContext(), EnterDevice.class);
                startActivityForResult(i, ACTIVITY_ADD);
            }
        });
        
        device_data = new MyDB(this);
        device_data.open();
        populateDisplay();
        
        registerForContextMenu(findViewById(R.id.devices_list));
        Intent i = getIntent();
        setResult(RESULT_OK,i);       
	}

	private void populateDisplay() {
        Cursor cursor1;
        String str1;
        cursor1 = device_data.getTables();
        cursor1.moveToFirst();
        mDevicesArrayAdapter.clear(); // always clear before adding items
        if (cursor1.getCount() > 0) {
        	do {
        		// need to exclude android_metadata and sqlite_sequence tables from results
        		str1 = cursor1.getString(0);
        		if (!(str1.equals("android_metadata")) 
        				&& !(str1.equals("sqlite_sequence"))) {
        			// convert underscores to spaces
        			str1 = str1.replace("_", " ");
        			mDevicesArrayAdapter.add(str1);
        		}
        	} while (cursor1.moveToNext());
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
	
//	@Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.md_options_menu, menu);
//        return true;
//    }
	
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//        case R.id.menu_add_item:
//            // Launch the function to ask for a name for device
//        	Intent i = new Intent(this, EnterDevice.class);
//            startActivityForResult(i, ACTIVITY_ADD);
//            return true;
//        }
//        return false;
//    }
    
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
        		device_data.createTable(return_string);
    		}
        	// refresh the display of items
    		populateDisplay();    		
    	}
    	if (requestCode == ACTIVITY_RENAME && resultCode == RESULT_OK) {
    		device_data.open();	// onActivityResult() is called BEFORE onResume() so need this!
    		return_bundle = intent.getExtras();
    		if ( return_bundle != null ) {
    			return_string = return_bundle.getString("returnStr");
        		device_data.renameTable(table_name, return_string);
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
			table_name = mDevicesArrayAdapter.getItem((int)(info.id));
			// replace spaces with underscores
			table_name = table_name.replace(" ", "_");
			device_data.removeTable(table_name);
			populateDisplay();
			return true;
		case ID_RENAME:
			// need to remove this table and repopulate list
			table_name = mDevicesArrayAdapter.getItem((int)(info.id));
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
