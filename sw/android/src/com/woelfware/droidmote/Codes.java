package com.woelfware.droidmote;

public class Codes {

	// note to self, byte is signed datatype
	public static String new_name;
	public static byte[] pod_data;
	// keeps track of bytes accumulated in LEARN_MODE
    //private static byte[] learn_data;
    public static int learn_data_index = 0;
    public static boolean learn_started = false;
    
	public static class Commands {
		public static final byte IDLE = (byte)0xFE;
		public static final byte RENAME_DEVICE = 0x01;
		public static final byte LEARN = 0x01;
		public static final byte GET_VERSION = 0x00;
		public static final byte IR_TRANSMIT = 0x02;
		public static final byte ABORT_LEARN = (byte)0xFD; // currently matt does not use this 
		public static final byte DEBUG = (byte)0xFF; 
	}
	public static class Return {
		public static final byte ACK = 0x06;
		public static final byte NACK = 0x15;
	}
}
