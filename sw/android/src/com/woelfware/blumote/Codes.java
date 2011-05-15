package com.woelfware.blumote;

class Codes {
	// private constructor to make this class non-instantiable
	private Codes() { }
	
	// note to self, byte is signed datatype
	static String new_name;
	static byte[] pod_data;
	// keeps track of bytes accumulated in LEARN_MODE
    //private static byte[] learn_data;
    static int data_index = 0;
    
    enum LEARN_STATE {
    	IDLE, STARTED, BYTE1, INITIALIZED, COLLECTING
    }
    static LEARN_STATE learn_state = LEARN_STATE.IDLE;
    
    enum INFO_STATE {
    	IDLE, BYTE0, BYTE1, BYTE2, BYTE3
    }
    static INFO_STATE info_state = INFO_STATE.IDLE;
    
    class Pod {
    	public static final byte IDLE = (byte)0xFE; // Default state - nothing going on
        public static final byte RENAME_DEVICE = 0x01; // Unused pod command
        public static final byte LEARN = 0x01; // Pod Command
        public static final byte GET_VERSION = 0x00;  // Pod command
        public static final byte IR_TRANSMIT = 0x02;  // Pod command
        public static final byte ABORT_LEARN = (byte)0xFD; // currently matt does not use this 
        public static final byte DEBUG = (byte)0xFF;  // testing purpose only
        public static final byte ACK = (byte)0x06;
        public static final byte NACK = (byte)0x15;
    }
    
    // this keeps track of what state the interface is in
    // this is useful for how to setup the options menus
    // and what actions buttons should take when pressed
    enum INTERFACE_STATE {
    	MAIN,
    	ACTIVITY, 
    	ACTIVITY_INIT,
    	LEARN,
        RENAME_STATE // renaming misc button
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
    }    
    
    static int debug_send = 0;
}
