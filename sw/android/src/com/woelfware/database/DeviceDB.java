package com.woelfware.database;

import java.io.IOException;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.woelfware.blumote.ButtonData;

/**
 * Helper class to manage operating on the SQLite database
 * @author keusej
 *
 */
public class DeviceDB {
	private SQLiteDatabase db;
	private final Context context;
	private final MyDBhelper dbhelper;	
	
	private static final String TAG = "DeviceDB";
	
	public final static String DB_NAME = "/data/com.woelfware.blumote/databases/"+Constants.DATABASE_NAME;
	
	public DeviceDB(Context c){
		context = c;
		dbhelper = new MyDBhelper(context, Constants.DATABASE_NAME, null,
				Constants.DATABASE_VERSION);
	}
	
	public void close()
	{
		db.close();
	}
	
	public void open() throws SQLiteException
	{
		try {
			db = dbhelper.getWritableDatabase();
		} catch(SQLiteException ex) {
			Log.v("Open database exception caught", ex.getMessage());
		}
	}
	
	// returns true on success, false otherwise
	public boolean restore() 
	{
		try {
			dbhelper.importDatabase("device_data.bak");
			open();
			return true;
		} catch (IOException e) {
			Log.v("restore database exception caught", e.getMessage());
			return false;
		}
	}
	
	/**
	 * Inserts a new button into the database
	 * @param curTable the table name which is typically the device name
	 * @param buttonID the name of the button that we want to insert into the table
	 * @param buttonRepeat the number of times to tell pod to repeat the code, null is acceptable for default
	 * @param content the button data (IR code) associated with the button
	 * @return the row ID of the database, this return value is not currently used
	 */
	public long insertButton(String curTable, String buttonID, String buttonRepeat, byte[] content)
	{
		curTable = curTable.replace(" ", "_");
		curTable = deviceNameFormat(curTable);
		
		try {
			Cursor c = db.query(curTable, null, Constants.DB_FIELDS.BUTTON_ID.getValue()+"='"+buttonID+"'",
					null, null, null, null);
			if (c.getCount() > 0) { // then we already have this entry so call updateButton
				updateButton(curTable, buttonID, buttonRepeat, content);
				return -1;
			}
			else { // this must be a new entry , so try to insert it
				try{
					ContentValues newTaskValue = new ContentValues();
					newTaskValue.put(Constants.DB_FIELDS.BUTTON_ID.getValue(), buttonID);
					newTaskValue.put(Constants.DB_FIELDS.BUTTON_DATA.getValue(), content);
					newTaskValue.put(Constants.DB_FIELDS.BUTTON_REPEAT_TIMES.getValue(), buttonRepeat);
					db = dbhelper.getWritableDatabase();
					return db.insertOrThrow(curTable, null, newTaskValue);
				} catch(SQLiteException ex) {
					Log.v("Insert into database exception caught", ex.getMessage());
					return -1;
				}
			}
		} catch (Exception e) {
			Log.e(TAG,e.getMessage());
			return -1;
		}
	}
	
	/**
	 * This should return all the buttons of a particular device selection
	 * @param curTable the name of the device that we want to extract the button data for
	 * @return the ButtonData[] which contains the properties for each button in the DB
	 */
	public ButtonData[] getButtons(String curTable)
	{
		curTable = curTable.replace(" ", "_");
		curTable = deviceNameFormat(curTable);
		
		Cursor c = db.query(curTable, null, null,
				null, null, null, null);
		
		ButtonData[] buttons = null;		
		if (c.getCount() > 0) {
			c.moveToFirst();
			buttons = new ButtonData[c.getCount()];
			// iterate through cursor to load up buttons array
			for (int i= 0; i < buttons.length; i++) {
				buttons[i] = new ButtonData(0, c.getString(1), c.getBlob(2), c.getInt(3));
				c.moveToNext();
			}
		}
		return buttons;
	}
	
	/**
	 * This should return the data for one of the buttons of a particular device selection
	 * @param device the name of the device which is also the table of the database
	 * @param buttonID the name of the button that we want to get the data for
	 * @return the byte[] for the IR codes associated with a button
	 */
	public byte[] getButton(String device, String buttonID)
	{
		device = device.replace(" ", "_");
		device = deviceNameFormat(device);
		
		byte[] button;
		
		Cursor c = db.query(device, null, Constants.DB_FIELDS.BUTTON_ID.getValue()+"='"+buttonID+"'",
				null, null, null, null);
		if (c != null) {
			c.moveToFirst();
			button = c.getBlob(c.getColumnIndex(Constants.DB_FIELDS.BUTTON_DATA.getValue()));
			//button = c.getString(c.getColumnIndex(Constants.BUTTON_DATA));
			return button;
		}
		return null;
	}
	
