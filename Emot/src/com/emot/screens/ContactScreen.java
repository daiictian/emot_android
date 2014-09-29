package com.emot.screens;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.VCard;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.emot.adapters.ContactArrayAdapter;
import com.emot.common.TaskCompletedRunnable;
import com.emot.constants.IntentStrings;
import com.emot.emotobjects.ConnectionQueue;
import com.emot.emotobjects.Contact;
import com.emot.model.EmotApplication;
import com.emot.persistence.ContactUpdater;
import com.emot.persistence.DBContract;
import com.emot.persistence.EmotDBHelper;

public class ContactScreen extends ActionBarActivity{
	private ListView listviewContact;
	private static String TAG = ContactScreen.class.getName();
	private ContactArrayAdapter contactsAdapter;
	private ArrayList<Contact> contacts;
	private XMPPConnection connection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);
		listviewContact = (ListView)findViewById(R.id.listview_contact);
		
//		ContactUpdater.updateContacts(new TaskCompletedRunnable() {
//
//			@Override
//			public void onTaskComplete(String result) {
//				//Contacts updated in SQLite. You might want to update UI
//			}
//		});
		
		contacts = new ArrayList<Contact>();
		contactsAdapter = new ContactArrayAdapter(EmotApplication.getAppContext(), R.layout.contact_row, contacts);
		listviewContact.setAdapter(contactsAdapter);
		
		Thread connectionThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
					//mConnectionQueue = EmotApplication.mConnectionQueue;
					Log.i(TAG, "b4 connection retreived " +connection + " ");
					while(true){
						connection = ConnectionQueue.get();
						Log.i(TAG, "after connection retreived " +connection);
					}
					
					
					//mCurrentChat = connection.getChatManager();
					//mChat = mCurrentChat.createChat("test6@emot-net", "test6@emot-net",mmlistener);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Log.i(TAG, "Queue exception ...");
					e.printStackTrace();
				}
				
			}
		});
		connectionThread.setName("Connection Thread");
		connectionThread.start();
		
		//new UpdateRosters().execute();
		new ShowContacts().execute();
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
				
			}
		});

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
	
	
	public class ShowContacts extends AsyncTask<Void, Contact, Void>{

		@Override
		protected Void doInBackground(Void... params) {
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

}
