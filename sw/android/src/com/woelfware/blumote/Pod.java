package com.woelfware.blumote;

// codes and state information for blumote pod operation
class Pod {
	// private constructor to make this class non-instantiable
	private Pod() { }
	
	// note to self, byte is signed datatype
	static String new_name;
	static byte[] pod_data;
	
	static int MIN_GAP_TIME = 20000;
	static int US_PER_SYS_TICK = 4; // needs to match pod FW
	
	// keeps track of bytes accumulated in LEARN_MODE
    //private static byte[] learn_data;
    static int data_index = 0;
    
    enum LEARN_STATE {
    	IDLE, CARRIER_FREQ, PKT_LENGTH, RESERVED, COLLECTING
    }
    static LEARN_STATE learn_state = LEARN_STATE.IDLE;
    
    enum INFO_STATE {
    	IDLE, BYTE0, BYTE1, BYTE2, BYTE3
    }
    static INFO_STATE info_state = INFO_STATE.IDLE;
    
    class Codes {
    	public static final byte IDLE = (byte)0xFE; // Default state - nothing going on
        public static final byte RENAME_DEVICE = 0x01; // Unused pod command
        public static final byte LEARN = 0x01; // Pod Command
        public static final byte GET_VERSION = 0x00;  // Pod command
        public static final byte IR_TRANSMIT = 0x02;  // Pod command
        public static final byte ABORT_LEARN = (byte)0xFD; // currently matt does not use this 
        public static final byte DEBUG = (byte)0xFF;  // testing purpose only
        public static final byte ACK = (byte)0x06;
        public static final byte NACK = (byte)0x15;
        public static final byte ABORT_TRANSMIT = (byte)0x03; // stop repeating IR command
    }
    
    // this is to keep track of state machines for example
    // for receiving data from bluetooth interface, how that
    // data should be interpreted
    enum BT_STATE {
        IDLE, // not doing anything
        LEARN, // in button learn mode
        GET_VERSION, // getting pod information
        IR_TRANSMIT, // transmit an ir code
        ABORT_LEARN, // aborting the learn mode
        DEBUG, // debug mode for testing
        ABORT_TRANSMIT,
    }    
    
    static int debug_send = 0;
}
