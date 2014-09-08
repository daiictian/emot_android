package com.emot.screens;

import java.io.File;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class ChatScreen extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatscreen);

		Thread connThread = new Thread(new Runnable() {

			@Override
			public void run() {
				int portInt = 5222;

				// Create a connection
				ConnectionConfiguration connConfig = new ConnectionConfiguration("192.168.0.104", portInt,"abhinavsingh.local");
				connConfig.setSASLAuthenticationEnabled(true);
				//connConfig.setCompressionEnabled(true);
				connConfig.setSecurityMode(SecurityMode.enabled);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					connConfig.setTruststoreType("AndroidCAStore");
					connConfig.setTruststorePassword(null);
					connConfig.setTruststorePath(null);
					Log.i("XMPPClient", "[XmppConnectionTask] Build Icecream");

				} else {
					connConfig.setTruststoreType("BKS");
					String path = System.getProperty("javax.net.ssl.trustStore");
					if (path == null)
						path = System.getProperty("java.home") + File.separator + "etc"
								+ File.separator + "security" + File.separator
								+ "cacerts.bks";
					connConfig.setTruststorePath(path);
					Log.i("XMPPClient", "[XmppConnectionTask] Build less than Icecream ");

				}
				connConfig.setDebuggerEnabled(true);
				XMPPConnection.DEBUG_ENABLED = true;
				XMPPConnection connection = new XMPPConnection(connConfig);

				try {
					connection.connect();
					Log.i("XMPPClient", "[SettingsDialog] Connected to " + connection.getHost());
					// publishProgress("Connected to host " + HOST);
				} catch (XMPPException ex) {
					Log.e("XMPPClient", "[SettingsDialog] Failed to connect to " + connection.getHost());
					Log.e("XMPPClient", ex.toString());
					//publishProgress("Failed to connect to " + HOST);
					//xmppClient.setConnection(null);
				}

				try {
					connection.login("abhi","abhi@123");
					Log.i("androxmpp", "Logged in as " + connection.getUser() + ". Authenticated : "+connection.isAuthenticated());



					//and here is my listener
					

					MessageListener mmlistener = new MessageListener() {
						@Override
						public void processMessage(Chat chat, Message message) {
							Log.d("Incoming message", message.getBody());
						}
					};
					ChatManager current_chat  = connection.getChatManager();
					Chat chat = current_chat.createChat("vishal@abhinavsingh.local", "vishal@abhinavsingh.local", mmlistener);
					try {
						chat.sendMessage("Howdy?!");
					} catch (Exception ex) { 
					}


					} catch(Exception ex){

						Log.i("androxmpp", "loginfails ");
						ex.printStackTrace();
					}
				}
			});

		connThread.start();
		}
	}
