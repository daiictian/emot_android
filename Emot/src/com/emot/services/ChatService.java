package com.emot.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.VCard;

import org.jivesoftware.smackx.ping.packet.Ping;
import org.jivesoftware.smackx.ping.packet.Pong;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.xmlpull.v1.XmlPullParser;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.emot.common.ImageHelper;
import com.emot.common.TaskCompletedRunnable;
import com.emot.constants.PreferenceKeys;
import com.emot.constants.WebServiceConstants;
import com.emot.emotobjects.ConnectionQueue;
import com.emot.model.EmotApplication;
import com.emot.persistence.DBContract;
import com.emot.persistence.EmotDBHelper;

public class ChatService extends Service implements MessageListener{
	ReconnectionManager reconnectionManager;
	static {
	    try {
	        Class.forName("org.jivesoftware.smack.ReconnectionManager");
	    } catch (ClassNotFoundException ex) {
	        // problem loading reconnection manager
	    }
	}
	

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
	private boolean mStopOtherChat;
	ReconnectionManager reconnectionManager2;
	class IncomingHandler extends Handler{

		@Override
		public void handleMessage(android.os.Message pMessage) {
			// TODO Auto-generated method stub
			super.handleMessage(pMessage);
			Log.i(TAG, "receieved in service");
			Bundle data = pMessage.getData();
			mChatFriend = data.getString("chat_friend");
			String msg = (String)data.getString("chat");
			boolean createChat = (Boolean)data.getBoolean("create_chat");
			
			if(createChat){
				Log.i(TAG, "createChat is " +createChat);
				if(chat!=null){
					Log.i(TAG, " participant is  = "+chat.getParticipant());
				}
				current_chat  = connection.getChatManager();
				
				if(chat == null || !chat.getParticipant().equals(mChatFriend)){
					chat = current_chat.createChat(mChatFriend+"@emot-net", ChatService.this);
					current_chat.addChatListener(mChatManagerListener);
				}else{
				//	current_chat.addChatListener(mChatManagerListener);
					//chat.addMessageListener(mmlistener);
				}
			}
			boolean joinRoom = (Boolean)data.getBoolean("join_room");
			Log.i(TAG, "message receieved in service " +msg);
			Log.i(TAG, "chatFriend in service " +mChatFriend);
			try {
				Log.i(TAG, "chat is " +mChat);
				
				
				
				
				while(chat != null){
					Log.i(TAG, "while loop 22222222222222");
					//chat.addMessageListener(mmlistener);
					Log.i(TAG, "chat being sent is " +chat);
					Log.i(TAG, "chat participant ID " +chat.getParticipant());
					if(connection != null){
						Log.i(TAG, "connection authentication status " +connection.isAuthenticated());
					}
					chat.sendMessage(msg);
					break;
				}
				Log.i(TAG, "muc is " +muc);
				while(muc != null ){
					Log.i(TAG, "while loop 3333333333333333");
					if(joinRoom){
					//	muc.join("test5@emot-net");
					//	muc.addMessageListener(mmGrouplistener);
						//muc.join("test6@emot-net");
						//joinRoom = false;
					}

					if(msg != null){}
					//muc.sendMessage(msg);

					//if(msg != null)
					//	muc.sendMessage(msg);

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

	private ChatManagerListener mChatManagerListener = new ChatManagerListener() {
		
		@Override
		public void chatCreated(Chat arg0, boolean createdLocally) {
			if (!createdLocally){
				Log.i(TAG, "chat created !!!");
				//chat.addMessageListener(mmlistener);
			}
			
		}
	};


	//Fetch the packet and deliver to needy.
	private PacketListener chatPacketListener = new PacketListener() {

		@Override
		public void processPacket(Packet packet) {
			Log.i("XMPPClient", "process packet ...in Service");

			String s = packet.getFrom();
			Log.i("XMPPClient", " packet ...in Service is from " +s);
			Log.i(TAG, "packet is " +packet.toXML());
			//Presence p = (Presence)packet;
			//Log.i("XMPPClient", " presence ...in Service is " +p);
			//Message m = (Message)packet;
			//Log.i("XMPPClient", " packet ...in Service is " +m.getBody());
			//if(m != null){
				
			
				
		//	}
			
			
			
		}
	};
	SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date now = new Date();
	String strDate = sdfDate.format(now);
	final String dateTime[] = strDate.split(" ");
	String user = "";

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
				Log.i(TAG, "sendign message to activity " +msg);
				msg.setData(data);
				
				/*

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
					 */
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
	
	private ConnectionListener mConnectionListener = new ConnectionListener() {
		
		@Override
		public void reconnectionSuccessful() {
			try {
				Log.i(TAG, "reconnection successful");
				muc.join("test5@emot-net");
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		@Override
		public void reconnectionFailed(Exception arg0) {
			Log.i(TAG, "reconnection failed");
			
		}
		
		@Override
		public void reconnectingIn(int arg0) {
			Log.i(TAG, "reconnecting in " +arg0);
			
		}
		
		@Override
		public void connectionClosedOnError(Exception arg0) {
			Log.i(TAG, "connection closed on error");
			
			
		}
		
		@Override
		public void connectionClosed() {
			Log.i(TAG, "connection closed");
			
			
			
			
		}
	};



	@Override
	public IBinder onBind(Intent intent) {
		int requestCode = intent.getIntExtra("request_code", REQUEST_CHAT_MESSAGE);
		if(requestCode == REQUEST_CHAT_MESSAGE){
			return mMessenger.getBinder();
		}else if(requestCode == REQUEST_PROFILE_UPDATE){
			Log.i(TAG, "Binding the messenger...");
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
	private ChatManager current_chat;
	private Chat grpChat;
	private MultiUserChat muc;
	private Notification mBackgroundMessage;
	private NotificationManager notificationManager;
	PacketCollector collector;
	@Override
	public void onCreate() {
		super.onCreate();
		  
	

		SmackAndroid.init(ChatService.this);


		SmackConfiguration.setPacketReplyTimeout(60000);
		Log.i(TAG, "___________ON CREATE____________" + connection);
		if(connection!=null && connection.isAuthenticated()){
			return;
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					try {
						Thread.sleep(10000);
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.i(TAG, "Connection state = "+connection.isAuthenticated() + " socket closed? = "+connection.isSocketClosed());
				}
				
			}
		}).start();
		
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
		chat = null;
		current_chat = null;
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
					if(avatar!=null){
						cvs.put(DBContract.ContactsDBEntry.PROFILE_THUMB, avatar);
					}
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


		Thread connThread = new Thread(new Runnable() {


			@Override
			public void run() {
				
				AndroidConnectionConfiguration connConfig = new AndroidConnectionConfiguration(WebServiceConstants.CHAT_SERVER, WebServiceConstants.CHAT_PORT, WebServiceConstants.CHAT_DOMAIN);
				connConfig.setSASLAuthenticationEnabled(true);
				connConfig.setReconnectionAllowed(true);


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
				SmackConfiguration.setPacketReplyTimeout(5000);
				connection = new XMPPConnection(connConfig);
				try {
					connection.connect();
					
					connection.addPacketListener(new PingPacketListener(connection), new PingPacketFilter());
					ProviderManager.getInstance().addIQProvider("ping", "urn:xmpp:ping", new PingProvider());
					connection.addConnectionListener(mConnectionListener);
					Log.i(TAG, "[SettingsDialog] Connected to " + connection.getHost());
					// publishProgress("Connected to host " + HOST);
				} catch (XMPPException ex) {
					Log.e(TAG, "[SettingsDialog] Failed to connect to " + connection.getHost());
					Log.e(TAG, ex.toString());
					//publishProgress("Failed to connect to " + HOST);
					//xmppClient.setConnection(null);
				}

				
				connection.addPacketListener(chatPacketListener, null); 
				PacketFilter filter  = new AndFilter(new PacketTypeFilter(Ping.class));//= new AndFilter(new PacketTypeFilter(Message.class));
		        collector = connection.createPacketCollector(filter);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						while(true){
							Packet p = collector.nextResult(120*1000);
							
							if(p== null){
								Log.i(TAG, "Connection dead, restart the conn");
								connection.disconnect();
								try {
									connection.connect();
									connection.addPacketListener(new PingPacketListener(connection), new PingPacketFilter());
									ProviderManager.getInstance().addIQProvider("ping", "urn:xmpp:ping", new PingProvider());
									connection.addConnectionListener(mConnectionListener);
									connection.login(EmotApplication.getValue(PreferenceKeys.USER_MOBILE, ""),EmotApplication.getValue(PreferenceKeys.USER_PWD, ""));
									
								} catch (XMPPException e) {
									Log.i(TAG, "Exception while connecting again");
									e.printStackTrace();
								}
							}else{
								Log.i(TAG, "Connection active");
							}
						
						
					}
					}
				}).start();
				
				

				try {
					connection.login(EmotApplication.getValue(PreferenceKeys.USER_MOBILE, ""),EmotApplication.getValue(PreferenceKeys.USER_PWD, ""));
					Log.i(TAG, "Logged in as " + connection.getUser() + ". Authenticated : "+connection.isAuthenticated());
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						while(true){
						Ping ping = new Ping();
						
						ping.setTo(mChatFriend+"@emot-net");
						ping.setFrom(EmotApplication.getValue(PreferenceKeys.USER_MOBILE, "") +"@emot-net");
						if(connection.isAuthenticated()){
							Log.i(TAG,"Sending pingssss");
						connection.sendPacket((ping));
						try {
							Thread.sleep(60*1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						}
						
					}
					}
				}).start();
				
				
				Log.i(TAG, "current_chat is "+current_chat);
				//connection.getRoster().createGroup("myroom");


				
				Log.i(TAG, "chat object is " +chat);
			 muc = new MultiUserChat(connection, "myroom@conference.emot-net");
			 Log.i(TAG, "chat object is after multiuser chat " +chat);

				

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
							Log.i(TAG, "while loop 1111111111111111");
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
						


					//	muc.invite("test6@emot-net", "hey");
					//	muc.join("test5@emot-net", "hey");	

						//muc.addMessageListener(mmGrouplistener);
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
//										try {
//												//muc.join("test5@emot-net");
//											} catch (XMPPException e) {
//												// TODO Auto-generated catch block
//												e.printStackTrace();
//											}// TODO Auto-generated method stub
										
									}
								});
								
						//muc.join("test6@conference.emot-net");
						
						
						 
					} catch (XMPPException e) {
						//Log.i(TAG, "exception " + e.getMessage());
						//e.printStackTrace();
					}

					//muc.invite("test6@emot-net", "hey");
					//muc.join("test5@emot-net", "hey");	
					//muc.addMessageListener(mmGrouplistener);
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

				Collection<RosterEntry> rosters = roster.getEntries();
				Log.i(TAG, "Roster Size = "+ rosters.size());
				roster.setSubscriptionMode(SubscriptionMode.accept_all);
				roster.addRosterListener(rosterListener);
			}
		});
		connThread.start();
	}

	public void updatePresence(){
		Thread presenceThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try{
					Cursor cr = emotHistoryDB.getReadableDatabase().query(DBContract.ContactsDBEntry.TABLE_NAME, new String[] {DBContract.ContactsDBEntry.MOBILE_NUMBER} , DBContract.ContactsDBEntry.SUBSCRIBED+" = 0;", null, null, null, null, null);
					Log.i(TAG, "Contacts to subscribe "+cr.getCount());
					while (cr.moveToNext())
					{
						String mobile = cr.getString(cr.getColumnIndex(DBContract.ContactsDBEntry.MOBILE_NUMBER));

						//SHOWING MY PRESENCE
						Presence presence2 = new Presence(Presence.Type.subscribe);
						presence2.setTo(mobile+"@"+WebServiceConstants.CHAT_DOMAIN);
						connection.sendPacket(presence2);

						//ASKING FOR PRESENCE
						/*
			            Presence presence3 = new Presence(Presence.Type.subscribed);
			            presence3.setTo(mobile+"@"+WebServiceConstants.CHAT_DOMAIN);
			            connection.sendPacket(presence3);
						 */

						ContentValues cvs = new ContentValues();
						cvs.put(DBContract.ContactsDBEntry.SUBSCRIBED, true);
						emotHistoryDB.getWritableDatabase().update(DBContract.ContactsDBEntry.TABLE_NAME, cvs, DBContract.ContactsDBEntry.MOBILE_NUMBER+" = '"+mobile+"'", null);
						Log.i(TAG, "Presence subscribe "+mobile);
					}
					cr.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}});
		presenceThread.start();
	}

	public void updateStatus(String status, TaskCompletedRunnable handler){
		new UpdateStatusTask(status, handler).execute();
	}

	public void updateAvatar(Bitmap bmp, TaskCompletedRunnable handler){
		//EmotApplication.configure(ProviderManager.getInstance());
		new UpdateAvatarTask(bmp, handler).execute();
	}
	
	public void updateAvatar(String file, TaskCompletedRunnable handler){
		//EmotApplication.configure(ProviderManager.getInstance());
		new UpdateAvatarTask(file, handler).execute();
	}

	public class UpdateStatusTask extends AsyncTask<Void, Void, Boolean>{
		private String status;
		private TaskCompletedRunnable handler;
		public UpdateStatusTask(String status, TaskCompletedRunnable handler){
			this.status = status;
			this.handler = handler;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try{
				Presence presence = new Presence(Presence.Type.available, status, 1, Mode.available);
				connection.sendPacket(presence);
				EmotApplication.setValue(PreferenceKeys.USER_STATUS, status);
				return true;
			}catch(Exception e){
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				handler.onTaskComplete("success");
			}else{
				handler.onTaskComplete("failed");
			}
		}

	}


	@Override
	public void processMessage(Chat arg0, final Message message) {
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


		}
		
		
		
	}
	
	class Ping extends IQ implements IQProvider{

		@Override
		public IQ parseIQ(XmlPullParser arg0) throws Exception {
			
			return null;
		}

		@Override
		public String getChildElementXML() {
			StringBuffer sb = new StringBuffer();
			sb.append("<ping xmlns='urn:xmpp:ping'/>");
			return sb.toString();
		}
		
		
	}
	
	class PingProvider implements IQProvider{

		@Override
		public IQ parseIQ(XmlPullParser parser) throws Exception {
			
			return new Ping();
		}
		
	}
	
	class PingPacketFilter implements PacketFilter{

		@Override
		public boolean accept(Packet packet) {
			if(packet instanceof Ping){
				Log.i(TAG, "Ping request received ");
				return true;
			}else{
				return false;
			}
			
			
		}
		}
	
	class Pong extends IQ{
		
		public Pong(){
			
			this.setType(Type.ERROR);
		}

		@Override
		public String getChildElementXML() {
			StringBuffer sb = new StringBuffer();
			sb.append("<ping xmlns= 'urn:xmpp:ping'/>");
			sb.append("<error type = 'cancel'>");
			sb.append("<service-unavailable xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>");
			sb.append("</error>");
			return sb.toString();
		}
		
	}
	class PingPacketListener implements PacketListener{
		private XMPPConnection conn;
		public PingPacketListener(final XMPPConnection pConnection){
			conn = pConnection;
		}
		@Override
		public void processPacket(Packet packet) {
			if(packet == null){
				Log.i(TAG, "Ping packet is null");
				return;
			}
		
			Pong pong = new Pong();
			String from = packet.getFrom();
			String to = packet.getTo();
			Log.i(TAG, "From " + from + "To " + to);
			pong.setFrom(to);
			pong.setTo(from);
			Log.i(TAG, "Sendign Pong packet");
//			new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					try {
//						connection.disconnect();
//						connection.connect();
//						connection.addPacketListener(new PingPacketListener(connection), new PingPacketFilter());
//						ProviderManager.getInstance().addIQProvider("ping", "urn:xmpp:ping", new PingProvider());
//						connection.addConnectionListener(mConnectionListener);
//						connection.login(EmotApplication.getValue(PreferenceKeys.USER_MOBILE, ""),EmotApplication.getValue(PreferenceKeys.USER_PWD, ""));
//						
//					} catch (XMPPException e) {
//						Log.i(TAG,"Exception while connecting again ");
//						e.printStackTrace();
//					}
//					
//				}
//			}).start();
			
			conn.sendPacket(pong);
			
		}
		
		
	}
	
	public class UpdateAvatarTask extends AsyncTask<Void, Void, Boolean>{

		private Bitmap bmp;
		private String file;
		private TaskCompletedRunnable handler;

		public UpdateAvatarTask(Bitmap bmp, TaskCompletedRunnable handler){
			this.bmp = bmp;
			this.handler = handler;
		}
		
		public UpdateAvatarTask(String file, TaskCompletedRunnable handler){
			this.file = file;
			this.handler = handler;
		}

		

		@Override
		protected Boolean doInBackground(Void... params) {
			SmackAndroid.init(EmotApplication.getAppContext());
			//ProviderManager.getInstance().addIQProvider("vCard","vcard-temp", new VCardProvider());
			EmotApplication.configure(ProviderManager.getInstance());
			VCard vCard = new VCard();
			Log.i(TAG, "file = "+file);
			if(file!=null){
				Log.i(TAG, "avatar file found");
				try{
					bmp = ImageHelper.shrinkBitmap(file, 50, 50);
				}catch(Exception e){
					e.printStackTrace();
					return false;
				}
			}
			bmp = Bitmap.createScaledBitmap(bmp, 120, 120, false);
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				Log.i(TAG, "size of bitmap = "+ImageHelper.sizeOf(bmp));
				byte[] bytes = stream.toByteArray();
				String encodedImage = StringUtils.encodeBase64(bytes);
				vCard.setAvatar(bytes);
				//vCard.setEncodedImage(encodedImage);
//				vCard.setField("PHOTO", 
//						"<TYPE>image/jpeg</TYPE><BINVAL>"
//								+ encodedImage + 
//								"</BINVAL>", 
//								true);
				vCard.save(connection);
				EmotApplication.setValue(PreferenceKeys.USER_AVATAR, encodedImage);
				Log.i(TAG, "Setting preference value ...");
			}  catch (XMPPException e) {
				Log.i(TAG, "XMPP EXCEPTION  ----------- ");
				e.printStackTrace();
				return false;
			}	catch(Exception e){
				Log.i(TAG, " EXCEPTION  ------ ");
				e.printStackTrace();
				return false;
			}
			
			
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				handler.onTaskComplete("success");
			}else{
				handler.onTaskComplete("failed");
			}
		}

	}

}
