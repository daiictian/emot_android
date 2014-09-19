package com.emot.screens;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.widget.ListView;

import com.emot.adapters.ContactArrayAdapter;
import com.emot.emotobjects.Contact;

public class ContactScreen extends Activity {
	private ListView listviewContact;
	private ArrayList<Contact> contacts = new ArrayList<Contact>();
	private static String TAG = ContactScreen.class.getName();
	
	@SuppressLint("InlinedApi")
	private final static String[] FROM_COLUMN = {
        Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.HONEYCOMB ?
                Contacts.DISPLAY_NAME_PRIMARY :
                Contacts.DISPLAY_NAME
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);
		listviewContact = (ListView)findViewById(R.id.listview_contact);
		setContacts(this.getContentResolver());
		ContactArrayAdapter contactAdapter = new ContactArrayAdapter(this.getApplicationContext(), R.layout.chat_row, contacts);
		listviewContact.setAdapter(contactAdapter);
	}
	
	public void setContacts(ContentResolver cr)
	{
	    Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
	    while (phones.moveToNext())
	    {
	      String name=phones.getString(phones.getColumnIndex(FROM_COLUMN[0]));
	      String mobile = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	      Log.i(TAG,"name = " + name + ". Phone = " + mobile); 
	      contacts.add(new Contact(name, mobile));
	    }

	}
}
