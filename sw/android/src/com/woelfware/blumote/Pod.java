package com.woelfware.blumote;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;

import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

// codes and state information for blumote pod operation
class Pod {
	// private constructor to make this class non-instantiable
	private Pod() { }

	static BluMote blumote; // reference to blumote instance
	
	// current State of the pod bluetooth communication
	static BT_STATES BT_STATE = BT_STATES.IDLE;
	
	// used to lock displaying dialogs for get_version cmd
	private static boolean lockDialog = false;

    // if the button has been pushed down recently, this prevents another button press which could overflow the
    // pod with too much button data
	private static boolean buttonLock = false;

	// these are all in ms (milli-seconds)
	private static int LOCK_RELEASE_TIME = 5000; // timeout to release IR transmit lock if pod doesn't send us an ACK		

	// number of times that pod should repeat when button held down
	private static final byte REPEAT_IR_LONG = (byte) 150;
	
	// the firmware log data we downloaded when requesting a firmware update proccess
	static String[] firmwareRevisions = null;
	
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
    enum BT_STATES {
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
    
    static void setBluMoteRef(BluMote ref) {
    	blumote = ref;
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
	static void processRawData() {
		
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
			blumote.storeButton();
		} else {
			Toast.makeText(blumote, "Data was not good, please retry",
					Toast.LENGTH_SHORT).show();
		}
	}

	static void setFirmwareRevision(String[] rev) {
		firmwareRevisions = rev;
	}

	/**
	 * A state machine error happened while receiving data over bluetooth
	 * @param code 1 is for errors while in LEARN_MODE and 2 is for errors
	 * while in GET_INFO mode, affects the usage of Toast
	 */
	private static void signalError(int code) {
		if (code == 1) {
			Toast.makeText(blumote, "Error occured, exiting learn mode!",
					Toast.LENGTH_SHORT).show();
			try {
				blumote.dismissDialog(BluMote.DIALOG_LEARN_WAIT);
			} catch (Exception e) {
				// if dialog had not been shown it throws an error, ignore
			}
			blumote.stopLearning();
//			BT_STATE = BT_STATES.IDLE;
//			learn_state = LEARN_STATE.IDLE;
		} else if (code == 2) {
			BT_STATE = BT_STATES.IDLE;
			info_state = INFO_STATE.IDLE;
		}
	}
	
	/**
	 * this function sends the byte[] for a button to the pod
	 * @param code the IR code data to send
	 */
	static void sendButtonCode(byte[] code) {
		if (!buttonLock && code != null) { // make sure we have not recently sent a button
			buttonLock = true;
			//create a new timer to avoid flooding pod with button data
			new CountDownTimer(LOCK_RELEASE_TIME, LOCK_RELEASE_TIME) {
				public void onTick(long millisUntilFinished) {
					// no need to use this function
				}
				public void onFinish() {
					// called when timer expired
					buttonLock = false; // release lock
				}
			}.start();	
			
			byte command = (byte) (Codes.IR_TRANSMIT);
			byte[] toSend = new byte[code.length + 2]; // 1 extra byte for command byte, 1 for repeat #
			toSend[0] = command;
			// insert different repeat flags based on if this
			// is a long press or a short press of the button
			if (BluMote.BUTTON_LOOPING) {
				toSend[1] = REPEAT_IR_LONG; // long press
			} else {
				toSend[1] = 0; // short press
			}
			for (int j = 2; j < toSend.length; j++) {	
				toSend[j] = code[j - 2];
			}
			setIrTransmitState();

			blumote.sendMessage(toSend); // send data if matches
		}
	}
	
//	static BT_STATES getCurrentState() {
//		return BT_STATE;
//	}
//	
	static boolean isLearnState() {
		return BT_STATE == BT_STATES.LEARN;
	}
	
	static void setStopLearnState() {
		BT_STATE = BT_STATES.ABORT_LEARN;
	}
	
	static void setStopTransmitState() {
		BT_STATE = BT_STATES.ABORT_TRANSMIT;
	}
	
	static void setGetVerState() {
		BT_STATE = BT_STATES.GET_VERSION;
	}
	
	static void setIrTransmitState() {
		BT_STATE = BT_STATES.IR_TRANSMIT;
	}
	
	static void setLearnState() {
		BT_STATE = BT_STATES.LEARN;
	}
	
	static void setIdleState() {
		BT_STATE = BT_STATES.IDLE;
	}
	
	static void setBslState() {
		BT_STATE = BT_STATES.BSL;
	}
	
	static void lockDialog() {
		lockDialog = true;
	}
	
	static void unlockDialog() {
		lockDialog = false;
	}
	
