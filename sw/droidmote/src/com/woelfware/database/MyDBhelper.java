package com.woelfware.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
//import android.util.Log;

public class MyDBhelper extends SQLiteOpenHelper{

	public MyDBhelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// NOTE: going to create each table programatically instead of hard coded create table statement
		
//		Log.v("MyDBhelper onCreate","Creating all the tables");
//		try {
//			db.execSQL(CREATE_TABLE);
//		} catch(SQLiteException ex) {
//			Log.v("Create table exception", ex.getMessage());
//		}
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
//		Log.w("TaskDBAdapter", "Upgrading from version "+oldVersion
//				+" to "+newVersion
//				+", which will destroy all old data");
//		db.execSQL("drop table if exists "+Constants.TABLE_NAME);
//		onCreate(db);
	}
}