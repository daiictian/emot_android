package com.emot.screens;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jivesoftware.smack.Chat;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.emot.androidclient.chat.XMPPChatServiceAdapter;
import com.emot.androidclient.data.ChatProvider;
import com.emot.androidclient.data.ChatProvider.ChatConstants;
import com.emot.androidclient.data.RosterProvider;
import com.emot.androidclient.service.IXMPPChatService;
import com.emot.androidclient.service.XMPPService;
import com.emot.common.EmotEditText;
import com.emot.common.EmotTextView;
import com.emot.model.EmotApplication;

public class ChatScreen extends ActionBarActivity{

	private Chat chat;
	private ImageView sendButton;
	private EmotEditText chatEntry;
	private TextView userTitle;
	private View emotSuggestion;
	private LinearLayout emotSuggestionLayout;
	private static String TAG = "ChatScreen";
	private ListView chatView;
	private String chatFriend;
	private Messenger mMessengerService = null;
	private boolean mMessengerServiceConnected = false;
	private Intent mServiceIntent;
	private ServiceConnection mServiceConnection;
	private XMPPChatServiceAdapter mServiceAdapter;
	private static final int DELAY_NEWMSG = 2000;
	private String chatAlias;
	public final static String INTENT_CHAT_FRIEND = "chat_friend";
	public final static String INTENT_CHAT_ALIAS = "chat_alias";

	private static final String[] PROJECTION_FROM = new String[] {
		ChatProvider.ChatConstants._ID, ChatProvider.ChatConstants.DATE,
		ChatProvider.ChatConstants.DIRECTION, ChatProvider.ChatConstants.JID,
		ChatProvider.ChatConstants.MESSAGE, ChatProvider.ChatConstants.DELIVERY_STATUS };

	private static final int[] PROJECTION_TO = new int[] { R.id.chat_date,
		R.id.chat_from, R.id.chat_message };
	//
	//	private class EmotHistoryTask extends AsyncTask<EmotDBHelper, ChatMessage, Void>{
	//
	//
	//
	//		@Override
	//		protected void onPostExecute(Void result) {
	//			
	//			Log.i(TAG, "chats = "+chatList.size());
	//			//chatView.setSelection(chatList.size()-1);
	//		}
	//		
	//		@Override
	//		protected void onProgressUpdate(ChatMessage... values) {
	//			if(values!=null){
	//				chatList.add(values[0]);
	//				chatlistAdapter.notifyDataSetChanged();
	//			}
	//			super.onProgressUpdate(values);
	//		}
	//
	//		@Override
	//		protected Void doInBackground(EmotDBHelper... params) {
	//			EmotDBHelper emotHistory = params[0];
	//			Cursor result = emotHistory.getEmotHistory(chatFriend);
	//			boolean valid  = result.moveToFirst();
	//			String chat = "";
	//			String location = "";
	//			String datetime = "";
	//			boolean emotlocation = false;
	//			if(valid && result != null && result.getCount() > 0){
	//				for(int i = 0; i < result.getCount(); i++){
	//					chat = result.getString(result.getColumnIndex(DBContract.EmotHistoryEntry.EMOTS));
	//					location = result.getString(result.getColumnIndex(DBContract.EmotHistoryEntry.EMOT_LOCATION));
	//					datetime = result.getString(result.getColumnIndex(DBContract.EmotHistoryEntry.DATETIME));
	//					result.moveToNext();
	//					Log.i(TAG, "chat from DB is " +chat);
	//					ChatMessage newChat = null;
	//					if(location.equals("left")){
	//						newChat = new ChatMessage(chat,datetime, false);
	//					}else if(location.equals("right")){
	//						newChat = new ChatMessage(chat,datetime, true);
	//					}
	//					publishProgress(newChat);
	//				}
	//
	//			}
	//			result.close();
	//			return null;
	//		}
	//
	//	}
	//

	@Override
	protected void onResume() {
		super.onResume();
		bindXMPPService();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindXMPPService();
	}

	private void registerXMPPService() {
		Log.i(TAG, "called startXMPPService()");
		mServiceIntent = new Intent(this, XMPPService.class);
		Uri chatURI = Uri.parse(chatFriend);
		mServiceIntent.setData(chatURI);
		mServiceIntent.setAction("org.emot.androidclient.XMPPSERVICE");

		mServiceConnection = new ServiceConnection() {

			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG, "called onServiceConnected()");
				mServiceAdapter = new XMPPChatServiceAdapter(
						IXMPPChatService.Stub.asInterface(service),
						chatFriend);

				mServiceAdapter.clearNotifications(chatFriend);
			}

