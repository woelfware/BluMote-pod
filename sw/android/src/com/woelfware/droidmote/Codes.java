package com.woelfware.droidmote;

public class Codes {

	// note to self, byte is signed datatype
	public static String new_name;
	public static byte[] pod_data;
	// keeps track of bytes accumulated in LEARN_MODE
    //private static byte[] learn_data;
    public static int data_index = 0;
    
    public enum LEARN_STATE {
    	IDLE, STARTED, BYTE1, INITIALIZED, COLLECTING
    }
    public static LEARN_STATE learn_state = LEARN_STATE.IDLE;
    
    public enum INFO_STATE {
    	IDLE, BYTE0, BYTE1, BYTE2, BYTE3
    }
    public static INFO_STATE info_state = INFO_STATE.IDLE;
    
    // some of these are are pod commands, others are used just for state logic
    public static final byte IDLE = (byte)0xFE; // Default state - nothing going on
    public static final byte RENAME_DEVICE = 0x01; // Unused pod command
    public static final byte LEARN = 0x01; // Pod Command
    public static final byte GET_VERSION = 0x00;  // Pod command
    public static final byte IR_TRANSMIT = 0x02;  // Pod command
    public static final byte ABORT_LEARN = (byte)0xFD; // currently matt does not use this 
    public static final byte DEBUG = (byte)0xFF;  // testing purpose only
    public static final byte ACTIVITY = (byte)0xFC; // state code, no pod usage 
    public static final byte ACK = 0x06;
    public static final byte NACK = 0x15;
}
