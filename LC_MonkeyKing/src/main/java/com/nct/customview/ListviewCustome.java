package com.nct.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ListviewCustome extends ListView {

	public ListviewCustome(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ListviewCustome(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListviewCustome(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightSpec;

		if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
			heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
					MeasureSpec.AT_MOST);
		} else {
			heightSpec = heightMeasureSpec;
		}

		super.onMeasure(widthMeasureSpec, heightSpec);

	}

}