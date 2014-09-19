package com.emot.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDBHelper extends SQLiteOpenHelper{
	
	private static final String TAG = "ContactDBHelper";
	private static final int DATABASE_VERSION = 1;
	private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " +
			DBContract.ContactsDBEntry.TABLE_NAME +
			" (" + DBContract.ContactsDBEntry._ID + " INTEGER PRIMARY KEY," +
			DBContract.ContactsDBEntry.CURRENT_STATUS + " TEXT," +
			DBContract.ContactsDBEntry.LAST_SEEN + " TEXT," +
			DBContract.ContactsDBEntry.NAME + " TEXT," +
			DBContract.ContactsDBEntry.PROFILE_IMG + " TEXT" + " )";
	
	
	public ContactDBHelper(Context context){
		super(context, DBContract.ContactsDBEntry.DATABASE_NAME, null, DATABASE_VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_QUERY);
		
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS contacts");
		onCreate(db);
		
	}

}
