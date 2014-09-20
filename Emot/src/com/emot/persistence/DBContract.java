package com.emot.persistence;

import android.provider.BaseColumns;

//cannot be subclassed
public final class DBContract {
	
	public DBContract(){}
	
	public static abstract class EmotHistoryEntry implements BaseColumns{
		
		public static final String TABLE_NAME = "emothistory";
        public static final String ENTRY_ID = "mobile";
        public static final String DATETIME = "date";
        public static final String EMOTS = "emots";
	}
	
	public static abstract class ContactsDBEntry implements BaseColumns{
		
		public static final String TABLE_NAME = "contacts";
        public static final String EMOT_NAME = "emot_name";
        public static final String CONTACT_NAME = "contact_name";
        public static final String MOBILE_NUMBER = "mobile";
        public static final String LAST_SEEN = "last_seen";
        public static final String PROFILE_IMG = "profile_image";
        public static final String CURRENT_STATUS = "current_status";
	}

}
