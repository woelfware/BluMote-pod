package com.woelfware.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class DeviceDB {
	private SQLiteDatabase db;
	private final Context context;
	private final MyDBhelper dbhelper;	
	
	
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
	
	// pass in the current-table that we are working with then the button ID and the content
	// returns the rowID of the addition
	public long insertButton(String curTable, String buttonID, String buttonCategory, byte[] content)
	{
		try {
		Cursor c = db.query(curTable, null, Constants.DB_FIELDS.BUTTON_ID.getValue()+"='"+buttonID+"'",
				null, null, null, null);
		if (c.getCount() > 0) { // then we already have this entry so call updateButton
			updateButton(curTable, buttonID, buttonCategory, content);
			return -1;
		}
		else { // this must be a new entry , so try to insert it
			try{
				ContentValues newTaskValue = new ContentValues();
				newTaskValue.put(Constants.DB_FIELDS.BUTTON_ID.getValue(), buttonID);
				newTaskValue.put(Constants.DB_FIELDS.BUTTON_DATA.getValue(), content);
				newTaskValue.put(Constants.DB_FIELDS.CATEGORY.getValue(), buttonCategory);
				db = dbhelper.getWritableDatabase();
				return db.insertOrThrow(curTable, null, newTaskValue);
			} catch(SQLiteException ex) {
				Log.v("Insert into database exception caught", ex.getMessage());
				return -1;
			}
		}
		} catch (Exception e) {
			Log.e("Oops",e.getMessage());
			return -1;
		}
	}
	
	// This should return all the buttons of a particular device selection
	public Cursor getButtons(String curTable)
	{
		Cursor c = db.query(curTable, null, null,
				null, null, null, null);
		return c;
	}
	
	// This should return one of the buttons of a particular device selection
	public byte[] getButton(String device, String buttonID)
	{
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
	
	// returns 1 if successful, 0 if some error and 2 if duplicate exists
	public int addDevice(String table) {
		Log.v("MyDB createTable","Creating table");
		String TABLE="create table "+
		table+" ("+
		Constants.KEY_ID+" integer primary key autoincrement, "+
		Constants.DB_FIELDS.BUTTON_ID.getValue()+" text not null, "+
		Constants.DB_FIELDS.BUTTON_DATA.getValue()+" text not null, "+
		Constants.DB_FIELDS.CATEGORY.getValue()+" text not null"+
		");";
		try {
			db.execSQL(TABLE);
			return 1;
		} catch(SQLiteException ex) {
			Log.v("Create table exception", ex.getMessage());
			return 0;
			// TODO - add check for duplicate table
		}
	}
	
	public void removeDevice(String table) {
		try {
			db.execSQL("drop table if exists "+table);
		} catch (SQLiteException ex) {
			Log.v("Remove table exception", ex.getMessage());
		}	
	}

	public void renameDevice(String table, String rename) {
		try {
			db.execSQL("ALTER TABLE "+table+" RENAME TO "+rename);
		} catch (SQLiteException ex) {
			Log.v("Remove table exception", ex.getMessage());
		}	
	}
	
	// gets String[] of all tables in database and returns as a cursor
	// excludes the android_metadata and sqlite_sequence tables
	public String[] getDevices() {		
		try {
			Cursor c = db.rawQuery("SELECT name FROM sqlite_master", null);
			// create array to hold table names, exclude android_metadata and sqlite_sequence
			String[] results = new String[c.getCount() - 2]; 
			String result = null;
			c.moveToFirst(); 
			int arrayIndex = 0;
			// loop through items, add items in correct format to the String[]			
			for (int cursorIndex = 0; cursorIndex < c.getCount(); cursorIndex++) {								
				result = c.getString(0); // get first column (table name)
				if (!(result.equals("android_metadata"))
						&& !(result.equals("sqlite_sequence"))) {
					// spaces are removed from table names, converted to
					// underscores, so convert them back here
					result = result.replace("_", " ");
					results[arrayIndex] = result;
					arrayIndex++;
				}

				c.moveToNext();
			}
			return results;
		} catch (Exception ex) {
			Log.v("List tables exception", ex.getMessage());
			return null;
		}		
	}
	
	// deletes a particular button
	// returns true if succeeded
    public boolean deleteButton(String curTable, String buttonID) 
    {
        return db.delete(curTable, Constants.DB_FIELDS.BUTTON_ID.getValue() + 
        		"=" + buttonID, null) > 0;
    }
    
    // updates a button
    // returns true if succeeded, externally should call insertButton
    private boolean updateButton(String curTable, String buttonID, String buttonCategory, byte[] content) 
    {
    	// DEBUG var
    	boolean debug;
        ContentValues args = new ContentValues();
        args.put(Constants.DB_FIELDS.BUTTON_DATA.getValue(), content);
        args.put(Constants.DB_FIELDS.CATEGORY.getValue(), buttonCategory);
        debug = db.update(curTable, args, 
                  Constants.DB_FIELDS.BUTTON_ID.getValue() + "='" + buttonID+"'", null) > 0;
        return debug;
    }
}