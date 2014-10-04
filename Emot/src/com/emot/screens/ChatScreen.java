package com.emot.screens;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.emot.adapters.ChatListArrayAdapter;
import com.emot.common.EmotEditText;
import com.emot.constants.IntentStrings;
import com.emot.emotobjects.ChatMessage;
import com.emot.model.EmotApplication;
import com.emot.persistence.DBContract;
import com.emot.persistence.EmotDBHelper;
import com.emot.services.ChatService;

public class ChatScreen extends ActionBarActivity{

	private Chat chat;
	private ImageView sendButton;
	private EmotEditText chatEntry;
	private TextView userTitle;
	private EmotDBHelper emotHistoryDB;
	private View emotSuggestion;
	private LinearLayout emotSuggestionLayout;
	private static String TAG = "ChatScreen";
	private ListView chatView;
	private Handler handler;
	private String chatFriend;
	private TextView mUserName;

	private class EmotHistoryTask extends AsyncTask<EmotDBHelper, ChatMessage, Void>{



		@Override
		protected void onPostExecute(Void result) {
			
			Log.i(TAG, "chats = "+chatList.size());
			chatView.setSelection(chatList.size()-1);
		}
		
		@Override
		protected void onProgressUpdate(ChatMessage... values) {
			if(values!=null){
				chatList.add(values[0]);
				chatlistAdapter.notifyDataSetChanged();
			}
			super.onProgressUpdate(values);
		}

		@Override
		protected Void doInBackground(EmotDBHelper... params) {
			EmotDBHelper emotHistory = params[0];
			Cursor result = emotHistory.getEmotHistory(chatFriend);
			boolean valid  = result.moveToFirst();
			String chat = "";
			String location = "";
			String datetime = "";
			boolean emotlocation = false;
			if(valid && result != null && result.getCount() > 0){
				for(int i = 0; i < result.getCount(); i++){
					chat = result.getString(result.getColumnIndex(DBContract.EmotHistoryEntry.EMOTS));
					location = result.getString(result.getColumnIndex(DBContract.EmotHistoryEntry.EMOT_LOCATION));
					datetime = result.getString(result.getColumnIndex(DBContract.EmotHistoryEntry.DATETIME));
					result.moveToNext();
					Log.i(TAG, "chat from DB is " +chat);
					ChatMessage newChat = null;
					if(location.equals("left")){
						newChat = new ChatMessage(chat,datetime, false);
					}else if(location.equals("right")){
						newChat = new ChatMessage(chat,datetime, true);
					}
					publishProgress(newChat);
				}

			}
			result.close();
			return null;
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
		if (mMessengerServiceConnected) {

			unbindService(mChatServiceConnection);

			chatList.clear();

			mMessengerServiceConnected = false;

		}
	}

	
	@Override
	protected void onStart() {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.emot.services.ChatService");
		startService(serviceIntent);
		super.onStart();
		 Intent chatservice = new Intent("com.emot.services.ChatService");
		 bindService(chatservice, mChatServiceConnection, Context.BIND_AUTO_CREATE);
	}
	 



	private Messenger mMessengerService = null;
	private boolean mMessengerServiceConnected = false;
	private ChatListArrayAdapter chatlistAdapter;
	ArrayList<ChatMessage> chatList;


