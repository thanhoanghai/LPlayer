
package com.baby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baby.cartoonnetwork.R;
import com.baby.customview.SectionedBaseAdapter;
import com.baby.parselink.SubsceneParseList;

import java.util.ArrayList;
import java.util.List;

public class SubtitleAdapter extends SectionedBaseAdapter {

    public static class SubtitleDataHolder {
        public String language;
        public List<SubsceneParseList.SubsceneObject> subtitles;
    }

    private List<SubtitleDataHolder> data = new ArrayList<>();

    public void setData(List<SubtitleDataHolder> data) {
        this.data = data;
    }

    @Override
    public SubsceneParseList.SubsceneObject getItem(int section, int position) {
        return data.get(section).subtitles.get(position);
    }

    @Override
    public long getItemId(int section, int position) {
        return position;
    }

    @Override
    public int getSectionCount() {
        return data.size();
    }

    @Override
    public int getCountForSection(int section) {
        return data.get(section).subtitles.size();
    }

    @Override
    public View getItemView(int section, int position, View convertView, ViewGroup parent) {
        SubsceneParseList.SubsceneObject subsceneObject = getItem(section, position);
        SubtitleItemViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.layout_subtitle_item, parent, false);

            holder = new SubtitleItemViewHolder();
            holder.tvTitle = (TextView) convertView.findViewById(R.id.subtitle_tvTitle);
            holder.tvRating = (TextView) convertView.findViewById(R.id.subtitle_tvRating);

            convertView.setTag(holder);
        } else {
            holder = (SubtitleItemViewHolder) convertView.getTag();
        }

        holder.tvTitle.setText(subsceneObject.title);
        holder.link = subsceneObject.link;

        return convertView;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        SubtitleDataHolder dataHolder = data.get(section);
        SubtitleSectionViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.layout_subtitle_section, parent, false);

            holder = new SubtitleSectionViewHolder();
            holder.tvSectionTitle = (TextView) convertView.findViewById(R.id.section_tvTitle);

            convertView.setTag(holder);
        } else {
            holder = (SubtitleSectionViewHolder) convertView.getTag();
        }

        holder.tvSectionTitle.setText(dataHolder.language);

        return convertView;
    }

    class SubtitleSectionViewHolder {
        TextView tvSectionTitle;
    }

    class SubtitleItemViewHolder {
        TextView tvTitle;
        TextView tvRating;
        String link;
    }
}
