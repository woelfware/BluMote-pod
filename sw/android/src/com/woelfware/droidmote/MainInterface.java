package com.woelfware.droidmote;

import java.util.HashMap;

import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

public class MainInterface {
	private  ImageButton fav_btn;
    private  ImageButton btn_volume_up;
    private  ImageButton btn_volume_down;
    private  ImageButton btn_channel_up;
    private  ImageButton btn_channel_down;
//    private  Spinner device_spinner;
//    private  ImageButton led_btn;
    private  ImageButton btn_pwr;
    private  ImageButton power2_btn;
    private  ImageButton back_skip_btn;
    private  ImageButton back_btn;
    private  ImageButton forward_btn;
    private  ImageButton skip_forward_btn;
    private  ImageButton record_btn;
    private  ImageButton stop_btn;
    private  ImageButton play_btn;
    private  ImageButton pause_btn;
    private  ImageButton eject_btn;
    private  Button disc_btn;
    private  ImageButton mute_btn;
    private  Button info_btn;
    private  ImageButton return_btn;
    private  ImageButton pgup_btn;
    private  ImageButton pgdn_btn;
    private  Button guide_btn;
    private  Button exit_btn;
    private  ImageButton move_left_btn;
    private  ImageButton move_right_btn;
    private  Button btn_input;
    private  Button btn_last;

    private  ImageButton move_left_n_btn;
    private  ImageButton move_right_n_btn;
	private Spinner device_spinner;
	private ImageButton left_btn;
	private ImageButton down_btn;
	private ImageButton right_btn;
	private ImageButton btn_up;
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
	private ImageButton btn_home;
	private Button btn_misc1;
	private Button btn_misc2;
	private Button btn_misc3;
	private Button btn_misc4;
	private Button btn_misc5;
	private Button btn_misc6;
	private Button btn_misc7;
	private Button btn_misc8;
	
    private  ImageButton move_left_a_btn;
    private  ImageButton move_right_a_btn;
    
    private Button add_activity_btn;
    
    // array adapter for the drop-down spinner
	private ArrayAdapter<String> mAdapter;
    
    private ArrayAdapter<String> mActivitiesArrayAdapter;
    ListView activitiesListView;
    
    private  Droidmote droidmote;
    
    private  HashMap<Integer,Object[]> button_map = null;
    
	public MainInterface(Droidmote d) {
		droidmote = d;
	}
	
