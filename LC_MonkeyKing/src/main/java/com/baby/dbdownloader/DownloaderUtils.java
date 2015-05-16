
package com.baby.dbdownloader;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.baby.constant.Constants;
import com.baby.downloader.DownloadCache;
import com.baby.policy.ActionCallback;
import com.baby.utils.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
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

    public static void downloadSubTitle(final Context context, String chapterTitle,
            String subtitleLink, final ActionCallback<File> onComplete) {
        try {
            chapterTitle = chapterTitle.replaceAll("\\W", "-");
            final File subFolder = new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) +
                            Constants.FOLDER_SUBTITLES, chapterTitle);
            if (!subFolder.exists()) {
                subFolder.mkdirs();
            }

            File subFile = new File(subFolder, chapterTitle + ".zip");
            if (!subFile.exists()) {
                subFile.createNewFile();
            }

            downloadFile(subtitleLink, subFile, new ActionCallback<File>() {
                @Override
                public void onComplete(final File zipFile) {
                    if (zipFile.exists()) {
                        unzip(zipFile, subFolder, new ActionCallback() {
                            @Override
                            public void onComplete(Object callbackData) {
                                zipFile.delete();

                                File[] files = subFolder.listFiles();
                                if (files != null && files.length > 0) {
                                    if (onComplete != null) {
                                        onComplete.onComplete(files[0]);
                                    }
                                }
                            }
                        });
                    }

                    if (onComplete != null) {
                        onComplete.onComplete(null);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            if (onComplete != null) {
                onComplete.onComplete(null);
            }
        }
    }

    public static void downloadFile(final String link, final File destinationFile,
            final ActionCallback<File> onComplete) {
        new AsyncTask<Void, Void, File>() {

            @Override
            protected File doInBackground(Void... params) {
                int count;

                try {
                    URL url = new URL(link);
                    URLConnection conexion = url.openConnection();
                    conexion.connect();

                    InputStream input = new BufferedInputStream(url.openStream());
                    OutputStream output = new FileOutputStream(destinationFile);

                    byte data[] = new byte[1024];

                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return destinationFile;
            }

            @Override
            protected void onPostExecute(File file) {
                if (onComplete != null) {
                    onComplete.onComplete(file);
                }
            }
        }.execute();
    }

    public static void unzip(final File zipFile, final File targetDirectory,
            final ActionCallback callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ZipInputStream zis;
                try {
                    zis = new ZipInputStream(
                            new BufferedInputStream(new FileInputStream(zipFile)));
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
                    }

                    zis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if (callback != null) {
                    callback.onComplete(null);
                }
            }
        }.execute();
    }

}
