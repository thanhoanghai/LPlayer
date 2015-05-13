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

import com.baby.adapters.StreamAdapter;
import com.baby.cartoonnetwork.R;
import com.baby.cartoonnetwork.VitamioPlayerActivity;
import com.baby.dataloader.URLProvider;
import com.baby.model.StreamResult;
import com.baby.utils.Utils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.io.IOException;

public class StreamDialog extends DialogFragment {
	int mNum;
	ListView streamList;
	private StreamListener mCallback;

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
	}

	/**
	 * Create a new instance of MyDialogFragment, providing "num" as an
	 * argument.
	 */
	public static StreamDialog newInstance(int num) {
		StreamDialog f = new StreamDialog();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putInt("num", num);
		f.setArguments(args);

		return f;
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

		if (StreamFragment.streamObject != null) {
			StreamAdapter adapter = new StreamAdapter(getActivity(),
					StreamFragment.streamObject.data);
			streamList.setAdapter(adapter);
		} else {
			loadExplosisData(VitamioPlayerActivity.chapterID);
		}

		streamList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				mCallback.onStreamChangeListener(position);
				getDialog().dismiss();
			}
		});

		return v;
	}

	private void loadExplosisData(String chapterID) {
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
							StreamFragment.streamObject = objectMapper
									.readValue(arg2, StreamResult.class);

							for (int i = 0; i < StreamFragment.streamObject.data
									.size(); i++) {
								StreamFragment.streamObject.data.get(i).stream = Utils
										.EncryptionKey(StreamFragment.streamObject.data
												.get(i).stream);
							}

							if (StreamFragment.streamObject != null) {
								StreamAdapter adapter = new StreamAdapter(
										getActivity(),
										StreamFragment.streamObject.data);
								streamList.setAdapter(adapter);
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
}
