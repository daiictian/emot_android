package com.emot.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class EmoticonDBHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "emoticon.db";
	private static final String DATABASE_PATH = Environment.getDataDirectory().toString()+"/data/com.emot.screens/databases/";
	private static final String TAG = EmoticonDBHelper.class.getSimpleName();
	private static EmoticonDBHelper emoticonHelperInstance;

	Context context;

	public static final String SQL_CREATE_TABLE_EMOT = "CREATE VIRTUAL TABLE" +
			" " + DBContract.EmotsDBEntry.TABLE_NAME +
			" USING fts3 " +
			" (" + DBContract.EmotsDBEntry._ID + " INTEGER PRIMARY KEY autoincrement," +
			DBContract.EmotsDBEntry.EMOT_HASH + " VARCHAR(20)," +
			DBContract.EmotsDBEntry.EMOT_IMG + " BLOB," +
			DBContract.EmotsDBEntry.TAGS + " TEXT" + ")";

	public EmoticonDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	public EmoticonDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	public static EmoticonDBHelper getInstance(Context context ) {

		if(emoticonHelperInstance == null){
			emoticonHelperInstance = new EmoticonDBHelper(context);
		}
		Log.i(TAG, "In Instance get!!!");
		return emoticonHelperInstance;


	}

	public void copyDataBase() throws IOException{

		//Open your local db as the input stream
		InputStream myInput = context.getAssets().open(DATABASE_NAME);

		// Path to the just created empty db
		String outFileName = DATABASE_PATH + DATABASE_NAME;
		File databaseFile = new File( DATABASE_PATH);
        // check if databases folder exists, if not create one and its subfolders
        if (!databaseFile.exists()){
            databaseFile.mkdir();
        }

		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0){
			myOutput.write(buffer, 0, length);
		}

		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void createDatabase(){
		//db.execSQL("create table emoticons (_id INTEGER PRIMARY KEY autoincrement, emot_hash varchar(20), emot_img BLOB, tags TEXT)");
		boolean dbExist = checkDataBase();
		if(dbExist){
			Log.i(TAG, "DATABASE EXISTS");
			//do nothing - database already exist
		}else{
			Log.i(TAG, "DATABASE DOES NOT EXISTS");
			try {
				copyDataBase();
				Log.i(TAG, "Copied database successfully !!!");
			} catch (IOException e) {
				Log.i(TAG, "Error copying database");
				e.printStackTrace();
			}
		}
	}

	private boolean checkDataBase(){
		Log.i(TAG, "Checking DBBBB");
		SQLiteDatabase checkDB = null;
		try{
			String dbPath = DATABASE_PATH + DATABASE_NAME;
			checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
		}catch(Exception e){
			//database does't exist yet.
			Log.i(TAG, "Exception caught !!!");
			e.printStackTrace();
		}
		if(checkDB != null){
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
