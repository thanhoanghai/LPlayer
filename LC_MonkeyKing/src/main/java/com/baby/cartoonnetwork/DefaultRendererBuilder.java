/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baby.cartoonnetwork;

import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;

import com.baby.cartoonnetwork.MoviePlayerActivity.RendererBuilder;
import com.baby.cartoonnetwork.MoviePlayerActivity.RendererBuilderCallback;
import com.baby.chromecast.BaseCastPlayerActivity;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.source.DefaultSampleSource;
import com.google.android.exoplayer.source.FrameworkSampleExtractor;

/**
 * A {@link RendererBuilder} for streams that can be read using
 * {@link android.media.MediaExtractor}.
 */
/* package */class DefaultRendererBuilder implements RendererBuilder {

	private final BaseCastPlayerActivity playerActivity;
	private final Uri uri;

	public DefaultRendererBuilder(BaseCastPlayerActivity playerActivity, Uri uri) {
		this.playerActivity = playerActivity;
		this.uri = uri;
	}

	@Override
	public void buildRenderers(RendererBuilderCallback callback) {
		// Build the video and audio renderers.
		DefaultSampleSource sampleSource = new DefaultSampleSource(
				new FrameworkSampleExtractor(playerActivity, uri, null), 2);
		MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(
				sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 0,
				new Handler(playerActivity.getMainLooper()), playerActivity, 50);
		MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(
				sampleSource);

		// Invoke the callback.
		callback.onRenderers(videoRenderer, audioRenderer);
	}

}
