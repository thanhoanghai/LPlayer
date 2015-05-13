package com.baby.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.baby.adapters.DetailAdapter;
import com.baby.cartoonnetwork.DetailActivity;
import com.baby.cartoonnetwork.R;
import com.baby.model.DetailObject;

public class ExplosisDialog extends DialogFragment {
	int mNum;
	ListView streamList;

	/**
	 * Create a new instance of MyDialogFragment, providing "num" as an
	 * argument.
	 */
	public static ExplosisDialog newInstance(int num) {
		ExplosisDialog f = new ExplosisDialog();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putInt("num", num);
		f.setArguments(args);

		return f;
	}

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mNum = getArguments().getInt("num");

		int style = DialogFragment.STYLE_NORMAL;
		int theme = android.R.style.Theme_Holo_Light_Dialog;

		setStyle(style, theme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_dialog, container, false);
		streamList = (ListView) v.findViewById(R.id.streamList);
		getDialog().setTitle("Sources");

		DetailObject detailObject = DetailActivity.detailObject;
		if (detailObject != null) {

			DetailAdapter adapter = new DetailAdapter(getActivity(),
					detailObject.data.chapters);
			streamList.setAdapter(adapter);
		}

		streamList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mCallback
						.onExplosisListener(DetailActivity.detailObject.data.chapters
								.get(position).id);
			}
		});

		return v;
	}
}
