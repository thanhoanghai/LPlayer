package com.baby.downloader;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import com.baby.cartoonnetwork.R;
import com.baby.dbdownloader.DownloaderUtils;
import com.baby.utils.Debug;

public class DownloadReceiver extends BroadcastReceiver {
	private static final String TAG = "DownloadReceiver";

	private DownloadManager dm;
	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {

		mContext = context;

		if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
			dm = (DownloadManager) mContext
					.getSystemService(Context.DOWNLOAD_SERVICE);
			long referenceId = intent.getLongExtra(
					DownloadManager.EXTRA_DOWNLOAD_ID, -1);

			Query query = new Query();
			query.setFilterById(referenceId);
			Cursor c = dm.query(query);

			if (c != null && c.moveToFirst()) {
				int columnIndex = c
						.getColumnIndex(DownloadManager.COLUMN_STATUS);
				int status = c.getInt(columnIndex);
				if (status == DownloadManager.STATUS_SUCCESSFUL) {
					Debug.logError(TAG, "STATUS_SUCCESSFUL ID " + referenceId);
					DownloadCache.getInstance(mContext).updateMovie(referenceId);
				} else if (status == DownloadManager.STATUS_FAILED) {
					Debug.logError(TAG, "STATUS_FAILED ID " + referenceId);
					DownloaderUtils.cancelDownload(mContext, referenceId);
					int error = c.getInt(c
							.getColumnIndex(DownloadManager.COLUMN_REASON));

					if (error == DownloadManager.ERROR_INSUFFICIENT_SPACE) {
						Toast.makeText(
								mContext,
								mContext.getResources().getString(
										R.string.error_insufficient_space),
								Toast.LENGTH_LONG).show();
					}
				}
			}

			if (c != null) {
				c.close();
			}
		}
	}
}
