package com.baby.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baby.policy.BaseFragment;
import com.baby.utils.Debug;

public class NewFragment extends BaseFragment {

	private static final String TAG = "NewFragment";

	public NewFragment() {
		Debug.logFlow(TAG, "NewFragment");
		category = "new";
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

}