	// this method will initialize the button elements
	// it will set the button_map hashmap in Droidmote
	// to it's members	
	public void initialize() {
		// if button_map is null then need to set it up, only do this once
		if (button_map == null) {
			/////////////////////////////////////
			////// MAIN SCREEN
			// Initialize the buttons with a listener for click and touch events
			btn_volume_up = (ImageButton) droidmote.findViewById(R.id.btn_volume_up);
			btn_volume_down = (ImageButton) droidmote.findViewById(R.id.btn_volume_down);
			btn_channel_up = (ImageButton) droidmote.findViewById(R.id.btn_channel_up);
			btn_channel_down = (ImageButton) droidmote.findViewById(R.id.btn_channel_down);
			btn_input = (Button) droidmote.findViewById(R.id.btn_input);
			//        led_btn = (ImageButton) droidmote.findViewById(R.id.led_btn);
			btn_pwr = (ImageButton) droidmote.findViewById(R.id.power_btn);
			power2_btn = (ImageButton) droidmote.findViewById(R.id.power2_btn);
			back_skip_btn = (ImageButton) droidmote.findViewById(R.id.back_skip_btn);
			back_btn = (ImageButton) droidmote.findViewById(R.id.back_btn);
			forward_btn = (ImageButton) droidmote.findViewById(R.id.forward_btn);
			skip_forward_btn = (ImageButton) droidmote.findViewById(R.id.skip_forward_btn); 
			record_btn = (ImageButton) droidmote.findViewById(R.id.record_btn);
			stop_btn = (ImageButton) droidmote.findViewById(R.id.stop_btn);
			play_btn = (ImageButton) droidmote.findViewById(R.id.play_btn);
			eject_btn = (ImageButton) droidmote.findViewById(R.id.eject_btn);
			disc_btn = (Button) droidmote.findViewById(R.id.disc_btn);
			mute_btn = (ImageButton) droidmote.findViewById(R.id.mute_btn);
			info_btn = (Button) droidmote.findViewById(R.id.info_btn);
			return_btn = (ImageButton) droidmote.findViewById(R.id.return_btn);
			pgup_btn = (ImageButton) droidmote.findViewById(R.id.pgup_btn);
			pgdn_btn = (ImageButton) droidmote.findViewById(R.id.pgdn_btn);
			guide_btn = (Button) droidmote.findViewById(R.id.guide_btn);
			exit_btn = (Button) droidmote.findViewById(R.id.exit_btn);
			move_right_btn = (ImageButton) droidmote.findViewById(R.id.move_right_btn);
			move_left_btn = (ImageButton) droidmote.findViewById(R.id.move_left_btn);
			pause_btn = (ImageButton) droidmote.findViewById(R.id.pause_btn);
			fav_btn = (ImageButton) droidmote.findViewById(R.id.fav_btn);
			btn_last = (Button) droidmote.findViewById(R.id.btn_last);
			btn_volume_up.setOnTouchListener(droidmote);
			btn_volume_up.setOnClickListener(droidmote);
			btn_volume_down.setOnTouchListener(droidmote);
			btn_volume_down.setOnClickListener(droidmote);
			btn_channel_up.setOnTouchListener(droidmote);
			btn_channel_up.setOnClickListener(droidmote);
			btn_channel_down.setOnTouchListener(droidmote);
			btn_channel_down.setOnClickListener(droidmote);
			btn_input.setOnClickListener(droidmote);
			btn_input.setOnTouchListener(droidmote);
			btn_pwr.setOnClickListener(droidmote);
			btn_pwr.setOnTouchListener(droidmote);
			power2_btn.setOnClickListener(droidmote);
			power2_btn.setOnTouchListener(droidmote);
			back_skip_btn.setOnClickListener(droidmote);
			back_skip_btn.setOnTouchListener(droidmote);
			back_btn.setOnClickListener(droidmote);
			back_btn.setOnTouchListener(droidmote);
			forward_btn.setOnClickListener(droidmote);
			forward_btn.setOnTouchListener(droidmote);
			skip_forward_btn.setOnClickListener(droidmote);
			skip_forward_btn.setOnTouchListener(droidmote);
			record_btn.setOnClickListener(droidmote);
			record_btn.setOnTouchListener(droidmote);
			stop_btn.setOnClickListener(droidmote);
			stop_btn.setOnTouchListener(droidmote);
			play_btn.setOnClickListener(droidmote);
			play_btn.setOnTouchListener(droidmote);
			eject_btn.setOnClickListener(droidmote);
			eject_btn.setOnTouchListener(droidmote);
			disc_btn.setOnClickListener(droidmote);
			disc_btn.setOnTouchListener(droidmote);        
			mute_btn.setOnClickListener(droidmote);
			mute_btn.setOnTouchListener(droidmote);;
			info_btn.setOnClickListener(droidmote);
			info_btn.setOnTouchListener(droidmote);
			return_btn.setOnClickListener(droidmote);
			return_btn.setOnTouchListener(droidmote);
			pgup_btn.setOnClickListener(droidmote);
			pgup_btn.setOnTouchListener(droidmote);
			pgdn_btn.setOnClickListener(droidmote);
			pgdn_btn.setOnTouchListener(droidmote);
			guide_btn.setOnClickListener(droidmote);
			guide_btn.setOnTouchListener(droidmote);
			exit_btn.setOnClickListener(droidmote);
			exit_btn.setOnTouchListener(droidmote);
			move_left_btn.setOnClickListener(droidmote);
			move_left_btn.setOnTouchListener(droidmote);
			move_right_btn.setOnClickListener(droidmote);
			move_right_btn.setOnTouchListener(droidmote);
			pause_btn.setOnClickListener(droidmote);
			pause_btn.setOnTouchListener(droidmote);
			fav_btn.setOnTouchListener(droidmote);
			fav_btn.setOnClickListener(droidmote);
			btn_last.setOnTouchListener(droidmote);
			btn_last.setOnClickListener(droidmote);

			//set bundle of associated button properties
			// order is : Button name, database string identifier for btn      
			button_map = new HashMap<Integer,Object[]>();
			button_map.put(R.id.btn_volume_up, new Object[] {btn_volume_up, "btn_volume_up"});
			button_map.put(R.id.btn_volume_down, new Object[] {btn_volume_down, "btn_volume_down"});
			button_map.put(R.id.btn_channel_up, new Object[] {btn_channel_up, "btn_channel_up"});
			button_map.put(R.id.btn_channel_down, new Object[] {btn_channel_down, "btn_channel_down"});
			button_map.put(R.id.btn_input, new Object[] {btn_input, "btn_input"});
			button_map.put(R.id.power_btn, new Object[] {btn_pwr, "btn_pwr"});
			button_map.put(R.id.power2_btn, new Object[] {power2_btn, "power2_btn"});
			button_map.put(R.id.back_skip_btn, new Object[] {back_skip_btn, "back_skip_btn"});
			button_map.put(R.id.back_btn, new Object[] {back_btn, "back_btn"});
			button_map.put(R.id.forward_btn, new Object[] {forward_btn, "forward_btn"});
			button_map.put(R.id.skip_forward_btn, new Object[] {skip_forward_btn, "skip_forward_btn"});
			button_map.put(R.id.record_btn, new Object[] {record_btn, "record_btn"});
			button_map.put(R.id.stop_btn, new Object[] {stop_btn, "stop_btn"});
			button_map.put(R.id.play_btn, new Object[] {play_btn, "play_btn"});
			button_map.put(R.id.pause_btn, new Object[] {pause_btn, "pause_btn"});
			button_map.put(R.id.eject_btn, new Object[] {eject_btn, "eject_btn"});
			button_map.put(R.id.disc_btn, new Object[] {disc_btn, "disc_btn"});
			button_map.put(R.id.mute_btn, new Object[] {mute_btn, "mute_btn"});
			button_map.put(R.id.info_btn, new Object[] {info_btn, "info_btn"});
			button_map.put(R.id.return_btn, new Object[] {return_btn, "return_btn"});
			button_map.put(R.id.pgup_btn, new Object[] {pgup_btn, "pgup_btn"});
			button_map.put(R.id.pgdn_btn, new Object[] {pgdn_btn, "pgdn_btn"});
			button_map.put(R.id.guide_btn, new Object[] {guide_btn, "guide_btn"});
			button_map.put(R.id.exit_btn, new Object[] {exit_btn, "exit_btn"});
			button_map.put(R.id.move_right_btn, new Object[] {move_right_btn, "move_right_btn"});
			button_map.put(R.id.move_left_btn, new Object[] {move_left_btn, "move_left_btn"});
			button_map.put(R.id.fav_btn, new Object[] {fav_btn, "fav_btn"});
			button_map.put(R.id.btn_last, new Object[] {btn_last, "btn_last"});
			///////////////////////////////////////////////////////////
			// NUMBERS SCREEN
			move_right_n_btn = (ImageButton) droidmote.findViewById(R.id.move_right_n_btn);
			move_left_n_btn = (ImageButton) droidmote.findViewById(R.id.move_left_n_btn);
			btn_n0 = (Button) droidmote.findViewById(R.id.btn_n0);
			btn_n1 = (Button) droidmote.findViewById(R.id.btn_n1);
			btn_n2 = (Button) droidmote.findViewById(R.id.btn_n2);
			btn_n3 = (Button) droidmote.findViewById(R.id.btn_n3);
			btn_n4 = (Button) droidmote.findViewById(R.id.btn_n4);
			btn_n5 = (Button) droidmote.findViewById(R.id.btn_n5);
			btn_n6 = (Button) droidmote.findViewById(R.id.btn_n6);
			btn_n7 = (Button) droidmote.findViewById(R.id.btn_n7);
			btn_n8 = (Button) droidmote.findViewById(R.id.btn_n8);
			btn_n9 = (Button) droidmote.findViewById(R.id.btn_n9);
			btn_dash = (Button) droidmote.findViewById(R.id.btn_dash);
			btn_enter = (Button) droidmote.findViewById(R.id.btn_enter);
			btn_exit = (Button) droidmote.findViewById(R.id.btn_exit);
			btn_home = (ImageButton) droidmote.findViewById(R.id.btn_home);
			left_btn = (ImageButton) droidmote.findViewById(R.id.left_btn);
			right_btn = (ImageButton) droidmote.findViewById(R.id.right_btn);
			btn_up = (ImageButton) droidmote.findViewById(R.id.btn_up);
			down_btn = (ImageButton) droidmote.findViewById(R.id.down_btn);
			btn_misc1 = (Button) droidmote.findViewById(R.id.btn_misc1);
			btn_misc2 = (Button) droidmote.findViewById(R.id.btn_misc2);
			btn_misc3 = (Button) droidmote.findViewById(R.id.btn_misc3);
			btn_misc4 = (Button) droidmote.findViewById(R.id.btn_misc4);
			btn_misc5 = (Button) droidmote.findViewById(R.id.btn_misc5);
			btn_misc6 = (Button) droidmote.findViewById(R.id.btn_misc6);
			btn_misc7 = (Button) droidmote.findViewById(R.id.btn_misc7);
			btn_misc8 = (Button) droidmote.findViewById(R.id.btn_misc8);

			// action listeners
			btn_n0.setOnTouchListener(droidmote);
			btn_n0.setOnClickListener(droidmote);
			btn_n1.setOnTouchListener(droidmote);
			btn_n1.setOnClickListener(droidmote);
			btn_n2.setOnTouchListener(droidmote);
			btn_n2.setOnClickListener(droidmote);
			btn_n3.setOnTouchListener(droidmote);
			btn_n3.setOnClickListener(droidmote);
			btn_n4.setOnTouchListener(droidmote);
			btn_n4.setOnClickListener(droidmote);
			btn_n5.setOnTouchListener(droidmote);
			btn_n5.setOnClickListener(droidmote);
			btn_n6.setOnTouchListener(droidmote);
			btn_n6.setOnClickListener(droidmote);
			btn_n7.setOnTouchListener(droidmote);
			btn_n7.setOnClickListener(droidmote);
			btn_n8.setOnTouchListener(droidmote);
			btn_n8.setOnClickListener(droidmote);
			btn_n9.setOnTouchListener(droidmote);
			btn_n9.setOnClickListener(droidmote);
			btn_dash.setOnTouchListener(droidmote);
			btn_dash.setOnClickListener(droidmote);
			btn_enter.setOnTouchListener(droidmote);
			btn_enter.setOnClickListener(droidmote);
			btn_exit.setOnTouchListener(droidmote);
			btn_exit.setOnClickListener(droidmote);
			btn_home.setOnTouchListener(droidmote);
			btn_home.setOnClickListener(droidmote);
			move_left_n_btn.setOnClickListener(droidmote);
			move_left_n_btn.setOnTouchListener(droidmote);
			move_right_btn.setOnClickListener(droidmote);
			move_right_btn.setOnTouchListener(droidmote);
			left_btn.setOnClickListener(droidmote);
			left_btn.setOnTouchListener(droidmote);
			right_btn.setOnClickListener(droidmote);
			right_btn.setOnTouchListener(droidmote);
			btn_up.setOnClickListener(droidmote);
			btn_up.setOnTouchListener(droidmote);
			down_btn.setOnClickListener(droidmote);
			down_btn.setOnTouchListener(droidmote);
			btn_misc1.setOnClickListener(droidmote);
			btn_misc1.setOnTouchListener(droidmote);
			btn_misc2.setOnClickListener(droidmote);
			btn_misc2.setOnTouchListener(droidmote);
			btn_misc3.setOnClickListener(droidmote);
			btn_misc3.setOnTouchListener(droidmote);
			btn_misc4.setOnClickListener(droidmote);
			btn_misc4.setOnTouchListener(droidmote);
			btn_misc5.setOnClickListener(droidmote);
			btn_misc5.setOnTouchListener(droidmote);
			btn_misc6.setOnClickListener(droidmote);
			btn_misc6.setOnTouchListener(droidmote);
			btn_misc7.setOnClickListener(droidmote);
			btn_misc7.setOnTouchListener(droidmote);
			btn_misc8.setOnClickListener(droidmote);
			btn_misc8.setOnTouchListener(droidmote);

			// set bundle of associated button properties
			// order is : Button name, String database id for btn, graphic for
			// unpressed, graphic for pushed

			// bundle all the button data into a big hashtable
			button_map.put(R.id.btn_n0, new Object[] { btn_n0, "btn_n0" });
			button_map.put(R.id.btn_n1, new Object[] { btn_n1, "btn_n1" });
			button_map.put(R.id.btn_n2, new Object[] { btn_n2, "btn_n2" });
			button_map.put(R.id.btn_n3, new Object[] { btn_n3, "btn_n3" });
			button_map.put(R.id.btn_n4, new Object[] { btn_n4, "btn_n4" });
			button_map.put(R.id.btn_n5, new Object[] { btn_n5, "btn_n5" });
			button_map.put(R.id.btn_n6, new Object[] { btn_n6, "btn_n6" });
			button_map.put(R.id.btn_n7, new Object[] { btn_n7, "btn_n7" });
			button_map.put(R.id.btn_n8, new Object[] { btn_n8, "btn_n8" });
			button_map.put(R.id.btn_n9, new Object[] { btn_n9, "btn_n9" });
			button_map.put(R.id.btn_dash, new Object[] { btn_dash, "btn_dash" });
			button_map.put(R.id.btn_enter, new Object[] { btn_enter, "btn_enter" });
			button_map.put(R.id.btn_exit, new Object[] { btn_exit, "btn_exit" });
			button_map.put(R.id.btn_home, new Object[] { btn_home, "btn_home" });
			button_map.put(R.id.move_right_n_btn, new Object[] { move_right_n_btn, "move_right_btn" });
			button_map.put(R.id.move_left_n_btn, new Object[] { move_left_n_btn, "move_left_btn" });
			button_map.put(R.id.left_btn, new Object[] { left_btn, "left_btn" });
			button_map.put(R.id.right_btn, new Object[] { right_btn, "right_btn" });
			button_map.put(R.id.btn_up, new Object[] { btn_up, "btn_up" });
			button_map.put(R.id.down_btn, new Object[] { down_btn, "down_btn" });
			button_map.put(R.id.btn_misc1, new Object[] { btn_misc1, "btn_misc1" });
			button_map.put(R.id.btn_misc2, new Object[] { btn_misc2, "btn_misc2" });
			button_map.put(R.id.btn_misc3, new Object[] { btn_misc3, "btn_misc3" });
			button_map.put(R.id.btn_misc4, new Object[] { btn_misc4, "btn_misc4" });
			button_map.put(R.id.btn_misc5, new Object[] { btn_misc5, "btn_misc5" });
			button_map.put(R.id.btn_misc6, new Object[] { btn_misc6, "btn_misc6" });
			button_map.put(R.id.btn_misc7, new Object[] { btn_misc7, "btn_misc7" });
			button_map.put(R.id.btn_misc8, new Object[] { btn_misc8, "btn_misc8" });
			/////////////////////////////////////////////////////////
			// ACTIVITY setup
			// initialize buttons
			move_right_a_btn = (ImageButton) droidmote.findViewById(R.id.move_right_a_btn);
			move_left_a_btn = (ImageButton) droidmote.findViewById(R.id.move_left_a_btn);
			move_left_a_btn.setOnClickListener(droidmote);
			move_left_a_btn.setOnTouchListener(droidmote);
			move_right_a_btn.setOnClickListener(droidmote);
			move_right_a_btn.setOnTouchListener(droidmote);
			
			button_map.put(R.id.move_right_a_btn, new Object[] { move_right_a_btn, "move_right_btn" });
			button_map.put(R.id.move_left_a_btn, new Object[] { move_left_btn, "move_left_btn" });

			// Initialize array adapter
			mActivitiesArrayAdapter = new ArrayAdapter<String>(droidmote,
					R.layout.manage_devices_item);

			// Find and set up the ListView
			activitiesListView = (ListView) droidmote.findViewById(R.id.activities_list);
			activitiesListView.setAdapter(mActivitiesArrayAdapter);
			activitiesListView.setOnItemClickListener(droidmote);

			// setup activity add button
			add_activity_btn = (Button) droidmote.findViewById(R.id.add_activity_btn);
			add_activity_btn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Launch the function to ask for a name for device
					Intent i = new Intent(droidmote, EnterDevice.class);
					droidmote.startActivityForResult(i, Droidmote.ACTIVITY_ADD);
				}
			});
			
			// TODO this is just a placeholder below, implement working with
			// preferences file to load up the items to add to the activities arraylist
			// Cursor cursor1;
			// String str1;
			// cursor1 = device_data.getTables();
			// cursor1.moveToFirst();
			// mActivitiesArrayAdapter.clear(); // always clear before adding items
			// if (cursor1.getCount() > 0) {
			// do {
			// // need to exclude android_metadata and sqlite_sequence tables from
			// results
			// str1 = cursor1.getString(0);
			// if (!(str1.equals("android_metadata"))
			// && !(str1.equals("sqlite_sequence"))) {
			// // convert underscores to spaces
			// str1 = str1.replace("_", " ");
			// mActivitiesArrayAdapter.add(str1);
			// }
			// } while (cursor1.moveToNext());
			// }
			
			///////////////////////////////////////////////
			// SPINNER setup
			// 
			device_spinner = (Spinner) droidmote.findViewById(R.id.device_spinner);
			mAdapter = new ArrayAdapter<String>(droidmote, R.layout.spinner_entry);
			mAdapter.setDropDownViewResource(R.layout.spinner_entry);
			device_spinner.setAdapter(mAdapter);
			device_spinner.setOnItemSelectedListener(droidmote);
			populateDropDown();

			// set spinner to default from last session if possible
			// prefs = getSharedPreferences("droidMoteSettings", MODE_PRIVATE);
			String prefs_table = droidmote.prefs.getString("lastDevice", null);
			if (prefs_table != null) {
				for (int i = 0; i < device_spinner.getCount(); i++) {
					if (prefs_table.equals(device_spinner.getItemAtPosition(i))) {
						device_spinner.setSelection(i);
					}
				}
			}
		}
	}
	
	public HashMap<Integer,Object[]> getButtonMap() {
		return button_map;
	}
	
	// delete activity from the arraylist
	public void deleteActivity(int position) {
		String name = mActivitiesArrayAdapter.getItem(position);
		mActivitiesArrayAdapter.remove(name);
		
		// convert spaces to underscores
		name = name.replace(" ", "_");
		//TODO implement preferences file logic
		
	}
	
	// add a new activity to the arraylist
	public void addActivity(String s) {
		// add to arraylist
		mActivitiesArrayAdapter.add(s);
		//TODO implement preferences file logic
		
		// convert spaces to underscores for storing in prefs file
		s = s.replace(" ", "_");
	}
	
	// rename an activity , pass in new name and position in arraylist
	public void renameActivity(String s, int position) { 
		String old_name = mActivitiesArrayAdapter.getItem(position);
		mActivitiesArrayAdapter.remove(old_name);
		mActivitiesArrayAdapter.add(s);
		//TODO implement preferences file logic
		// make sure when adding item to arraylist that we convert
		// underscores back to spaces for proper displaying		
	}
	
	// sets up the drop-down list, pulls rows from DB to populate
	public void populateDropDown() {
		String str1;
		Cursor cursor1;
		cursor1 = droidmote.device_data.getTables();
		cursor1.moveToFirst();
		mAdapter.clear(); // clear before adding
		if (cursor1.getCount() > 0) {
			do {
				// need to exclude android_metadata and sqlite_sequence tables
				// from results
				str1 = cursor1.getString(0);
				if (!(str1.equals("android_metadata"))
						&& !(str1.equals("sqlite_sequence"))) {
					// spaces are removed from table names, converted to
					// underscores, so convert them back here
					str1 = str1.replace("_", " ");
					mAdapter.add(str1);
				}
			} while (cursor1.moveToNext());
		}
	}
	
	// this function updates the current table with what is selected in
	// drop-down
	// it then grabs the button keys from that table into local devices Cursor
	public void fetchButtons() {
		// first update the cur_table from spinner
		if (device_spinner.getCount() > 0) {
			Object table = device_spinner.getSelectedItem();
			if (table != null) {
				// replace spaces with underscores and then set cur_table to
				// that
				droidmote.cur_table = table.toString().replace(" ", "_");
				droidmote.devices = droidmote.device_data.getKeys(droidmote.cur_table);
			}
		}
	}
}
