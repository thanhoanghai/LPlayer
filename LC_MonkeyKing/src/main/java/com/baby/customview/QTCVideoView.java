package com.baby.customview;

import com.baby.utils.Debug;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.VideoView;

public class QTCVideoView extends VideoView {

	private Context mContext;
	
	private int mForceHeight = 0;
	private int mForceWidth = 0;
	
	private int screen_width;
	private int screen_height;
	private int screen_large_height;

	public QTCVideoView(Context context) {
		super(context);
		mContext = context;
//		initView();
	}

	public QTCVideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mContext = context;
//		initView();
	}

	public QTCVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
//		initView();
	}

//	private void initView() {
//		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
//		screen_width = metrics.widthPixels;
//		screen_large_height = metrics.heightPixels;
//
//		screen_height = screen_width * 9 / 16;
//		int temp = 0;
//		if (screen_width > screen_large_height) {
//			temp = screen_width;
//			screen_width = screen_large_height;
//			screen_large_height = temp;
//		}
//	}

	public void setDimensions(int w, int h) {
		this.mForceHeight = h;
		this.mForceWidth = w;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mForceWidth, mForceHeight);
	}
}