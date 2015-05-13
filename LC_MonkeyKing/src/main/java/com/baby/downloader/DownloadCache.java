package com.baby.downloader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.baby.dbdownloader.DaoMaster;
import com.baby.dbdownloader.DaoMaster.DevOpenHelper;
import com.baby.dbdownloader.DaoSession;
import com.baby.dbdownloader.MovieObject;
import com.baby.dbdownloader.MovieObjectDao;
import com.baby.dbdownloader.MovieObjectDao.Properties;

import de.greenrobot.dao.query.QueryBuilder;

public class DownloadCache {

	public static final String DOWNLOAD_UNSUCCESSFUL = "0";
	public static final String DOWNLOAD_SUCCESSFUL = "1";

	private static DownloadCache mInstance;
	private Context mContext;

	private SQLiteDatabase db;
	private DaoMaster daoMaster;
	private DaoSession daoSession;

	private MovieObjectDao movieObjectDAO;

	public DownloadCache(Context context) {
		mContext = context;

		DevOpenHelper helper = new DaoMaster.DevOpenHelper(context,
				"PLAYBOX_DB", null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();

		movieObjectDAO = daoSession.getMovieObjectDao();
	}

	public static DownloadCache getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DownloadCache(context);
		}
		return mInstance;
	}

	public ArrayList<MovieObject> getMovies() {
		QueryBuilder<MovieObject> query = movieObjectDAO.queryBuilder();
		List<MovieObject> result = query.where(
				Properties.Downloaded.eq(DOWNLOAD_SUCCESSFUL)).list();
		ArrayList<MovieObject> movieList = new ArrayList<MovieObject>(
				result.size());
		movieList.addAll(result);
		return movieList;
	}

	public boolean existedMovie(MovieObject movieObject) {
		if (!TextUtils.isEmpty(movieObject.getMovieId())) {
			QueryBuilder<MovieObject> query = movieObjectDAO.queryBuilder();
			synchronized (query) {
				List<MovieObject> result = query.where(
						Properties.MovieId.eq(movieObject.getMovieId())).list();

				if (result != null && result.size() > 0) {
					return true;
				}
				return false;
			}
		}

		return false;
	}

	public void insertMovie(MovieObject movieObject) {
		if (movieObject != null) {
			if (!existedMovie(movieObject)) {
				movieObjectDAO.insert(movieObject);
			}
		}
	}

	public void updateMovie(long referenceId) {
		List<MovieObject> songs = movieObjectDAO.queryBuilder()
				.where(Properties.Status.eq(referenceId)).list();

		if (songs != null && songs.size() > 0) {
			for (int i = 0; i < songs.size(); i++) {
				MovieObject song = songs.get(i);
				song.setDownloaded(DownloadCache.DOWNLOAD_SUCCESSFUL);
				movieObjectDAO.update(song);
			}
		}
	}

	public void updateMovie(MovieObject songObject) {
		List<MovieObject> songs = movieObjectDAO.queryBuilder()
				.where(Properties.MovieId.eq(songObject.getMovieId())).list();

		if (songs != null && songs.size() > 0) {
			for (int i = 0; i < songs.size(); i++) {
				MovieObject movieObject = songs.get(i);
				movieObject.setDownloaded(DownloadCache.DOWNLOAD_SUCCESSFUL);
				movieObjectDAO.update(movieObject);
			}
		}
	}

}