	/**
	 * This method should be called whenever we receive a byte[] from the pod.
	 * @param response the circular buffer that contains the data that was received over BT
	 * @param bytes how many bytes were received and stored in response[] on the call to this method
	 * @param index the starting index into the circular data buffer that should be read
	 */
	static void interpretResponse(byte[] response, int bytes, int index) {
		switch (BT_STATE) {
		case LEARN:
			try { // catch any unforseen state machine errors.....
				// learn data may not come all together, so need to process data
				// in chunks
				while (bytes > 0) {
					switch (learn_state) {
					case IDLE:
						if (response[index] == Codes.ACK) {
							learn_state = LEARN_STATE.PKT_LENGTH;
							index = (index + 1)	% BluetoothChatService.buffer_size;
							bytes--;
							data_index = 0;
						} else {
							signalError(1);
							return;
						}
						break;

					case PKT_LENGTH:
						// if we got here then we are on the second byte of data
						if (Util.isGreaterThanUnsignedByte(
								response[index], 0)) {
							pod_data = new byte[(0x00FF & response[index]) + HP_OFFSET];  
							// first three bytes are 'pkt_length carrier_freq reserved' 
							pod_data[data_index++] = response[index];
							bytes--;
							index = (index + 1)	% BluetoothChatService.buffer_size;							
							learn_state = LEARN_STATE.CARRIER_FREQ;
						} else {
							signalError(1);
							return;
						}
						break;

					case CARRIER_FREQ:
						// third byte should be carrier frequency
						if (checkPodDataBounds(bytes)) {
							learn_state = LEARN_STATE.RESERVED;
							pod_data[data_index++] = response[index];
							bytes--;
							index = (index + 1)	% BluetoothChatService.buffer_size;
						} else {
							signalError(1);
							return;
						}
						break;

					case RESERVED:
						// fourth byte should be reserved
						if (checkPodDataBounds(bytes)) {
							learn_state = LEARN_STATE.COLLECTING;
							pod_data[data_index++] = 0; // default to 0
							bytes--;
							index = (index + 1)	% BluetoothChatService.buffer_size;
						} else {
							signalError(1);
							return;
						}
						break;

					case COLLECTING:
						if (checkPodDataBounds(bytes)) {
							pod_data[data_index++] = response[index];
							// first check to see if this is the last byte
							if (Util.isGreaterThanUnsignedByte(
									data_index, pod_data[0] + 2)) { 
								// data index at final position is nth or N+1 total items, pod_data[0]
								// is the # of bytes of IR data, so adding 2 gives (n+2)th index.
								// After last byte received then data_index points to an index after
								// the last so we exit the collecting routine and store data.
								learn_state = LEARN_STATE.IDLE; 
								blumote.dismissDialog(BluMote.DIALOG_LEARN_WAIT);
								processRawData(); // first 3 bytes are not to be analyzed
								return;
							}							
							bytes--;
							index = (index + 1)	% BluetoothChatService.buffer_size;
						} else {
							signalError(1);
							return;
						}
						break;
					} // end switch/case
				} // end while loop
			} catch (Exception e) { // something unexpected occurred....exit
				Toast.makeText(blumote, "Communication error, exiting learn mode",
						Toast.LENGTH_SHORT).show();
				BT_STATE = BT_STATES.IDLE;
				return;
			}
			break;

		case GET_VERSION:
			try { 
				while (bytes > 0) {
					switch (info_state) {
					case IDLE:
						if (response[index] == Codes.ACK) {
							pod_data = new byte[INFO_STATE.values().length]; 
							info_state = INFO_STATE.BYTE0;
							index = (index + 1)	% BluetoothChatService.buffer_size;
							bytes--;
							data_index = 0;
						} else {
							signalError(2);
							return;
						}
						break;

					case BYTE0:
						pod_data[0] = response[index];
						info_state = INFO_STATE.BYTE1;
						bytes--;
						index = (index + 1)	% BluetoothChatService.buffer_size;
						break;

					case BYTE1:
						pod_data[1] = response[index];
						info_state = INFO_STATE.BYTE2;
						bytes--;
						index = (index + 1)	% BluetoothChatService.buffer_size;
						break;

					case BYTE2:
						pod_data[2] = response[index];
						info_state = INFO_STATE.BYTE3;
						bytes--;
						index = (index + 1)	% BluetoothChatService.buffer_size;
						break;

					case BYTE3:
						pod_data[3] = response[index];
						info_state = INFO_STATE.IDLE;
						bytes--;
						index = (index + 1) % BluetoothChatService.buffer_size;

						if (lockDialog) { // this gets set when we are doing a FW update process
							lockDialog = false; //always unlock after receiving data
							Intent i = new Intent(blumote, FwUpdateActivity.class);
							// tack on the downloaded lines of text
							i.putExtra(FwUpdateActivity.FW_IMAGES, firmwareRevisions);								
							blumote.startActivityForResult(i, BluMote.UPDATE_FW);
							return;
						} else {
							// else this request was sent by the menu
							// create a dialog to display data to user
							blumote.showDialog(BluMote.DIALOG_SHOW_INFO);
						}
						break;
					}
				}
			} catch (Exception e) {
				Toast.makeText(blumote,
						"Communication error, exiting learn mode",
						Toast.LENGTH_SHORT).show();
				BT_STATE = BT_STATES.IDLE;
				return;
			}
			break;

		case ABORT_LEARN:
			BT_STATE = BT_STATES.IDLE; // reset state
			if (response[index] == Codes.ACK) {

			}
			break;

		case IR_TRANSMIT:
			BT_STATE = BT_STATES.IDLE;			
			if (response[index] == Codes.ACK) {
				// release lock if we get an ACK
				buttonLock = false;
				if (BluMote.DEBUG) {
					Toast.makeText(blumote, "ACK received - lock removed", Toast.LENGTH_SHORT).show();
				}
			}
			break;

		case ABORT_TRANSMIT:
			BT_STATE = BT_STATES.IDLE; // reset state
			if (BluMote.DEBUG) {
				if (response[index] == Codes.ACK) {
					Toast.makeText(blumote, "ACK received for abort transmit", Toast.LENGTH_SHORT).show();
				}
			}
			break;

		case BSL:
			// Just log the messages we get from the Pod during BSL
			Log.v("BSL", response.toString() );
			break;

		case SYNC:
			// post the result to the Pod class
			//TODO
			sync_data = response;
		}
	}
	
