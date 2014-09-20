package com.emot.model;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class EmotApplication extends Application {
	
	private static final String TAG = EmotApplication.class.getSimpleName();
	private static Context context;
	private static SharedPreferences prefs;

	public void onCreate() {
		super.onCreate();
		EmotApplication.context = getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(context
				.getApplicationContext());
	}
	
	public static Context getAppContext() {
		return EmotApplication.context;
	}
	
	public static boolean setValue(String k, String v) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(k, v);
		editor.commit();
		return true;
	}

	public static String getValue(String k, String d) {
		return prefs.getString(k, d);
	}
	
	public static String getAppID(){
		//Replace this with shared pref value
		return "edd7d5d6e48a699a240b57f4e1c2478e";
	}
}
