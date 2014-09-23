package com.emot.emotobjects;

public class ChatMessage {
	
	private String mMessage;
	private boolean right;
	
	public ChatMessage(final String pMessage, final boolean pRight){
		this.mMessage = pMessage;
		this.right = pRight;
		
	}

	public String getmMessage() {
		return mMessage;
	}

	

	public boolean isRight() {
		return right;
	}

	
	
}
