package com.example.quoter;

import java.util.Random;

import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.quoter.Contract.Quotes;

/**
 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
 * representing an object in the collection.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

	Cursor quotesCursor;
	private String[] colors = {
			"#ff7300", "#c42f69", "#8c5991", "#e6cc5d", "#1fa9b4", "#31c3c2", "#0099cc", "#CC0000", "#669900"};
		
	private Random rand = new Random();
	
    public PagerAdapter(FragmentManager fm, Cursor cursor) {
        super(fm);
        this.quotesCursor = cursor;
    }
    
    @Override
    public Fragment getItem(int i) {
    	if(quotesCursor == null){
    		return new QuoteFragment(null, Color.BLACK);
    	}
    	else{
        	quotesCursor.moveToPosition(i);
            return new QuoteFragment(quoteFromCursor(quotesCursor), 
            		Color.parseColor(colors[rand.nextInt(colors.length)]));

    	}
    }
    
    private Quote quoteFromCursor(Cursor cursor){
    	
    	Quote quote = new Quote();
    	quote.setId(cursor.getString(cursor.getColumnIndex(Quotes.ID)));
    	quote.setAuthorName(cursor.getString(cursor.getColumnIndex(Quotes.AUTHOR)));
    	quote.setText(cursor.getString(cursor.getColumnIndex(Quotes.TEXT)));
    	return quote;
    }
    
    @Override
    public int getCount() {
    	if(quotesCursor == null) return 1;
        return quotesCursor.getCount();
    }
}
