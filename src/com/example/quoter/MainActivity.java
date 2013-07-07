package com.example.quoter;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;

import ru.sunsoft.quoter.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.quoter.DemoObjectFragment.onlikeButtonClickListener;

public class MainActivity extends FragmentActivity implements onlikeButtonClickListener {

	DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;

	ViewPager mViewPager;

	static final String url = "https://dl.dropboxusercontent.com/u/67723735/quotes.xml";

	private int currentPosition;
	
	List<Integer> savedQuotesIds;
	
	GeoApplication mainApp;

	ProgressDialog mProgressDialog;
	boolean loadSaved;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		mainApp = ((GeoApplication)getApplication());
		
		loadSaved = getIntent().getBooleanExtra("loadSaved", false);

		savedQuotesIds = new XmlHandler(this).getSavedQuotes();

		setContentView(R.layout.activity_collection_demo);
		mViewPager = (ViewPager) findViewById(R.id.pager);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(R.string.download_quotes));
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		
		if (loadSaved) {
			if(new File(getFilesDir(), XmlHandler.likedQuotesFile).exists())
				new RetreiveFeedTask("file:" + new File(getFilesDir(), XmlHandler.likedQuotesFile).toString(),
						mainApp.getSavedQuotes()).execute();
		} else {
			currentPosition = mainApp.getCurrentPosition();
			new RetreiveFeedTask(url, mainApp.getQuotes()).execute();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(!loadSaved){
			mainApp.setCurrentPosition(mViewPager.getCurrentItem());
		}
		mainApp.removeUpdates();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mainApp.requestLocationUpdates();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if(!loadSaved){
			currentPosition = mainApp.getCurrentPosition();
		}
	}

	class RetreiveFeedTask extends AsyncTask<String, Integer, Void> {

		private String openingFile;
		private List<Quote> quotes;
		private boolean needUpdate = false, loadSaved, needOpenFile;
		File allQuotes;
		String link;

		public RetreiveFeedTask(String openingFile, List<Quote> quotes) {
			
			this.openingFile = openingFile;
			this.quotes = quotes;
			
			this.link = url;
			
			loadSaved = openingFile != url;
			
			allQuotes = new File(getFilesDir(), "allQuotes.xml");
			
			this.needUpdate =  openingFile == url && !allQuotes.exists();
			this.needOpenFile = quotes.isEmpty() || needUpdate;
			
			if(!loadSaved) openingFile = "file:" + allQuotes.toString();
		}
		
		
		public RetreiveFeedTask(String openingFile, List<Quote> quotes, boolean needUpdate) {
			this(openingFile, quotes);
			this.needUpdate = needUpdate;
			this.needOpenFile = true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog.show();
			if (needUpdate) {
				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

				if (activeInfo == null || !activeInfo.isConnected() || !activeInfo.isAvailable()) {
					cancel(false);
				}
			}
		}

		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			mProgressDialog.setProgress(progress[0]);
		}

		protected Void doInBackground(String... urls) {
			if (!isCancelled()) {
				try {
					if (needUpdate) {
						
						URL url = new URL(link);
						URLConnection connection = url.openConnection();
						connection.setConnectTimeout(5000);
						connection.setReadTimeout(10000);
						connection.connect();

						int fileLength = connection.getContentLength();

						// download the file
						InputStream input = new BufferedInputStream(url.openStream());
						OutputStream output = new FileOutputStream(allQuotes);

						byte data[] = new byte[1024];
						long total = 0;
						int count;
						Log.v("Ilya", "start download quotes");
						while ((count = input.read(data)) != -1) {
							total += count;
							// publishing the progress....
							publishProgress((int) (total * 100 / fileLength));
							output.write(data, 0, count);
						}
						Log.v("Ilya", "end download quotes");
						output.flush();
						output.close();
						input.close();
					}
					
					if(needOpenFile){
						quotes = new SaxQuoteParser(openingFile).parse();
						if(!loadSaved){
							mainApp.setQuotes(quotes);
							Collections.shuffle(quotes);
						}
					}

				} catch (MalformedURLException e) {
				} catch (SocketTimeoutException e) {
					cancel(false);
				} catch (Exception e) {
				}
			}
			return null;
		}

		protected void onPostExecute(Void params) {
			Log.v("Ilya", "On post execute");
			mProgressDialog.dismiss();
			
			setContentView(R.layout.activity_collection_demo);
			mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager(), quotes, savedQuotesIds);
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mDemoCollectionPagerAdapter);
			mViewPager.setCurrentItem(currentPosition);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mProgressDialog.dismiss();
			
			Log.v("Ilya", "On canceled");
			Toast.makeText(getApplicationContext(), R.string.network_error, Toast.LENGTH_LONG).show();
			setContentView(R.layout.network_unavailable);
			findViewById(R.id.btnTryAgain).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new RetreiveFeedTask(openingFile, quotes).execute();
				}
			});
		}
	}

	@Override
	public void likeButtonClick(DemoObjectFragment fragment, Quote quote) {
		ImageButton likeButton = (ImageButton) fragment.getView().findViewById(R.id.btnLike);
		XmlHandler xh = new XmlHandler(this);
		if (fragment.isLiked) {
			// remove like
			likeButton.setImageResource(R.drawable.like_not_pressed);
			savedQuotesIds.remove(Integer.valueOf(quote.getId()));
			xh.removeQuote(quote);
			fragment.isLiked = false;
		} else {
			// make like
			likeButton.setImageResource(R.drawable.like_pressed);
			xh.addQuoteToFile(quote, mainApp.getCoordinates());
			savedQuotesIds.add(Integer.valueOf(quote.getId()));
			fragment.isLiked = true;
		}
	}

	

	// Populates the activity's options menu.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.all:
			Intent loadAll = new Intent(this, MainActivity.class);
			loadAll.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(loadAll);
			return true;
			
		case R.id.saved:
			Intent loadSaved = new Intent(this, MainActivity.class);
			loadSaved.putExtra("loadSaved", true);
			loadSaved.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(loadSaved);
			return true;

		case R.id.refresh:
			new RetreiveFeedTask(url, mainApp.getQuotes(), true).execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
