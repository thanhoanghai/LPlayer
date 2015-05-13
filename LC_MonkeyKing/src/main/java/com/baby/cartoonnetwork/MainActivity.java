package com.baby.cartoonnetwork;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.baby.constant.Constants;
import com.baby.constant.GlobalSingleton;
import com.baby.fragments.ContentFragment;
import com.baby.policy.BaseActivity;

public class MainActivity extends BaseActivity {

	private Fragment mContent;

	public MainActivity() {
		super(R.string.app_name);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		GlobalSingleton.getInstance().kidMode = sharedPref.getInt(
				Constants.PRE_CARTOON, 0);

		// set the Above View
		setContentView(R.layout.content_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, new ContentFragment()).commit();

		setSlidingActionBarEnabled(true);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
	}

	public void switchContent(Fragment fragment) {
		mContent = fragment;
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
		getSlidingMenu().showContent();
	}

}