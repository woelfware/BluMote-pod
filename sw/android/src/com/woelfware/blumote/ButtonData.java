package com.woelfware.blumote;

/** 
 * 
 * This class will structure button data
 * The buttonId, buttonCategory and buttonName fields are write only
 * buttonData can be set after creation if needed
 * @author keusej
 *
 */
public class ButtonData {
	private int buttonId;
	private byte[] buttonData;
	private String buttonName;
	private String buttonCategory;
	
	// takes an activity and activity button and converts to a device and button
	public ButtonData(int buttonId, String buttonName, byte[] buttonData, String buttonCategory) {
		this.buttonId = buttonId;
		this.buttonName = buttonName;
		this.buttonData = buttonData;
		this.buttonCategory = buttonCategory;
	}
	
	int getButtonId() {
		return buttonId;
	}
	
	String getButtonName() {
		return buttonName;
	}
	
	byte[] getButtonData() {
		return buttonData;
	}		
	
	String getButtonCategory() {
		return buttonCategory;
	}
	
	void setButtonData(byte[] buttonData) {
		this.buttonData = buttonData;
	}
}
