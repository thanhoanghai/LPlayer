package com.baby.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baby.cartoonnetwork.R;
import com.baby.constant.GlobalSingleton;
import com.baby.fragments.HotFragment;
import com.baby.fragments.NewFragment;
import com.baby.utils.Debug;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class MainFragment extends Fragment {
	private FragmentTabHost mTabHost;
	private LinearLayout adLayout;

	public MainFragment() {
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_tabs, container,
				false);

		adLayout = (LinearLayout) rootView.findViewById(R.id.adView);

		mTabHost = (FragmentTabHost) rootView
				.findViewById(android.R.id.tabhost);
		mTabHost.setup(getActivity(), getChildFragmentManager(),
				R.id.realtabcontent);

		mTabHost.addTab(
				mTabHost.newTabSpec("tab_hot").setIndicator(
						getResources().getString(R.string.tab_hot)),
				HotFragment.class, null);
		mTabHost.addTab(
				mTabHost.newTabSpec("tab_new").setIndicator(
						getResources().getString(R.string.tab_new)),
				NewFragment.class, null);

		for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
			TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i)
					.findViewById(android.R.id.title);
			tv.setTextColor(getResources().getColor(R.color.tab_text));
		}

		return rootView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				mMessageReceiver, new IntentFilter("loadeddata"));
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (isAdded()) {
				// Get extra data included in the Intent
				String adUnitId = GlobalSingleton.getInstance().configureObject.adsid;
				if (!TextUtils.isEmpty(adUnitId)) {
					Debug.logData("mMessageReceiver", adUnitId);
					AdView mAdView = new AdView(getActivity());
					mAdView.setAdSize(AdSize.SMART_BANNER);
					mAdView.setAdUnitId(adUnitId);
					AdRequest adRequest = new AdRequest.Builder().build();
					mAdView.loadAd(adRequest);
					adLayout.addView(mAdView);

					// Start loading the ad in the background.
					mAdView.loadAd(adRequest);
					adLayout.setVisibility(View.VISIBLE);
				}
			}
		}
	};

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
				mMessageReceiver);
		mTabHost = null;
	}

}