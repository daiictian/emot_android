package com.emot.persistence;

import android.provider.BaseColumns;

//cannot be subclassed
public final class EmotHistoryContract {
	
	public EmotHistoryContract(){}
	
	public static abstract class EmotHistoryEntry implements BaseColumns{
		
		public static final String TABLE_NAME = "emothistory";
        public static final String ENTRY_ID = "mobilenumber";
        public static final String DATE = "date";
        public static final String TIME = "time";
        public static final String EMOTS = "emots";
        public static final String DATABASE_NAME = " emothistory.db";
	}

}
