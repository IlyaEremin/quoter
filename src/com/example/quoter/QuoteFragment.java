package com.example.quoter;

import ru.sunsoft.quoter.R;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A dummy fragment representing a section of the app, but that simply displays dummy text.
 */
@SuppressLint("ValidFragment")
public class QuoteFragment extends Fragment implements PullToRefreshAttacher.OnRefreshListener {
	
	OnQuoteShowListener mListener;
	private PullToRefreshAttacher mPullToRefreshAttacher;
	
	public interface OnQuoteShowListener {
        public void onQuoteVisible(Quote quote);
    }
	
	private final int color;
	
	private Quote quote;
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
            mListener = (OnQuoteShowListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
	}
	
	public QuoteFragment(Quote quote, int color){
        this.quote = quote;
        this.color = color;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_collection_object, container, false);
		
        mPullToRefreshAttacher = ((MainActivity) getActivity())
                .getPullToRefreshAttacher();

        mPullToRefreshAttacher.addRefreshableView(rootView, this);
        
        if(quote != null){
        	((TextView) rootView.findViewById(R.id.quote)).setText(quote.getQuoteText().toUpperCase());
            ((TextView) rootView.findViewById(R.id.authorName)).setText(quote.getQuoteAuthor());
            rootView.findViewById(R.id.ScrollView01).setBackgroundColor(color);
            if(color == Color.parseColor("#e6cc5d") || color == Color.parseColor("#31c3c2")){
            	 ((TextView) rootView.findViewById(R.id.quote)).setTextColor(Color.BLACK);
                 ((TextView) rootView.findViewById(R.id.authorName)).setTextColor(Color.BLACK);
            }
        }
		
        return rootView;
    }
    
    @Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser){
			mListener.onQuoteVisible(quote);
		}
	}

	@Override
	public void onRefreshStarted(View view) {
		((MainActivity) getActivity()).update();
	}
    
}

