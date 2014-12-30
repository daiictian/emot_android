package com.emot.androidclient.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.text.format.DateFormat;

public class EmotUtils {

	public static String generateRoomID(){
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}

	
	public static String getTimeSimple() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date();
		return getTimeSimple(dateFormat.format(date));
	}
	
	public static String getTimeSimple(String dt){
		try {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date inputDate;
			inputDate = fmt.parse(dt);
			
			Calendar smsTime = Calendar.getInstance();
		    smsTime.setTimeInMillis(inputDate.getTime());
		    Calendar now = Calendar.getInstance();
		    final String timeFormatString = "h:mm aa";
		    final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
		    final long HOURS = 60 * 60 * 60;
		    if(now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) ){
		        return "Today " + DateFormat.format(timeFormatString, smsTime);
		    }else if(now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1 ){
		        return "Yesterday " + DateFormat.format(timeFormatString, smsTime);
		    }else if(now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)){
		        return DateFormat.format(dateTimeFormatString, smsTime).toString();
		    }else
		        return DateFormat.format("MMMM dd yyyy, h:mm aa", smsTime).toString();
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
		
		
		
	}
}
