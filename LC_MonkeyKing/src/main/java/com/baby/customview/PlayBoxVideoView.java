package com.baby.customview;

import android.content.Context;
import android.util.AttributeSet;

public class PlayBoxVideoView extends io.vov.vitamio.widget.VideoView {

	private int mForceHeight = 0;
	private int mForceWidth = 0;

	public PlayBoxVideoView(Context context) {
		super(context);
	}

	public PlayBoxVideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PlayBoxVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setDimensions(int w, int h) {
		this.mForceHeight = h;
		this.mForceWidth = w;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mForceWidth, mForceHeight);
	}
}