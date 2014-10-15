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

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.emot.adapters.ContactArrayAdapter;
import com.emot.androidclient.IXMPPRosterCallback.Stub;
import com.emot.androidclient.XMPPRosterServiceAdapter;
import com.emot.androidclient.data.ChatProvider;
import com.emot.androidclient.data.EmotConfiguration;
import com.emot.androidclient.data.RosterProvider;
import com.emot.androidclient.data.RosterProvider.RosterConstants;
import com.emot.androidclient.service.IXMPPRosterService;
import com.emot.androidclient.service.XMPPService;
import com.emot.androidclient.util.ConnectionState;
import com.emot.common.TaskCompletedRunnable;
import com.emot.constants.IntentStrings;
import com.emot.constants.WebServiceConstants;
import com.emot.emotobjects.Contact;
import com.emot.model.EmotApplication;
import com.emot.persistence.ContactUpdater;

public class ContactScreen extends ActionBarActivity{
	private ListView listviewContact;
	private static String TAG = ContactScreen.class.getName();
	private ArrayList<Contact> contacts;
	private ShowContacts showContactsThread;

	private Intent xmppServiceIntent;
	private ServiceConnection xmppServiceConnection;
	private XMPPRosterServiceAdapter serviceAdapter;
	private Stub rosterCallback;
	private EmotConfiguration mConfig;
	private ContactArrayAdapter contactAdapter;
	
	final static private String[] CONTACT_PROJECTION = new String[] {
		RosterConstants.ALIAS, RosterConstants.STATUS_MESSAGE,
		RosterConstants.AVATAR, RosterConstants.JID};
	
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


				String jid = contacts.get(position).getJID();
				Intent chatIntent = new Intent(ContactScreen.this, ChatScreen.class);
				chatIntent.putExtra(IntentStrings.CHAT_FRIEND, jid);
				startActivity(chatIntent);

				//startChatActivity(mobile+"@"+WebServiceConstants.CHAT_DOMAIN, "alias", null);

			}
		});
		registerXMPPService();
		contactAdapter = new ContactArrayAdapter(EmotApplication.getAppContext(), R.layout.contact_row, contacts);
		listviewContact.setAdapter(contactAdapter);
	}

	public void updateContacts(){
		//Update Contacts
		ContactUpdater.updateContacts(new TaskCompletedRunnable() {

			@Override
			public void onTaskComplete(String result) {

			}
		}, serviceAdapter);
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
	protected void onStop() {
		showContactsThread.cancel(true);
		super.onStop();
	}



	public class UpdateRosters extends AsyncTask<Void, Contact, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			Log.i(TAG, "time 1");
			while(!serviceAdapter.isAuthenticated()){
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
				Cursor cr = getContentResolver().query(RosterProvider.CONTENT_URI,
				CONTACT_PROJECTION, null,
				null, null);
				
				Log.i(TAG, "contacts found  = "+cr.getCount());
				while (cr.moveToNext()) {
				    Contact contact = new Contact(cr.getString(cr.getColumnIndex(RosterConstants.ALIAS)), cr.getString(cr.getColumnIndex(RosterConstants.JID)));
				    contact.setStatus(cr.getString(cr.getColumnIndex(RosterConstants.STATUS_MESSAGE)));
				    publishProgress(contact);
				}
				cr.close();
				Log.i(TAG, "time 2");
				return true;
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}

		}

		protected void onProgressUpdate(Contact... contact){
			contacts.add(contact[0]);
			contactAdapter.notifyDataSetChanged();
			Log.i(TAG, "Adding contact ...");
			return;
		}

		protected void onPostExecute(Boolean resp) {
			if(!resp){
				Toast.makeText(EmotApplication.getAppContext(), "Sorry encountered some error while fetching contacts. Please try again later.", Toast.LENGTH_LONG).show();
			}
		}
	}


	private void registerXMPPService() {
		Log.i(TAG, "called startXMPPService()");
		xmppServiceIntent = new Intent(this, XMPPService.class);
		xmppServiceIntent.setAction("org.emot.androidclient.XMPPSERVICE");

		xmppServiceConnection = new ServiceConnection() {

			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG, "called onServiceConnected()");
				serviceAdapter = new XMPPRosterServiceAdapter(
						IXMPPRosterService.Stub.asInterface(service));
				serviceAdapter.registerUICallback(rosterCallback);
				Log.i(TAG, "getConnectionState(): "
						+ serviceAdapter.getConnectionState());
				//invalidateOptionsMenu();	// to load the action bar contents on time for access to icons/progressbar
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
				updateContacts();
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

}
