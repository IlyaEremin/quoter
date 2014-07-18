package com.example.quoter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ru.sunsoft.quoter.R;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class QuoteFragment extends Fragment implements OnRefreshListener {

    @InjectView(R.id.quote) TextView quoteTextView;
    @InjectView(R.id.authorName) TextView authorNameTextView;
    @InjectView(R.id.quoteContainer) ScrollView quoteContainer;

    private final int color;
    private Quote quote;

	OnQuoteShowListener mListener;
    PullToRefreshLayout mPullToRefreshLayout;

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
        // TODO placeholder
		View rootView = inflater.inflate(R.layout.quote_fragment, container, false);
        ButterKnife.inject(this, rootView);

        mPullToRefreshLayout = (PullToRefreshLayout)rootView.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

        if(quote != null){
            quoteTextView.setText(quote.getQuoteText().toUpperCase());
            authorNameTextView.setText(quote.getQuoteAuthor());
            quoteContainer.setBackgroundColor(color);
            if(color == Color.parseColor("#e6cc5d") || color == Color.parseColor("#31c3c2")){
                quoteTextView.setTextColor(Color.BLACK);
                authorNameTextView.setTextColor(Color.BLACK);
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
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onRefreshStarted(View view) {
        ((MainActivity)getActivity()).update();
    }

    public interface OnQuoteShowListener {
        public void onQuoteVisible(Quote quote);
    }


}

