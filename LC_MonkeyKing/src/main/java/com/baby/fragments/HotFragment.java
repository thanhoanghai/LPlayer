package com.baby.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baby.policy.BaseFragment;
import com.baby.utils.Debug;

public class HotFragment extends BaseFragment {

	private static final String TAG = "HotFragment";

	public HotFragment() {
		Debug.logFlow(TAG, "HotFragment");
		category = "popular";
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	protected void showUpdateApp(String appLink) {
		super.showUpdateApp(appLink);
		showDialog(appLink);
	}

	@Override
	protected void showForceApp() {
		super.showForceApp();
		showDialog().show();
	}
}