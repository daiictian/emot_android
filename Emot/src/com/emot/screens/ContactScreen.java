package com.emot.screens;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListView;

import com.emot.common.TaskCompletedRunnable;
import com.emot.persistence.ContactUpdater;

public class ContactScreen extends Activity {
	private ListView listviewContact;
	private static String TAG = ContactScreen.class.getName();
	private SimpleCursorAdapter contactsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);
		listviewContact = (ListView)findViewById(R.id.listview_contact);
		ContactUpdater.updateContacts(new TaskCompletedRunnable() {
			
			@Override
			public void onTaskComplete(Object result) {
				//Contacts updated in SQLLite. You might want to update UI
			}
		});
		Cursor c = null;
		c = dbPointer.runRawQuery("Select * from contact");
		String[] from = {"name", "mobile", "status"};
		int[] to = {R.id.text_contact_name, R.id.text_contact_number, R.id.text_contact_status};
		contactsAdapter = new SimpleCursorAdapter (this, R.layout.contact_row, c, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		listviewContact.setAdapter(contactsAdapter);
	}
	
	
}
