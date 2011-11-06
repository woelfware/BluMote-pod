package com.woelfware.blumote;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

// codes and state information for blumote pod operation
class Pod {
	// private constructor to make this class non-instantiable
	private Pod() { }

	static BluMote blumote; // reference to blumote instance
	
	// note to self, byte is signed datatype
	static String new_name;
	static byte[] pod_data;
	static byte[] sync_data; // data used explicitly for syncing during BSL process
	
	// status return codes
	static final int ERROR = -1;
	
	static int MIN_GAP_TIME = 20000; // us
	static final int US_PER_SYS_TICK = 4; // needs to match pod FW, micro-secs per system clock tick
	static final int HDR_PULSE_TOL = 25; // 1/25 = +/-4%
	static final int HDR_SPACE_TOL = 25; // 1/25 = +/-4%
	static final int GAP_TOL = 10; // 1/10 = +/- 10%
	// define offsets to important pieces of data in bytestream
	static final int LENGTH_OFFSET = 0; // length of data sent from pod 
										// (minus length/ack/reserved)
	static final int MODFREQ_OFFSET = 1; // modulation frequency
	static final int RESERVED = 2; // reserved byte
	static final int DATA_SIZE = 2; // # bytes of each data element
	static final int HP_OFFSET = RESERVED + 1; // header space
	static final int HS_OFFSET = HP_OFFSET + DATA_SIZE; // header pulse
	static final int FPULSE_OFFSET = HS_OFFSET + DATA_SIZE; // First pulse after header
	static final int FSPACE_OFFSET = FPULSE_OFFSET + DATA_SIZE; // First space after header
	
	// keeps track of bytes accumulated in LEARN_MODE
    //private static byte[] learn_data;
    static int data_index = 0;
    
    static int offset = 0; // keeps track of offset in pod_data being retrieved
    
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
    
    class BSL_CODES {
    	public static final String CMD_MODE = "$$$";
    	public static final byte SYNC = (byte)0x80;
    	public static final byte DATA_ACK = (byte)0x90;
    	public static final byte DATA_NAK = (byte)0xA0;
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
        BSL, // bootstrap loader
        SYNC, // Syncing the BSL to the Pod
    }    
    
    static int debug_send = 0;
      
    // component ID is defined in blumote spec
    static final HashMap<Integer, String> componentMap = new HashMap<Integer,String>();
    static {
        componentMap.put(0, "Hardware");
        componentMap.put(1, "Firmware");
        componentMap.put(2, "Software");
    }
    
    private static int popInt() {
    	try {
	    	int upperByte = 0x00FF & (byte)pod_data[offset++];
	    	int lowerByte = 0x00FF & (byte)pod_data[offset++];
	    	return (upperByte<<8) | lowerByte;
    	} catch (ArrayIndexOutOfBoundsException e ) {
    		return 0;
    	}
    }
    
    private static int getLastOffset() {
    	// last offset is length as computed by pod + offset of first real data element
		int length = 0x00FF & (byte)pod_data[LENGTH_OFFSET];
		return (length + HP_OFFSET);
    }
    
    private static int matchHeaders() {
		offset = HP_OFFSET; // start of actual data
		    	    	
    	int headerPulse = popInt();
		int headerSpace = popInt();
    	int headerPulseMax = headerPulse + headerPulse/HDR_PULSE_TOL;
		int headerPulseMin = headerPulse - headerSpace/HDR_PULSE_TOL;
		int headerSpaceMax = headerSpace + headerSpace/HDR_SPACE_TOL;
		int headerSpaceMin = headerSpace - headerSpace/HDR_SPACE_TOL;
    	
		int workingData;
		int endOffset = getLastOffset();
		
    	offset = FPULSE_OFFSET; // start of first pulse after header
		workingData = popInt();
		// find the start of the next pkt
		while (offset <= endOffset) {						
			if (headerPulseMin <= workingData && workingData <= headerPulseMax) {
				workingData = popInt(); // get space
				if (headerSpaceMin <= workingData && workingData <= headerSpaceMax) {
					// found the start of the next packet, return offset right before header
					return (offset - 2*DATA_SIZE);
				}
			}
			
			popInt(); // skip over the spaces, start by checking pulses only
			workingData = popInt(); // grab the pulse
		}
		
		// if we failed to find it , return ERROR
		return ERROR;
    }
    
