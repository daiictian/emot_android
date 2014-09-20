package com.emot.screens;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.emot.adapters.ChatListArrayAdapter;
import com.emot.emotobjects.ChatMessage;
import com.emot.persistence.DBContract;
import com.emot.persistence.EmotHistoryHelper;

public class ChatScreen extends Activity{

	private Chat chat;
	private XMPPConnection connection;
	private ImageView sendButton;
	private EditText chatEntry;
	private TextView userTitle;
	private EmotHistoryHelper emotHistoryDB;

	private class EmotHistoryTask extends AsyncTask<EmotHistoryHelper, Void, Cursor>{



		@Override
		protected void onPostExecute(Cursor result) {
			// TODO Auto-generated method stub
			boolean valid  = result.moveToFirst();
			if(valid && result != null && result.getCount() > 0){
				String chat = result.getString(result.getColumnIndex(DBContract.EmotHistoryEntry.EMOTS));
				chatList.add(new ChatMessage(chat, false));
				chatlistAdapter.notifyDataSetChanged();
			}else{
				//chatList.add("DB unfriendly");
			}

		}

		@Override
		protected Cursor doInBackground(EmotHistoryHelper... params) {
			EmotHistoryHelper emotHistory = params[0];
			Cursor emotHistoryCursor = emotHistory.getEmotHistory("test2");
			return emotHistoryCursor;
		}




	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		/**/	
	}

	private void connectToUser(final String userName){


	}



	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}



	private ListView chatView;
	private Handler handler;
	private ChatMessage mChatReceivedMessage;
	private ChatMessage mChatSentMessage;

	private ChatListArrayAdapter chatlistAdapter;
	ArrayList<ChatMessage> chatList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent incomingIntent = getIntent();
		String userName = "";


		setContentView(R.layout.activity_chat_screen);
		chatView = (ListView)findViewById(R.id.chatView);
		sendButton = (ImageView)findViewById(R.id.dove_send);
		userTitle = (TextView)findViewById(R.id.username);
		chatEntry = (EditText)findViewById(R.id.editText1);
		if(incomingIntent != null){
			userName = incomingIntent.getStringExtra("USERNAME");
			userTitle.setText(userName);
		}
		chatList = new ArrayList<ChatMessage>();
		handler = new Handler();
		emotHistoryDB = new EmotHistoryHelper(ChatScreen.this);
		EmotHistoryTask eht = new EmotHistoryTask();
		//eht.execute(new EmotHistoryHelper[]{emotHistoryDB});
		//chatList.add("Howdy");
		chatlistAdapter = new ChatListArrayAdapter(this, chatList);  
		if(chat == null){
			sendButton.setEnabled(false);
		}
		sendButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date now = new Date();
					String strDate = sdfDate.format(now);
					final String dateTime[] = strDate.split(" "); 
					chat.sendMessage(chatEntry.getText().toString());
					new Thread(new Runnable() {

						@Override
						public void run() {
							emotHistoryDB.insertChat("test2", chatEntry.getText().toString(), dateTime[0], dateTime[1]);

						}
					}).start(); 
					chatList.add(new ChatMessage(chatEntry.getText().toString(), true));
					chatlistAdapter.notifyDataSetChanged();
					chatEntry.setText("");
				} catch (Exception ex) { 
				}

			}
		});
		chatView.setAdapter(chatlistAdapter);
		Thread connThread = new Thread(new Runnable() {

			@Override
			public void run() {
				int portInt = 5222;

				// Create a connection
				ConnectionConfiguration connConfig = new ConnectionConfiguration("ec2-54-85-148-36.compute-1.amazonaws.com", portInt,"emot-net");
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
				connection = new XMPPConnection(connConfig);

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
					connection.login("test1","1234");
					Log.i("androxmpp", "Logged in as " + connection.getUser() + ". Authenticated : "+connection.isAuthenticated());



					//and here is my listener


					MessageListener mmlistener = new MessageListener() {
						@Override
						public void processMessage(Chat chat, final Message message) {
							Log.d("Incoming message", message.getBody());
							handler.post(new Runnable() {

								@Override

								public void run() {
									
									chatList.add(new ChatMessage(message.getBody(), false));
									chatlistAdapter.notifyDataSetChanged();
									//progressBar.setProgress(value);

								}

							});



						}
					};

					ChatManager current_chat  = connection.getChatManager();
					chat = current_chat.createChat("test2@emot-net", "test2@emot-net", mmlistener);
					runOnUiThread(new Thread(new Runnable() {

						@Override
						public void run() {
							if(chat != null){
								sendButton.setEnabled(true);
							}

						}
					}));


				} catch(Exception ex){

					Log.i("androxmpp", "loginfails ");
					ex.printStackTrace();
				}
			}
		});
		connThread.start();


		//startActivity(new Intent(this, ContactScreen.class));

	}

	
}
