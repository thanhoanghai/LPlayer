
package com.baby.dbdownloader;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.baby.constant.Constants;
import com.baby.downloader.DownloadCache;
import com.baby.policy.ActionCallback;
import com.baby.utils.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import org.apache.http.Header;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    public static void downloadSubTitle(final Context context, String subtitleLink,
            final ActionCallback<File> onComplete) {
        String timestamp = System.currentTimeMillis() + "";
        final File subFolder = new File(context.getCacheDir() + Constants.FOLDER_SUBTITLES + timestamp);
        if (!subFolder.exists()) {
            subFolder.mkdirs();
        }

        downloadFile(context, subtitleLink, new ActionCallback<File>() {
            @Override
            public void onComplete(File zipFile) {
                unzip(zipFile, subFolder);
                File[] files = subFolder.listFiles();
                if (files.length > 0 && onComplete != null) {
                    onComplete.onComplete(files[0]);
                }
            }
        });
    }

    public static void downloadFile(Context context, String url,
            final ActionCallback<File> onComplete) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new FileAsyncHttpResponseHandler(context) {

            @Override
            public void onSuccess(int i, Header[] headers, File file) {
                if (onComplete != null) {
                    onComplete.onComplete(file);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                if (onComplete != null) {
                    onComplete.onComplete(null);
                }
            }
        });
    }

    public static void unzip(File zipFile, File targetDirectory) {
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(
                    new BufferedInputStream(new FileInputStream(zipFile)));

            try {
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[8192];
                while ((ze = zis.getNextEntry()) != null) {
                    File file = new File(targetDirectory, ze.getName());
                    File dir = ze.isDirectory() ? file : file.getParentFile();
                    if (!dir.isDirectory() && !dir.mkdirs())
                        throw new FileNotFoundException("Failed to ensure directory: " +
                                dir.getAbsolutePath());
                    if (ze.isDirectory())
                        continue;
                    FileOutputStream fout = new FileOutputStream(file);
                    try {
                        while ((count = zis.read(buffer)) != -1)
                            fout.write(buffer, 0, count);
                    } finally {
                        fout.close();
                    }
                    /*
                     * if time should be restored as well long time = ze.getTime(); if (time > 0)
                     * file.setLastModified(time);
                     */
                }
            } finally {
                zis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
