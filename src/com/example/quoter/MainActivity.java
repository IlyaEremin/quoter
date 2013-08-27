package com.example.quoter;


import java.lang.reflect.Field;

import ru.sunsoft.quoter.R;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.quoter.Contract.Quotes;
import com.example.quoter.DemoObjectFragment.OnQuoteShowListener;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;

public class MainActivity extends ActionBarActivity implements
ActionBar.OnNavigationListener, OnQuoteShowListener {

	DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
	private PullToRefreshAttacher mPullToRefreshAttacher;

	ViewPager mViewPager;
	Quote currentQuote;
	private MenuItem likeItem;
	final String TAG = getClass().getSimpleName();
	private RestRequestManager requestManager;
	ActionBar actionBar;
	private static final int TAB_ALL = 0;
	private static final int TAB_LIKED = 1;
	private static final int LOADER_ID = 1;
	private static final int LOADER_ID_LIKED = 2;
	private static final String[] PROJECTION = { 
		Quotes.ID,
		Quotes.TEXT,
		Quotes.AUTHOR
	};
	
	private LoaderCallbacks<Cursor> loaderCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
			return new CursorLoader(
				MainActivity.this,
				Quotes.CONTENT_URI,
				PROJECTION,
				null,
				null,
				null );
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
			
			mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager(), cursor);
			mViewPager.setAdapter(mDemoCollectionPagerAdapter);
			mViewPager.setCurrentItem(((QuoterApplication)getApplication()).getCurrentAllPosition());
			
			if (cursor.getCount() == 0) {
				mPullToRefreshAttacher.setRefreshing(true);
				update();
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
		}
	};
	
	private LoaderCallbacks<Cursor> LikedQuotesloaderCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
			return new CursorLoader(
				MainActivity.this,
				Quotes.LIKED_CONTENT_URI,
				PROJECTION,
				null,
				null,
				null
			);
		}

		@Override	
		public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
			
			mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager(), cursor);
			mViewPager.setAdapter(mDemoCollectionPagerAdapter);
			mViewPager.setCurrentItem(((QuoterApplication)getApplication()).getCurrentLikedPosition());
			
			if (cursor.getCount() == 0) {
				Toast.makeText(MainActivity.this, "Сохранённых цитат нет, пока...", Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
		}
	};
	
	
	RequestListener requestListener = new RequestListener() {
		
		@Override
		public void onRequestFinished(Request request, Bundle resultData) {
			mPullToRefreshAttacher.setRefreshComplete();
			actionBar.setSelectedNavigationItem(0);
		}
		
		void showError() {
			mPullToRefreshAttacher.setRefreshComplete();
			Toast.makeText(MainActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
		}
		
		@Override
		public void onRequestDataError(Request request) {
			showError();
		}
		
		@Override
		public void onRequestCustomError(Request request, Bundle resultData) {
			showError();
		}
		
		@Override
		public void onRequestConnectionError(Request request, int statusCode) {
			showError();
		}
	};
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception ex) {}
		mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
		
		actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bg_black));
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item,
				getResources().getStringArray(R.array.Category));
		adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
		actionBar.setListNavigationCallbacks(adapter, this);
		actionBar.setDisplayShowTitleEnabled(false);
		
		setContentView(R.layout.activity_collection_demo);
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		
		if(savedInstanceState != null){
			actionBar.setSelectedNavigationItem(savedInstanceState.getInt("Category", TAB_ALL));
		}
		requestManager = RestRequestManager.from(this);
		
	}
	
	@Override
	protected void onPause() {
		if(actionBar.getSelectedNavigationIndex() == TAB_ALL){
			((QuoterApplication)getApplication()).setCurrentAllPosition(mViewPager.getCurrentItem());
		}
		else ((QuoterApplication)getApplication()).setCurrentLikedPosition(mViewPager.getCurrentItem());
		super.onPause();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("Category", actionBar.getSelectedNavigationIndex());
		super.onSaveInstanceState(outState);
	}
	
	// Populates the activity's options menu.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_bar_buttons, menu);
		likeItem = menu.findItem(R.id.action_like);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.v("Ilya", "prepare");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_share:
			share_button();
			return true;
		case R.id.action_like:
			like_button();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	

	private void share_button(){
		if(currentQuote!= null){
			String shareBody = currentQuote.getQuoteText();
			if (currentQuote.getQuoteAuthor() != null){
				shareBody += '\n' + currentQuote.getQuoteAuthor();
			}
		    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		        sharingIntent.setType("text/plain");
		        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
		}
		else Toast.makeText(this, "Цитат нет, попробуйте обновить", Toast.LENGTH_SHORT).show();
		
	}
	
	void update() {
		Request updateRequest = new Request(RequestFactory.REQUEST_QUOTES);
		requestManager.execute(updateRequest, requestListener);
	}

	private void like_button(){
		if(currentQuote != null){
			if (isLiked(currentQuote)) {
				// remove like
				getContentResolver().delete(Quotes.LIKED_CONTENT_URI, Quotes.ID + "= ?", 
						new String[] {String.valueOf(currentQuote.getId())});
				((QuoterApplication)getApplication()).removeLikedId(currentQuote.getId());
			} else {
				// like
				ContentValues values = new ContentValues();
				values.put(Quotes.ID, currentQuote.getId());
				values.put(Quotes.AUTHOR, currentQuote.getQuoteAuthor());
				values.put(Quotes.TEXT, currentQuote.getQuoteText());
				
				getContentResolver().insert(Quotes.LIKED_CONTENT_URI, values);
				((QuoterApplication)getApplication()).addLikedId(currentQuote.getId());
			}
			onArticleSelected(currentQuote);
		}
		else Toast.makeText(this, "Цитат нет, попробуйте обновить", Toast.LENGTH_SHORT).show();

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if(itemPosition == TAB_ALL){
			((QuoterApplication)getApplication()).setCurrentLikedPosition(mViewPager.getCurrentItem());
			getSupportLoaderManager().initLoader(LOADER_ID, null, loaderCallbacks);
			return true;
		}
		else if (itemPosition == TAB_LIKED){
			((QuoterApplication)getApplication()).setCurrentAllPosition(mViewPager.getCurrentItem());
			getSupportLoaderManager().restartLoader(LOADER_ID_LIKED, null, LikedQuotesloaderCallbacks);
			return true;
		}
		return false;
	}
	
	private boolean isLiked(Quote quote){
		return ((QuoterApplication)getApplication()).getLikedIds().contains(quote.getId());
	}

	@Override
	public void onArticleSelected(Quote quote) {
		currentQuote = quote;
		if (isLiked(currentQuote)) {
			likeItem.setIcon(R.drawable.like_pressed);
		}
		else likeItem.setIcon(R.drawable.like_not_pressed);
	}
	
	PullToRefreshAttacher getPullToRefreshAttacher() {
        return mPullToRefreshAttacher;
    }

}
