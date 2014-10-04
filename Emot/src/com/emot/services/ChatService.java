package com.emot.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.VCardProvider;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.emot.constants.PreferenceKeys;
import com.emot.constants.WebServiceConstants;
import com.emot.emotobjects.ConnectionQueue;
import com.emot.model.EmotApplication;
import com.emot.persistence.DBContract;
import com.emot.persistence.EmotDBHelper;
import com.emot.screens.R;

public class ChatService extends Service{
	
	private static XMPPConnection connection;
	private static final String TAG = "ChatService";
	public static final int MESSAGE_TYPE_TEXT = 2;
	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	private Messenger mChatPacketSender;
	public static BlockingQueue<XMPPConnection> mConnectionQueue = new ArrayBlockingQueue<XMPPConnection>(1);
	private Roster roster;
	private RosterListener rosterListener;
	private final IBinder profileBinder = new ProfileBinder();
	public static int REQUEST_CHAT_MESSAGE = 1;
	public static int REQUEST_PROFILE_UPDATE = 2;
	private String mChatFriend;
	class IncomingHandler extends Handler{

		@Override
		public void handleMessage(android.os.Message pMessage) {
			// TODO Auto-generated method stub
			super.handleMessage(pMessage);
			Log.i(TAG, "receieved in service");
			Bundle data = pMessage.getData();
			mChatFriend = data.getString("chat_friend");
			String msg = (String)data.getString("chat");
			boolean joinRoom = (Boolean)data.getBoolean("join_room");
			Log.i(TAG, "message receieved in service " +msg);
			Log.i(TAG, "chatFriend in service " +mChatFriend);
			try {
				Log.i(TAG, "chat is " +mChat);
				while(chat != null){
					chat.sendMessage(msg);
				break;
				}
				Log.i(TAG, "muc is " +muc);
				while(muc != null ){
					if(joinRoom){
						muc.join("test5@emot-net");
						muc.addMessageListener(mmGrouplistener);
						//muc.join("test6@emot-net");
						//joinRoom = false;
					}
					if(msg != null)
					muc.sendMessage(msg);
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
	
	public class ProfileBinder extends Binder{
		public ChatService getService() {
            return ChatService.this;
        }
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}




	//Fetch the packet and deliver to needy.
	private PacketListener chatPacketListener = new PacketListener() {
		
		@Override
		public void processPacket(Packet packet) {
			Log.i("XMPPClient", "process packet ...in Service");
			
			String s = packet.getFrom();
			Log.i("XMPPClient", " packet ...in Service is from " +s);
			//Presence p = (Presence)packet;
			//Log.i("XMPPClient", " presence ...in Service is " +p);
			Message m = (Message)packet;
			Log.i("XMPPClient", " packet ...in Service is " +m.getBody());
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
		String user = "";
		@Override
		public void processMessage(Chat chat, final Message message) {
			if(message.getBody() != null){
			Log.d("Incoming message", message.getBody());
			user = message.getFrom();
			Bundle data = new Bundle();
			data.putString("user", user);
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
					emotHistoryDB.insertChat(mChatFriend, message.getBody(), dateTime[0], dateTime[1], "left");

				}
			}).start(); 


		}}
	};
	
	private MessageListener mmGlistener = new MessageListener() {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date now = new Date();
		String strDate = sdfDate.format(now);
		final String dateTime[] = strDate.split(" ");
		String user = "";
		@Override
		public void processMessage(Chat chat, final Message message) {
			Log.d("Incoming message", message.getBody());
			user = message.getFrom();
			Bundle data = new Bundle();
			data.putString("user", user);
			data.putCharSequence("chat", message.getBody());
			data.putCharSequence("time", dateTime[1]);
			android.os.Message msg = android.os.Message.obtain(null, MESSAGE_TYPE_TEXT);
			Log.i("XMPPClient", "sendign message to activity " +message.getBody() );
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
					emotHistoryDB.insertChat(mChatFriend, message.getBody(), dateTime[0], dateTime[1], "left");

				}
			}).start(); 


		}
	};

	
	private PacketListener mmGrouplistener = new PacketListener() {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date now = new Date();
		String strDate = sdfDate.format(now);
		final String dateTime[] = strDate.split(" ");
		String user = "";
		@Override
		public void processPacket(Packet packet) {
			Log.i(TAG, "In process packet");
			if (packet instanceof Message){
				final Message message = (Message)packet;
				Log.d("Incoming message", message.getBody());
				user = message.getFrom();
				Bundle data = new Bundle();
				if(user != null && !user.equals("myroom@conference.emot-net/myroom")){
				data.putString("user", user);
				data.putCharSequence("chat", message.getBody());
				data.putCharSequence("time", dateTime[1]);
				android.os.Message msg = android.os.Message.obtain(null, MESSAGE_TYPE_TEXT);
				Log.i("XMPPClient", "sendign message to activity " +msg);
				msg.setData(data);
				try {
					notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
					mBackgroundMessage = new Notification.Builder(ChatService.this).
							setContentTitle("TESTUSER").
							setContentText(message.getBody()).
							setSmallIcon(R.drawable.ic_launcher).build();
					notificationManager.notify(0, mBackgroundMessage);
					if(mChatPacketSender != null){
					mChatPacketSender.send(msg);
					 
					}
				} catch (RemoteException e) {
					Log.i("XMPPClient", "RemoteException occured " +e.getMessage());
					e.printStackTrace();
				}
				new Thread(new Runnable() {

					@Override
					public void run() {
						emotHistoryDB.insertGroupChat(user,mChatFriend, message.getBody(), dateTime[0], dateTime[1], "left");

					}
				}).start(); 
			}
			}
			
		}
	};
	
	

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "Binding the messenger...");
		int requestCode = intent.getIntExtra("request_code", REQUEST_CHAT_MESSAGE);
		if(requestCode == REQUEST_CHAT_MESSAGE){
			return mMessenger.getBinder();
		}else if(requestCode == REQUEST_PROFILE_UPDATE){
			return profileBinder;
		}else{
			return null;
		}
	}
	
	

	
	private boolean stop;
	private ChatManager mCurrentChat;
	private Chat mChat;
	//private BlockingQueue<XMPPConnection> mConnectionQueue;
	private BlockingQueue<Chat> chatQueue = new ArrayBlockingQueue<Chat>(1);
	private Chat chat;
	private Chat grpChat;
	private MultiUserChat muc;
	private Notification mBackgroundMessage;
	private NotificationManager notificationManager;
	@Override
	public void onCreate() {
		super.onCreate();
		  
		
		Log.i(TAG, "___________ON CREATE____________" + connection);
		if(connection!=null && connection.isAuthenticated()){
			return;
		}
		Log.i(TAG, "___________INITIALIZING____________");
//		while(mServerConnection == null){
//		mServerConnection = EmotApplication.getConnection();
//		Log.i(TAG, "mServerConnection is "+mServerConnection);
//	if(mServerConnection != null){
	//		mCurrentChat = mServerConnection.getChatManager();
	//	Log.i(TAG, "current_chat is "+mCurrentChat);
	//	mChat = mCurrentChat.createChat("test6@emot-net", "test6@emot-net",mmlistener);
		//	}
		//}
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
		Log.i(TAG, "On create of chat service !!!!!!!!!!!!!!!!!!!");
		rosterListener = new RosterListener() {
			// Ignored events public void entriesAdded(Collection<String> addresses) {}
			public void entriesDeleted(Collection<String> addresses) {}
			public void entriesUpdated(Collection<String> addresses) {}
			public void presenceChanged(Presence presence) {
				try {
					Log.i(TAG, "Presence Type = "+presence.getType() + " status = "+presence.getStatus());
					Log.i(TAG, "Presence changed: " + presence.getFrom() + "  " + presence + " pres = "+roster.getPresence(presence.getFrom()));
					String mobile = presence.getFrom().split("@"+WebServiceConstants.CHAT_DOMAIN)[0];
					ContentValues cvs = new ContentValues();
					cvs.put(DBContract.ContactsDBEntry.MOBILE_NUMBER, mobile);
					cvs.put(DBContract.ContactsDBEntry.CURRENT_STATUS, presence.getStatus());
					EmotApplication.configure(ProviderManager.getInstance());
					VCard vCard = new VCard();
					Log.i(TAG, "B4 try catch");
				
					vCard.load(connection, mobile+"@"+WebServiceConstants.CHAT_DOMAIN);
					byte[] avatar = vCard.getAvatar();
					cvs.put(DBContract.ContactsDBEntry.PROFILE_THUMB, avatar);
					cvs.put(DBContract.ContactsDBEntry.LAST_SEEN, EmotApplication.getDateTime());
					
					Log.i(TAG, "Thumb avatar = "+avatar);
					Log.i(TAG, "Nick name = " + vCard.getNickName() + " Name = " + mobile);
					emotHistoryDB.getWritableDatabase().update(DBContract.ContactsDBEntry.TABLE_NAME, cvs, DBContract.ContactsDBEntry.MOBILE_NUMBER+" = '"+mobile+"'", null);
				} catch (XMPPException e) {
					Log.i(TAG, "Error getting profile pic ....");
					e.printStackTrace();
				} catch (Exception e){
					Log.i(TAG, "Error exception ....");
					e.printStackTrace();
				}
				
			}
			@Override
			public void entriesAdded(Collection<String> arg0) {
				
			}
		};
		
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
				
				//connection.addPacketListener(chatPacketListener, null);
				try {
					connection.login("test5","1234");
					Log.i(TAG, "Logged in as " + connection.getUser() + ". Authenticated : "+connection.isAuthenticated());
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				ChatManager current_chat  = connection.getChatManager();
				SmackAndroid smack = SmackAndroid.init(getApplicationContext());
				Log.i(TAG, "current_chat is "+current_chat);
				//connection.getRoster().createGroup("myroom");
				chat = current_chat.createChat(mChatFriend+"@emot-net", mChatFriend+"@emot-net", mmlistener);
			 muc = new MultiUserChat(connection, "myroom@conference.emot-net");
				
				Form form = null;
					Log.i(TAG, "creating multi user chat " +muc);
					try {
						if(muc.getRoom() == null){
						muc.create("myroom");
						Log.i(TAG, "creating multi user chat2 " +muc);
						Log.i(TAG, "creating multi user chat @" +muc);
						form = muc.getConfigurationForm();
						Form submitForm = form.createAnswerForm(); 
						Iterator<FormField> fields = form.getFields();
						while(fields.hasNext()){
							FormField field = (FormField) fields.next();
							if(!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null){
								submitForm.setDefaultAnswer(field.getVariable()); 
							}
						}
						List<String> owners = new ArrayList<String>(); 
						Log.i(TAG, "creating multi user chat 3" +muc);
						owners.add("test5@emot-net");
						//owners.add("test6@emot-net");
			//			submitForm.setAnswer("muc#roomconfig_roomowners", owners);
						Log.i(TAG, "creating multi user chat 4" +muc);
						try {
							muc.sendConfigurationForm(submitForm);
							
							
						} catch (XMPPException e) {
							Log.i(TAG, "Exception " +e.getMessage());
							e.printStackTrace();
						}
						}
						
						muc.invite("test6@emot-net", "hey");
						muc.join("test5@emot-net", "hey");	
						muc.addMessageListener(mmGrouplistener);
						//boolean supports = MultiUserChat.isServiceEnabled(connection, "test6@emot-net");
						//		Log.i(TAG, "test6 supports MUC? " +supports);
								//muc.join("test6@emot-net");
								
								MultiUserChat.addInvitationListener(connection, new InvitationListener() {
									

									@Override
									public void invitationReceived(
											Connection arg0, String arg1,
											String arg2, String arg3,
											String arg4, Message arg5) {
										Log.i(TAG, "Invitation received");
										try {
												muc.join("test5@emot-net");
											} catch (XMPPException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}// TODO Auto-generated method stub
										
									}
								});
								
						//muc.join("test6@conference.emot-net");
						
						
						 
					} catch (XMPPException e) {
						Log.i(TAG, "exception " + e.getMessage());
						e.printStackTrace();
					}
					
					
					
					Log.i(TAG, "Group Chat is " +muc.getRoom());
					
					
				//chat = muc.createPrivateChat("myroom@conference.emot-net",mmGlistener);
				
				//Roster Listener
				roster = connection.getRoster();
//				try {
//					roster.createEntry("9342464980@emot-net", "abhi", null);
//				} catch (XMPPException e1) {
//					Log.i(TAG, "Create entry error");
//					e1.printStackTrace();
//				}
				Collection<RosterEntry> rosters = roster.getEntries();
				Log.i(TAG, "Roster Size = "+ rosters.size());
				roster.setSubscriptionMode(SubscriptionMode.accept_all);
				roster.addRosterListener(rosterListener);
				
//				  Presence presence = roster.getPresence(emotter.getString("mobile")+"@"+WebServiceConstants.CHAT_DOMAIN);
//                Log.i(TAG, "Presence name = " + presence.getType().name());
//                Log.i(TAG, "Status = " + presence.getStatus());
                
//                Presence presence1 = new Presence(Presence.Type.subscribed);
//                presence1.setTo(emotter.getString("mobile")+"@"+WebServiceConstants.CHAT_DOMAIN);
//                EmotApplication.getConnection().sendPacket(presence1);
//                
				
				Cursor cr = emotHistoryDB.getReadableDatabase().query(DBContract.ContactsDBEntry.TABLE_NAME, new String[] {DBContract.ContactsDBEntry.MOBILE_NUMBER} , DBContract.ContactsDBEntry.SUBSCRIBED+" = 0;", null, null, null, null, null);
				while (cr.moveToNext())
				{
					String mobile = cr.getString(cr.getColumnIndex(DBContract.ContactsDBEntry.MOBILE_NUMBER));
					Presence presence2 = new Presence(Presence.Type.subscribe);
	                presence2.setTo(mobile+"@"+WebServiceConstants.CHAT_DOMAIN);
	                connection.sendPacket(presence2);
					ContentValues cvs = new ContentValues();
					cvs.put(DBContract.ContactsDBEntry.SUBSCRIBED, true);
					emotHistoryDB.getWritableDatabase().update(DBContract.ContactsDBEntry.TABLE_NAME, cvs, DBContract.ContactsDBEntry.MOBILE_NUMBER+" = '"+mobile+"'", null);
					Log.i(TAG, "Presence subscribe "+mobile);
				}
				cr.close();
				
                
//                cvs.put(DBContract.ContactsDBEntry.CURRENT_STATUS, presence.getType().name());
                
                
                
//				EmotApplication.configure(ProviderManager.getInstance());
//				VCard vCard = new VCard();
//				try {
//					vCard.load(EmotApplication.getConnection(), emotter.getString("mobile")+"@"+WebServiceConstants.CHAT_DOMAIN);
//					byte[] avatar = vCard.getAvatar();
//					cvs.put(DBContract.ContactsDBEntry.PROFILE_THUMB, avatar);
//					Log.i(TAG, "Thumb avatar = "+avatar);
//				} catch (XMPPException e) {
//					Log.i(TAG, "Error getting profile pic ....");
//					e.printStackTrace();
//				}
//				Log.i(TAG, "Nick name = " + vCard.getNickName() + " Name = " + emotter.getString("mobile"));
			}
		});
		connThread.start();
			}
	
	public void updateStatus(String status){
		Presence presence = new Presence(Presence.Type.available, status, 1, Mode.available);
		connection.sendPacket(presence);
		EmotApplication.setValue(PreferenceKeys.USER_STATUS, status);
	}
	
	public void updateAvatar(Bitmap bmp){
		//EmotApplication.configure(ProviderManager.getInstance());
		ProviderManager.getInstance().addIQProvider("vCard","vcard-temp", new VCardProvider());
		VCard vCard = new VCard();
		try {
			//Bitmap bmp = BitmapFactory.decodeResource(EmotApplication.getAppContext().getResources(), R.drawable.asin);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] bytes = stream.toByteArray();
            String encodedImage = StringUtils.encodeBase64(bytes);
            vCard.setAvatar(bytes, encodedImage);
          //  vCard.setEncodedImage(encodedImage);
            vCard.setField("PHOTO", 
            		"<TYPE>image/jpg</TYPE><BINVAL>"
                    + encodedImage + 
                    "</BINVAL>", 
                    true);
            vCard.save(connection);
            EmotApplication.setValue(PreferenceKeys.USER_AVATAR, encodedImage);
            Log.i(TAG, "Setting preference value ...");
        }  catch (XMPPException e) {
        	Log.i(TAG, "XMPP EXCEPTION  ----------- ");
			e.printStackTrace();
		}	catch(Exception e){
			Log.i(TAG, " EXCEPTION  ------ ");
			e.printStackTrace();
		}
	}
	
}
