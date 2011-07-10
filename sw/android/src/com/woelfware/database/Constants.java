package com.woelfware.database;

public class Constants {
	// database properties
	public static final String DATABASE_NAME="device_data";
	public static final int DATABASE_VERSION=1;
	public static final String TABLE_NAME="devices";
	public static final String KEY_ID="_id";

	// Columns in the database
//	public class DB_FIELD {
//		public static final String BUTTON_ID = "button_id";
//		public static final String BUTTON_DATA = "button_data"; 
//		public static final String CATEGORY = "button_category"; // for example, TV, DVD, etc
//		
//	}
	
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
		BUTTON_ID("button_id"), BUTTON_DATA("button_data"), CATEGORY("button_category");
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