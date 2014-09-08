package com.emot.screens;


import java.io.File;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.Log;


public class ChatScreen extends Activity {
	
	private static String TAG = "CHATSCREEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);
    }

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
			super.onResume();
			SmackAndroid.init(this);
			Thread thr = new Thread(new Runnable() {
				
			@Override
			public void run() {
					//ConnectionConfiguration connConfig = new ConnectionConfiguration("192.168.0.101", 5222, "vishals-macbook-pro.local");
					ConnectionConfiguration connConfig = new ConnectionConfiguration("192.168.0.101", 5222);
					
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
						connConfig.setTruststoreType("AndroidCAStore");
						connConfig.setTruststorePassword(null);
						connConfig.setTruststorePath(null);
					} else {
						connConfig.setTruststoreType("BKS");
					    String path = System.getProperty("javax.net.ssl.trustStore");
					    if (path == null)
					        path = System.getProperty("java.home") + File.separator + "etc"
					            + File.separator + "security" + File.separator
					            + "cacerts.bks";
					    connConfig.setTruststorePath(path);
					}
					
					//connConfig.setCompressionEnabled(true);
					connConfig.setSASLAuthenticationEnabled(false);
					connConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
		    		final XMPPConnection connection = new XMPPConnection(connConfig);
			    		
			
				try {
					connection.connect();
					SASLAuthentication.supportSASLMechanism("PLAIN", 0);
					Log.d(TAG, "Connected: " + connection.isConnected());
					connection.login("test1@vishals-macbook-pro.local", "1234");
					Log.i(TAG, "Connection successfull");	
				}catch (XMPPException e) {
					Log.i("CONNECTION", "XMPP error");
					e.printStackTrace();
				}catch (NetworkOnMainThreadException e) {
					Log.i("CONNECTION", "Network error");
					e.printStackTrace();
				}catch (Exception e) {
					Log.i("CONNECTION", "Conneciton error");
					e.printStackTrace();
				}
				
				//Log.i("CONNECTION", "Successfully Connected");
				
			}
		});
		thr.start();
		
		
Thread thr2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				ConnectionConfiguration connConfig =
			            new ConnectionConfiguration("192.168.0.101", 5222);
			    final XMPPConnection connection = new XMPPConnection(connConfig);
				try {
					try {
						connection.connect();
						connection.login("test2", "5678");
						
						ChatManager chatmanager = connection.getChatManager();
						
						 final Chat newChat = chatmanager.createChat("test1@192.168.0.101", new MessageListener() {
							
							@Override
							public void processMessage(Chat chat, Message message) {
								
								 Message outMsg = new Message(message.getBody());
								    
								      //Send Message object
								      Log.i("CHAT", outMsg.toString());
								    
								// TODO Auto-generated method stub
								
							}
						});
						 
						 try {
							  //Send String as Message
							  newChat.sendMessage("How are you?");
							} catch (XMPPException e) {
							  //Error
							}
						
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (NetworkOnMainThreadException e) {
					Log.i("CONNECTION", "Conneciton error");
					e.printStackTrace();
				}catch (Exception e) {
					Log.i("CONNECTION", "Conneciton error");
					e.printStackTrace();
				}
				
				Log.i("CONNECTION", "Successfully Connected");
				
			}
		});
		//thr2.start();
		
		
		
		
	}


    
    
}
