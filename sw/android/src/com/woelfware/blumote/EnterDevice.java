
package com.woelfware.blumote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

public class EnterDevice extends Activity {
	private EditText entered_device;
	CharSequence device_string;
	private Button closeButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enter_device);
		entered_device = (EditText) findViewById(R.id.device_name);

		entered_device.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// register the text when "enter" is pressed
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {

					return true;
				}
				return false;
			}
		});

		closeButton = (Button)findViewById(R.id.enter_button);
		closeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// grab the text for use in the activity
				device_string = entered_device.getText();
				Intent i = getIntent();
				i.putExtra("returnStr", device_string.toString());
				setResult(RESULT_OK,i);
				finish();	
			}
		});

		Intent i = getIntent();
		setResult(RESULT_OK,i);
	} // end of oncreate
}
