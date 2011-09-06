package com.woelfware.database;

/**
 * Helper class to hold all the database properties
 * @author keusej
 *
 */
public class Constants {
	// database properties
	public static final String DATABASE_NAME="device_data";
	public static final int DATABASE_VERSION=1;
	public static final String TABLE_NAME="devices";
	public static final String KEY_ID="_id";
	
	public enum CATEGORIES {
		TV_DVD("tv-dvd"), ACTIVITY("activity");
		private final String field;
		CATEGORIES(String field) {
			this.field = field;
		}
		public String getValue() {
			return field;
		}
	}
	
	public enum DB_FIELDS {
		BUTTON_ID("button_id"), 
		BUTTON_DATA("button_data"), 
		// The following fields are reserved for future use
		BUTTON_REPEAT_TIMES("repeat_times"),		
		// these fields are for the whole device, not per button
		CATEGORY("button_category"), 
		;
		private final String field;
		DB_FIELDS(String field) {
			this.field = field;
		}
		public String getValue() {
			return field;
		}
	}	 

	// Other constants
}