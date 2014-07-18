package com.example.quoter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import ru.sunsoft.quoter.R;

public class LeftMenuListFragment extends ListFragment {

    ListFragmentItemClickListener ifaceItemClickListener;
    private int mCurCheckPosition;

    public interface ListFragmentItemClickListener {
        void onListFragmentItemClick(String tag);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            ifaceItemClickListener = (ListFragmentItemClickListener) activity;
        } catch (Exception e) {
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("curChoice", mCurCheckPosition);
        super.onSaveInstanceState(outState);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.left_menu, null);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.Category,
                android.R.layout.simple_list_item_activated_1);
        setListAdapter(adapter);
        if (savedInstanceState != null) {
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
        }

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ifaceItemClickListener.onListFragmentItemClick(getResources()
                .getStringArray(R.array.Category)[0]);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ifaceItemClickListener
                .onListFragmentItemClick(((CharSequence) getListAdapter()
                        .getItem(position)).toString());
        getListView().setItemChecked(position, true);
        super.onListItemClick(l, v, position, id);
    }

}
