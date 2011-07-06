
package com.woelfware.blumote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

public class ActivityInitRun extends Activity {	
	private static final String TAG = "ActivityInitRun";
	
	// these static variables will deal with executing all the 
	// initialization steps, need to be static so successive
	// calls to processInitItems() works
	private int initItemIndex = 0;	
	private String[] initItems = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init_run);
		
		Intent i = getIntent();
		initItems = i.getStringArrayExtra("DATA");
		setResult(RESULT_OK,i);
		
		processInitItems();

	} // end of oncreate

	private void processInitItems() {
		// TODO - use initItemIndex to deal with getting through all the items
		// if run into a delay item then need to spawn AsyncTask and then AsyncTask
		// will call this method after it finishes...
		String item;
		while (initItemIndex < initItems.length) {
			item = initItems[initItemIndex];
			initItemIndex++;
			if (item == "") {
				// log error and continue to next item
				Log.e(TAG, "initialization item was null - malformed item");
			}
			// check if item is null and is a DELAY xx item
			else if (item.startsWith("DELAY")) {
				// extract value after the space
				String delay = (item.split(" ")[1]);
				try {
					int delayTime = Integer.parseInt(delay);
					//need to start a wait timer for this period of time
					new CountDownTimer(delayTime, delayTime) {
						public void onTick(long millisUntilFinished) {
							// no need to use this function
						}

						public void onFinish() {
							// called when timer expired
							processInitItems(); // continue on the quest to finish the initItems
						}
					}.start();					
					break; // exit while loop while we are waiting for Delay to finish
				} catch (Exception e) {
					// failed, skip and go to next item in this case.
					Log.e(TAG, "Failed to execute an initialization delay!");
				}
			}			
			// else if the item is a button in format "Device Button"
			else {
				//TODO
				// open DB table for 'device'
				//byte[] toSend = getButton(String curTable, String buttonID)
				// extract button code from DB
				// execute button code
			}			
		} // end of while loop		
	} // end of processInitItems()
}
