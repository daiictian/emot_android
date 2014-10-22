package com.emot.screens;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.emot.androidclient.data.ChatProvider;
import com.emot.androidclient.data.ChatProvider.ChatConstants;
import com.emot.model.EmotApplication;

public class LastChatScreen extends ActionBarActivity {
	private static String TAG = LastChatScreen.class.getSimpleName();
	private ListView listLastChat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.last_chat_screen);
		intializeUI();
		setAdapter();
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
	            Intent chatIntent = new Intent(LastChatScreen.this, ChatScreen.class);
				chatIntent.putExtra(ChatScreen.INTENT_CHAT_FRIEND, jid);
				startActivity(chatIntent);
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
	
	class LastChatWrapper{
		public TextView username;
		public TextView lastchat;
		
		public LastChatWrapper(View base){
			username = (TextView)base.findViewById(R.id.textLastChatUser);
			lastchat = (TextView)base.findViewById(R.id.textLastChatItem);
		}
		
		public void populateRow(String user, String last_chat, boolean isNew){
			//username.setText(user);
			EmotApplication.setAliasFromDB(user, username);
			lastchat.setText(last_chat);
			if(isNew){
				lastchat.setTextColor(EmotApplication.getAppContext().getResources().getColor(R.color.green));
			}else{
				lastchat.setTextColor(EmotApplication.getAppContext().getResources().getColor(R.color.black));
			}
		}
	}
}
