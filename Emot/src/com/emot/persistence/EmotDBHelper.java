package com.emot.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.emot.persistence.DBContract;;

public class EmotDBHelper extends SQLiteOpenHelper{

	private static final int DATABASE_VERSION = 0;
	private static final String TAG = null;
	private static  String SQL_CREATE_TABLE_EMOTHISTORY = "CREATE TABLE IF NOT EXISTS " +
			DBContract.EmotHistoryEntry.TABLE_NAME +
			" (" + DBContract.EmotHistoryEntry._ID + " INTEGER PRIMARY KEY," +
			DBContract.EmotHistoryEntry.ENTRY_ID + " TEXT," +
			DBContract.EmotHistoryEntry.EMOTS + " TEXT," +
			DBContract.EmotHistoryEntry.DATE + " TEXT," +
			DBContract.EmotHistoryEntry.TIME + " TEXT" + " )";
	
	private static String SQL_CREATE_TABLE_CONTACTDETAILS = "CREATE TABLE IF NOT EXISTS " +
			DBContract.ContactsDBEntry.TABLE_NAME +
			" (" + DBContract.ContactsDBEntry._ID + " INTEGER PRIMARY KEY," +
			DBContract.ContactsDBEntry.CURRENT_STATUS + " TEXT," +
			DBContract.ContactsDBEntry.LAST_SEEN + " TEXT," +
			DBContract.ContactsDBEntry.NAME + " TEXT," +
			DBContract.ContactsDBEntry.PROFILE_IMG + " TEXT" + " )";
	private static EmotDBHelper emotDBHelperInstance;
	
	private EmotDBHelper(Context context) {
		super(context, DBContract.EmotHistoryEntry.DATABASE_NAME, null, DATABASE_VERSION);
		
	}

	public static EmotDBHelper getInstance(Context context ) {
		
		if(emotDBHelperInstance == null){
			emotDBHelperInstance = new EmotDBHelper(context);
		}
		
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
		db.execSQL(SQL_CREATE_TABLE_EMOTHISTORY);
		db.execSQL(SQL_CREATE_TABLE_CONTACTDETAILS);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS contacts");
		onCreate(db);

		
	}

}
