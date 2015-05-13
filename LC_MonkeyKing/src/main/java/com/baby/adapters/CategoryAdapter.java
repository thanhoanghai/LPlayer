package com.baby.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baby.cartoonnetwork.R;
import com.baby.model.CategoryObject;

public class CategoryAdapter extends BaseAdapter {

	private ArrayList<CategoryObject> moviesList;
	private Context mContext;
	private LayoutInflater layoutInflater;

	public CategoryAdapter(Context context) {
		mContext = context;
		moviesList = new ArrayList<CategoryObject>();
		layoutInflater = LayoutInflater.from(mContext);
	}

	public void setData(ArrayList<CategoryObject> data) {
		moviesList.clear();
		moviesList.addAll(data);
	}

	public void clearData() {
		if (moviesList != null) {
			moviesList.clear();
		}
	}

	public ArrayList<CategoryObject> getData() {
		return moviesList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = layoutInflater.inflate(R.layout.category_item,
					parent, false);

			holder.title = (TextView) convertView.findViewById(R.id.title);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		CategoryObject info = moviesList.get(position);
		holder.title.setText(info.name);

		return convertView;
	}

	public final int getCount() {
		return moviesList.size();
	}

	public final Object getItem(int position) {
		return moviesList.get(position);
	}

	public final long getItemId(int position) {
		return position;
	}

	class ViewHolder {
		TextView title;
	}

}