package com.woelfware.blumote;

/**
 * Simple exception class to use in the bootloader code
 * @author keusej
 *
 */
public class BslException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String oopsMsg;
	
	public BslException() {
		super();
		oopsMsg = "unknown";
	}
	
	public BslException(String msg) {
		super();
		oopsMsg = msg;
	}
	
	@Override
	public String getMessage() {
		return oopsMsg;
	}
}
