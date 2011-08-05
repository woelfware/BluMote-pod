package com.woelfware.blumote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

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
	public static int bytesToInt(byte a, byte b) {  
		int i = 0;
	    i |= a & 0xFF;
	    i <<= 8;
	    i |= b & 0xFF;
	    return i;  
	}
	
	public static class FileUtils {
	    /**
	     * Creates the specified <code>toFile</code> as a byte for byte copy of the
	     * <code>fromFile</code>. If <code>toFile</code> already exists, then it
	     * will be replaced with a copy of <code>fromFile</code>. The name and path
	     * of <code>toFile</code> will be that of <code>toFile</code>.<br/>
	     * <br/>
	     * <i> Note: <code>fromFile</code> and <code>toFile</code> will be closed by
	     * this function.</i>
	     * 
	     * @param fromFile
	     *            - File for the file to copy from.
	     * @param toFile
	     *            - File for the file to copy to.
	     */
	    public static void copyFile(File fromFile, File toFile) throws IOException {
	    	FileChannel fromChannel = null;
	        FileChannel toChannel = null;
	        try {
	            fromChannel = new FileInputStream(fromFile).getChannel();
	            toChannel = new FileOutputStream(toFile).getChannel();
	            fromChannel.transferTo(0, fromChannel.size(), toChannel);
	        } finally {
	            try {
	                if (fromChannel != null) {
	                    fromChannel.close();
	                }
	            } finally {
	                if (toChannel != null) {
	                    toChannel.close();
	                }
	            }
	        }
	    }
	}
}
