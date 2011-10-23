package com.woelfware.blumote;

import java.util.Arrays;

import android.widget.Toast;

// codes and state information for blumote pod operation
class Pod {
	// private constructor to make this class non-instantiable
	private Pod() { }
	
	// note to self, byte is signed datatype
	static String new_name;
	static byte[] pod_data;
	
	// status return codes
	static final int ERROR = -1;
	
	static int MIN_GAP_TIME = 20000; // us
	static final int US_PER_SYS_TICK = 4; // needs to match pod FW, micro-secs per system clock tick
	static final int HDR_PULSE_TOL = 25; // 1/25 = +/-4%
	static final int HDR_SPACE_TOL = 25; // 1/25 = +/-4%
	static final int GAP_TOL = 10; // 1/10 = +/- 10%
	
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
    
    /**
	 * 
	 * @param startingOffset
	 */
	protected static int findEndOfPkt() {	
		int minGapTime = MIN_GAP_TIME;
		int offset = 0;
		int headerPulse = (pod_data[offset++]<<8) | pod_data[offset++];
		int headerSpace = (pod_data[offset++]<<8) | pod_data[offset++];
		int endOffset = pod_data.length - 1; // last index -1 to account for integer size extractions
		int headerPulseMax = headerPulse + headerPulse/HDR_PULSE_TOL;
		int headerPulseMin = headerPulse - headerSpace/HDR_PULSE_TOL;
		int headerSpaceMax = headerSpace + headerSpace/HDR_SPACE_TOL;
		int headerSpaceMin = headerSpace - headerSpace/HDR_SPACE_TOL;
		int workingData;
		
		offset = 4; // start of first pulse
		workingData = (pod_data[offset++]<<8) | pod_data[offset++];
		// find the start of the next pkt
		while (offset <= endOffset) {						
			if (headerPulseMin <= workingData && workingData <= headerPulseMax) {
				workingData = (pod_data[offset++]<<8) | pod_data[offset++]; // get space
				if (headerSpaceMin <= workingData && workingData <= headerSpaceMax) {
					// found the start of the next packet, return offset right before it
					return (offset - 2);
				}
			}
			// skip over the spaces, start by checking pulses only
			offset += 4;
		}
		
		// if we get here we failed to find end of pkt with normal algorithm
		
		// try finding the gap by sorting the 3 largest gaps in the data
		offset = 6; // go to first space data : HP HS P S
		int[] threeLargest = {0, 0, 0}; // need to save three largest values			
		while (offset <= endOffset) {
			workingData = (pod_data[offset++]<<8) | pod_data[offset++];
			if (workingData > threeLargest[0]) {
				if (workingData > threeLargest[1] ) {
					if (workingData > threeLargest[2] ) {
						threeLargest[2] = workingData;						
					} else {					
						threeLargest[1] = workingData;
					} 
				} else {
					threeLargest[0] = workingData;
				}
			}
			offset += 2; // skip over the pulses
		}
		// now compare the 3 largest values and see if they 
		// are relatively close (if so then this must be the gap)
		boolean closeEnough1 = false;
		boolean closeEnough2 = false;
		boolean closeEnough3 = false;
		if (threeLargest[0] > (threeLargest[1] - threeLargest[1]/GAP_TOL)) {
			closeEnough1 = true;
		} else if (threeLargest[0] > (threeLargest[1] + threeLargest[1]/GAP_TOL)) {
			closeEnough1 = true;		
		}
		if (threeLargest[1] > (threeLargest[2] - threeLargest[2]/GAP_TOL)) {
			closeEnough2 = true;
		} else if (threeLargest[0] > (threeLargest[1] + threeLargest[1]/GAP_TOL)) {
			closeEnough2 = true;
		}
		if (threeLargest[0] > (threeLargest[2] - threeLargest[2]/GAP_TOL)) {
			closeEnough3 = true;
		} else if (threeLargest[0] > (threeLargest[2] + threeLargest[2]/GAP_TOL)) {
			closeEnough3 = true;
		}
		
		if (closeEnough1 && closeEnough2 && closeEnough3) {
			// if they are all close enough together....then use this new derated value as the gap
			Arrays.sort(threeLargest);
			minGapTime = threeLargest[0] - threeLargest[0]/GAP_TOL;
		}
		
		// try finding the gap
		offset = 6; // go to the first space data : HP HS P S
        while (offset <= endOffset) {
        	workingData = (pod_data[offset++]<<8) | pod_data[offset++];
            if (workingData >= (minGapTime / US_PER_SYS_TICK)) {
            	return offset;
            }
        }
        
        // if we fail at this then return an error
        return ERROR;
	}
	
	/** 
	 * Analyzes the data from the pod to determine the packet to store to the DB.
	 * @param startingAddr
	 */
	protected static void processRawData(BluMote caller) {
		
		int endingOffset = findEndOfPkt();
		
		if (endingOffset != ERROR) {			
			// need to set the size field to be the endingOffset....before calling storebutton
			// the size is the 0th element of the array
			pod_data[0] = (byte)(endingOffset + 1);
			// after data is processed, store it to the database		
			caller.storeButton();
		} else {
			Toast.makeText(caller, "Data was not good, please retry",
					Toast.LENGTH_SHORT).show();
		}
	}
}
