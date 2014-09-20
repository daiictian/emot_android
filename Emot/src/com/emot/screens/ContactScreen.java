package com.emot.screens;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.ListView;

import com.emot.common.TaskCompletedRunnable;
import com.emot.model.EmotApplication;
import com.emot.persistence.ContactUpdater;
import com.emot.persistence.DBContract;
import com.emot.persistence.EmotDBHelper;

public class ContactScreen extends Activity {
	private ListView listviewContact;
	private static String TAG = ContactScreen.class.getName();
	private SimpleCursorAdapter contactsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);
		listviewContact = (ListView)findViewById(R.id.listview_contact);
		SQLiteDatabase db = EmotDBHelper.getInstance(EmotApplication.getAppContext()).getWritableDatabase();
		ContactUpdater.updateContacts(new TaskCompletedRunnable() {
			
			@Override
			public void onTaskComplete(String result) {
				//Contacts updated in SQLite. You might want to update UI
			}
		});
		Cursor c = null;
		
		c = db.rawQuery("Select * from "+DBContract.ContactsDBEntry.TABLE_NAME, null);
		Log.i(TAG, "Count of contacts " + c.getCount());
		String[] from = {DBContract.ContactsDBEntry.CONTACT_NAME, DBContract.ContactsDBEntry.MOBILE_NUMBER, DBContract.ContactsDBEntry.CURRENT_STATUS};
		int[] to = {R.id.text_contact_name, R.id.text_contact_number, R.id.text_contact_status};
		contactsAdapter = new SimpleCursorAdapter (this, R.layout.contact_row, c, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		listviewContact.setAdapter(contactsAdapter);
	}
	
	
}
