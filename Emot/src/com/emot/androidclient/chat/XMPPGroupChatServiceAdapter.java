package com.emot.androidclient.chat;

import java.util.ArrayList;
import java.util.List;

import android.os.RemoteException;
import android.util.Log;

import com.emot.androidclient.service.IXMPPChatService;
import com.emot.androidclient.service.IXMPPGroupChatService;
import com.emot.emotobjects.Contact;

public class XMPPGroupChatServiceAdapter {

	private static final String TAG = "yaxim.XMPPCSAdapter";
	private IXMPPGroupChatService xmppGrpServiceStub;
	private String grpJabberID;
	private boolean createGroup;

	public XMPPGroupChatServiceAdapter(IXMPPGroupChatService xmppServiceStub,
			String jabberID) {
		Log.i(TAG, "New XMPPChatServiceAdapter construced");
		this.xmppGrpServiceStub = xmppServiceStub;
		this.grpJabberID = jabberID;
		
	}
	
	public void createGroup(final String grpName, final List<Contact> members){
		Log.i(TAG, "Called createGroup(): " + grpJabberID + ": " + grpName);
		try {
			xmppGrpServiceStub.createGroup(grpName, members);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void joinExistingGroup(final String grpName, final boolean isCreateGrp){
		
		try {
			xmppGrpServiceStub.joinGroup(grpName, isCreateGrp);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public String sendMessage(String user, String message) {
		String piD = "";
		try {
			Log.i(TAG, "Called sendMessage(): " + grpJabberID + ": " + message);
			 piD =  xmppGrpServiceStub.sendGroupMessage(user, message);
		} catch (RemoteException e) {
			Log.e(TAG, "caught RemoteException: " + e.getMessage());
		}
		
		return piD;
	}
	
	public boolean isServiceAuthenticated() {
		try {
			return xmppGrpServiceStub.isAuthenticated();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void clearNotifications(String Jid) {
		try {
			xmppGrpServiceStub.clearNotifications(Jid);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
