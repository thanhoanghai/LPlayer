package com.baby.utils;

import android.util.Log;

import com.baby.constant.Constants;

public class Debug {

	public static void logError(String tag, String msg) {
		if (Constants.DEBUG_ERROR) {
			Log.e(tag, msg);
		}
	}

	public static void logURL(String tag, String msg) {
		if (Constants.DEBUG_URL) {
			Log.e(tag, msg);
		}
	}

	public static void logFlow(String tag, String msg) {
		if (Constants.DEBUG_FLOW) {
			Log.e(tag, msg);
		}
	}

	public static void logData(String tag, String msg) {
		if (Constants.DEBUG_DATA) {
			Log.e(tag, msg);
		}
	}
}