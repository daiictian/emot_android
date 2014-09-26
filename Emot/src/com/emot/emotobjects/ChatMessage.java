package com.emot.emotobjects;

public class ChatMessage {
	
	private String mMessage;
	private boolean right;
	private String mDate;
	private String mTime;
	
	public ChatMessage(final String pMessage, final String pTime, final boolean pRight){
		this.mMessage = pMessage;
		this.right = pRight;
		this.mTime = pTime;
		
	}

	public String getmMessage() {
		return mMessage;
	}

	

	public boolean isRight() {
		return right;
	}

	public String getmDate() {
		return mDate;
	}

	public void setmDate(String mDate) {
		this.mDate = mDate;
	}

	public String getmTime() {
		return mTime;
	}

	public void setmTime(String mTime) {
		this.mTime = mTime;
	}
	
	

	
	
}
