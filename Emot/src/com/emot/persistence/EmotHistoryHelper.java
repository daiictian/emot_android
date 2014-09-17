package com.emot.persistence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.emot.emotobjects.CurrentEmot;
import com.emot.persistence.EmotHistoryContract;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EmotHistoryHelper extends SQLiteOpenHelper {
	private static final String TAG = "EmotHistoryHelper";
	private static final int DATABASE_VERSION = 1;
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS " +
			EmotHistoryContract.EmotHistoryEntry.TABLE_NAME +
			" (" + EmotHistoryContract.EmotHistoryEntry._ID + " INTEGER PRIMARY KEY," +
			EmotHistoryContract.EmotHistoryEntry.ENTRY_ID + " TEXT," +
			EmotHistoryContract.EmotHistoryEntry.EMOTS + " TEXT," +
			EmotHistoryContract.EmotHistoryEntry.DATE + " TEXT," +
			EmotHistoryContract.EmotHistoryEntry.TIME + " TEXT" + " )";
	



	public EmotHistoryHelper(Context context) {
		super(context, EmotHistoryContract.EmotHistoryEntry.DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	
	public Cursor getEmotHistory(final String entryID){
		Log.i(TAG,SQL_CREATE_ENTRIES );
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor emots = db.rawQuery("SELECT " + EmotHistoryContract.EmotHistoryEntry.EMOTS + " from " +
				EmotHistoryContract.EmotHistoryEntry.TABLE_NAME + " where " + 
				EmotHistoryContract.EmotHistoryEntry.ENTRY_ID + " = '" + entryID  + "'", null);
		
		return emots;
		
	}
	
	public void insertChat(final String entryID, final String chat, final String date, final String time){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(EmotHistoryContract.EmotHistoryEntry.ENTRY_ID, entryID);
		contentValues.put(EmotHistoryContract.EmotHistoryEntry.EMOTS, chat);
		contentValues.put(EmotHistoryContract.EmotHistoryEntry.DATE, date);	
		contentValues.put(EmotHistoryContract.EmotHistoryEntry.TIME, time);
		
		long i = db.insert(EmotHistoryContract.EmotHistoryEntry.TABLE_NAME, null, contentValues);
		if(i < 0){
			Log.i(TAG,"Could not insert chat");
		}else{
			Log.i(TAG,"Chat inserted successfully");
		}

	}
	
	private  List<String> getUsers(final SQLiteDatabase db){
		
		String sql = "SELECT DISTINCT " + EmotHistoryContract.EmotHistoryEntry.ENTRY_ID +
					 " from " + EmotHistoryContract.EmotHistoryEntry.TABLE_NAME;
		Log.i(TAG,sql);
		Cursor usersList = db.rawQuery(sql, null);
		usersList.moveToFirst();
		List<String> users = new ArrayList<String>();
		Log.i(TAG,"sersList.isAfterLast() :" + usersList.isAfterLast());
		while(usersList.isAfterLast() == false){
			
			users.add(usersList.getString(usersList.getColumnIndex(EmotHistoryContract.EmotHistoryEntry.ENTRY_ID)));
			usersList.moveToNext();
		}
		
		return users;
		
	}
	
	public List<CurrentEmot> getCurrentEmots(){
		SQLiteDatabase db = this.getReadableDatabase();
		List<CurrentEmot> currentEmots = new ArrayList<CurrentEmot>();
		CurrentEmot currentEmot = new CurrentEmot(); 
		String user = null;
		String chat = null;
		Cursor userLastEmot = null;
		String sql = null;
		List<String> usersList = getUsers(db);
		Log.i(TAG,"userList size is " +usersList.size());
		Iterator<String> iterator = usersList.iterator();
		while(iterator.hasNext()){
			user = iterator.next();
			Log.i(TAG,"user is " +user);
			sql = prepareSQLforEntryID(user);
			userLastEmot = db.rawQuery(sql, null);
			userLastEmot.moveToFirst();
			chat = userLastEmot.getString(userLastEmot.getColumnIndex(EmotHistoryContract.EmotHistoryEntry.EMOTS));
			Log.i(TAG,"chat  is " +chat);
			currentEmot.setUserLastEmot(chat);
			currentEmot.setUserName(user);
			currentEmots.add(currentEmot);
		}
		
		
		return currentEmots;
		
		
	}
	
	//prepare SQL statements for user id, actually termed as entryID
	private String prepareSQLforEntryID(final String entryID){
		
		String sql = "SELECT " + EmotHistoryContract.EmotHistoryEntry.EMOTS + " from " +
				EmotHistoryContract.EmotHistoryEntry.TABLE_NAME + " where " + 
				EmotHistoryContract.EmotHistoryEntry.ENTRY_ID + " = '" + entryID  + "'";
		
		return sql;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES)	;
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS contacts");
		onCreate(db);

	}

}