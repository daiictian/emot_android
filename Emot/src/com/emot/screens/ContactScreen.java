package com.emot.screens;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.VCardProvider;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.emot.adapters.ContactArrayAdapter;
import com.emot.emotobjects.Contact;
import com.emot.model.EmotApplication;
import com.emot.persistence.EmotDBHelper;

public class ContactScreen extends Activity {
	private ListView listviewContact;
	private static String TAG = ContactScreen.class.getName();
	private ContactArrayAdapter contactsAdapter;
	private ArrayList<Contact> contacts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);
		listviewContact = (ListView)findViewById(R.id.listview_contact);
		SQLiteDatabase db = EmotDBHelper.getInstance(EmotApplication.getAppContext()).getWritableDatabase();
		
		/*
		ContactUpdater.updateContacts(new TaskCompletedRunnable() {

			@Override
			public void onTaskComplete(String result) {
				//Contacts updated in SQLite. You might want to update UI
			}
		});
		*/
		
		contacts = new ArrayList<Contact>();
		contactsAdapter = new ContactArrayAdapter(EmotApplication.getAppContext(), R.layout.contact_row, contacts);
		listviewContact.setAdapter(contactsAdapter);
		new UpdateRosters().execute();
		
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
			Log.i(TAG, "time 2");
			Roster roster = EmotApplication.getConnection().getRoster();
			Log.i(TAG, "time 3");
			Collection<RosterEntry> entries = roster.getEntries();
			Log.i(TAG, "time 4");
			for (RosterEntry entry : entries) {
				Log.i(TAG, "Entry = Name: "+entry.getName() + " User: " + entry.getUser() + " Status: " + entry.getStatus() + " Type: " + entry.getName());
				Contact contact = new Contact(entry.getName(), entry.getUser());
				contact.setStatus(" ... ");
				
				EmotApplication.configure(ProviderManager.getInstance());
				Log.i(TAG, "-------------------");
				VCard vCard = new VCard();
				try {
					vCard.load(EmotApplication.getConnection(), entry.getUser());
					byte[] avatar = vCard.getAvatar();
					Log.i(TAG, "avatar = "+avatar);
					contact.setAvatar(avatar);
				} catch (XMPPException e) {
					e.printStackTrace();
				}
				Log.i(TAG, "Nick name 1 = " + vCard.getNickName());
				
				publishProgress(contact);
			}
			
			
			Log.i(TAG, "time 5");
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
