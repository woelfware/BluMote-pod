package com.woelfware.droidmote;

public class Codes {

	// note to self, byte is signed datatype
	public static String new_name;
	public static byte[] pod_data;
	// keeps track of bytes accumulated in LEARN_MODE
    //private static byte[] learn_data;
    public static int learn_data_index;
    
	public static class Commands {
		public static final byte IDLE = 0x00;
		public static final byte RENAME_DEVICE = 0x01;
		public static final byte LEARN = 0x02;
		public static final byte GET_VERSION = 0x03;
		public static final byte IR_TRANSMIT = 0x04;
		public static final byte ABORT_LEARN = 0x05; 
		public static final byte DEBUG = (byte)0xFF; 
	}
	public static class Return {
		public static final byte ACK = 0x06;
		public static final byte NACK = 0x15;
	}
}
