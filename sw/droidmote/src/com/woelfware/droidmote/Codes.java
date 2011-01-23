package com.woelfware.droidmote;

public class Codes {
	// this keeps track of sequence code that pod uses to make 
	// sure its the first time its seen something
	public static byte PKT_COUNT = 0x00; 
	// note to self, byte is signed datatype
	public static class Commands {
		public static final byte INIT = 0x00; 
		public static final byte RENAME_DEVICE = 0x01;
		public static final byte LEARN = 0x02;
		public static final byte GET_VERSION = 0x03;
		public static final byte IR_TRANSMIT = 0x04;
		public static final byte ABORT_LEARN = 0x05; // TODO tell matt about this
	}
	public static class Return {
		public static final byte ACK = 0x06;
		public static final byte NACK = 0x15;
	}
}
