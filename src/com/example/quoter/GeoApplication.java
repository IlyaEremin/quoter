package com.example.quoter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.sunsoft.quoter.R;

import android.app.Application;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GeoApplication extends Application {

	private List<Quote> quotes = new ArrayList<Quote>(), savedQuotes = new ArrayList<Quote>();
	
	private int currentPosition;
	
	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}
	
	public List<Quote> getQuotes() {
		return quotes;
	}
	
	public void setQuotes(List<Quote> quotes) {
		this.quotes = new ArrayList<Quote>(quotes);
		Collections.shuffle(this.quotes);
	}
	
	public List<Quote> getSavedQuotes() {
		return savedQuotes;
	}

	public void setSavedQuotes(List<Quote> savedQuotes) {
		this.savedQuotes = new ArrayList<Quote>(savedQuotes);
	}
	
	public void removeFromSaved(Quote quote){
		savedQuotes.remove(quote);
	}
	
	public void addToSaved(Quote quote){
		savedQuotes.add(quote);
	}

	public void shuffleQuotes() {
		Collections.shuffle(quotes);
	}

	private LocationListener geoLocationListener;

	public LocationListener getLocationListener() {
		if (null == geoLocationListener) {
			geoLocationListener = new LocMngListner();
		}

		return geoLocationListener;
	}

	private LocationManager locationManager;

	public LocationManager getLocationManager() {
		if (null == locationManager) {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}

		return locationManager;
	}

	public class LocMngListner implements LocationListener {

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onLocationChanged(Location location) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
	
	public void removeUpdates() {

		LocationManager locationManager = getLocationManager();
		LocationListener locationListner = getLocationListener();

		locationManager.removeUpdates(locationListner);
	}

	public void requestLocationUpdates() {
		LocationManager locationManager = getLocationManager();
		LocationListener locationListner = getLocationListener();
		String provider = getBestProvider(locationManager);
		locationManager.requestLocationUpdates(provider, 10, 0, locationListner);
	}

	public String getBestProvider(LocationManager locationManager) {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		String provider = locationManager.getBestProvider(criteria, true);

		return provider;
	}

	public String getCoordinates() {
		LocationManager locationManager = getLocationManager();
		String provider = getBestProvider(locationManager);
		Location location = locationManager.getLastKnownLocation(provider);
		if (null != location) {
			return "geo:0,0?q=" + location.getLatitude() + "," + location.getLongitude();
		} else {
			return getString(R.string.location_not_set);
		}
	}

	
}
