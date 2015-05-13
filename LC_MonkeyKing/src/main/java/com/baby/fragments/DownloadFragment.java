package com.baby.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.baby.adapters.DownloadAdapter;
import com.baby.cartoonnetwork.DemoUtil;
import com.baby.cartoonnetwork.R;
import com.baby.cartoonnetwork.VitamioPlayerActivity;
import com.baby.constant.GlobalSingleton;
import com.baby.dbdownloader.MovieObject;
import com.baby.downloader.DownloadCache;
import com.baby.utils.Debug;

import java.util.ArrayList;

public class DownloadFragment extends Fragment {

	private static final String TAG = "DownloadFragment";

	private GridView mGrid;
	private ArrayList<MovieObject> moviesList;
	private DownloadAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		moviesList = DownloadCache.getInstance(getActivity()).getMovies();
		adapter = new DownloadAdapter(getActivity());
		adapter.setData(moviesList);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.download_fragment, container,
				false);
		mGrid = (GridView) view.findViewById(R.id.filmsGridView);
		mGrid.setAdapter(adapter);

		mGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Debug.logData(TAG, adapter.getData().get(position).getPath());
				Debug.logData(TAG, adapter.getData().get(position).getURLLink());
				GlobalSingleton.getInstance().offline = true;
				Intent movieActivity = new Intent(getActivity(),
						VitamioPlayerActivity.class);
				movieActivity.putExtra(DemoUtil.CONTENT_ID_EXTRA, adapter
						.getData().get(position).getPath());
				startActivity(movieActivity);
			}
		});

		return view;
	}
}