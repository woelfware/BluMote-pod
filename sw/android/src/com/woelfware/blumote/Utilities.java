package com.woelfware.blumote;

public class Utilities {
    // this method will perform x > y with what should be unsigned bytes
    public static boolean isGreaterThanUnsignedByte(int x, int y) {
    	int xl = 0x00FF & x;
    	int yl = 0x00FF & y;
    	
    	if (xl > yl) { return true; }
    	else { return false; }
    }
    
	// returns integer from two bytes
	// a is upper nibble, b is lower nibble	q
	public int bytesToInt(byte a, byte b) {  
		int i = 0;
	    i |= a & 0xFF;
	    i <<= 8;
	    i |= b & 0xFF;
	    return i;  
	}  
}
