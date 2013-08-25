package com.example.quoter;

import ru.sunsoft.quoter.R;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.quoter.Contract.Quotes;
import com.example.quoter.DemoObjectFragment.OnQuoteShowListener;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;

public class MainActivity extends ActionBarActivity implements
ActionBar.OnNavigationListener, OnQuoteShowListener {

	DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;

	ViewPager mViewPager;
	
	boolean loadSaved;
	
	Quote currentQuote;
	private MenuItem refreshItem;
	final String TAG = getClass().getSimpleName();

	
	private RestRequestManager requestManager;
	
	private static final int LOADER_ID = 1;
	private static final int LOADER_ID_LIKED = 2;
	private static final String[] PROJECTION = { 
		Quotes.ID,
		Quotes.TEXT,
		Quotes.AUTHOR
	};
	ActionBar actionBar;
	
	private LoaderCallbacks<Cursor> loaderCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {

			return new CursorLoader(
				MainActivity.this,
				Quotes.CONTENT_URI,
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
			mViewPager.setCurrentItem(((QuoterApplication)getApplication()).getCurrentAllPosition());
			
			if (refreshItem != null && MenuItemCompat.getActionView(refreshItem) != null) {
				MenuItemCompat.getActionView(refreshItem).clearAnimation();
				MenuItemCompat.setActionView(refreshItem, null);
	        }
			
			if (cursor.getCount() == 0) {
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
		}
		
		void showError() {
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
		actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bg_black));
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
				getResources().getStringArray(R.array.Category));
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		actionBar.setListNavigationCallbacks(adapter, this);
		actionBar.setDisplayShowTitleEnabled(false);
		
		setContentView(R.layout.activity_collection_demo);
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		
		if(savedInstanceState != null){
			actionBar.setSelectedNavigationItem(savedInstanceState.getInt("Category", 0));
		}
		
		requestManager = RestRequestManager.from(this);
	}
	
	@Override
	protected void onPause() {
		if(actionBar.getSelectedNavigationIndex() == 0){
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
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar_buttons, menu);
		return super.onCreateOptionsMenu(menu);
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
		case R.id.action_refresh:
			refreshItem = item;
			update();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(currentQuote != null){
			if(isLiked(currentQuote)){
				menu.findItem(R.id.action_like).setIcon(R.drawable.like_pressed);
			}
			else menu.findItem(R.id.action_like).setIcon(R.drawable.like_not_pressed);
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	private void share_button(){
		String shareBody = currentQuote.getQuoteText();
		if (currentQuote.getQuoteAuthor() != null){
			shareBody += '\n' + currentQuote.getQuoteAuthor();
		}
	    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
	        sharingIntent.setType("text/plain");
	        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
	        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
	}
	
	void update() {
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_animation, null);

		Animation rotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh);
		rotation.setRepeatCount(Animation.INFINITE);
		iv.startAnimation(rotation);

		MenuItemCompat.setActionView(refreshItem, iv);

		Request updateRequest = new Request(RequestFactory.REQUEST_QUOTES);
		updateRequest.put("screen_name", "habrahabr");
		requestManager.execute(updateRequest, requestListener);
	}

	private void like_button(){

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
		ActivityCompat.invalidateOptionsMenu(MainActivity.this);

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if(itemPosition == 0){
			((QuoterApplication)getApplication()).setCurrentLikedPosition(mViewPager.getCurrentItem());
			getSupportLoaderManager().initLoader(LOADER_ID, null, loaderCallbacks);
			return true;
		}
		else if (itemPosition == 1){
			((QuoterApplication)getApplication()).setCurrentAllPosition(mViewPager.getCurrentItem());
			getSupportLoaderManager().restartLoader(LOADER_ID_LIKED, null, LikedQuotesloaderCallbacks);
			Log.v("Ilya", "saved");
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
		ActivityCompat.invalidateOptionsMenu(MainActivity.this);
	}
	

}
