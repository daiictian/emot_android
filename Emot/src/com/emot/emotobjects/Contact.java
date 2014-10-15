package com.emot.emotobjects;

public class Contact {
	private String name;
	private String jid;
	private String profileImgUrl;
	private String status;
	private byte[] avatar;
	
	public Contact(String name, String jid){
		this.setName(name);
		this.setJID(jid);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJID() {
		return jid;
	}

	public void setJID(String jid) {
		this.jid = jid;
	}

	public String getProfileImgUrl() {
		return profileImgUrl;
	}

	public void setProfileImgUrl(String profileImgUrl) {
		this.profileImgUrl = profileImgUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public byte[] getAvatar() {
		return avatar;
	}

	public void setAvatar(byte[] avatar) {
		this.avatar = avatar;
	}
}
