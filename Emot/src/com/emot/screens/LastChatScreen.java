package com.emot.screens;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.emot.androidclient.IXMPPRosterCallback.Stub;
import com.emot.androidclient.XMPPRosterServiceAdapter;
import com.emot.androidclient.data.ChatProvider;
import com.emot.androidclient.data.ChatProvider.ChatConstants;
import com.emot.androidclient.data.EmotConfiguration;
import com.emot.androidclient.data.RosterProvider;
import com.emot.androidclient.data.RosterProvider.RosterConstants;
import com.emot.androidclient.service.IXMPPRosterService;
import com.emot.androidclient.service.XMPPService;
import com.emot.androidclient.util.ConnectionState;
import com.emot.common.ImageHelper;
import com.emot.common.TaskCompletedRunnable;
import com.emot.emotobjects.Contact;
import com.emot.model.EmotApplication;
import com.emot.persistence.ContactUpdater;

public class LastChatScreen extends ActionBarActivity {
	private static String TAG = LastChatScreen.class.getSimpleName();
	private ListView listLastChat;
	
	private Intent xmppServiceIntent;
	private ServiceConnection xmppServiceConnection;
	private XMPPRosterServiceAdapter serviceAdapter;
	private Stub rosterCallback;
	private EmotConfiguration mConfig;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.last_chat_screen);
		Intent i = new Intent(this, XMPPService.class);
		//i.setAction("com.emot.services.ChatService");
		this.startService(i);
		intializeUI();
		setAdapter();
		registerXMPPService();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
		Log.i(TAG, "Action bar creating menu");
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_actions_lastchat, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_profile:
	            startActivity(new Intent(LastChatScreen.this, UpdateProfileScreen.class));
	            return true;
	        case android.R.id.home:
	        	Log.i(TAG, "back pressed");
	            this.finish();
	            return true;
	        case R.id.action_search:
	            startActivity(new Intent(LastChatScreen.this, ContactScreen.class));
	            return true;
	        case R.id.action_settings:
	        	startActivity(new Intent(LastChatScreen.this, CreateGroup.class));
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void intializeUI(){
		listLastChat = (ListView)findViewById(R.id.listLastChat);
	}
	
	private void setAdapter(){
		Log.i(TAG, "Starting Adapter set ...");
		String[] projection = new String[] {
				ChatProvider.ChatConstants._ID,
				ChatProvider.ChatConstants.JID,
				ChatProvider.ChatConstants.CHAT_TYPE,
				"MAX("+ChatProvider.ChatConstants.DATE+")",
				ChatProvider.ChatConstants.MESSAGE, 
				ChatProvider.ChatConstants.DELIVERY_STATUS 
		};
		int[] projection_to = new int[] { R.id.textLastChatUser, R.id.textLastChatItem };
		String selection = ChatProvider.ChatConstants.JID +" != '"+EmotApplication.getConfig().jabberID+"'" + ") GROUP BY ("+ ChatProvider.ChatConstants.JID;
		String[] groupby = new String[]{ChatProvider.ChatConstants.JID};
		Cursor cursor = getContentResolver().query(ChatProvider.CONTENT_URI, projection, selection, null, null);
		Log.i(TAG, "cursor count "+cursor.getCount());
		final ListAdapter adapter = new LastChatAdapter(cursor, projection, projection_to);
		listLastChat.setAdapter(adapter);
		listLastChat.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
				Cursor cur = (Cursor) adapter.getItem(position);
	            cur.moveToPosition(position);
	            String jid = cur.getString(cur.getColumnIndex(ChatProvider.ChatConstants.JID));
	            String type = cur.getString(cur.getColumnIndex(ChatProvider.ChatConstants.CHAT_TYPE));
	            Log.i(TAG, "jid of room or freind is " + jid);
	            if(type.equals("chat")){
	            Intent chatIntent = new Intent(LastChatScreen.this, ChatScreen.class);
				chatIntent.putExtra(ChatScreen.INTENT_CHAT_FRIEND, jid);
				startActivity(chatIntent);
	            }else if(type.equals("groupchat")){
	            	Intent chatIntent = new Intent(LastChatScreen.this, GroupChatScreen.class);
					chatIntent.putExtra(GroupChatScreen.INTENT_GRPCHAT_NAME, jid);
					startActivity(chatIntent);	
	            }
				//cur.close();
			}});
		Log.i(TAG, "Adapter set !!");
	}
	
	class LastChatAdapter extends SimpleCursorAdapter{

		public LastChatAdapter(Cursor c,String[] from, int[] to) {
			super(LastChatScreen.this, R.layout.last_chat_row, c, from, to);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			LastChatWrapper wrapper;
			Cursor cursor = this.getCursor();
			cursor.moveToPosition(position);
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.last_chat_row, null);
				wrapper = new LastChatWrapper(row);
				row.setTag(wrapper);
			} else {
				wrapper = (LastChatWrapper) row.getTag();
			}
			String user = cursor.getString(cursor.getColumnIndex(ChatProvider.ChatConstants.JID));
			String type = cursor.getString(cursor.getColumnIndex(ChatProvider.ChatConstants.CHAT_TYPE));
			if(type.equals("groupchat")){
			EmotApplication.addRooms(user);
			}
			String message = cursor.getString(cursor.getColumnIndex(ChatProvider.ChatConstants.MESSAGE));
			String status = cursor.getString(cursor.getColumnIndex(ChatProvider.ChatConstants.DELIVERY_STATUS));
			boolean isNew = false;
			if(status.equals(ChatConstants.DS_NEW)){
				isNew = true;
			}
			wrapper.populateRow(user, message, isNew);
			return row;
		}
		
	}
	
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
	
	private void unbindXMPPService() {
		try {
			unbindService(xmppServiceConnection);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Service wasn't bound!");
		}
	}

	private void bindXMPPService() {
		bindService(xmppServiceIntent, xmppServiceConnection, BIND_AUTO_CREATE);
	}
	
	private void registerXMPPService() {
		Log.i(TAG, "called startXMPPService()");
		mConfig = EmotApplication.getConfig();
		xmppServiceIntent = new Intent(this, XMPPService.class);
		xmppServiceIntent.setAction("com.emot.services.XMPPSERVICE");

		xmppServiceConnection = new ServiceConnection() {

			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG, "called onServiceConnected()");
				serviceAdapter = new XMPPRosterServiceAdapter(
						IXMPPRosterService.Stub.asInterface(service));
				//serviceAdapter.registerUICallback(rosterCallback);
				Log.i(TAG, "getConnectionState(): "
						+ serviceAdapter.getConnectionState());
				//invalidateOptionsMenu();	// to load the action bar contents on time for access to icons/progressbar
				ConnectionState cs = serviceAdapter.getConnectionState();
				//				updateConnectionState(cs);
				//				updateRoster();

				// when returning from prefs to main activity, apply new config
				if (mConfig.reconnect_required && cs == ConnectionState.ONLINE) {
					// login config changed, force reconnection
					Log.i(TAG, "--------- RECONNECTING LASTCHATSCREEN----------");
					serviceAdapter.disconnect();
					serviceAdapter.connect();
				} else if (mConfig.presence_required && isConnected()){
					Log.i(TAG, "--------- SETTING STATUS LASTCHATSCREEN ----------");
					serviceAdapter.setStatusFromConfig();
				}else if(!isConnected()){
					Log.i(TAG, "--------- TRYING TO CONNECT ----------");
					serviceAdapter.connect();
				}
				updateContacts();
				// handle server-related intents after connecting to the backend
				//handleJabberIntent();
			}

			public void onServiceDisconnected(ComponentName name) {
				Log.i(TAG, "called onServiceDisconnected()");
			}
		};
	}
	
	private boolean isConnected() {
		return serviceAdapter != null && serviceAdapter.isAuthenticated();
	}
	
	class LastChatWrapper{
		public TextView username;
		public TextView lastchat;
		public ImageView useravatar;
		
		public LastChatWrapper(View base){
			username = (TextView)base.findViewById(R.id.textLastChatUser);
			lastchat = (TextView)base.findViewById(R.id.textLastChatItem);
			useravatar = (ImageView)base.findViewById(R.id.image_last_chat);
		}
		
		public void populateRow(String user, String last_chat, boolean isNew){
			//username.setText(user);
			
			
			//new UpdateRow().execute(user);
			
			/*Doing in main thread*/
			String jid = user;
			String alias = jid.split("@")[0];
			byte[] avatar = null;
			String selection = RosterProvider.RosterConstants.JID + "='" + jid + "'";
			String[] projection = new String[] {RosterProvider.RosterConstants.ALIAS, RosterProvider.RosterConstants.AVATAR};
			Cursor cursor = EmotApplication.getAppContext().getContentResolver().query(RosterProvider.CONTENT_URI, projection, selection, null, null);
			Log.i(TAG, "users found length = "+cursor.getCount());
			if(cursor.getCount()>0){
				while(cursor.moveToNext()){
					alias = cursor.getString(cursor.getColumnIndex(RosterProvider.RosterConstants.ALIAS));
					Log.i(TAG, "chat alias : "+alias);
					avatar = cursor.getBlob(cursor.getColumnIndex(RosterConstants.AVATAR));
					Log.i(TAG, "avatar : "+avatar);
					
				}
			}
			cursor.close();
			Contact contact = new Contact(alias, jid);
			contact.setAvatar(avatar);
			username.setText(contact.getName());
        	byte[] avt = contact.getAvatar();
        	Bitmap bitmap;
        	if(avatar!=null){
				bitmap = BitmapFactory.decodeByteArray(avt , 0, avatar.length);
			}else{
				bitmap = BitmapFactory.decodeResource(EmotApplication.getAppContext().getResources(), R.drawable.blank_user_image);
			}
        	useravatar.setImageBitmap(ImageHelper.getRoundedCornerBitmap(bitmap, 10));
        	
        	
			
			lastchat.setText(last_chat);
			if(isNew){
				lastchat.setTextColor(EmotApplication.getAppContext().getResources().getColor(R.color.green));
			}else{
				lastchat.setTextColor(EmotApplication.getAppContext().getResources().getColor(R.color.blue));
			}
		}
		
		
		private class UpdateRow extends AsyncTask<String, Void, Contact> {

	        @Override
	        protected Contact doInBackground(String... params) {
	        	String jid = params[0];
				String alias = jid.split("@")[0];
				byte[] avatar = null;
				String selection = RosterProvider.RosterConstants.JID + "='" + jid + "'";
				String[] projection = new String[] {RosterProvider.RosterConstants.ALIAS, RosterProvider.RosterConstants.AVATAR};
				Cursor cursor = EmotApplication.getAppContext().getContentResolver().query(RosterProvider.CONTENT_URI, projection, selection, null, null);
				Log.i(TAG, "users found length = "+cursor.getCount());
				if(cursor.getCount()>0){
					while(cursor.moveToNext()){
						alias = cursor.getString(cursor.getColumnIndex(RosterProvider.RosterConstants.ALIAS));
						Log.i(TAG, "chat alias : "+alias);
						avatar = cursor.getBlob(cursor.getColumnIndex(RosterConstants.AVATAR));
						Log.i(TAG, "avatar : "+avatar);
						
					}
				}
				cursor.close();
				Contact contact = new Contact(alias, jid);
				contact.setAvatar(avatar);
				return contact;
	        }

	        @Override
	        protected void onPostExecute(Contact contact) {
	        	username.setText(contact.getName());
	        	byte[] avatar = contact.getAvatar();
	        	Bitmap bitmap;
	        	if(avatar!=null){
					bitmap = BitmapFactory.decodeByteArray(avatar , 0, avatar.length);
				}else{
					bitmap = BitmapFactory.decodeResource(EmotApplication.getAppContext().getResources(), R.drawable.blank_user_image);
				}
	        	useravatar.setImageBitmap(ImageHelper.getRoundedCornerBitmap(bitmap, 10));
	        }
	    }
	}
	
	public void updateContacts(){
		//Update Contacts
		Log.i(TAG, "Updating contacts !!!!");
		ContactUpdater.updateContacts(new TaskCompletedRunnable() {

			@Override
			public void onTaskComplete(String result) {
				
			}
		}, serviceAdapter);
	}
}