    private static int searchGapSize(int minGapTime) {
    	int workingData;
    	int endOffset = getLastOffset();
    	
    	// try finding the gap using fixed size estimate
		offset = FSPACE_OFFSET; // go to the first space data : HP HS P S
        while (offset <= endOffset) {
        	workingData = popInt();
            if (workingData >= minGapTime) {
            	return offset;
            }
            popInt(); // skip over the pulses
        }
        
        // if we fail at this then return an error
        return ERROR;
    }
    
    private static int largeSpace() {    	
    	int minGapTime = MIN_GAP_TIME;    	
    	return searchGapSize(minGapTime);    	
    }
    
    private static int threeLargestSpaces() {
    	int workingData;
    	int endOffset = getLastOffset();
    	int minGapTime = MIN_GAP_TIME; 
    		
    	// try finding the gap by sorting the 3 largest gaps in the data
		offset = FSPACE_OFFSET; // go to first space data : HP HS P S
		int[] threeLargest = {0, 0, 0}; // need to save three largest values			
		while (offset <= endOffset) {
			workingData = popInt();
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
			popInt(); // skip over the pulses
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
		
		return searchGapSize(minGapTime);
    }
    
    /**
	 * 
	 * @param startingOffset
	 */
	static int findEndOfPkt() {			

		int lastIndex; 
		lastIndex = matchHeaders();
		if (lastIndex != ERROR) {
			return lastIndex;
		} 
		lastIndex = largeSpace();
		if (lastIndex != ERROR) {
			return lastIndex;
		}
		lastIndex = threeLargestSpaces();
		if (lastIndex != ERROR) {
			return lastIndex;
		}				
		
		// if all methods fail, then return error
		return ERROR;
	}
	
	/** 
	 * Analyzes the data from the pod to determine the packet to store to the DB.
	 * @param startingAddr
	 */
	static void processRawData(BluMote caller) {
		
		int endingOffset = findEndOfPkt();
		
		if (endingOffset != ERROR) {			
			// need to set the size field to be the endingOffset - informational bytes
			// endOffset points to the data element after the last....
			pod_data[0] = (byte)(endingOffset-HP_OFFSET);
			// now need to trim the pod_data to be the exact size of the data
			byte[] temp = new byte[endingOffset];
			System.arraycopy(pod_data, 0, temp, 0, endingOffset);
			pod_data = temp;
			// after data is processed, store it to the database		
			caller.storeButton();
		} else {
			Toast.makeText(caller, "Data was not good, please retry",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Implements scheme to enter the BSL and get ready to receive the image
	 * TODO determine if we need to add delays between these steps
//	 * TODO implement error handling?
	 */
	static void startBSL(BluMote blumote) throws BslException {
		Pod.blumote = blumote;
		byte[] msg;
		
		byte test = 1 << 2; // PIO-10
		byte rst = 1 << 3; // PIO-11
		
		// step 1, enter the command mode
		sendBSLString(BSL_CODES.CMD_MODE);
		
		// step 2, enter the BSL
		// http://www.ti.com/lit/ug/slau319a/slau319a.pdf
		// rst  ________|------
		// test ___|-|_|--|____		
		sendBSLString( String.format("S*,%02X%02X\r\n", (rst|test), 0) );
		sendBSLString( String.format("S*,%02X%02X\r\n", test, test) );
		sendBSLString( String.format("S*,%02X%02X\r\n", test, 0) );
		sendBSLString( String.format("S*,%02X%02X\r\n", test, test) );
		sendBSLString( String.format("S*,%02X%02X\r\n", rst, rst) );
		sendBSLString( String.format("S*,%02X%02X\r\n", test, 0) );

		// step 3, set to 9600 baud
		sendBSLString( "U,9600,E\r\n" );
		
		// step 4, mass erase
		sync();
		msg = new byte[] {(byte) 0x80, 0x18, 0x04, 0x04, 0x00, 0x00, 0x06, (byte) 0xA5};
		blumote.sendMessage(Util.concat(msg, calcChkSum(msg) ) );
		
		// step 5, sending Rx password
		sync();
		msg = new byte[] {(byte)0x80, 0x10, 0x24, 0x24, 0x00, 0x00, 0x00, 0x00};
		byte[] passwd = {
				(byte)0xFF, (byte)0xFF,	// 0xFFE0		
				(byte)0xFF, (byte)0xFF,	// 0xFFE2
				(byte)0xFF, (byte)0xFF,	// 0xFFE4
				(byte)0xFF, (byte)0xFF,	// 0xFFE6
				(byte)0xFF, (byte)0xFF,	// 0xFFE8
				(byte)0xFF, (byte)0xFF,	// 0xFFEA
				(byte)0xFF, (byte)0xFF,	// 0xFFEC
				(byte)0xFF, (byte)0xFF,	// 0xFFEE
				(byte)0xFF, (byte)0xFF,	// 0xFFF0
				(byte)0xFF, (byte)0xFF,	// 0xFFF2
				(byte)0xFF, (byte)0xFF,	// 0xFFF4
				(byte)0xFF, (byte)0xFF,	// 0xFFF6
				(byte)0xFF, (byte)0xFF,	// 0xFFF8
				(byte)0xFF, (byte)0xFF,	// 0xFFFA
				(byte)0xFF, (byte)0xFF,	// 0xFFFC
				(byte)0xFF, (byte)0xFF	// 0xFFFE
		};
		msg = Util.concat(msg, passwd);
		blumote.sendMessage(Util.concat(msg, calcChkSum(msg) ) );
		
		// after this is finished we are ready to start flashing the hex code to the pod
	}
	
	/**
	 * Sync's the loader program to the pod, requires a ACK/NAK to continue
	 */
	private static void sync() throws BslException {
		sync_data = null;
		blumote.BT_STATE = BT_STATE.SYNC;
		final SyncWait waiter = new SyncWait(100);
		
		while ( waiter.waitTime < waiter.maxWaitTime ) {
			// check if data was received yet from Pod
			if (sync_data != null) { 
				break;  
			} else if (waiter.unlocked) {
				new CountDownTimer(1, 1) {
					public void onTick(long millisUntilFinished) {
						// no need to use this function
					}
					public void onFinish() {
						// called when timer expired
						waiter.unlocked = true; // release lock
						waiter.waitTime++;
					}
				}.start();
			}
		}
		
		// process the data received, if it ever came
		if ( (waiter.waitTime >= waiter.maxWaitTime)) {
			throw new BslException("Exceeded max wait time during sync");
		}
		
		if ( sync_data == null ) {
			throw new BslException("never received any data during sync");
		}
		
		switch (sync_data[0]) {
		case BSL_CODES.DATA_ACK:
			return;

		case BSL_CODES.DATA_NAK:
			throw new BslException("Received NAK sync byte");			

		default:
			throw new BslException("Received invalid sync byte");
		}
	}

	private static class SyncWait {
		public final int maxWaitTime;
		public int waitTime = 0;
		public boolean unlocked = true;
		
		public SyncWait(int maxWait) {
			maxWaitTime = maxWait;
		}		
	}
	
	/**
	 * calculate the 2 byte checksum for transactions to Pod
	 */
	private static byte[] calcChkSum(byte[] data) {
		byte[] result = {0,0};
		
		for (int i = 0; i < data.length; i++) {
			if ( (i % 2) == 1) {
				result[1] ^= data[i]; // upper byte
			} else {
				result[0] ^= data[i]; // lower byte
			}
		}
		
		result[0] ^= 0xFF;
		result[1] ^= 0xFF;
		
		return result;		
	}
	
	/**
	 * Sends a string to the pod in ascii format
	 */
	private static void sendBSLString(String code) {
		try {
			blumote.sendMessage(code.getBytes("ASCII") );			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendBytes(byte[] data, int number) 
			throws BslException {
		//TODO
		byte[] toSend = new byte[number];
		for (int i = 0; i < number ; i++ ) {
			toSend[i] = data[i];
		}
	}
}
