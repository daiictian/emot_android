package com.emot.screens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.VCard;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.emot.adapters.ContactArrayAdapter;
import com.emot.androidclient.IXMPPRosterCallback.Stub;
import com.emot.androidclient.MainWindow.RosterExpListAdapter;
import com.emot.androidclient.XMPPRosterServiceAdapter;
import com.emot.androidclient.data.YaximConfiguration;
import com.emot.androidclient.service.IXMPPRosterService;
import com.emot.androidclient.service.XMPPService;
import com.emot.androidclient.util.ConnectionState;
import com.emot.common.TaskCompletedRunnable;
import com.emot.constants.IntentStrings;
import com.emot.constants.WebServiceConstants;
import com.emot.emotobjects.Contact;
import com.emot.model.EmotApplication;
import com.emot.persistence.ContactUpdater;
import com.emot.persistence.DBContract;
import com.emot.persistence.EmotDBHelper;
import com.emot.services.ChatService;
import com.emot.services.ChatService.ProfileBinder;

public class ContactScreen extends SherlockActivity{
	private ListView listviewContact;
	private static String TAG = ContactScreen.class.getName();
	private ContactArrayAdapter contactsAdapter;
	private ArrayList<Contact> contacts;
	private ShowContacts showContactsThread;
	private ChatService chatService;
	boolean mBound = false;
	private Handler mainHandler = new Handler();

	private Intent xmppServiceIntent;
	private ServiceConnection xmppServiceConnection;
	private XMPPRosterServiceAdapter serviceAdapter;
	private Stub rosterCallback;
	private RosterExpListAdapter rosterListAdapter;
	private TextView mConnectingText;
	private YaximConfiguration mConfig;

//	private ContentObserver mRosterObserver = new RosterObserver();
//	private ContentObserver mChatObserver = new ChatObserver();
	
	private HashMap<String, Boolean> mGroupsExpanded = new HashMap<String, Boolean>();

	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mConfig = EmotApplication.getConfig(this);
		setContentView(R.layout.contacts);
		listviewContact = (ListView)findViewById(R.id.listview_contact);

//		ContentValues cvs = new ContentValues();
//		String imgHash = EmotApplication.randomId();
//		Log.i(TAG, "Image hash  = "+ imgHash);
//		cvs.put(DBContract.EmotsDBEntry.EMOT_HASH, imgHash);
//		cvs.put(DBContract.EmotsDBEntry.TAGS, "asin yes no what");
//		cvs.put(DBContract.EmotsDBEntry.EMOT_IMG, ImageHelper.getByteArray(BitmapFactory.decodeResource(EmotApplication.getAppContext().getResources(),R.drawable.asin)));
//		EmotDBHelper.getInstance(ContactScreen.this).getWritableDatabase().insertWithOnConflict(DBContract.EmotsDBEntry.TABLE_NAME, null, cvs, SQLiteDatabase.CONFLICT_REPLACE);

		showContactsThread = new ShowContacts();
		showContactsThread.execute();
		

		//Update Contacts
		ContactUpdater.updateContacts(new TaskCompletedRunnable() {

			@Override
			public void onTaskComplete(String result) {
				
			}
		});

		 Intent i = new Intent();
         i.setAction("com.emot.services.ChatService");
        // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         this.startService(i);
//		ContactUpdater.updateContacts(new TaskCompletedRunnable() {
//
//			@Override
//			public void onTaskComplete(String result) {
//				//Contacts updated in SQLite. You might want to update UI
//			}
//		});

		
//		Intent chatIntent = new Intent(ContactScreen.this, ChatScreen.class);
//		chatIntent.putExtra(IntentStrings.CHAT_FRIEND, "1234567890");
//		startActivity(chatIntent);
		
		contacts = new ArrayList<Contact>();
		contactsAdapter = new ContactArrayAdapter(EmotApplication.getAppContext(), R.layout.contact_row, contacts);
		listviewContact.setAdapter(contactsAdapter);
		
//		Thread connectionThread = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(10000);
//					//mConnectionQueue = EmotApplication.mConnectionQueue;
//					Log.i(TAG, "b4 connection retreived " +connection + " ");
//					while(true){
//						connection = ConnectionQueue.get();
//						Log.i(TAG, "after connection retreived " +connection);
//					}
//
//				} catch (InterruptedException e) {
//					Log.i(TAG, "Queue exception ...");
//					e.printStackTrace();
//				}
//				
//			}
//		});
//		connectionThread.setName("Connection Thread");
//		connectionThread.start();
		
