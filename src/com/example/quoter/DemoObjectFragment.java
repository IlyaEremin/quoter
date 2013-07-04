package com.example.quoter;

import java.util.Random;

import ru.sunsoft.quoter.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A dummy fragment representing a section of the app, but that simply displays dummy text.
 */
@SuppressLint("ValidFragment")
public class DemoObjectFragment extends Fragment {
	
	public interface onlikeButtonClickListener {
	    public void likeButtonClick(DemoObjectFragment frag, Quote quote);
	  }
	
	onlikeButtonClickListener likeButtonClickListener;
	
	@Override
	  public void onAttach(Activity activity) {
	    super.onAttach(activity);
	        try {
	        	likeButtonClickListener = (onlikeButtonClickListener) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
	        }
	  }
	
	private String[] colors = {
		"#ff7300", "#c42f69", "#8c5991", "#2cc7cb", "#e6cc5d", "#1fa9b4", "#31c3c2" };
	
	private static Random rand = new Random();
	
	boolean isLiked;
	private Quote quote;
	private int color = Color.parseColor(colors[rand.nextInt(colors.length)]);
	DemoObjectFragment thisFrag;
	
	public void setColor(int color){
		this.color = color;
	}
	
	public DemoObjectFragment(Quote quote, boolean isLiked){
        this.quote = quote;
        this.isLiked = isLiked;
		thisFrag = this;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		Log.v("Ilya", "Fragment onCreate");
	}
		

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v("Ilya", "Fragment onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_collection_object, container, false);
        
        ((TextView) rootView.findViewById(R.id.quote)).setText(quote.getQuoteText().toUpperCase());
        ((TextView) rootView.findViewById(R.id.authorName)).setText(quote.getQuoteAuthor());
        
        if(quote.getLocation() != null){
        	final Button loc = ((Button) rootView.findViewById(R.id.location));
        	loc.setText(quote.getLocation());
        	if(quote.getLocation().equals(getString(R.string.location_not_set) )){
				loc.setEnabled(false);
        	}
        	else{
        		loc.setOnClickListener(new OnClickListener() {
    				
    				@Override
    				public void onClick(View v) {
    					Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(loc.getText() + " (Point)"));
    					startActivity(intent);
    				}
    			});
        	}
        	
        }
        else ((TextView) rootView.findViewById(R.id.location)).setVisibility(View.GONE);
        
        rootView.findViewById(R.id.ScrollView01).setBackgroundColor(color);
        
        Button btnLike = (Button)rootView.findViewById(R.id.btnLike);
        btnLike.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				likeButtonClickListener.likeButtonClick(thisFrag, quote);
			}
		});
        
    	if(isLiked){
        	btnLike.setText("<3");
        }
        
        return rootView;
    }
}

