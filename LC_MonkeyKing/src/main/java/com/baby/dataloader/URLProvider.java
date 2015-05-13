package com.baby.dataloader;

import java.net.URLEncoder;

import com.baby.constant.Constants;
import com.baby.constant.GlobalSingleton;
import com.baby.utils.Debug;

public class URLProvider {

	private static final String TAG = "URLProvider";

	/**
	 * 
	 * @param type
	 *            new|popular|title
	 * @param category
	 *            movie|show|cartoon|anime
	 * @param pageIndex
	 *            start with 1
	 * @return
	 */
	public static String getData(String type, String category,
			String categoryID, int pageIndex) {
		String client = Constants.SERVICE_URL;

		if (client != null) {
			try {
				client += "type=" + type;
				client += "&t=" + category;
				client += "&page=" + String.valueOf(pageIndex);
				client += "&catid=" + categoryID;
				client += "&os=" + GlobalSingleton.getInstance().OS;
				client += "&v=" + GlobalSingleton.getInstance().Version;
				client += "&k=" + GlobalSingleton.getInstance().kidMode;
				client += "&ui="
						+ URLEncoder.encode(Constants.DEVICEINFO, "UTF-8");

				Debug.logURL(TAG, client);

				return client;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getDetail(String id) {
		String client = Constants.SERVICE_URL;

		if (client != null) {
			try {
				client += "type=detail";
				client += "&id=" + id;
				client += "&os=" + GlobalSingleton.getInstance().OS;
				client += "&v=" + GlobalSingleton.getInstance().Version;
				client += "&k=" + GlobalSingleton.getInstance().kidMode;
				client += "&ui="
						+ URLEncoder.encode(Constants.DEVICEINFO, "UTF-8");
				Debug.logURL(TAG, client);

				return client;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getStreamLink(String chapterID) {
		String client = Constants.SERVICE_URL;

		if (client != null) {
			try {
				client += "type=stream";
				client += "&id=" + chapterID;
				client += "&os=" + GlobalSingleton.getInstance().OS;
				client += "&v=" + GlobalSingleton.getInstance().Version;
				client += "&k=" + GlobalSingleton.getInstance().kidMode;
				client += "&ui="
						+ URLEncoder.encode(Constants.DEVICEINFO, "UTF-8");
				Debug.logURL(TAG, client);

				return client;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String getDrirectLink(String data) {
		String client = Constants.SERVICE_URL;

		if (client != null) {
			try {
				client += "type=directlink";
				client += "&id=" + data;
				client += "&os=" + GlobalSingleton.getInstance().OS;
				client += "&v=" + GlobalSingleton.getInstance().Version;
				client += "&k=" + GlobalSingleton.getInstance().kidMode;
				client += "&ui="
						+ URLEncoder.encode(Constants.DEVICEINFO, "UTF-8");
				Debug.logURL(TAG, client);

				return client;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getSearch(String keyword, int page) {
		String client = Constants.SERVICE_URL;

		if (client != null) {
			try {
				client += "type=search";
				client += "&keyword=" + keyword;
				client += "&os=" + GlobalSingleton.getInstance().OS;
				client += "&v=" + GlobalSingleton.getInstance().Version;
				client += "&k=" + GlobalSingleton.getInstance().kidMode;
				client += "&page=" + page;
				client += "&ui="
						+ URLEncoder.encode(Constants.DEVICEINFO, "UTF-8");
				Debug.logURL(TAG, client);

				return client;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getCategory() {
		String client = Constants.SERVICE_URL;

		if (client != null) {
			try {
				client += "type=listcat";
				client += "&os=" + GlobalSingleton.getInstance().OS;
				client += "&v=" + GlobalSingleton.getInstance().Version;
				client += "&k=" + GlobalSingleton.getInstance().kidMode;
				client += "&ui="
						+ URLEncoder.encode(Constants.DEVICEINFO, "UTF-8");
				Debug.logURL(TAG, client);

				return client;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getBoxChannel() {

		String client = Constants.SERVICE_URL;

		if (client != null) {
			try {
				client += "type=list";
				client += "&os=" + GlobalSingleton.getInstance().OS;
				client += "&v=" + GlobalSingleton.getInstance().Version;
				client += "&k=" + GlobalSingleton.getInstance().kidMode;
				client += "&t=box";
				client += "&ui="
						+ URLEncoder.encode(Constants.DEVICEINFO, "UTF-8");
				Debug.logURL(TAG, client);

				return client;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getFilmsBox(String boxID, int pageIndex) {
		String client = Constants.SERVICE_URL;

		if (client != null) {
			try {
				client += "type=detail";
				client += "&os=" + GlobalSingleton.getInstance().OS;
				client += "&v=" + GlobalSingleton.getInstance().Version;
				client += "&k=" + GlobalSingleton.getInstance().kidMode;
				client += "&t=box";
				client += "&id=" + boxID;
				client += "&page=" + pageIndex;
				client += "&ui="
						+ URLEncoder.encode(Constants.DEVICEINFO, "UTF-8");
				Debug.logURL(TAG, client);

				return client;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}