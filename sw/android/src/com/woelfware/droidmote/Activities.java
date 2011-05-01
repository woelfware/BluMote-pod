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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.woelfware.database.MyDB;


public class Activities extends Activity {
	private MyDB device_data;  //TODO see if we can share this from Droidmote.java
	private ArrayAdapter<String> mActivitiesArrayAdapter;
    private static final int ACTIVITY_ADD=0;
    private static final int ACTIVITY_RENAME=1;
    private static final int ID_DELETE = 0;
    private static final int ID_RENAME = 1;
    private Button add_activity_btn;
    
    ListView activitiesListView;
	String table_name; // holds table that we were working on (like when rename is called)

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.manage_devices);
        
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
        
        device_data = new MyDB(this);
        device_data.open();
        populateDisplay();
        
        registerForContextMenu(findViewById(R.id.activities_list));
        Intent i = getIntent();
        setResult(RESULT_OK,i);       
	}

	private void populateDisplay() {
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
			table_name = mActivitiesArrayAdapter.getItem((int)(info.id));
			// replace spaces with underscores
			table_name = table_name.replace(" ", "_");
			device_data.removeTable(table_name);
			populateDisplay();
			return true;
		case ID_RENAME:
			// need to remove this table and repopulate list
			table_name = mActivitiesArrayAdapter.getItem((int)(info.id));
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
    
	  // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

        }
    };

}