			public void onServiceDisconnected(ComponentName name) {
				Log.i(TAG, "called onServiceDisconnected()");
			}

		};
	}

	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Service wasn't bound!");
		}
	}

	private void bindXMPPService() {
		bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        	Log.i(TAG, "back pressed");
	            this.finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		Intent incomingIntent = getIntent();
		chatFriend = incomingIntent.getStringExtra(ChatScreen.INTENT_CHAT_FRIEND);
		chatAlias = incomingIntent.getStringExtra(ChatScreen.INTENT_CHAT_ALIAS);
		if(chatAlias==null){
			setAliasFromDB();
		}
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
		ab.setTitle(chatAlias);
		/*if(incomingIntent != null){

			userName = incomingIntent.getStringExtra("USERNAME");
			userTitle.setText(userName);
		}*/
		//handler = new Handler();
		//emotHistoryDB = EmotDBHelper.getInstance(ChatScreen.this);
		//EmotHistoryTask eht = new EmotHistoryTask();
		//eht.execute(new EmotDBHelper[]{emotHistoryDB});
		//chatList.add("Howdy");

		sendButton.setEnabled(true);

		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage(chatEntry.getText().toString());
			}
		});
		
		//chatView.setAdapter(chatlistAdapter);
		setChatWindowAdapter();
		
		chatEntry.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				emotSuggestion.setVisibility(View.VISIBLE);
			}
		});
		registerXMPPService();
		//setChatWindowAdapter();

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

	private void sendMessage(String message) {
		chatEntry.setText(null);
		mServiceAdapter.sendMessage(chatFriend, message);
		if (!mServiceAdapter.isServiceAuthenticated()){
			//Show single tick and try later
			//showToastNotification(R.string.toast_stored_offline);
		}
	}
	
	private void setAliasFromDB(){
		String selection = RosterProvider.RosterConstants.JID + "='" + chatFriend + "'";;
		String[] projection = new String[] {RosterProvider.RosterConstants.ALIAS};
		Cursor cursor = managedQuery(RosterProvider.CONTENT_URI, projection, selection, null, null);
		Log.i(TAG, "users found length = "+cursor.getCount());
		while(cursor.moveToNext()){
			chatAlias = cursor.getString(cursor.getColumnIndex(RosterProvider.RosterConstants.ALIAS));
			Log.i(TAG, "chat alias : "+chatAlias);
		}
		cursor.close();
	}

	private void markAsReadDelayed(final int id, final int delay) {
		new Thread() {
			@Override
			public void run() {
				try { Thread.sleep(delay); } catch (Exception e) {}
				markAsRead(id);
			}
		}.start();
	}

	private void markAsRead(int id) {
		Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY + "/" + ChatProvider.TABLE_NAME + "/" + id);
		Log.d(TAG, "markAsRead: " + rowuri);
		ContentValues values = new ContentValues();
		values.put(ChatConstants.DELIVERY_STATUS, ChatConstants.DS_SENT_OR_READ);
		getContentResolver().update(rowuri, values, null, null);
	}

	private void setChatWindowAdapter() {
		String selection = ChatConstants.JID + "='" + chatFriend + "'";
		Cursor cursor = managedQuery(ChatProvider.CONTENT_URI, PROJECTION_FROM, selection, null, null);
		ListAdapter adapter = new ChatScreenAdapter(cursor, PROJECTION_FROM, PROJECTION_TO, chatFriend, chatFriend);
		chatView.setAdapter(adapter);
	}
	
	class ChatScreenAdapter extends SimpleCursorAdapter {
		String mScreenName, mJID;

		public ChatScreenAdapter(Cursor cursor, String[] from, int[] to, String JID, String screenName) {
			super(ChatScreen.this, R.layout.chat_row, cursor, from, to);
			mScreenName = screenName;
			mJID = JID;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			ChatItemWrapper wrapper = null;
			Cursor cursor = this.getCursor();
			cursor.moveToPosition(position);

			long dateMilliseconds = cursor.getLong(cursor
					.getColumnIndex(ChatProvider.ChatConstants.DATE));

			int _id = cursor.getInt(cursor
					.getColumnIndex(ChatProvider.ChatConstants._ID));
			String date = getDateString(dateMilliseconds);
			String message = cursor.getString(cursor
					.getColumnIndex(ChatProvider.ChatConstants.MESSAGE));
			boolean from_me = (cursor.getInt(cursor
					.getColumnIndex(ChatProvider.ChatConstants.DIRECTION)) ==
					ChatConstants.OUTGOING);
			String jid = cursor.getString(cursor
					.getColumnIndex(ChatProvider.ChatConstants.JID));
			int delivery_status = cursor.getInt(cursor
					.getColumnIndex(ChatProvider.ChatConstants.DELIVERY_STATUS));

			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.chat_row, null);
				wrapper = new ChatItemWrapper(row);
				row.setTag(wrapper);
			} else {
				wrapper = (ChatItemWrapper) row.getTag();
			}

			if (!from_me && delivery_status == ChatConstants.DS_NEW) {
				markAsReadDelayed(_id, DELAY_NEWMSG);
			}

			String from = jid;
			if (jid.equals(mJID))
				from = mScreenName;
			wrapper.populateFrom(date, from_me, from, message, delivery_status);

			return row;
		}
	}
	
	private String getDateString(long milliSeconds) {
		SimpleDateFormat dateFormater = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		Date date = new Date(milliSeconds);
		return dateFormater.format(date);
	}

	public class ChatItemWrapper {
		public EmotTextView mChatTextLeft; 
		public View chatBoxLeft;
		public TextView mDateTimeLeft;
		public TextView mChatStatusLeft;
		
		public EmotTextView mChatTextRight; 
		public View chatBoxRight;
		public TextView mDateTimeRight;
		public TextView mChatStatusRight;
		

		ChatItemWrapper(View base) {
			chatBoxLeft = base.findViewById(R.id.messageContainerLeft);
			mDateTimeLeft = (TextView)base.findViewById(R.id.chatDateLeft);
			mChatTextLeft = (EmotTextView) base.findViewById(R.id.chatContentLeft); 
			mChatStatusLeft = (TextView) base.findViewById(R.id.chatStatusLeft);
			
			chatBoxRight = base.findViewById(R.id.messageContainerRight);
			mDateTimeRight = (TextView)base.findViewById(R.id.chatDateRight);
			mChatTextRight = (EmotTextView) base.findViewById(R.id.chatContentRight);
			mChatStatusRight = (TextView) base.findViewById(R.id.chatStatusRight);
		}

		void populateFrom(String date, boolean from_me, String from, String message,
				int delivery_status) {
//			Log.i(TAG, "populateFrom(" + from_me + ", " + from + ", " + message + ")");
			
			if (from_me) {
				chatBoxRight.setVisibility(View.GONE);
				chatBoxLeft.setVisibility(View.VISIBLE);
				mDateTimeLeft.setText(date);
				mChatTextLeft.setText(message);
				switch (delivery_status) {
					case ChatConstants.DS_NEW:
						mChatStatusLeft.setText("new");
						break;
					case ChatConstants.DS_SENT_OR_READ:
						mChatStatusLeft.setText("sent or read");
						break;
					case ChatConstants.DS_ACKED:
						mChatStatusLeft.setText("acknowledged");
						break;
					case ChatConstants.DS_FAILED:
						mChatStatusLeft.setText("failed");
						break;
				}
			} else {
				chatBoxLeft.setVisibility(View.GONE);
				chatBoxRight.setVisibility(View.VISIBLE);
				mDateTimeRight.setText(date);
				mChatTextRight.setText(message);
				switch (delivery_status) {
				case ChatConstants.DS_NEW:
					mChatStatusRight.setText("new");
					break;
				case ChatConstants.DS_SENT_OR_READ:
					mChatStatusRight.setText("sent or read");
					break;
				case ChatConstants.DS_ACKED:
					mChatStatusRight.setText("acknowledged");
					break;
				case ChatConstants.DS_FAILED:
					mChatStatusRight.setText("failed");
					break;
				}
			}
			
//			getMessageView().setText(message);
//			getMessageView().setTextSize(TypedValue.COMPLEX_UNIT_SP, chatWindow.mChatFontSize);
//			getDateView().setTextSize(TypedValue.COMPLEX_UNIT_SP, chatWindow.mChatFontSize*2/3);
//			getFromView().setTextSize(TypedValue.COMPLEX_UNIT_SP, chatWindow.mChatFontSize*2/3);
		}
	}



}



