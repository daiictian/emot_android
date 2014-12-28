package com.emot.screens;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.emot.adapters.ContactArrayAdapter;
import com.emot.androidclient.data.RosterProvider;
import com.emot.androidclient.data.RosterProvider.RosterConstants;
import com.emot.emotobjects.Contact;
import com.emot.model.EmotApplication;




public class GroupInfo extends Activity{
	
	private TextView currentSubject;
	private ArrayList<String> currentMembers;
	private String subject;
	
	private ContactArrayAdapter contactAdapter;
	private ListView currentList;
	private ShowContacts showContactsThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_info);
		currentSubject = (TextView)findViewById(R.id.currentGrpSubject);
		currentList = (ListView)findViewById(R.id.listviewMembers);
		subject = getIntent().getStringExtra("currentSubject");
		currentMembers = getIntent().getStringArrayListExtra("currentMembers");
		Log.i(TAG, "currentMembers are " +currentMembers);
		contacts = new ArrayList<Contact>();
		contactAdapter = new ContactArrayAdapter(EmotApplication.getAppContext(), R.layout.contact_row, contacts);
		currentList.setAdapter(contactAdapter);
		currentSubject.setText(subject);
		refreshContacts();
		
		
		
	}
	
	public void refreshContacts(){
		Log.i(TAG, "Refreshing contacts !!!!");
		showContactsThread = new ShowContacts();
		showContactsThread.execute();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	private static final String TAG = "GroupInfo";
	final static private String[] CONTACT_PROJECTION = new String[] {
		RosterConstants.ALIAS, RosterConstants.STATUS_MESSAGE,
		RosterConstants.AVATAR, RosterConstants.JID};
	private ArrayList<Contact> contacts;
	public class ShowContacts extends AsyncTask<Void, Contact, Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			try{
				Cursor cr = getContentResolver().query(RosterProvider.CONTENT_URI, CONTACT_PROJECTION, null, null, null);
				
				Log.i(TAG, "contacts found  = "+cr.getCount());
				while (cr.moveToNext()) {
					if(currentMembers.contains((String)cr.getString(cr.getColumnIndex(RosterConstants.JID)))){
				    Contact contact = new Contact(cr.getString(cr.getColumnIndex(RosterConstants.ALIAS)), cr.getString(cr.getColumnIndex(RosterConstants.JID)));
				    contact.setStatus(cr.getString(cr.getColumnIndex(RosterConstants.STATUS_MESSAGE)));
				    contact.setAvatar(cr.getBlob(cr.getColumnIndex(RosterConstants.AVATAR)));
				    publishProgress(contact);
					}
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

}
