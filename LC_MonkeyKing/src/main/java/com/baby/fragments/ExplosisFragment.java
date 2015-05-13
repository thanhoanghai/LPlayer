package com.baby.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.baby.adapters.DetailAdapter;
import com.baby.cartoonnetwork.DetailActivity;
import com.baby.model.DetailObject;

public class ExplosisFragment extends ListFragment {

	private ExplosisListener mCallback;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (ExplosisListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		DetailObject detailObject = DetailActivity.detailObject;
		if (detailObject != null) {

			DetailAdapter adapter = new DetailAdapter(getActivity(),
					detailObject.data.chapters);
			setListAdapter(adapter);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Send the event to the host activity
		mCallback.onExplosisListener(DetailActivity.detailObject.data.chapters
				.get(position).id);
	}
}
