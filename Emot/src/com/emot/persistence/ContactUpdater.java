package com.emot.persistence;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.VCard;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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
	private static HashMap<String, String> contacts;
	@SuppressLint("InlinedApi")
	private final static String[] FROM_COLUMN = {
		Build.VERSION.SDK_INT
		>= Build.VERSION_CODES.HONEYCOMB ?
				Contacts.DISPLAY_NAME_PRIMARY :
					Contacts.DISPLAY_NAME
	};


	public static void updateContacts(final TaskCompletedRunnable taskCompleteHandler){
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
				//result = "[{\"hbjh\"}]";
				//Log.i(TAG, "Result "+result.toString());

				SQLiteDatabase db = EmotDBHelper.getInstance(EmotApplication.getAppContext()).getWritableDatabase();
				try {
					AddRoster addRoster = new AddRoster(new JSONArray(result));
					addRoster.execute();
					
					/*
					JSONArray emotters = new JSONArray(result);
					int len = emotters.length();
					
					for(int i=0; i<len; i++){
						try {
							
							JSONObject emotter = emotters.getJSONObject(i);
							
							//Database entry
							ContentValues cvs = new ContentValues();
							cvs.put(DBContract.ContactsDBEntry.MOBILE_NUMBER, emotter.getString("mobile"));
							cvs.put(DBContract.ContactsDBEntry.EMOT_NAME, emotter.getString("name"));
							cvs.put(DBContract.ContactsDBEntry.CONTACT_NAME, contacts.get(emotter.getString("mobile")));
							cvs.put(DBContract.ContactsDBEntry.PROFILE_IMG, emotter.getString("profile_image"));
							cvs.put(DBContract.ContactsDBEntry.PROFILE_THUMB, emotter.getString("profile_thumbnail"));
							db.insertWithOnConflict(DBContract.ContactsDBEntry.TABLE_NAME, null, cvs, SQLiteDatabase.CONFLICT_REPLACE);
							updateProfileBitmap(emotter.getString("profile_image"), emotter.getString("mobile"));
							
							//roster.createEntry(emotter.getString("mobile"), "emot-net", null);
							
							
						} catch (JSONException e) {
							e.printStackTrace();
						} 
					}
					*/
				}catch (JSONException e1) {
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
		HashMap<String, String> cntcts = new HashMap<String, String>();
		Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
		while (phones.moveToNext())
		{
			String name=phones.getString(phones.getColumnIndex(FROM_COLUMN[0]));
			String mobile = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("-", "");
			//Log.i(TAG, "name = " + name + ". Phone = " + mobile); 
			cntcts.put(mobile,  name);
		}
		return cntcts;
	}
	
	public static void updateProfileBitmap(String url, String mobile){
		if(url!=null && url != ""){
			
		}
	}
	
	public static class AddRoster extends AsyncTask<Void, Void, Void>{

		private JSONArray emotters;
		
		public AddRoster(JSONArray emotters){
			this.emotters = emotters;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			int len = emotters.length();
//			while (EmotApplication.getConnection()==null || !EmotApplication.getConnection().isAuthenticated()){
//				//Wait till connected
//				Log.i(TAG, "Wait for connection to establish");
//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			
			SQLiteDatabase db = EmotDBHelper.getInstance(EmotApplication.getAppContext()).getWritableDatabase();
			for(int i=0; i<len; i++){
				try {
					
					JSONObject emotter = emotters.getJSONObject(i);
					
					//Database entry
					ContentValues cvs = new ContentValues();
					cvs.put(DBContract.ContactsDBEntry.MOBILE_NUMBER, emotter.getString("mobile"));
					cvs.put(DBContract.ContactsDBEntry.EMOT_NAME, emotter.getString("name"));
					cvs.put(DBContract.ContactsDBEntry.CONTACT_NAME, contacts.get(emotter.getString("mobile")));
					//cvs.put(DBContract.ContactsDBEntry.PROFILE_IMG, emotter.getString("profile_image"));
					//cvs.put(DBContract.ContactsDBEntry.PROFILE_THUMB, emotter.getString("profile_thumbnail"));
					db.insertWithOnConflict(DBContract.ContactsDBEntry.TABLE_NAME, null, cvs, SQLiteDatabase.CONFLICT_REPLACE);
					//updateProfileBitmap(emotter.getString("profile_image"), emotter.getString("mobile"));
					Log.i(TAG, "Putting in DB "+emotter.getString("mobile"));
					
				} catch (JSONException e) {
					e.printStackTrace();
				} 
			}
			return null;
		}
		
	}
	
}