	static void requestLearn() {
		byte[] toSend;
		toSend = new byte[1];
		toSend[0] = (byte)Codes.LEARN;
		setLearnState();
		blumote.sendMessage(toSend);
	}
	
	static void abortLearn() {
		byte[] toSend;
		toSend = new byte[1];
		toSend[0] = (byte)Codes.ABORT_LEARN;
		setStopLearnState();
		blumote.sendMessage(toSend);
	}
	
	static void getVersion() {
		byte[] toSend;
		setGetVerState();
		toSend = new byte[1];
		toSend[0] = (byte)Codes.GET_VERSION;
		blumote.sendMessage(toSend);
	}
	
	static void abortTransmit() {
		byte[] toSend;
		setStopTransmitState();
		toSend = new byte[1];
		toSend[0] = (byte)Codes.ABORT_TRANSMIT;
		blumote.sendMessage(toSend);
	}
		
	/**
	 * Determines if more bytes are being read that is available in the local data structure. This
	 * function should be called whenever a new set of data is COLLECTING in interpretResponse()
	 * @param bytes the number of bytes received
	 * @return false if the data is outside of the local storage space available and true if there is no error.
	 */
	private static boolean checkPodDataBounds(int bytes) {
		if (bytes > (pod_data.length - data_index)) {
			return false;
		}
		return true;
	}

	/**
	 * Implements scheme to enter the BSL and get ready to receive the image
	 * TODO determine if we need to add delays between these steps
	 */
	static void startBSL() throws BslException {
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
	static void sync() throws BslException {
		sync_data = null;
		BT_STATE = BT_STATES.SYNC;
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
	private static void sendBSLString(String code) throws BslException {
		try {
			blumote.sendMessage(code.getBytes("ASCII") );			
		} catch (UnsupportedEncodingException e) {
			throw new BslException("Encoding error while starting BSL");
		}
	}
	
	public static void sendFwLine(String line) 
			throws BslException {
		sync();
		line = line.replace("\r\n", "");
		
		//<start_code><byte_cnt><addr><record_type><data><chksum>
		// overhead is bytes of each element in the formatted BSL packet (except data)
		int overhead = 1 + 2 + 4 + 2 + 2; 
		
		// improperly formatted file if doesn't start with :
		if ( ! line.startsWith(":") ) {
			throw new BslException("malformed fw image line");
		}
		
		// verify the byte count matches the line record
		int assert1 = Integer.parseInt(line.substring(1, 3), 16); // byte_cnt
		int assert2 = (line.length() - overhead) / 2;
		if (assert1 != assert2) {
			throw new BslException("malformed fw image line");
		}
		
		// See TI MSP430 datasheet for the definition of these fields
		byte AH = Byte.parseByte(line.substring(3, 5), 16);
		byte AL = Byte.parseByte(line.substring(5, 7), 16);
		byte HDR = (byte)0x80;
		byte CMD = 0x12;
		byte LL = (byte) (Byte.parseByte((line.substring(9, line.length()-2)))/2);
		if (LL == 0) {
			return;
		}
		byte LH = 0;
		byte L1 = (byte) (LL + 4);
		byte L2 = (byte) (LL + 4);
		byte[] msg = {HDR, CMD, L1, L2, AL, AH, LL, LH};
		
		// extract the data bytes from the intel hex message
		byte[] dataBytes;
		try {
			dataBytes = line.substring(9, line.length()-3).getBytes("ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new BslException("Encoding error in sending firmware");
		}
		// add together msg bytes, data bytes and 2 bytes for checksum
		byte[] msgPlusData = new byte[msg.length + dataBytes.length + 2];
		for (int i= 0; i < msg.length; i++) {
			msgPlusData[i] = msg[i];
		}
		for (int i = 0; i < dataBytes.length; i++) {
			msgPlusData[i+msg.length] = dataBytes[i]; 
		}
		// append checksum for total frame
		byte[] chkSum = calcChkSum(msgPlusData);		
		msgPlusData[msgPlusData.length - 2] = chkSum[0];
		msgPlusData[msgPlusData.length - 1] = chkSum[1];
		
		// send to Pod
		blumote.sendMessage(msgPlusData);
	}
}
