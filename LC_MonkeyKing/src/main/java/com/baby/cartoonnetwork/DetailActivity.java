package com.baby.cartoonnetwork;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baby.adapters.DetailAdapter;
import com.baby.app.BabyApplication;
import com.baby.constant.Constants;
import com.baby.constant.GlobalSingleton;
import com.baby.dataloader.DataLoader;
import com.baby.dataloader.URLProvider;
import com.baby.model.ChapterObject;
import com.baby.model.ConfigureObject;
import com.baby.model.DetailObject;
import com.baby.utils.Debug;
import com.baby.utils.Utils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.loopj.android.http.TextHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;

import java.io.IOException;

public class DetailActivity extends ActionBarActivity {

	private static final String TAG = "DetailActivity";

	private ImageView thumb;
	private TextView title;
	private TextView actor;
	private TextView brief;
	private TextView description;
	private ListView chapterList;

	private ProgressBar loadingBtn;
	private ImageButton disconnectBtn;
	private LinearLayout adLayout;

	private String id = "";
	public static String poster = "";

	private ImageLoader mImageLoader;
	private DisplayImageOptions options;

	public static DetailObject detailObject;

	private InterstitialAd interstitial;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.detail_activity);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		chapterList = (ListView) findViewById(R.id.chapterList);
		loadingBtn = (ProgressBar) findViewById(R.id.loadingBtn);
		disconnectBtn = (ImageButton) findViewById(R.id.disconnectBtn);
		adLayout = (LinearLayout) findViewById(R.id.adLayout);

		ViewGroup headerView = (ViewGroup) getLayoutInflater().inflate(
				R.layout.header_layout, chapterList, false);
		chapterList.addHeaderView(headerView, null, false);
		disconnectBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loadData();
			}
		});

		thumb = (ImageView) headerView.findViewById(R.id.thumb);
		title = (TextView) headerView.findViewById(R.id.title);
		actor = (TextView) headerView.findViewById(R.id.actor);
		brief = (TextView) headerView.findViewById(R.id.brief);
		description = (TextView) headerView.findViewById(R.id.description);

		mImageLoader = BabyApplication.getInstance().imageLoader;
		options = Utils.getDefaultOptionImage(R.drawable.default_poster1);

		id = getIntent().getStringExtra(Constants.FILM_ID);
		poster = getIntent().getStringExtra(Constants.FILM_POSTER);

		Utils.notifyGA(getApplication(), "Item (" + id + ")");

		chapterList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ChapterObject chapterObject = (ChapterObject) chapterList.getAdapter().getItem(position);

				GlobalSingleton.getInstance().offline = false;
				Intent moviePlayer = new Intent(DetailActivity.this,
						VitamioPlayerActivity.class);
				moviePlayer.putExtra(Constants.CHAPTER_ID,
						detailObject.data.chapters.get(position - 1).id);
				moviePlayer.putExtra(Constants.CHAPTER_TITLE,
						detailObject.data.chapters.get(position - 1).title);
				moviePlayer.putExtra(Constants.CHAPTER_IMAGE, poster);
				moviePlayer.putExtra(Constants.CHAPTER_SUBSCENE, chapterObject.subscene);
				startActivity(moviePlayer);
			}
		});

		loadData();

		mImageLoader.displayImage(poster, thumb, options);

	}

	private void loadData() {
		showLoading();
		if (!TextUtils.isEmpty(id)) {
			DataLoader.get(URLProvider.getDetail(id),
					new TextHttpResponseHandler() {

						@Override
						public void onFailure(int arg0, Header[] arg1,
								String arg2, Throwable arg3) {
							showDisconnect();
						}

						@Override
						public void onSuccess(int arg0, Header[] arg1,
								String arg2) {
							Debug.logData(TAG, arg2);

							ObjectMapper objectMapper = new ObjectMapper();
							objectMapper
									.configure(
											DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
											false);
							try {
								detailObject = objectMapper.readValue(arg2,
										DetailObject.class);

								if (detailObject != null) {
									title.setText(detailObject.data.title);
									actor.setText(detailObject.data.genre);
									brief.setText(detailObject.data.description);
									description
											.setText(detailObject.data.description);

									DetailAdapter adapter = new DetailAdapter(
											DetailActivity.this,
											detailObject.data.chapters);
									chapterList.setAdapter(adapter);

									showContent();

									if (!TextUtils.isEmpty(detailObject.cf)) {
										detailObject.cf = Utils
												.EncryptionKey(detailObject.cf);

										ConfigureObject configure = objectMapper
												.readValue(detailObject.cf,
														ConfigureObject.class);
										GlobalSingleton.getInstance().Version = configure.v;

										if (configure.adsscreen > 0) {
											loadAdv(configure.adsid);
										}
									} else {
										if (GlobalSingleton.getInstance().configureObject.adsscreen > 0) {
											loadAdv(GlobalSingleton
													.getInstance().configureObject.adsid);
										}
									}
								}
							} catch (IOException e) {
								e.printStackTrace();
								detailObject = null;
								showDisconnect();
							}
						}

					});
		}
	}

	private void loadAdv(String admobID) {
		AdView mAdView = new AdView(DetailActivity.this);
		mAdView.setAdSize(AdSize.SMART_BANNER);
		mAdView.setAdUnitId(admobID);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
		adLayout.addView(mAdView);

		// Start loading the ad in the
		// background.
		mAdView.loadAd(adRequest);
		adLayout.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (GlobalSingleton.getInstance().configureObject == null){
			return;
		}

		if (GlobalSingleton.getInstance().configureObject.adsscreen > 0
				&& GlobalSingleton.getInstance().countFilm >= GlobalSingleton
						.getInstance().configureObject.adsfullcount) {
			GlobalSingleton.getInstance().countFilm = 0;
			// Create the interstitial.
			interstitial = new InterstitialAd(this);
			interstitial
					.setAdUnitId(GlobalSingleton.getInstance().configureObject.adsfullid);
			// Create ad request.
			AdRequest adRequest = new AdRequest.Builder().build();

			// Set an AdListener.
			interstitial.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
					displayInterstitial();
				}

				@Override
				public void onAdClosed() {
					// Proceed to the next level.
				}
			});

			// Begin loading your interstitial.
			interstitial.loadAd(adRequest);

		}
	}

	// Invoke displayInterstitial() when you are ready to display an
	// interstitial.
	public void displayInterstitial() {
		if (interstitial.isLoaded()) {
			interstitial.show();
		}
	}

	private void showLoading() {
		loadingBtn.setVisibility(View.VISIBLE);
		disconnectBtn.setVisibility(View.GONE);
		chapterList.setVisibility(View.GONE);
	}

	private void showContent() {
		loadingBtn.setVisibility(View.GONE);
		disconnectBtn.setVisibility(View.GONE);
		chapterList.setVisibility(View.VISIBLE);
	}

	private void showDisconnect() {
		loadingBtn.setVisibility(View.GONE);
		disconnectBtn.setVisibility(View.VISIBLE);
		chapterList.setVisibility(View.GONE);
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
