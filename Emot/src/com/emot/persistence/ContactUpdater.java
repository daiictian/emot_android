package com.emot.persistence;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

import com.emot.api.EmotHTTPClient;
import com.emot.common.TaskCompletedRunnable;
import com.emot.model.EmotApplication;

public class ContactUpdater {
	
	private static String TAG = ContactUpdater.class.getName();
	@SuppressLint("InlinedApi")
	private final static String[] FROM_COLUMN = {
        Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.HONEYCOMB ?
                Contacts.DISPLAY_NAME_PRIMARY :
                Contacts.DISPLAY_NAME
	};
	
	
	public static void updateContacts(final TaskCompletedRunnable taskCompleteHandler){
		HashMap<String, String> contacts;
		contacts = getContacts(EmotApplication.getAppContext().getContentResolver());
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("appid", EmotApplication.getAppID()));
		JSONArray numbers = new JSONArray();
		Set<String> numberSet = contacts.keySet();
		for(String number: numberSet){
			numbers.put(number);
		}
		params.add(new BasicNameValuePair("number_list", numbers.toString()));
		URL cUrl;
		try{
			cUrl = new URL("/api/getemotters/");
		}catch(MalformedURLException e){
			e.printStackTrace();
			return;
		}
		EmotHTTPClient contactCall = new EmotHTTPClient(cUrl, params , new TaskCompletedRunnable() {
			
			@Override
			public void onTaskComplete(Object result) {
				//Put this in database
				Log.i(TAG, "Result "+result.toString());
				taskCompleteHandler.onTaskComplete(result);
			}
		});
		contactCall.execute();
	}
	
	public static HashMap<String, String> getContacts(ContentResolver cr)
	{
		HashMap<String, String> contacts = new HashMap<String, String>();
	    Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
	    while (phones.moveToNext())
	    {
	      String name=phones.getString(phones.getColumnIndex(FROM_COLUMN[0]));
	      String mobile = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	      Log.i(TAG, "name = " + name + ". Phone = " + mobile); 
	      contacts.put(mobile,  name);
	    }
	    return contacts;
	}
}
