package com.woelfware.blumote;

import android.app.Activity;
import android.content.Intent;
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


public class ActivityInitEdit extends Activity {	
	private ArrayAdapter<String> activityArrayAdapter;
    // define the callback codes
	private static final int CHANGE_DELAY = 0;
	
	public static final String REDO = "redo";
    private static final int ID_DELETE = 0;
    private static final int ID_CHANGE = 1;
	private Button redo_init_btn;
    
    ListView activitiesListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_edit);
        
        // Initialize array adapters. 
        activityArrayAdapter = new ArrayAdapter<String>(this, R.layout.manage_devices_item);
        
        // Find and set up the ListView for paired devices
        activitiesListView = (ListView) findViewById(R.id.activity_edit_list);
        activitiesListView.setAdapter(activityArrayAdapter);
//        activitiesListView.setOnItemClickListener(mDeviceClickListener);
        
        redo_init_btn = (Button) findViewById(R.id.redo_init_btn);
        redo_init_btn.setOnClickListener( new OnClickListener() {
            public void onClick(View v) {
            	// need to tell calling activity that we want to redo the init
            	// calling activity then needs to re-enter mode to re initialize
            	// TODO should pop up an 'are you sure?' window
            	Intent i = getIntent();
				i.putExtra("returnStr", REDO);
				finish();	
            }
        });
        
        populateDisplay();
        
        registerForContextMenu(findViewById(R.id.activity_edit_list));
        Intent i = getIntent();
        //i.putExtra("returnStr", device_string.toString());
        setResult(RESULT_OK,i);       
	}

	private void populateDisplay() {
        
        activityArrayAdapter.clear(); // always clear before adding items
        // TODO implement pulling new sequence from prefs file and binding to the arraylist
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	String return_string;
    	Bundle return_bundle;
    	
    	if (requestCode == CHANGE_DELAY && resultCode == RESULT_OK) {
    		
    		// add the new item to the database
    		return_bundle = intent.getExtras();
    		if ( return_bundle != null ) {
    			return_string = return_bundle.getString("returnStr");
    			// spaces don't work for table names, so replace with underscore
    			return_string = return_string.replace(" ", "_");
    			// TODO implement putting this new delay into the prefs file...
    		}
        	// refresh the display of items
    		populateDisplay();    		
    	}
    	
    	super.onActivityResult(requestCode, resultCode, intent);        
    }

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		String theItem;
		switch(item.getItemId()) {
		case ID_DELETE:
			theItem = activityArrayAdapter.getItem((int)(info.id));
			// TODO implement deleting this part from the prefs file
			populateDisplay();
			return true;
		case ID_CHANGE:
			theItem = activityArrayAdapter.getItem((int)(info.id));
            //TODO implement editing the delay if the item is a delay
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.devices_list) {
			// TODO change menu presented if the item is a delay or not.
			// if delay then need to offer up the 'change delay'
			
			menu.setHeaderTitle("Menu");
			menu.add(0, ID_DELETE, 0, "Delete Item");
			menu.add(0, ID_CHANGE, 0, "Change Delay");
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}
    
    

}
