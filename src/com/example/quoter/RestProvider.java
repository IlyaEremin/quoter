package com.example.quoter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.example.quoter.Contract.Quotes;

public class RestProvider extends ContentProvider {

	final String TAG = getClass().getSimpleName();
	
	private static String TABLE_QUOTES = "quotes";
	private static String TABLE_LIKED_QUOTES = "savedquotes";
	
	private static final String DB_NAME = "quoter.db";
	private static final int DB_VERSION = 1;
	
	private String table;
	private Uri contentUri;
	
	private static final String CREATE_QUOTES_TABLE = 				
			"create table if not exists " + TABLE_QUOTES + " (" + 
			Quotes.ID + " integer primary key, " +
			Quotes.AUTHOR + " text, " +
			Quotes.TEXT + " text " + ")";
		private static final String CREATE_LIKED_QUOTES_TABLE =
			"create table if not exists " + TABLE_LIKED_QUOTES + " (" + 
			Quotes.ID + " integer primary key, " +
			Quotes.AUTHOR + " text, " +
			Quotes.TEXT + " text " + ")";
	
	private static final UriMatcher sUriMatcher;
	
	private static final int PATH_ROOT = 0;
	private static final int PATH_QUOTES = 1;
	private static final int PATH_LIKED_QUOTES = 2;
	
	static {
		sUriMatcher = new UriMatcher(PATH_ROOT);
		sUriMatcher.addURI(Contract.AUTHORITY, Contract.Quotes.CONTENT_PATH, PATH_QUOTES); 
		sUriMatcher.addURI(Contract.AUTHORITY, Contract.Quotes.LIKED_CONTENT_PATH, PATH_LIKED_QUOTES);
	}
	
	private DatabaseHelper mDatabaseHelper;
	
	class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_QUOTES_TABLE);
			db.execSQL(CREATE_LIKED_QUOTES_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
		
	}

	@Override
	public boolean onCreate() {
		mDatabaseHelper = new DatabaseHelper(getContext(), DB_NAME, null, DB_VERSION);
		return true;
	}
	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		switch (sUriMatcher.match(uri)) {
		case PATH_QUOTES: {
			Cursor cursor = mDatabaseHelper.getReadableDatabase().query(TABLE_QUOTES, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), Contract.Quotes.CONTENT_URI);
			return cursor;
		}
		case PATH_LIKED_QUOTES: {
			Cursor cursor = mDatabaseHelper.getReadableDatabase().query(TABLE_LIKED_QUOTES, projection, selection, selectionArgs, null, null, sortOrder);
			return cursor;
		}
		default:
			return null;
		}
		
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case PATH_QUOTES:
		case PATH_LIKED_QUOTES: {
			return Contract.Quotes.CONTENT_TYPE;
		}
		default:
			return null;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (sUriMatcher.match(uri)) {
		case PATH_QUOTES: {
			table = TABLE_QUOTES;
			contentUri = Contract.Quotes.CONTENT_URI;
			break;
		}
		case PATH_LIKED_QUOTES: {
			table = TABLE_LIKED_QUOTES;
			contentUri = Contract.Quotes.LIKED_CONTENT_URI;
			break;
		}
		}
		mDatabaseHelper.getWritableDatabase().insert(table, null, values);
		getContext().getContentResolver().notifyChange(contentUri, null);
		return null;
		
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
		case PATH_QUOTES:
			table = TABLE_QUOTES;
			contentUri = Contract.Quotes.CONTENT_URI;
			break;
		case PATH_LIKED_QUOTES: {
			table = TABLE_LIKED_QUOTES;
			contentUri = Contract.Quotes.LIKED_CONTENT_URI;
			break;
		}
		default:
			return 0;
		}
		return mDatabaseHelper.getWritableDatabase().delete(table, selection, selectionArgs);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
		case PATH_QUOTES:
			table = TABLE_QUOTES;
			contentUri = Contract.Quotes.CONTENT_URI;
			break;
		case PATH_LIKED_QUOTES: {
			table = TABLE_LIKED_QUOTES;
			contentUri = Contract.Quotes.LIKED_CONTENT_URI;
			break;
		}
		default:
			return 0;
		}
		return mDatabaseHelper.getWritableDatabase().update(table, values, selection, selectionArgs);
	}


}
