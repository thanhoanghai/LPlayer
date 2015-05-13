package com.baby.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.baby.adapters.StreamAdapter;
import com.baby.cartoonnetwork.VitamioPlayerActivity;
import com.baby.dataloader.URLProvider;
import com.baby.model.StreamObject;
import com.baby.model.StreamResult;
import com.baby.utils.Debug;
import com.baby.utils.Utils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.io.IOException;
import java.util.ArrayList;

public class StreamFragment extends ListFragment {

	private static final String TAG = "StreamFragment";

	public static StreamResult streamObject;
	private StreamListener mCallback;
	private static StreamAdapter adapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (StreamListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}

		loadExplosisData(VitamioPlayerActivity.chapterID);
	}

	public void refreshStream(String chapterID) {
		Debug.logData(TAG, "refreshStream");
		loadExplosisData(chapterID);

		if (mCallback != null) {
			mCallback.onStreamChangeListener(0);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		loadExplosisData(VitamioPlayerActivity.chapterID);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		loadExplosisData(VitamioPlayerActivity.chapterID);

	}

	private void loadExplosisData(String chapterID) {
		Debug.logData(TAG, "loadExplosisData");
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("ah", "get");
		client.get(URLProvider.getStreamLink(chapterID),
				new TextHttpResponseHandler() {

					@Override
					public void onSuccess(int arg0, Header[] arg1, String arg2) {

						ObjectMapper objectMapper = new ObjectMapper();
						objectMapper
								.configure(
										DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
										false);
						try {
							streamObject = objectMapper.readValue(arg2,
									StreamResult.class);

							for (int i = 0; i < streamObject.data.size(); i++) {
								streamObject.data.get(i).stream = Utils
										.EncryptionKey(streamObject.data.get(i).stream);
							}

							if (streamObject != null) {
								adapter = new StreamAdapter(
										getActivity(), streamObject.data);
								setListAdapter(adapter);
							}

						} catch (JsonParseException e) {
							e.printStackTrace();
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(int arg0, Header[] arg1, String arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
					}
				});
	}
	
	public static void updateData(ArrayList<StreamObject> data) {
//		adapter.clearData();
		if (adapter != null) {
			adapter.updateData(data);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Send the event to the host activity
		mCallback.onStreamChangeListener(position);
	}
}
