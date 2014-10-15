package com.emot.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.emot.androidclient.util.PreferenceConstants;
import com.emot.screens.R;

public class EmotUser {
	
	private static String TAG = EmotUser.class.getSimpleName();
	
	public static void setStatus(String status){
		
	}

	public static void setAvatar(Bitmap image){
		
	}
	
	public static Bitmap getAvatar(){
		String img = EmotApplication.getValue(PreferenceConstants.USER_AVATAR, null);
		Bitmap bitmap;
		Log.i(TAG, "img is "+img);
		if(img==null){
			bitmap = BitmapFactory.decodeResource(EmotApplication.getAppContext().getResources(), R.drawable.blank_user_image);
		}else{
			byte[] bArray =  Base64.decode(img, Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(bArray , 0, bArray.length);
		}
		
		return bitmap;
	}
	
	public static String getStatus(){
		return EmotApplication.getValue(PreferenceConstants.STATUS_MESSAGE, "Default status");
	}

}
