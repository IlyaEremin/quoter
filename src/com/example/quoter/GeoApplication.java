package com.example.quoter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GeoApplication extends Application {
	
	private List<Integer> savedQuotesIds;
	private List<Quote> quotes = null, savedQuotes = null;
	
	public List<Quote> getQuotes(){
		return quotes;
	}
	
	public void shuffleQuotes(){
		Collections.shuffle(quotes);
	}
	
	public void setQuotes(List<Quote> quotes){
		this.quotes = new ArrayList<Quote>(quotes);
	}
	
	public List<Quote> getSavedQuotes() {
		return savedQuotes;
	  }

	public void setSavedQuotes(List<Quote> savedQuotes) {
		this.savedQuotes = new ArrayList<Quote>(savedQuotes);
	}
	
	  private LocationListener geoLocationListener;
	  public LocationListener getLocationListener(){
	    if(null == geoLocationListener){
	      geoLocationListener = new LocMngListner();
	    }
	 
	    return geoLocationListener;
	  }
	  private LocationManager locationManager;
	  public LocationManager getLocationManager(){
	    if(null == locationManager){
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
	}
