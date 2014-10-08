package com.emot.model;

import java.io.ByteArrayOutputStream;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.VCard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.emot.constants.PreferenceKeys;
import com.emot.screens.R;

public class EmotUser {
	
	private static String TAG = EmotUser.class.getSimpleName();
	
	public static void setStatus(String status){
		
	}

	public static void setAvatar(Bitmap image){
		
	}
	
	public static Bitmap getAvatar(){
		String img = EmotApplication.getValue(PreferenceKeys.USER_AVATAR, null);
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
		return EmotApplication.getValue(PreferenceKeys.USER_STATUS, "Default status");
	}
	
	public static void updateAvatar(Bitmap bmp){
		EmotApplication.configure(ProviderManager.getInstance());
		VCard vCard = new VCard();
		try {
			//Bitmap bmp = BitmapFactory.decodeResource(EmotApplication.getAppContext().getResources(), R.drawable.asin);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] bytes = stream.toByteArray();
            String encodedImage = StringUtils.encodeBase64(bytes);
            vCard.setAvatar(bytes, encodedImage);
           // vCard.setEncodedImage(encodedImage); 
            vCard.setField("PHOTO", 
            		"<TYPE>image/jpg</TYPE><BINVAL>"
                    + encodedImage + 
                    "</BINVAL>", 
                    true);
            vCard.save(EmotApplication.getConnection());
            EmotApplication.setValue(PreferenceKeys.USER_AVATAR, encodedImage);
            Log.i(TAG, "Setting preference value ...");
        }  catch (XMPPException e) {
			e.printStackTrace();
		}	catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void updateStatus(String status){
		Presence presence = new Presence(Presence.Type.available, status, 1, Mode.available);
		EmotApplication.getConnection().sendPacket(presence);
		EmotApplication.setValue(PreferenceKeys.USER_STATUS, status);
	}
}
