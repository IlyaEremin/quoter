package com.example.quoter;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.example.quoter.Contract.Quotes;
import com.example.quoter.QuoteFragment.OnQuoteShowListener;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.lang.reflect.Field;

import ru.sunsoft.quoter.R;

public class MainActivity extends ActionBarActivity implements
        OnQuoteShowListener, LeftMenuListFragment.ListFragmentItemClickListener {

    PagerAdapter mPagerAdapter;

    ViewPager mViewPager;
    Quote currentQuote;
    private MenuItem likeItem;
    final String TAG = ((Object)this).getClass().getSimpleName();
    private RestRequestManager requestManager;
    ActionBar actionBar;
    SlidingMenu slidingMenu;

    private static final int TAB_ALL = 0;
    private static final int TAB_LIKED = 1;
    private static final int LOADER_ID = 1;
    private static final int LOADER_ID_LIKED = 2;

    private static final String[] PROJECTION = {
            Quotes.ID,
            Quotes.TEXT,
            Quotes.AUTHOR
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quotes_pager);

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
        }

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        slidingMenu = new SlidingMenu(this);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMenu(R.layout.menu_frame);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.menu_frame, new LeftMenuListFragment())
                .commit();

        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bg_black));
        actionBar.setDisplayHomeAsUpEnabled(true);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        requestManager = RestRequestManager.from(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("Category", actionBar.getSelectedNavigationIndex());
        super.onSaveInstanceState(outState);
    }

    private LoaderCallbacks<Cursor> loaderCallbacks = new LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
            return new CursorLoader(
                    MainActivity.this,
                    Quotes.CONTENT_URI,
                    PROJECTION,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

            mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), cursor);
            mViewPager.setAdapter(mPagerAdapter);
            mViewPager.setCurrentItem(((QuoterApplication) getApplication()).getCurrentAllPosition());
            if (cursor.getCount() == 0) {
                update();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
        }
    };

    private LoaderCallbacks<Cursor> LikedQuotesloaderCallbacks = new LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
            return new CursorLoader(
                    MainActivity.this,
                    Quotes.LIKED_CONTENT_URI,
                    PROJECTION,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

            mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), cursor);
            mViewPager.setAdapter(mPagerAdapter);
            mViewPager.setCurrentItem(((QuoterApplication) getApplication()).getCurrentLikedPosition());
            if (cursor.getCount() == 0) {
                mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), null);
                mViewPager.setAdapter(mPagerAdapter);
                Toast.makeText(MainActivity.this, "Сохранённых цитат нет, пока...", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
        }
    };


    RequestListener requestListener = new RequestListener() {

        @Override
        public void onRequestFinished(Request request, Bundle resultData) {
        }

        void showError() {
            Toast.makeText(MainActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
            if (currentQuote == null) {
                mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), null);
                mViewPager.setAdapter(mPagerAdapter);
                Log.v("Ilya", "olo");
            }
        }

        @Override
        public void onRequestDataError(Request request) {
            showError();
        }

        @Override
        public void onRequestCustomError(Request request, Bundle resultData) {
            showError();
        }

        @Override
        public void onRequestConnectionError(Request request, int statusCode) {
            showError();
        }
    };


    // Populates the activity's options menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_buttons, menu);
        likeItem = menu.findItem(R.id.action_like);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_like:
                like_button();
                return true;
            case R.id.action_share:
                share_button();
                return true;
            case android.R.id.home:
                slidingMenu.toggle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void update() {
        Request updateRequest = new Request(RequestFactory.REQUEST_QUOTES);
        requestManager.execute(updateRequest, requestListener);
    }

    private void share_button() {
        if (currentQuote != null) {
            String shareBody = currentQuote.getQuoteText();
            if (currentQuote.getQuoteAuthor() != null) {
                shareBody += '\n' + currentQuote.getQuoteAuthor();
            }
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
        } else Toast.makeText(this, "Цитат нет, попробуйте обновить", Toast.LENGTH_SHORT).show();

    }

    private void like_button() {
        if (currentQuote != null) {
            if (isLiked(currentQuote)) {
                // remove like
                getContentResolver().delete(Quotes.LIKED_CONTENT_URI, Quotes.ID + "= ?",
                        new String[]{String.valueOf(currentQuote.getId())});
                ((QuoterApplication) getApplication()).removeLikedId(currentQuote.getId());
            } else {
                // like
                ContentValues values = new ContentValues();
                values.put(Quotes.ID, currentQuote.getId());
                values.put(Quotes.AUTHOR, currentQuote.getQuoteAuthor());
                values.put(Quotes.TEXT, currentQuote.getQuoteText());

                getContentResolver().insert(Quotes.LIKED_CONTENT_URI, values);
                ((QuoterApplication) getApplication()).addLikedId(currentQuote.getId());
            }
            onQuoteVisible(currentQuote);
        } else Toast.makeText(this, "Цитат нет, попробуйте обновить", Toast.LENGTH_SHORT).show();

    }

    private boolean isLiked(Quote quote) {
        if (quote == null) return false;
        return ((QuoterApplication) getApplication()).getLikedIds().contains(quote.getId());
    }

    @Override
    public void onQuoteVisible(Quote quote) {
        currentQuote = quote;
        if(likeItem != null) {
            if (isLiked(currentQuote)) {
                likeItem.setIcon(R.drawable.like_pressed);
            } else likeItem.setIcon(R.drawable.like_not_pressed);
        }
    }

    @Override
    public void onListFragmentItemClick(String tag) {
        if(tag.equals(getResources().getStringArray(R.array.Category)[TAB_ALL])){
            ((QuoterApplication)getApplication()).setCurrentLikedPosition(mViewPager.getCurrentItem());
            getSupportLoaderManager().initLoader(LOADER_ID, null, loaderCallbacks);
        }
        else if(tag.equals(getResources().getStringArray(R.array.Category)[TAB_LIKED])){
            ((QuoterApplication)getApplication()).setCurrentAllPosition(mViewPager.getCurrentItem());
            getSupportLoaderManager().restartLoader(LOADER_ID_LIKED, null, LikedQuotesloaderCallbacks);
        }
        if(slidingMenu.isMenuShowing()){
            slidingMenu.showContent();
        }
    }

    @Override
    public void onBackPressed() {
        if (slidingMenu.isMenuShowing()) {
            slidingMenu.showContent();
        } else {
            super.onBackPressed();
        }
    }
}
