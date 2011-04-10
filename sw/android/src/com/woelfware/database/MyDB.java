package com.woelfware.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class MyDB {
	private SQLiteDatabase db;
	private final Context context;
	private final MyDBhelper dbhelper;	
	
	
	public MyDB(Context c){
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
		Cursor c = db.query(curTable, null, Constants.BUTTON_ID+"='"+buttonID+"'",
				null, null, null, null);
		if (c.getCount() > 0) { // then we already have this entry so call updateButton
			updateButton(curTable, buttonID, buttonCategory, content);
			return -1;
		}
		else { // this must be a new entry , so try to insert it
			try{
				ContentValues newTaskValue = new ContentValues();
				newTaskValue.put(Constants.BUTTON_ID, buttonID);
				newTaskValue.put(Constants.BUTTON_DATA, content);
				newTaskValue.put(Constants.CATEGORY, buttonCategory);
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
	public Cursor getKeys(String curTable)
	{
		Cursor c = db.query(curTable, null, null,
				null, null, null, null);
		return c;
	}
	
	// This should return one of the buttons of a particular device selection
	public byte[] getKey(String curTable, String buttonID)
	{
		byte[] button;
		
		Cursor c = db.query(curTable, null, Constants.BUTTON_ID+"='"+buttonID+"'",
				null, null, null, null);
		if (c != null) {
			c.moveToFirst();
			button = c.getBlob(c.getColumnIndex(Constants.BUTTON_DATA));
			//button = c.getString(c.getColumnIndex(Constants.BUTTON_DATA));
			return button;
		}
		return null;
	}
	
	// returns 1 if successful, 0 if some error and 2 if duplicate exists
	public int createTable(String table) {
		Log.v("MyDB createTable","Creating table");
		String TABLE="create table "+
		table+" ("+
		Constants.KEY_ID+" integer primary key autoincrement, "+
		Constants.BUTTON_ID+" text not null, "+
		Constants.BUTTON_DATA+" text not null, "+
		Constants.CATEGORY+" text not null"+
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
	
	public void removeTable(String table) {
		try {
			db.execSQL("drop table if exists "+table);
		} catch (SQLiteException ex) {
			Log.v("Remove table exception", ex.getMessage());
		}	
	}

	public void renameTable(String table, String rename) {
		try {
			db.execSQL("ALTER TABLE "+table+" RENAME TO "+rename);
		} catch (SQLiteException ex) {
			Log.v("Remove table exception", ex.getMessage());
		}	
	}
	
	// gets list of all tables in database and returns as a cursor
	public Cursor getTables() {
		Cursor c;
		try {
			c = db.rawQuery("SELECT name FROM sqlite_master", null);			
			return c;
		} catch (SQLiteException ex) {
			Log.v("List tables exception", ex.getMessage());
			return null;
		}		
	}
	
	// deletes a particular button
	// returns true if succeeded
    public boolean deleteButton(String curTable, String buttonID) 
    {
        return db.delete(curTable, Constants.BUTTON_ID + 
        		"=" + buttonID, null) > 0;
    }
    
    // updates a button
    // returns true if succeeded, externally should call insertButton
    private boolean updateButton(String curTable, String buttonID, String buttonCategory, byte[] content) 
    {
    	// DEBUG var
    	boolean debug;
        ContentValues args = new ContentValues();
        args.put(Constants.BUTTON_DATA, content);
        args.put(Constants.CATEGORY, buttonCategory);
        debug = db.update(curTable, args, 
                  Constants.BUTTON_ID + "='" + buttonID+"'", null) > 0;
        return debug;
    }
}