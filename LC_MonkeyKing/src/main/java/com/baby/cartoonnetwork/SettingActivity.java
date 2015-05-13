package com.baby.cartoonnetwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.TextUtils;

import com.baby.constant.Constants;
import com.baby.constant.GlobalSingleton;
import com.baby.utils.Debug;
import com.baby.utils.Utils;

public class SettingActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(getResources().getString(R.string.setting_title));
		//getActionBar().setDisplayHomeAsUpEnabled(true);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		Preference helpCenter = findPreference("helpcenter");
		helpCenter
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						if (!TextUtils.isEmpty(GlobalSingleton.getInstance().configureObject.helpcenter)) {
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse(GlobalSingleton.getInstance().configureObject.helpcenter));
							startActivity(i);
							return true;
						}
						return false;
					}
				});

		Preference requestMovie = findPreference("requestmovie");
		requestMovie
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						Utils.sendEmail(SettingActivity.this, "[Request Movie]");
						return false;
					}
				});

		Preference requestProblem = findPreference("requestproblem");
		requestProblem
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						Utils.sendEmail(SettingActivity.this, "[Problem]");
						return false;
					}
				});

		Preference contactUs = findPreference("contactus");
		contactUs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Utils.sendEmail(SettingActivity.this, "[Contact]");
				return false;
			}
		});

		Preference shareapp = findPreference("shareapp");
		shareapp.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"Play Box");
				sendIntent
						.putExtra(
								android.content.Intent.EXTRA_TEXT,
								GlobalSingleton.getInstance().configureObject.sharelink);
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, "Share App"));
				return true;
			}
		});

		Preference termpolicies = findPreference("termpolicies");
		termpolicies
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						if (!TextUtils.isEmpty(GlobalSingleton.getInstance().configureObject.policies)) {
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse(GlobalSingleton.getInstance().configureObject.policies));
							startActivity(i);
							return true;
						}
						return false;
					}
				});

		final CheckBoxPreference checkbox_preference = (CheckBoxPreference) findPreference("checkbox_preference");
		checkbox_preference
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						Debug.logData("Settings", newValue.toString());
						if (newValue.toString().equals("true")) {
							GlobalSingleton.getInstance().kidMode = 1;
							SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putInt(Constants.PRE_CARTOON, 1);
							editor.commit();
						} else {
							GlobalSingleton.getInstance().kidMode = 0;
							SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putInt(Constants.PRE_CARTOON, 0);
							editor.commit();
						}
						GlobalSingleton.getInstance().changeMode = true;
						return true;
					}
				});
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
