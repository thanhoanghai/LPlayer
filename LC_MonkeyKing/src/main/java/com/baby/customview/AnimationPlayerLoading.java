package com.baby.customview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AnimationPlayerLoading extends ImageView {

	public AnimationPlayerLoading(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public AnimationPlayerLoading(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AnimationPlayerLoading(Context context) {
		super(context);
		init();
	}

	private void init() {
		AnimationDrawable frameAnimation = (AnimationDrawable) getDrawable();
		frameAnimation.setCallback(this);
		frameAnimation.setVisible(true, true);
		frameAnimation.start();
	}
}