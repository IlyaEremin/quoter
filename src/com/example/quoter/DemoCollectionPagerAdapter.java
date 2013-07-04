package com.example.quoter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

/**
 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
 * representing an object in the collection.
 */
public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {

	List<Quote> quotes;
	List<Integer> savedQuotes;
	
    public DemoCollectionPagerAdapter(FragmentManager fm, List<Quote> quotes, List<Integer> savedQuotes) {
        super(fm);
        this.quotes = quotes;
        this.savedQuotes = savedQuotes;
		Log.v("Ilya", "FragmentAdapter create");
    }
    
    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new DemoObjectFragment(quotes.get(i), isLiked(quotes.get(i).getId()));
        return fragment;
    }
    
    private boolean isLiked(int id){
    	if(savedQuotes != null){
    		return savedQuotes.contains(id);
    	}
    	return false;
    }

    @Override
    public int getCount() {
        return quotes.size();
    }
}
