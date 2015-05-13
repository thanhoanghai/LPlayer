package com.baby.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baby.cartoonnetwork.R;
import com.baby.model.StreamObject;

public class StreamAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<StreamObject> values;

	public StreamAdapter(Context context, ArrayList<StreamObject> values) {
		this.context = context;
		this.values = values;
	}

//	public void clearData() {
//		if (values != null) {
//			values.clear();
//			notifyDataSetChanged();
//		}
//	}

	public void updateData(ArrayList<StreamObject> data) {
		values = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.stream_item, parent, false);

			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.type = (TextView) convertView.findViewById(R.id.type);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.title.setText(values.get(position).server);
		holder.type.setText(values.get(position).quality);

		return convertView;
	}

	@Override
	public int getCount() {
		if (values != null) {
			return values.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	class ViewHolder {
		TextView title;
		TextView type;
	}
}
