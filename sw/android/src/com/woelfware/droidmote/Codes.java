package com.woelfware.droidmote;

public class Codes {

	// note to self, byte is signed datatype
	public static String new_name;
	public static byte[] pod_data;
	public static class Commands {
		public static final byte RENAME_DEVICE = 0x01;
		public static final byte LEARN = 0x02;
		public static final byte GET_VERSION = 0x03;
		public static final byte IR_TRANSMIT = 0x04;
		public static final byte ABORT_LEARN = 0x05; // TODO tell matt about this
		public static final byte DEBUG = 0x7F; 
	}
	public static class Return {
		public static final byte ACK = 0x06;
		public static final byte NACK = 0x15;
	}
}