		//new UpdateRosters().execute();
		//new UpdateRosters().execute();
		
		//From DB
		/*
		Cursor c = null;

		c = db.rawQuery("Select * from "+DBContract.ContactsDBEntry.TABLE_NAME, null);
		Log.i(TAG, "Count of contacts " + c.getCount());
		String[] from = {DBContract.ContactsDBEntry.PROFILE_THUMB, DBContract.ContactsDBEntry.CONTACT_NAME, DBContract.ContactsDBEntry.MOBILE_NUMBER, DBContract.ContactsDBEntry.CURRENT_STATUS};
		int[] to = {R.id.image_contact_profile, R.id.text_contact_name, R.id.text_contact_number, R.id.text_contact_status};
		contactsAdapter = new ContactCursorAdapter (this, R.layout.contact_row, c, from, to);
		listviewContact.setAdapter(contactsAdapter);
		*/
		
		listviewContact.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//startActivity(new Intent(EmotApplication.getAppContext(), UpdateProfileScreen.class));
				
				
				String mobile = contacts.get(position).getMobile();
				Intent chatIntent = new Intent(ContactScreen.this, ChatScreen.class);
				chatIntent.putExtra(IntentStrings.CHAT_FRIEND, mobile);
				startActivity(chatIntent);
				
				startChatActivity(mobile+"@"+WebServiceConstants.CHAT_DOMAIN, "alias", null);
				
			}
		});
		registerXMPPService();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unbindXMPPService();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		bindXMPPService();
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Log.i(TAG, "On start of update profile");
        Intent intent = new Intent(this, ChatService.class);
        intent.putExtra("request_code", ChatService.REQUEST_PROFILE_UPDATE);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
	
	@Override
	protected void onStop() {
		showContactsThread.cancel(true);
		// Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
		super.onStop();
	}
	
	

	public class UpdateRosters extends AsyncTask<Void, Contact, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			Log.i(TAG, "time 1");
			while(!EmotApplication.getConnection().isAuthenticated()){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Roster roster = EmotApplication.getConnection().getRoster();
			Collection<RosterEntry> entries = roster.getEntries();
			Presence presence;
			for (RosterEntry entry : entries) {
				Log.i(TAG, "Entry = Name: "+entry.getName() + " User: " + entry.getUser() + " Status: " + entry.getStatus() + " Type: " + entry.getName());
				if(entry.getStatus()==null){
					presence = roster.getPresence(entry.getUser());
					Contact contact = new Contact(entry.getName(), entry.getUser());
					contact.setStatus(presence.getStatus());
					
					EmotApplication.configure(ProviderManager.getInstance());
					VCard vCard = new VCard();
					try {
						vCard.load(EmotApplication.getConnection(), entry.getUser());
						byte[] avatar = vCard.getAvatar();
						Log.i(TAG, "Avatar in update roster = "+avatar);
						contact.setAvatar(avatar);
					} catch (XMPPException e) {
						e.printStackTrace();
					}
					Log.i(TAG, "Nick name = " + vCard.getNickName() + " Firstname = " + vCard.getFirstName());
					
					publishProgress(contact);
				}
			}
			
			Log.i(TAG, "time 2");
			return null;
		}
		
		protected void onProgressUpdate(Contact... contact){
			contacts.add(contact[0]);
			contactsAdapter.notifyDataSetChanged();
			Log.i(TAG, "Adding contact ...");
			return;
		}
		
		protected void onPostExecute(Void resp) {
			
		}
	}
	
	
	public class ShowContacts extends AsyncTask<Void, Contact, Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			try{
				Log.i(TAG, "time 1");
				SQLiteDatabase db = EmotDBHelper.getInstance(EmotApplication.getAppContext()).getWritableDatabase();
				Cursor cursor = db.rawQuery("select * from "+DBContract.ContactsDBEntry.TABLE_NAME, null);
				Log.i(TAG, "Cursor length "+cursor.getCount());
				if (cursor.moveToFirst()){
					   do{
					      String name = cursor.getString(cursor.getColumnIndex(DBContract.ContactsDBEntry.CONTACT_NAME));
					      String mobile = cursor.getString(cursor.getColumnIndex(DBContract.ContactsDBEntry.MOBILE_NUMBER));
					      String status = cursor.getString(cursor.getColumnIndex(DBContract.ContactsDBEntry.CURRENT_STATUS));
					      byte[] avatar = cursor.getBlob(cursor.getColumnIndex(DBContract.ContactsDBEntry.PROFILE_THUMB));
					      Contact contact = new Contact(name, mobile);
					      contact.setStatus(status);
					      contact.setAvatar(avatar);
					      Log.i(TAG, "Name = "+name + " Mobile = "+mobile+ " Avatar = "+avatar);
					      publishProgress(contact);
					      
					   }while(cursor.moveToNext());
					}
					cursor.close();
				
				
				Log.i(TAG, "time 2");
				return true;
			}catch(Exception e){
				return false;
			}
			
		}
		
		protected void onProgressUpdate(Contact... contact){
			contacts.add(contact[0]);
			contactsAdapter.notifyDataSetChanged();
			Log.i(TAG, "Adding contact ...");
			return;
		}
		
		protected void onPostExecute(Boolean resp) {
			if(!resp){
				Toast.makeText(EmotApplication.getAppContext(), "Sorry encountered some error while fetching contacts. Please try again later.", Toast.LENGTH_LONG);
			}
		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			Log.i(TAG, "service connected ... ");
			ProfileBinder binder = (ProfileBinder) service;
			chatService = binder.getService();
			mBound = true;
			//Update presence of friends
			chatService.updatePresence();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
			Log.i(TAG, "service disconnected ... ");
		}
	};
	
	private void registerXMPPService() {
		Log.i(TAG, "called startXMPPService()");
		xmppServiceIntent = new Intent(this, XMPPService.class);
		xmppServiceIntent.setAction("org.yaxim.androidclient.XMPPSERVICE");

		xmppServiceConnection = new ServiceConnection() {

			@TargetApi(Build.VERSION_CODES.HONEYCOMB) // required for Sherlock's invalidateOptionsMenu */
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG, "called onServiceConnected()");
				serviceAdapter = new XMPPRosterServiceAdapter(
						IXMPPRosterService.Stub.asInterface(service));
				serviceAdapter.registerUICallback(rosterCallback);
				Log.i(TAG, "getConnectionState(): "
						+ serviceAdapter.getConnectionState());
				invalidateOptionsMenu();	// to load the action bar contents on time for access to icons/progressbar
				ConnectionState cs = serviceAdapter.getConnectionState();
//				updateConnectionState(cs);
//				updateRoster();

				// when returning from prefs to main activity, apply new config
				if (mConfig.reconnect_required && cs == ConnectionState.ONLINE) {
					// login config changed, force reconnection
					serviceAdapter.disconnect();
					serviceAdapter.connect();
				} else if (mConfig.presence_required && isConnected())
					serviceAdapter.setStatusFromConfig();

				// handle server-related intents after connecting to the backend
				//handleJabberIntent();
			}

			public void onServiceDisconnected(ComponentName name) {
				Log.i(TAG, "called onServiceDisconnected()");
			}
		};
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
	
	private boolean isConnected() {
		return serviceAdapter != null && serviceAdapter.isAuthenticated();
	}
	private boolean isConnecting() {
		return serviceAdapter != null && serviceAdapter.getConnectionState() == ConnectionState.CONNECTING;
	}
	
	private void startChatActivity(String user, String userName, String message) {
		Intent chatIntent = new Intent(this,
				com.emot.androidclient.chat.ChatWindow.class);
		Uri userNameUri = Uri.parse(user);
		chatIntent.setData(userNameUri);
		chatIntent.putExtra(com.emot.androidclient.chat.ChatWindow.INTENT_EXTRA_USERNAME, userName);
		if (message != null) {
			chatIntent.putExtra(com.emot.androidclient.chat.ChatWindow.INTENT_EXTRA_MESSAGE, message);
		}
		startActivity(chatIntent);
	}

}
