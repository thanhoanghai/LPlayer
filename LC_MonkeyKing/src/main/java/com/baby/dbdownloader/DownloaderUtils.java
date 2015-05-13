package com.baby.dbdownloader;

import java.io.File;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.baby.constant.Constants;
import com.baby.downloader.DownloadCache;
import com.baby.utils.Utils;

public class DownloaderUtils {
	public static void downloadMovie(Context mContext, MovieObject movieObject) {

		if (movieObject == null) {
			return;
		}

		// Check on sdcard and Download song
		String convertPath = getDownloadPath(movieObject.getMovieId());
		if (!checkFileExistent(convertPath)) {

			long downloadID = existedDownloadPending(mContext,
					movieObject.getURLLink());

			if (downloadID < 0) {
				downloadID = downloadMovieWithDM(mContext, movieObject);
			}

			// Add song to Database with "No" status download
			movieObject.setDownloaded(DownloadCache.DOWNLOAD_UNSUCCESSFUL);
			movieObject.setStatus(String.valueOf(downloadID));
			movieObject.setPath(convertPath);
			DownloadCache.getInstance(mContext).insertMovie(movieObject);
		} else {
			if (!DownloadCache.getInstance(mContext).existedMovie(movieObject)) {
				movieObject.setDownloaded(DownloadCache.DOWNLOAD_SUCCESSFUL);
				movieObject.setPath(convertPath);
				DownloadCache.getInstance(mContext).insertMovie(movieObject);
			} else {
				DownloadCache.getInstance(mContext).updateMovie(movieObject);
			}
		}
	}

	private static long existedDownloadPending(Context context, String url) {
		DownloadManager downloadManager = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		Query myDownloadQuery = new Query();
		myDownloadQuery.setFilterByStatus(DownloadManager.STATUS_RUNNING
				| DownloadManager.STATUS_PENDING);

		Cursor cursor = downloadManager.query(myDownloadQuery);

		if (cursor.moveToFirst()) {
			do {
				int filenameIndex = cursor
						.getColumnIndex(DownloadManager.COLUMN_URI);
				String temp = cursor.getString(filenameIndex);

				if (temp.equals(url)) {
					return cursor.getLong(cursor
							.getColumnIndex(DownloadManager.COLUMN_ID));
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		return -1;
	}

	public static void cancelDownload(Context mContext, long idDownload) {
		try {
			DownloadManager downloadManager = (DownloadManager) mContext
					.getSystemService(Context.DOWNLOAD_SERVICE);
			if (idDownload > 0) {
				downloadManager.remove(idDownload);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static boolean checkFileExistent(String path) {
		File f = new File(path);
		return f.exists();
	}

	private static String getDownloadPath(String movieID) {

		try {
			String fileName = Utils.md5(movieID);
			String path = Environment.getExternalStorageDirectory().getPath()
					+ Constants.FOLDER_DOWNLOAD;

			File tempFile = new File(path);
			if (!tempFile.exists()) {
				tempFile.mkdirs();
			}
			return path + fileName;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	private static long downloadMovieWithDM(Context mContext,
			MovieObject movieObject) {

		String url = movieObject.getURLLink();
		if (TextUtils.isEmpty(url)) {
			return -1;
		}

		try {
			DownloadManager downloadManager = (DownloadManager) mContext
					.getSystemService(Context.DOWNLOAD_SERVICE);
			Uri Download_Uri = Uri.parse(url);
			DownloadManager.Request request = new DownloadManager.Request(
					Download_Uri);
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
					| DownloadManager.Request.NETWORK_MOBILE);
			request.setAllowedOverRoaming(false);
			request.setTitle(movieObject.getMovieTitle());
			request.setDescription(movieObject.getMovieTitle());
			request.setDestinationInExternalPublicDir(
					Constants.FOLDER_DOWNLOAD,
					getNameFile(movieObject.getMovieId()));
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			return downloadManager.enqueue(request);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return -1;
	}

	public static String getNameFile(String movieID) {
		String fileName = Utils.md5(movieID);
		return fileName;
	}

}