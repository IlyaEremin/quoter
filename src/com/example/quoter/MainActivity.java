package com.example.quoter;

// TODO кнопка лайк и просмотр лайкнутых

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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.widget.Button;
import android.widget.Toast;

import com.example.quoter.DemoObjectFragment.onlikeButtonClickListener;

public class MainActivity extends FragmentActivity implements
		onlikeButtonClickListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments representing each object in a collection. We use a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter} derivative,
	 * which will destroy and re-create fragments as needed, saving and
	 * restoring their state in the process. This is important to conserve
	 * memory and is a best practice when allowing navigation between objects in
	 * a potentially large collection.
	 */
	DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;

	/**
	 * The {@link android.support.v4.view.ViewPager} that will display the
	 * object collection.
	 */
	ViewPager mViewPager;

	String url = "https://dl.dropboxusercontent.com/u/67723735/quotes.xml";

	QuoteParser sqp;

	private int currentPosition;
	List<Integer> savedQuotes;
	String openingFile;

	ProgressDialog mProgressDialog;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent intent = getIntent();
		boolean loadSaved = intent.getBooleanExtra("loadSaved", false);

		XmlHandler xmlHandler = new XmlHandler(this);
		savedQuotes = xmlHandler.getSavedQuotes();

		setContentView(R.layout.activity_collection_demo);
		mViewPager = (ViewPager) findViewById(R.id.pager);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage("ќбновл€ю цитаты");
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		if (loadSaved) {
			try {
				openingFile = new File(getFilesDir(),XmlHandler.likedQuotesFile).toURL().toString();
				new RetreiveFeedTask(openingFile, null, false, true).execute();
			} catch (MalformedURLException e) {
			}
		} else {
			openingFile = url;
			new RetreiveFeedTask(openingFile,((GeoApplication) getApplication()).getQuotes(), false, false).execute();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("position", mViewPager.getCurrentItem());
	}

	@Override
	protected void onPause() {
		super.onPause();
		currentPosition = mViewPager.getCurrentItem();
		removeUpdates();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (((GeoApplication) getApplication()).getQuotes() != null) {
			mViewPager.setCurrentItem(currentPosition);
		}
		requestLocationUpdates();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			currentPosition = savedInstanceState.getInt("position");
		}
	}

	class RetreiveFeedTask extends AsyncTask<Void, Integer, Void> {

		private String openingFile;
		private List<Quote> quotes;
		private boolean isNeedUpdate = false, loadSaved;
		File allQuotes;

		public RetreiveFeedTask(String openingFile, List<Quote> quotes, boolean isNeedUpdate, boolean loadSaved) {
			this.openingFile = openingFile;
			this.quotes = quotes;
			allQuotes = new File(getFilesDir(), "allQuotes.xml");
			this.isNeedUpdate =  !loadSaved && (isNeedUpdate || !allQuotes.exists());	
			
			this.loadSaved = loadSaved;
			
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (isNeedUpdate) {
				mProgressDialog.show();
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

		protected Void doInBackground(Void... params) {
			if (!isCancelled()) {
				try {
					
					String fileForSaveAllQuotes = allQuotes.toURL().toString();
					if (isNeedUpdate) {

						URL url = new URL(openingFile);
						URLConnection connection = url.openConnection();
						connection.connect();

						int fileLength = connection.getContentLength();

						// download the file
						InputStream input = new BufferedInputStream(url.openStream());
						OutputStream output = new FileOutputStream(allQuotes);

						byte data[] = new byte[1024];
						long total = 0;
						int count;
						while ((count = input.read(data)) != -1) {
							total += count;
							// publishing the progress....
							publishProgress((int) (total * 100 / fileLength));
							output.write(data, 0, count);
						}
						output.flush();
						output.close();
						input.close();
					}
					if(loadSaved){
						quotes = new SaxQuoteParser(openingFile).parse();
					}
					else{
						if(quotes == null){
							quotes = new SaxQuoteParser(fileForSaveAllQuotes).parse();
							((GeoApplication) getApplication()).setQuotes(quotes);
						}
						Collections.shuffle(quotes);
					}
					
					Log.v("Ilya", "download quotes");
					

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
			mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager(), quotes, savedQuotes);
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mDemoCollectionPagerAdapter);
			mViewPager.setCurrentItem(currentPosition);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			Log.v("Ilya", "On canceled");
			Toast.makeText(getApplicationContext(), R.string.network_error, Toast.LENGTH_LONG).show();
			setContentView(R.layout.network_unavailable);
			Button b = (Button) findViewById(R.id.btnTryAgain);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new RetreiveFeedTask(openingFile, quotes, true, false).execute();
				}
			});
		}
	}

	@Override
	public void likeButtonClick(DemoObjectFragment frag, Quote quote) {
		Button likeButton = (Button) frag.getView().findViewById(R.id.btnLike);
		XmlHandler xh = new XmlHandler(this);
		if (frag.isLiked) {
			// remove like
			likeButton.setText("</3");
			savedQuotes.remove(Integer.valueOf(quote.getId()));
			xh.removeQuote(quote);
			frag.isLiked = false;
		} else {
			// make like
			likeButton.setText("<3");
			quote.setLocation(getCoordinates());
			xh.addQuoteToFile(quote);
			savedQuotes.add(Integer.valueOf(quote.getId()));
			frag.isLiked = true;
		}
	}

	private void removeUpdates() {

		LocationManager locationManager = ((GeoApplication) getApplication()).getLocationManager();
		LocationListener locationListner = ((GeoApplication) getApplication()).getLocationListener();

		locationManager.removeUpdates(locationListner);
	}

	private void requestLocationUpdates() {
		final LocationManager locationManager = ((GeoApplication) getApplication()).getLocationManager();
		LocationListener locationListner = ((GeoApplication) getApplication()).getLocationListener();
		final String provider = getBestProvider(locationManager);
		locationManager.requestLocationUpdates(provider, 10, 0, locationListner);
//		locationManager.requestLocationUpdates(provider, 100, 0, locationListner);
	}

	private String getBestProvider(LocationManager locationManager) {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		String provider = locationManager.getBestProvider(criteria, true);

		return provider;
	}

	private String getCoordinates() {
		LocationManager locationManager = ((GeoApplication) getApplication()).getLocationManager();
		String provider = getBestProvider(locationManager);
		Location location = locationManager.getLastKnownLocation(provider);
		if (null != location) {
			return "geo:0,0?q=" + location.getLatitude() + "," + location.getLongitude();
		} else {
			return getString(R.string.location_not_set);
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
			new RetreiveFeedTask(openingFile,((GeoApplication) getApplication()).getQuotes(), true, false).execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
