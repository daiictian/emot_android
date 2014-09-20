package com.emot.persistence;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

import com.emot.api.EmotHTTPClient;
import com.emot.common.TaskCompletedRunnable;
import com.emot.constants.WebServiceConstants;
import com.emot.model.EmotApplication;

public class ContactUpdater {

	private static String TAG = ContactUpdater.class.getSimpleName();
	@SuppressLint("InlinedApi")
	private final static String[] FROM_COLUMN = {
		Build.VERSION.SDK_INT
		>= Build.VERSION_CODES.HONEYCOMB ?
				Contacts.DISPLAY_NAME_PRIMARY :
					Contacts.DISPLAY_NAME
	};


	public static void updateContacts(final TaskCompletedRunnable taskCompleteHandler){
		final HashMap<String, String> contacts;
		Log.i(TAG, "Context "+EmotApplication.getAppContext());
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

			cUrl = new URL(WebServiceConstants.HTTP + "://"+ 
					WebServiceConstants.SERVER_IP+":"+WebServiceConstants.SERVER_PORT
					+WebServiceConstants.PATH_API+WebServiceConstants.OP_GETCONTACT);
			Log.i(TAG, "URL "+cUrl);
		}catch(MalformedURLException e){
			e.printStackTrace();
			return;
		}
		Log.i(TAG, "Calling API ...");
		EmotHTTPClient contactCall = new EmotHTTPClient(cUrl, params , new TaskCompletedRunnable() {

			@Override
			public void onTaskComplete(String result) {
				//Put this in database
				Log.i(TAG, "Result "+result.toString());

				SQLiteDatabase db = EmotDBHelper.getInstance(EmotApplication.getAppContext()).getWritableDatabase();
				try {
					JSONArray emotters = new JSONArray(result);
					int len = emotters.length();
					ContentValues cvs = new ContentValues();
					for(int i=0; i<len; i++){
						try {
							JSONObject emotter = emotters.getJSONObject(i);
							cvs.put(DBContract.ContactsDBEntry.MOBILE_NUMBER, emotter.getString("mobile"));
							cvs.put(DBContract.ContactsDBEntry.EMOT_NAME, emotter.getString("name"));
							cvs.put(DBContract.ContactsDBEntry.CONTACT_NAME, contacts.get(emotter.getString("mobile")));
							cvs.put(DBContract.ContactsDBEntry.PROFILE_IMG, emotter.getString("profile_image"));
							db.insert(DBContract.ContactsDBEntry.TABLE_NAME, null, cvs);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}finally{
					db.close();
				}
				
				
				taskCompleteHandler.onTaskComplete(result);
				Log.i(TAG, "Db ran ... ");
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
			String mobile = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("-", "");
			//Log.i(TAG, "name = " + name + ". Phone = " + mobile); 
			contacts.put(mobile,  name);
		}
		return contacts;
	}
}
