package com.baby.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baby.app.BabyApplication;
import com.baby.cartoonnetwork.R;
import com.baby.model.MovieObject;
import com.baby.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FilmAdapter extends BaseAdapter {
	private ArrayList<MovieObject> moviesList;
	private Context mContext;
	private LayoutInflater layoutInflater;
	private ImageLoader mImageLoader;
	private DisplayImageOptions options;

	public FilmAdapter(Context context) {
		mContext = context;
		moviesList = new ArrayList<MovieObject>();
		layoutInflater = LayoutInflater.from(mContext);

		mImageLoader = BabyApplication.getInstance().imageLoader;
		options = Utils.getDefaultOptionImage(R.drawable.default_poster1);
	}

	public void setData(ArrayList<MovieObject> data) {
		if (moviesList != null) {
			moviesList.addAll(data);
		}
	}

	public void clearData() {
		if (moviesList != null) {
			moviesList.clear();
		}
	}

	public ArrayList<MovieObject> getData() {
		return moviesList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = layoutInflater.inflate(R.layout.grid_item, parent,
					false);

			holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);
			holder.title = (TextView) convertView.findViewById(R.id.title);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		MovieObject info = moviesList.get(position);
		mImageLoader.displayImage(info.poster, holder.thumb, options);
		holder.title.setText(info.title);

		return convertView;
	}

	public final int getCount() {
		if (moviesList != null) {
			return moviesList.size();
		}
		return 0;
	}

	public final Object getItem(int position) {
		return moviesList.get(position);
	}

	public final long getItemId(int position) {
		return position;
	}

	class ViewHolder {
		ImageView thumb;
		TextView title;
	}

}
