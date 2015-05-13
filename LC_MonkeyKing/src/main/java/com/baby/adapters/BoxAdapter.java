package com.baby.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.baby.app.BabyApplication;
import com.baby.cartoonnetwork.R;
import com.baby.model.BoxObject;
import com.baby.utils.Utils;
import com.nct.customview.ImageViewCustome;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BoxAdapter extends BaseAdapter {
	private ArrayList<BoxObject> moviesList;
	private Context mContext;
	private LayoutInflater layoutInflater;
	private ImageLoader mImageLoader;
	private DisplayImageOptions options;

	public BoxAdapter(Context context) {
		mContext = context;
		moviesList = new ArrayList<BoxObject>();
		layoutInflater = LayoutInflater.from(mContext);

		mImageLoader = BabyApplication.getInstance().imageLoader;
		options = Utils.getDefaultOptionImage(R.drawable.default_poster1);
	}

	public void setData(ArrayList<BoxObject> data) {
		if (moviesList != null) {
			moviesList.addAll(data);
		}
	}

	public void clearData() {
		if (moviesList != null) {
			moviesList.clear();
		}
	}

	public ArrayList<BoxObject> getData() {
		return moviesList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = layoutInflater.inflate(R.layout.box_item, parent,
					false);

			holder.thumb = (ImageViewCustome) convertView.findViewById(R.id.thumb);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		BoxObject info = moviesList.get(position);
		mImageLoader.displayImage(info.image, holder.thumb, options);

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
		ImageViewCustome thumb;
	}

}
