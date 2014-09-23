package com.emot.screens;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.emot.adapters.ChatListArrayAdapter;
import com.emot.model.EmotApplication;
import com.emot.emotobjects.ChatMessage;
import com.emot.persistence.DBContract;
import com.emot.persistence.EmotHistoryHelper;

public class ChatScreen extends Activity{

	private Chat chat;
	private ImageView sendButton;
	private EditText chatEntry;
	private TextView userTitle;
	private EmotHistoryHelper emotHistoryDB;
	private static String TAG = ChatScreen.class.getSimpleName();

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


		MessageListener mmlistener = new MessageListener() {
			@Override
			public void processMessage(Chat chat, final Message message) {
				Log.d("Incoming message", message.getBody());
				handler.post(new Runnable() {
					@Override
					public void run() {
						chatList.add(message.getBody());
						chatlistAdapter.notifyDataSetChanged();
						//progressBar.setProgress(value);
					}
				});
			}
		};

		if(EmotApplication.getConnection() != null){
			ChatManager current_chat  = EmotApplication.getConnection().getChatManager();
			chat = current_chat.createChat("test2@emot-net", "test2@emot-net", mmlistener);
			runOnUiThread(new Thread(new Runnable() {
				@Override
				public void run() {
					if(chat != null){
						sendButton.setEnabled(true);
					}

				}
			}));
		}
		
		EmotApplication.startConnection();

		startActivity(new Intent(EmotApplication.getAppContext(), ContactScreen.class));



	}

	private void setChatContents(final String message){

		chatList.add(message);
		chatlistAdapter.notifyDataSetChanged();
		//chatView.setAdapter(chatlistAdapter);
	}

}
