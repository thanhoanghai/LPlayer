
package com.baby.chromecast;

import android.media.MediaCodec;
import android.media.RemoteControlClient;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.widget.SeekBar;

import com.baby.cartoonnetwork.R;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;

import java.util.concurrent.TimeUnit;

public class BaseCastPlayerActivity extends ActionBarActivity implements
        MediaCodecVideoTrackRenderer.EventListener {

    private static final String TAG = "BaseCastPlayerActivity";

    protected static final double VOLUME_INCREMENT = 0.05;
    protected static final double MAX_VOLUME_LEVEL = 20;

    protected Handler mHandler;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;

    private RemoteControlClient mRemoteControlClient;

    protected boolean mSeeking;
    private boolean mIsUserSeeking;

    protected SeekBar timeSeekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mHandler = new Handler();

        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = buildMediaRouteSelector();
        mMediaRouterCallback = new MyMediaRouterCallback();

        mIsUserSeeking = false;
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    @Override
    protected void onStop() {
        mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat
                .getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            onVolumeChange(VOLUME_INCREMENT);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            onVolumeChange(-VOLUME_INCREMENT);
        } else {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    public void onVolumeChange(double delta) {
    };

    public void onPlayMedia(MediaInfo media) {
    };

    private MediaRouteSelector buildMediaRouteSelector() {
        return new MediaRouteSelector.Builder().addControlCategory(
                getControlCategory()).build();
    }

    @Override
    public void onDroppedFrames(int i, long l) {

    }

    @Override
    public void onVideoSizeChanged(int i, int i1, float v) {

    }

    @Override
    public void onDrawnToSurface(Surface surface) {

    }

    @Override
    public void onDecoderInitializationError(
            MediaCodecTrackRenderer.DecoderInitializationException e) {

    }

    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {

    }

    private class MyMediaRouterCallback extends MediaRouter.Callback {
        @Override
        public void onRouteSelected(MediaRouter router, RouteInfo route) {
            Log.e(TAG, "onRouteSelected: route=" + route);
            BaseCastPlayerActivity.this.onRouteSelected(route);
        }

        @Override
        public void onRouteUnselected(MediaRouter router, RouteInfo route) {
            Log.e(TAG, "onRouteUnselected: route=" + route);
            BaseCastPlayerActivity.this.onRouteUnselected(route);
        }
    }

    protected final String getReceiverApplicationId() {
        return CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
    }

    protected String getControlCategory() {
        return CastMediaControlIntent
                .categoryForCast(getReceiverApplicationId());
    }

    /**
     * @param position The stream position, or 0 if no media is currently loaded, or -1 to leave the
     *            value unchanged.
     * @param duration The stream duration, or 0 if no media is currently loaded, or -1 to leave the
     *            value unchanged.
     */
    protected final void refreshPlaybackPosition(long position, long duration) {
        if (!mIsUserSeeking) {
            if (position == 0) {
                // mStreamPositionTextView.setText(R.string.no_time);
                timeSeekbar.setProgress(0);
            } else if (position > 0) {
                timeSeekbar.setProgress((int) TimeUnit.MILLISECONDS
                        .toSeconds(position));
            }
            // mStreamPositionTextView.setText(formatTime(position));
        }

        if (duration == 0) {
            // mStreamDurationTextView.setText(R.string.no_time);
            timeSeekbar.setMax(0);
        } else if (duration > 0) {
            // mStreamDurationTextView.setText(formatTime(duration));
            if (!mIsUserSeeking) {
                timeSeekbar.setMax((int) TimeUnit.MILLISECONDS
                        .toSeconds(duration));
            }
        }
    }

    protected final boolean isUserSeeking() {
        return mIsUserSeeking;
    }

    protected void onRouteSelected(RouteInfo route) {
    };

    protected void onRouteUnselected(RouteInfo route) {
    };
}
