package com.baby.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.inputmethod.InputMethodManager;

import com.baby.app.BabyApplication;
import com.baby.app.BabyApplication.TrackerName;
import com.baby.cartoonnetwork.R;
import com.baby.constant.AES256Cipher;
import com.baby.constant.Constants;
import com.baby.constant.GlobalSingleton;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class Utils {
	public static DisplayImageOptions getDefaultOptionImage(int idImage) {
		return new DisplayImageOptions.Builder().showImageOnLoading(idImage)
				.showImageForEmptyUri(idImage).showImageOnFail(idImage)
				.bitmapConfig(Bitmap.Config.RGB_565).cacheInMemory(true)
				.imageScaleType(ImageScaleType.EXACTLY).cacheOnDisk(true)
				.build();
	}

	public static String EncryptionKey(String value) {
		String base64Text = "";
		String plainText = "";
		try {
			String key = Constants.AES_KEY;
			byte[] keyBytes = key.getBytes("UTF-8");
			byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
					0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
			byte[] cipherData;

			base64Text = value;
			cipherData = AES256Cipher
					.decrypt(ivBytes, keyBytes, Base64.decode(
							base64Text.getBytes("UTF-8"), Base64.DEFAULT));
			plainText = new String(cipherData, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return plainText;
	}

	private static StringBuilder sFormatBuilder = new StringBuilder();
	private static Formatter sFormatter = new Formatter(sFormatBuilder,
			Locale.getDefault());
	private static final Object[] sTimeArgs = new Object[5];

	public static String makeTimeString(Context context, long secs) {
		String durationformat = context
				.getString(secs < 3600 ? R.string.durationformatshort
						: R.string.durationformatlong);

		/*
		 * Provide multiple arguments so the format can be changed easily by
		 * modifying the xml.
		 */
		sFormatBuilder.setLength(0);

		final Object[] timeArgs = sTimeArgs;
		timeArgs[0] = secs / 3600;
		timeArgs[1] = secs / 60;
		timeArgs[2] = (secs / 60) % 60;
		timeArgs[3] = secs;
		timeArgs[4] = secs % 60;

		return sFormatter.format(durationformat, timeArgs).toString();
	}

	public static int getProgressPercentage(long currentDuration,
			long totalDuration) {
		return (int) (currentDuration * totalDuration / 100f);
	}

	public static void notifyGA(Context context, String screenName) {
		// Get tracker.
		Tracker t = ((BabyApplication) context)
				.getTracker(TrackerName.APP_TRACKER);

		// Set screen name.
		t.setScreenName(screenName);

		// Send a screen view.
		t.send(new HitBuilders.AppViewBuilder().build());
	}

	public static void notifyEvent(Context context, String category,
			String action, String label) {
		// Get tracker.
		Tracker tracker = ((BabyApplication) context)
				.getTracker(TrackerName.APP_TRACKER);
		tracker.send(new HitBuilders.EventBuilder().setCategory(category)
				.setAction(action).setLabel(label).setValue(1).build());
	}

	public static void sendEmail(Context context, String subject) {
		final Intent emailIntent = new Intent(
				android.content.Intent.ACTION_SEND);
		/* Fill it with Data */
		emailIntent.setType("plain/email");
		emailIntent
				.putExtra(
						android.content.Intent.EXTRA_EMAIL,
						new String[] { GlobalSingleton.getInstance().configureObject.email });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				"Sent from my Android");
		/* Send it off to the Activity-Chooser */
		context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}

	public static String md5(String s) {
		if (s != null) {
			try {
				MessageDigest digest = java.security.MessageDigest
						.getInstance("MD5");
				digest.update(s.getBytes());
				byte messageDigest[] = digest.digest();

				StringBuffer hexString = new StringBuffer();
				for (int i = 0; i < messageDigest.length; i++) {
					String hex = Integer.toHexString(0xFF & messageDigest[i]);
					if (hex.length() == 1)
						hexString.append('0');
					hexString.append(hex);
				}
				return hexString.toString();

			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return "";
		} else
			return "";
	}

	public static void updateDeviceInfo(Context context) {
		try {
			String serviceName = Context.TELEPHONY_SERVICE;
			TelephonyManager m_telephonyManager = (TelephonyManager) context
					.getSystemService(serviceName);
			String imei = m_telephonyManager.getDeviceId();

			if (TextUtils.isEmpty(imei)) {
				WifiManager wimanager = (WifiManager) context
						.getSystemService(Context.WIFI_SERVICE);
				imei = Utils.md5(wimanager.getConnectionInfo().getMacAddress());
			} else {
				imei = Utils.md5(imei);
			}

			Constants.DEVICEINFO = context.getString(R.string.deviceInfo, imei,
					Build.VERSION.SDK_INT);
		} catch (Exception ex) {
			ex.printStackTrace();
			Constants.DEVICEINFO = context.getString(R.string.deviceInfo,
					"0123456789", Build.VERSION.SDK_INT);
		}

	}
	public static void keyBoardForceHide(Context mContext) {
		InputMethodManager imm = (InputMethodManager) mContext
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	}
}
