package com.baby.app;

import android.app.Application;
import android.os.AsyncTask;

import com.baby.cartoonnetwork.R;
import com.baby.constant.Constants;
import com.baby.constant.GlobalSingleton;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger.LogLevel;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.HashMap;

import io.vov.vitamio.Vitamio;

public class BabyApplication extends Application {

	private static BabyApplication sInstance;
	public ImageLoader imageLoader;

	private static String sApplicationId;
	public static final double VOLUME_INCREMENT = 0.05;

	/**
	 * Enum used to identify the tracker that needs to be used for tracking.
	 * 
	 * A single tracker is usually enough for most purposes. In case you do need
	 * multiple trackers, storing them all in Application object helps ensure
	 * that they are created only once per application instance.
	 */
	public enum TrackerName {
		APP_TRACKER, // Tracker used only in this app.
		GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg:
						// roll-up tracking.
		ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a
							// company.
	}

	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

	public static BabyApplication getInstance() {
		return sInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initVitamio();

		sInstance = this;

		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them, or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this); method
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				this).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new WeakMemoryCache())
				.diskCacheSize(50 * 1024 * 1024).diskCacheFileCount(80)
				.tasksProcessingOrder(QueueProcessingType.LIFO).build();

		// Initialize ImageLoader with configuration.
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);

		GlobalSingleton.getInstance().OS = "Android";
		GlobalSingleton.getInstance().Version = "1.0";

		// Set the log level to verbose.
		GoogleAnalytics.getInstance(this).getLogger()
				.setLogLevel(LogLevel.VERBOSE);

		com.baby.utils.Utils.updateDeviceInfo(this);
	}

	private void initVitamio() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				Vitamio.initialize(BabyApplication.this);
				return null;
			}
		}.execute();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (imageLoader != null) {
			imageLoader.clearMemoryCache();
		}
	}

	public synchronized Tracker getTracker(TrackerName trackerId) {
		if (!mTrackers.containsKey(trackerId)) {

			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics
					.newTracker(Constants.GA_ID)
					: (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics
							.newTracker(R.xml.global_tracker) : analytics
							.newTracker(R.xml.ecommerce_tracker);
			mTrackers.put(trackerId, t);

		}
		return mTrackers.get(trackerId);
	}

}
