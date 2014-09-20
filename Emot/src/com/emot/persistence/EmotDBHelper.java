package com.emot.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EmotDBHelper extends SQLiteOpenHelper{

	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "emot.db";
	private static final String TAG = null;
	private static final  String SQL_CREATE_TABLE_EMOTHISTORY = "CREATE TABLE IF NOT EXISTS " +
			DBContract.EmotHistoryEntry.TABLE_NAME +
			" (" + DBContract.EmotHistoryEntry._ID + " INTEGER PRIMARY KEY," +
			DBContract.EmotHistoryEntry.ENTRY_ID + " VARCHAR(20)," +
			DBContract.EmotHistoryEntry.EMOTS + " TEXT," +
			DBContract.EmotHistoryEntry.DATETIME + " DATETIME" + " )";
	
	private static final String SQL_CREATE_TABLE_CONTACTDETAILS = "CREATE TABLE IF NOT EXISTS " +
			DBContract.ContactsDBEntry.TABLE_NAME +
			" (" + DBContract.ContactsDBEntry._ID + " INTEGER PRIMARY KEY autoincrement," +
			DBContract.ContactsDBEntry.CURRENT_STATUS + " VARCHAR(100) NULL," +
			DBContract.ContactsDBEntry.LAST_SEEN + " DATETIME NULL," +
			DBContract.ContactsDBEntry.EMOT_NAME + " VARCHAR(100) NULL," +
			DBContract.ContactsDBEntry.CONTACT_NAME + " VARCHAR(100) NULL," +
			DBContract.ContactsDBEntry.MOBILE_NUMBER + " VARCHAR(20)," +
			DBContract.ContactsDBEntry.PROFILE_IMG + " VARCHAR(100) NULL" + " )";
	private static EmotDBHelper emotDBHelperInstance;
	
	private EmotDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}

	public static EmotDBHelper getInstance(Context context ) {
		
		if(emotDBHelperInstance == null){
			emotDBHelperInstance = new EmotDBHelper(context);
		}
		Log.d(TAG, "In Instance get!!!");
		return emotDBHelperInstance;
		
	
	}
	
	public Cursor runQuery(final SQLiteDatabase db, final String sql){
		
		Cursor result = db.rawQuery(sql, null);
		return result;
	}
	
	public void insert(final String tableName, final ContentValues contentValues){
		SQLiteDatabase db = this.getWritableDatabase();
		long i = db.insert(tableName, null, contentValues);
		if(i < 0){
			Log.i(TAG,"Could not insert records into table "+tableName);
		}else{
			Log.i(TAG,"Records inserted successfully into table "+tableName);
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "Tables created !!!");
		db.execSQL(SQL_CREATE_TABLE_EMOTHISTORY);
		db.execSQL(SQL_CREATE_TABLE_CONTACTDETAILS);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}

}
