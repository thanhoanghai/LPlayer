package com.baby.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baby.cartoonnetwork.R;

public class MenuAdapter extends BaseAdapter {

	private LayoutInflater mInflater;

	public MenuAdapter(Context context) {
		// Cache the LayoutInflate to avoid asking for a new one each time.
		mInflater = LayoutInflater.from(context);
	}

	// private String[] items = new String[] { "Movies", "TV Shows", "Cartoons",
	// "Animes", "Favorites", "Download", "Settings" };
	private String[] items = new String[] { "Movies", "TV Shows", "Cartoons",
			"Animes", "Download", "Settings" };

	private Integer[] drawables = new Integer[] { R.drawable.ico_movie,
			R.drawable.ico_show, R.drawable.ico_cartoon, R.drawable.ico_anime,
			R.drawable.ico_setting, R.drawable.ico_download,
			R.drawable.ico_setting };

	@Override
	public int getCount() {
		if (items != null) {
			return items.length;
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid unneccessary
		// calls
		// to findViewById() on each row.
		ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is no
		// need
		// to reinflate it. We only inflate a new View when the convertView
		// supplied
		// by ListView is null.
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.menu_item, parent, false);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.title);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);

			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		holder.text.setText(items[position]);
		holder.icon.setImageResource(drawables[position]);

		return convertView;
	}

	static class ViewHolder {
		TextView text;
		ImageView icon;
	}

}
