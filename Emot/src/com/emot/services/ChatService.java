package com.emot.services;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import com.emot.emotobjects.ChatMessage;
import com.emot.emotobjects.ConnectionQueue;
import com.emot.model.EmotApplication;
import com.emot.persistence.EmotDBHelper;
import com.emot.persistence.EmotHistoryHelper;
import com.emot.screens.ChatScreen;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class ChatService extends Service{
	
	private XMPPConnection connection;
	private static final String TAG = "ChatService";
	public static final int MESSAGE_TYPE_TEXT = 2;
	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	private Messenger mChatPacketSender;
	public static BlockingQueue<XMPPConnection> mConnectionQueue = new ArrayBlockingQueue<XMPPConnection>(1);
	class IncomingHandler extends Handler{

		@Override
		public void handleMessage(android.os.Message pMessage) {
			// TODO Auto-generated method stub
			super.handleMessage(pMessage);
			Log.i(TAG, "receieved in service");
			Bundle data = pMessage.getData();
			String msg = (String)data.getString("chat");
			Log.i(TAG, "message receieved in service " +msg);
			try {
				Log.i(TAG, "chat is " +mChat);
				while(chat != null){
					chat.sendMessage(msg);
				break;
				}
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mChatPacketSender = pMessage.replyTo;
			Log.i(TAG, "mChatPacketSender is " + mChatPacketSender);
		}
		
		
		
		
	}
	
	//Fetch the packet and deliver to needy.
	private PacketListener chatPacketListener = new PacketListener() {
		
		@Override
		public void processPacket(Packet packet) {
			Log.i("XMPPClient", "process packet ...in Service");
			Log.i("XMPPClient", " packet ...in Service is " );
			String s = packet.getFrom();
			Log.i("XMPPClient", " packet ...in Service is " +s);
			//Presence p = (Presence)packet;
			//Log.i("XMPPClient", " presence ...in Service is " +p);
			Message m = (Message)packet;
			Log.i("XMPPClient", " packet ...in Service is " +m);
			if(m != null){
				
			
				
			}
			
			
			
		}
	};
	
	EmotDBHelper emotHistoryDB = EmotDBHelper.getInstance(ChatService.this);
	private MessageListener mmlistener = new MessageListener() {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date now = new Date();
		String strDate = sdfDate.format(now);
		final String dateTime[] = strDate.split(" ");
		@Override
		public void processMessage(Chat chat, final Message message) {
			Log.d("Incoming message", message.getBody());
			Bundle data = new Bundle();
			
			data.putCharSequence("chat", message.getBody());
			data.putCharSequence("time", dateTime[1]);
			android.os.Message msg = android.os.Message.obtain(null, MESSAGE_TYPE_TEXT);
			Log.i("XMPPClient", "sendign message to activity ");
			msg.setData(data);
			try {
				mChatPacketSender.send(msg);
			} catch (RemoteException e) {
				Log.i("XMPPClient", "RemoteException occured " +e.getMessage());
				e.printStackTrace();
			}
			new Thread(new Runnable() {

				@Override
				public void run() {
					emotHistoryDB.insertChat("test6", message.getBody(), dateTime[0], dateTime[1], "left");

				}
			}).start(); 


		}
	};
	
	

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "Binding the messenger...");

        return mMessenger.getBinder();
		
	}
	
	

	
	private boolean stop;
	private ChatManager mCurrentChat;
	private Chat mChat;
	//private BlockingQueue<XMPPConnection> mConnectionQueue;
	private BlockingQueue<Chat> chatQueue = new ArrayBlockingQueue<Chat>(1);
	private Chat chat;

	@Override
	public void onCreate() {
		super.onCreate();
//		while(mServerConnection == null){
//		mServerConnection = EmotApplication.getConnection();
//		Log.i(TAG, "mServerConnection is "+mServerConnection);
//		if(mServerConnection != null){
//			mCurrentChat = mServerConnection.getChatManager();
//			Log.i(TAG, "current_chat is "+mCurrentChat);
//			mChat = mCurrentChat.createChat("test6@emot-net", "test6@emot-net",mmlistener);
//			}
//		}
		//mConnectionQueue = EmotApplication.mConnectionQueue;
		
		/*
		Log.i(TAG, "before connection retreived " +connection);
		try {
			Log.i(TAG, "connection queue size " +EmotApplication.mConnectionQueue.size());
			
			connection = EmotApplication.mConnectionQueue.take();
			
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			Log.i(TAG, "Error in qeue take ...");
			e1.printStackTrace();
		}
		Log.i(TAG, "after connection retreived " +connection);
		*/
		
		//mCurrentChat = connection.getChatManager();
		//mChat = mCurrentChat.createChat("test6@emot-net", "test6@emot-net",mmlistener);
		Thread connectionThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
					//mConnectionQueue = EmotApplication.mConnectionQueue;
					Log.i(TAG, "b4 connection retreived " +connection + " ");
					while(true){
						connection = ConnectionQueue.get();
						Log.i(TAG, "after connection retreived " +connection);
					}
					
					
					//mCurrentChat = connection.getChatManager();
					//mChat = mCurrentChat.createChat("test6@emot-net", "test6@emot-net",mmlistener);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Log.i(TAG, "Queue exception ...");
					e.printStackTrace();
				}
				
			}
		});
		connectionThread.setName("Connection Thread");
		//connectionThread.start();
		
		
		final int portInt = 5222;
		Thread connThread = new Thread(new Runnable() {
			
			
			@Override
			public void run() {
				ConnectionConfiguration connConfig = new ConnectionConfiguration("ec2-54-85-148-36.compute-1.amazonaws.com", portInt,"emot-net");
				connConfig.setSASLAuthenticationEnabled(true);
				connConfig.setSecurityMode(SecurityMode.enabled);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					connConfig.setTruststoreType("AndroidCAStore");
					connConfig.setTruststorePassword(null);
					connConfig.setTruststorePath(null);
					Log.i(TAG, "[XmppConnectionTask] Build Icecream");

				} else {
					connConfig.setTruststoreType("BKS");
					String path = System.getProperty("javax.net.ssl.trustStore");
					if (path == null)
						path = System.getProperty("java.home") + File.separator + "etc"
								+ File.separator + "security" + File.separator
								+ "cacerts.bks";
					connConfig.setTruststorePath(path);
					Log.i(TAG, "[XmppConnectionTask] Build less than Icecream ");

				}
				connConfig.setDebuggerEnabled(true);
				XMPPConnection.DEBUG_ENABLED = true;
				connection = new XMPPConnection(connConfig);
				try {
					connection.connect();
					Log.i(TAG, "[SettingsDialog] Connected to " + connection.getHost());
					// publishProgress("Connected to host " + HOST);
				} catch (XMPPException ex) {
					Log.e(TAG, "[SettingsDialog] Failed to connect to " + connection.getHost());
					Log.e(TAG, ex.toString());
					//publishProgress("Failed to connect to " + HOST);
					//xmppClient.setConnection(null);
				}
				
				connection.addPacketListener(chatPacketListener, null);
				try {
					connection.login("test5","1234");
					Log.i(TAG, "Logged in as " + connection.getUser() + ". Authenticated : "+connection.isAuthenticated());
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				ChatManager current_chat  = connection.getChatManager();
				Log.i(TAG, "current_chat is "+current_chat);
				chat = current_chat.createChat("test6@emot-net", "test6@emot-net",mmlistener);
			
				//Log.i(TAG, "chat in thread is "+chat);
				
			}
		});
		connThread.start();
			}
	
	
	
	
}
