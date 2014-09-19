package com.emot.persistence;

import android.provider.BaseColumns;

//cannot be subclassed
public final class DBContract {
	
	public DBContract(){}
	
	public static abstract class EmotHistoryEntry implements BaseColumns{
		
		public static final String TABLE_NAME = "emothistory";
        public static final String ENTRY_ID = "mobilenumber";
        public static final String DATE = "date";
        public static final String TIME = "time";
        public static final String EMOTS = "emots";
        public static final String DATABASE_NAME = " emot.db";
	}
	
	public static abstract class ContactsDBEntry implements BaseColumns{
		
		public static final String TABLE_NAME = "contactsdetails";
        public static final String NAME = "contactName";
        public static final String LAST_SEEN = "lastSeen";
        public static final String PROFILE_IMG = "profileImage";
        public static final String CURRENT_STATUS = "currentStatus";
        public static final String DATABASE_NAME = " emothistory.db";
	}

}