	/**
	 * Add a new device to the database
	 * @param table the name of the device, which is also the table name in the DB
	 * @return 1 if successful, 0 if there was an error and 2 if a duplicate exists
	 */
	public int addDevice(String table) {
		table = table.replace(" ", "_");
		table = deviceNameFormat(table);
		
		Log.v("MyDB createTable","Creating table");
		String TABLE="create table "+ table +" ("+
		Constants.KEY_ID+" integer primary key autoincrement, "+
		Constants.DB_FIELDS.BUTTON_ID.getValue()+" text not null, "+
		Constants.DB_FIELDS.BUTTON_DATA.getValue()+" text not null, "+
		//Constants.DB_FIELDS.CATEGORY.getValue()+" text not null"+
		Constants.DB_FIELDS.BUTTON_REPEAT_TIMES + " integer" +
		");";
		try {
			db.execSQL(TABLE);
			return 1;
		} catch(SQLiteException ex) {
			Log.v("Create table exception", ex.getMessage());
			return 0;
			// TODO - add check for duplicate table
			// TODO - add a entry (that is not a button) that is for button category
		}
	}
	
	/**
	 * Removes a device from the database, all button codes are destroyed
	 * @param table the name of the device to delete
	 */
	public void removeDevice(String table) {
		table = table.replace(" ", "_");
		table = deviceNameFormat(table);
		
		try {
			db.execSQL("drop table if exists "+table);
		} catch (SQLiteException ex) {
			Log.v("Remove table exception", ex.getMessage());
		}	
	}

	/**
	 * Renames a device in the database
	 * @param table the existing name
	 * @param rename the new name
	 */
	public void renameDevice(String table, String rename) {
		table = table.replace(" ", "_");
		table = deviceNameFormat(table);
		rename = rename.replace(" ", "_");
		rename = "["+rename+"]";
		try {
			db.execSQL("ALTER TABLE "+table+" RENAME TO "+rename);
		} catch (SQLiteException ex) {
			Log.v("Remove table exception", ex.getMessage());
		}	
	}
	
	/**
	 * Returns all the devices that are currently being stored in the database
	 */
	public String[] getDevices() {		
		try {
			Cursor c = db.rawQuery("SELECT name FROM sqlite_master", null);
			// create array to hold table names, exclude android_metadata and sqlite_sequence
			if (c!=null) {
				ArrayList<String> results = new ArrayList<String>();								 
				String result = null;
				c.moveToFirst(); 
				// loop through items, add items in correct format to the String[]			
				for (int cursorIndex = 0; cursorIndex < c.getCount(); cursorIndex++) {								
					result = c.getString(0); // get first column (table name)
					if (!(result.equals("android_metadata"))
							&& !(result.equals("sqlite_sequence"))) {
						// spaces are removed from table names, converted to
						// underscores, so convert them back here
						result = result.replace("_", " ");
						results.add(result);
					}

					c.moveToNext();
				}
				String[] returnValue = new String[results.size()];
				// convert arraylist to a string[]
				for (int i=0 ; i< results.size(); i++) {
					returnValue[i] = results.get(i);
				}
				return returnValue;
			}
			else {
				return null;
			}
		} catch (Exception ex) {
			Log.v("List tables exception", ex.getMessage());
			return null;
		}		
	}
	
	/**
	 * Deletes a button from the database
	 * @param curTable the device name that has the button
	 * @param buttonID the button that we want deleted
	 * @return true if successful, false if not
	 */
    public boolean deleteButton(String curTable, String buttonID) 
    {
    	curTable = curTable.replace(" ", "_");
    	curTable = deviceNameFormat(curTable);
    	
        return db.delete(curTable, Constants.DB_FIELDS.BUTTON_ID.getValue() + 
        		"=" + buttonID, null) > 0;
    }
    
    /**
     * Updates a button with new data (IR code)
     * @param curTable the name of the device
     * @param buttonID the button name
     * @param buttonCategory the button category
     * TODO - consider category change to get rid of it per button
     * @param content the code associated with the button
     */
    private boolean updateButton(String curTable, String buttonID, String buttonCategory, byte[] content) 
    {
    	curTable = curTable.replace(" ", "_");
    	curTable = deviceNameFormat(curTable);
    	
    	// DEBUG var
    	boolean debug;
        ContentValues args = new ContentValues();
        args.put(Constants.DB_FIELDS.BUTTON_DATA.getValue(), content);
        args.put(Constants.DB_FIELDS.CATEGORY.getValue(), buttonCategory);
        debug = db.update(curTable, args, 
                  Constants.DB_FIELDS.BUTTON_ID.getValue() + "='" + buttonID+"'", null) > 0;
        return debug;
    }
    
    private String deviceNameFormat( String deviceName ) {
    	if (deviceName.startsWith("[")) {
    		return deviceName;
    	}
    	return "["+deviceName+"]";    		
    }
}