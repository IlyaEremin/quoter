package com.example.quoter;

import java.util.ArrayList;
import java.util.List;

import com.example.quoter.Contract.Quotes;

import android.app.Application;
import android.database.Cursor;

public class QuoterApplication extends Application {
	
	private List<Long> likedQuotesIds = new ArrayList<Long>();
	private boolean isLoaded = false;
	private int allPosition, likedPosition;
	
	public List<Long> getLikedIds(){
		if(!isLoaded){
			Cursor likedQuotesIdCursor = this.getContentResolver().
					query(Quotes.LIKED_CONTENT_URI, new String[] {Quotes.ID}, null, null, null);
			
			likedQuotesIdCursor.moveToFirst();
			while(!likedQuotesIdCursor.isAfterLast()){
				likedQuotesIds.add(likedQuotesIdCursor.getLong(0));
				likedQuotesIdCursor.moveToNext();
			}
			isLoaded = true;
		}
		return likedQuotesIds;
	}
	
	public void addLikedId(long id){
		likedQuotesIds.add(id);
	}
	
	public void removeLikedId(long id){
		likedQuotesIds.remove(id);
	}
	
	public int getCurrentAllPosition(){
		return allPosition;
	}
	
	public void setCurrentAllPosition(int position){
		allPosition = position;
	}
	public int getCurrentLikedPosition(){
		return likedPosition;
	}
	
	public void setCurrentLikedPosition(int position){
		likedPosition = position;
	}
	

}
