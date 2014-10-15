package com.emot.screens;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
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
import com.emot.androidclient.data.EmotConfiguration;
import com.emot.androidclient.data.RosterProvider;
import com.emot.androidclient.data.RosterProvider.RosterConstants;
import com.emot.androidclient.service.IXMPPRosterService;
import com.emot.androidclient.service.XMPPService;
import com.emot.androidclient.util.ConnectionState;
import com.emot.common.TaskCompletedRunnable;
import com.emot.constants.IntentStrings;
import com.emot.emotobjects.Contact;
import com.emot.model.EmotApplication;
import com.emot.persistence.ContactUpdater;

public class ContactScreen extends ActionBarActivity{
	private ListView listviewContact;
	private static String TAG = ContactScreen.class.getSimpleName();
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


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mConfig = EmotApplication.getConfig(this);
		setContentView(R.layout.contacts);
		listviewContact = (ListView)findViewById(R.id.listview_contact);
		showContactsThread = new ShowContacts();
		showContactsThread.execute();


		Intent i = new Intent();
		i.setAction("com.emot.services.ChatService");
		this.startService(i);
		contacts = new ArrayList<Contact>();
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
		Log.i(TAG, "Updating contacts !!!!");
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