	private ServiceConnection mChatServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mMessengerService = null;
			mMessengerServiceConnected = false;

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i("XMPPClient", "Activity Connected to Service");
			mMessengerService = new Messenger(service);
			mMessengerServiceConnected = true;
			android.os.Message msg = android.os.Message.obtain(null,ChatService.MESSAGE_TYPE_TEXT);
			Bundle data = new Bundle();
			data.putString("chat_friend", chatFriend);
			Log.i("XMPPClient", "meg reply to is " +msg.replyTo + " friend = "+chatFriend);
			msg.replyTo = mMessenger;
			msg.setData(data);
			try {
				mMessengerService.send(msg);
			} catch (RemoteException e) {

				e.printStackTrace();
			}

		}
	};



	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	class IncomingHandler extends Handler{

		@Override
		public void handleMessage(android.os.Message pMessage) {
			// TODO Auto-generated method stub
			super.handleMessage(pMessage);
			Log.i("XMPPClient", "received message from service");
			Bundle b = pMessage.getData();
			String s = b.getString("chat");
			String time = b.getString("time");

			chatList.add(new ChatMessage(s,time, false));
			chatlistAdapter.notifyDataSetChanged();

		}




	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent incomingIntent = getIntent();
		String userName = "";
		chatFriend = incomingIntent.getStringExtra(IntentStrings.CHAT_FRIEND);
		Log.i(TAG, "chatFriend is " +chatFriend);
		if (chatFriend==null){
			Toast.makeText(EmotApplication.getAppContext(), "Incorrect username", Toast.LENGTH_LONG).show();
			finish();
		}

		setContentView(R.layout.activity_chat_screen);
		chatView = (ListView)findViewById(R.id.chatView);
		sendButton = (ImageView)findViewById(R.id.dove_send);
		userTitle = (TextView)findViewById(R.id.username);
		emotSuggestion = findViewById(R.id.viewEmotSuggestion);
		emotSuggestionLayout = (LinearLayout)findViewById(R.id.viewEmotSuggestionLayout);

		chatEntry = (EmotEditText)findViewById(R.id.editTextStatus);
		chatEntry.setEmotSuggestionLayout(emotSuggestionLayout);
		userTitle.setText(chatFriend);
		ActionBar ab = getSupportActionBar();
	    ab.setTitle(chatFriend);
		/*if(incomingIntent != null){

			userName = incomingIntent.getStringExtra("USERNAME");
			userTitle.setText(userName);
		}*/
		chatList = new ArrayList<ChatMessage>();
		handler = new Handler();
		emotHistoryDB = EmotDBHelper.getInstance(ChatScreen.this);
		EmotHistoryTask eht = new EmotHistoryTask();
		eht.execute(new EmotDBHelper[]{emotHistoryDB});
		//chatList.add("Howdy");
		chatlistAdapter = new ChatListArrayAdapter(this, R.layout.chat_row, chatList);  

		sendButton.setEnabled(true);

		sendButton.setOnClickListener(new View.OnClickListener() {
			String chat;
			@Override
			public void onClick(View v) {
				try {
					Log.i("XMPPClient", "Send onClick called");
					SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date now = new Date();
					String strDate = sdfDate.format(now);
					final String dateTime[] = strDate.split(" ");
					Bundle data = new Bundle();

					data.putString("chat_friend", chatFriend);
					data.putString("chat", chatEntry.getText().toString());
					//data.putCharSequence("chat", chatEntry.getText().toString());
					android.os.Message msg = android.os.Message.obtain();
					msg.setData(data);
					msg.replyTo = mMessenger;
					mMessengerService.send(msg);
					chat = chatEntry.getText().toString();
					new Thread(new Runnable() {

						@Override
						public void run() {
							emotHistoryDB.insertChat(chatFriend, chat, dateTime[0], dateTime[1], "right");

						}
					}).start(); 
					chatList.add(new ChatMessage(chat, dateTime[1],true));
					chatlistAdapter.notifyDataSetChanged();
					chatEntry.setText("");
					chatEntry.clearSuggestion();

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		});
		chatView.setAdapter(chatlistAdapter);


		if(EmotApplication.getConnection() != null){
			ChatManager current_chat  = EmotApplication.getConnection().getChatManager();

			runOnUiThread(new Thread(new Runnable() {
				@Override
				public void run() {
					if(chat != null){
						sendButton.setEnabled(true);
					}

				}
			}));
		}

		chatEntry.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				emotSuggestion.setVisibility(View.VISIBLE);
			}
		});
		
//		new EmoticonDBHelper(EmotApplication.getAppContext()).createDatabase();
		
//		EmoticonDBHelper.getInstance(EmotApplication.getAppContext()).getWritableDatabase().execSQL(EmoticonDBHelper.SQL_CREATE_TABLE_EMOT);
//		EmoticonDBHelper.getInstance(EmotApplication.getAppContext()).getWritableDatabase().execSQL("INSERT INTO emots select * from emoticons");
		
		
//		ContentValues cvs = new ContentValues();
//		String imgHash = EmotApplication.randomId();
//		Log.i(TAG, "Image hash  = "+ imgHash);
//		cvs.put(DBContract.EmotsDBEntry.EMOT_HASH, imgHash);
//		cvs.put(DBContract.EmotsDBEntry.TAGS, "asin yes no what sad");
//		cvs.put(DBContract.EmotsDBEntry.EMOT_IMG, ImageHelper.getByteArray(BitmapFactory.decodeResource(EmotApplication.getAppContext().getResources(),R.drawable.mad)));
//		EmoticonDBHelper.getInstance(ChatScreen.this).getWritableDatabase().insertWithOnConflict("emoticons", null, cvs, SQLiteDatabase.CONFLICT_REPLACE);
//		
//		cvs = new ContentValues();
//		imgHash = EmotApplication.randomId();
//		Log.i(TAG, "Image hash  = "+ imgHash);
//		cvs.put(DBContract.EmotsDBEntry.EMOT_HASH, imgHash);
//		cvs.put(DBContract.EmotsDBEntry.TAGS, "hello happy angry");
//		cvs.put(DBContract.EmotsDBEntry.EMOT_IMG, ImageHelper.getByteArray(BitmapFactory.decodeResource(EmotApplication.getAppContext().getResources(),R.drawable.sad)));
//		EmoticonDBHelper.getInstance(ChatScreen.this).getWritableDatabase().insertWithOnConflict("emoticons", null, cvs, SQLiteDatabase.CONFLICT_REPLACE);

	}

}



