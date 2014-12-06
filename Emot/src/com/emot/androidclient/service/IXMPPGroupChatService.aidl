package com.emot.androidclient.service;
import java.util.List;
import com.emot.emotobjects.Contact;
interface IXMPPGroupChatService {
	String sendGroupMessage(String user, String message, String tag);
	boolean isAuthenticated();
	void clearNotifications(String Jid);
	void createGroup(String grpName,inout List<Contact> members);
	void joinGroup(String grpName, boolean isCreateGroup, long date);
}