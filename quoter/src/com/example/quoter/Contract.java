package com.example.quoter;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Contract {
	
	public static final String AUTHORITY = "com.example.quoter";
	
	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
	
	public interface QuoteColums {
		public static final String ID = "_id";
		public static final String AUTHOR = "author";
		public static final String TEXT = "text";
		public static final String QUOTE = "quote";
	}
	
	public static final class Quotes implements BaseColumns, QuoteColums {
		public static final String CONTENT_PATH = "quotes";
		public static final String LIKED_CONTENT_PATH = "savedquotes";		
		public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_PATH);
		
		public static final Uri LIKED_CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, LIKED_CONTENT_PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CONTENT_PATH;
	}

}
