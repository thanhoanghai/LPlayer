package com.baby.chromecast;

import java.io.IOException;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;

import com.baby.cartoonnetwork.R;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastStatusCodes;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.WebImage;

public class SdkCastPlayerActivity extends BaseCastPlayerActivity {

	private static final String TAG = "SdkCastPlayerActivity";

	private CastDevice mSelectedDevice;
	private GoogleApiClient mApiClient;
	private CastListener mCastListener;
	private ConnectionCallbacks mConnectionCallbacks;
	private ConnectionFailedListener mConnectionFailedListener;
	private RemoteMediaPlayer mMediaPlayer;
	private boolean mShouldPlayMedia;
	protected MediaInfo mSelectedMedia;
	private ApplicationMetadata mAppMetadata;

	private CastingFilmSuccess mCastingFilm;

	private boolean mWaitingForReconnect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mConnectionCallbacks = new ConnectionCallbacks();
		mConnectionFailedListener = new ConnectionFailedListener();

		mCastListener = new CastListener();

	}

	public void setCastingFilmSuccess(CastingFilmSuccess listener) {
		this.mCastingFilm = listener;
	}

	public void onVolumeChange(double delta) {
		if (mApiClient == null) {
			return;
		}

		try {
			double volume = Cast.CastApi.getVolume(mApiClient) + delta;
			// refreshDeviceVolume(volume, Cast.CastApi.isMute(mApiClient));
			Cast.CastApi.setVolume(mApiClient, volume);
		} catch (IOException e) {
			Log.w(TAG, "Unable to change volume", e);
		} catch (IllegalStateException e) {
			Log.w(TAG, "Unable to change volume", e);
		}
	}

	/*
	 * Connects to the device (if necessary), and then casts the currently
	 * selected video.
	 */
	public void onPlayMedia(final MediaInfo media) {
		mSelectedMedia = media;

		if (mAppMetadata == null) {
			return;
		}

		playMedia(mSelectedMedia);
	}

	public void onPlayClicked() {
		if (mMediaPlayer == null) {
			return;
		}
		try {
			mMediaPlayer.play(mApiClient);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void onPauseClicked() {
		if (mMediaPlayer == null) {
			return;
		}
		try {
			mMediaPlayer.pause(mApiClient);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void onStopClicked() {
		if (mMediaPlayer == null) {
			return;
		}
		try {
			mMediaPlayer.stop(mApiClient);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void onSeekBarMoved(long position) {
		if (mMediaPlayer == null) {
			return;
		}

		refreshPlaybackPosition(position, -1);
		//
		// int behavior = getSeekBehavior();
		//
		// int resumeState;
		// switch (behavior) {
		// case AFTER_SEEK_PLAY:
		// resumeState = RemoteMediaPlayer.RESUME_STATE_PLAY;
		// break;
		// case AFTER_SEEK_PAUSE:
		// resumeState = RemoteMediaPlayer.RESUME_STATE_PAUSE;
		// break;
		// case AFTER_SEEK_DO_NOTHING:
		// default:
		// resumeState = RemoteMediaPlayer.RESUME_STATE_UNCHANGED;
		// }
		mSeeking = true;
		mMediaPlayer.seek(mApiClient, position,
				RemoteMediaPlayer.RESUME_STATE_PLAY).setResultCallback(
				new ResultCallback<MediaChannelResult>() {
					@Override
					public void onResult(MediaChannelResult result) {
						Status status = result.getStatus();
						if (status.isSuccess()) {
							mSeeking = false;
						} else {
							Log.w(TAG,
									"Unable to seek: " + status.getStatusCode());
						}
					}

				});
	}

	private void attachMediaPlayer() {
		if (mMediaPlayer != null) {
			return;
		}

		mMediaPlayer = new RemoteMediaPlayer();
		mMediaPlayer
				.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {

					@Override
					public void onStatusUpdated() {
						Log.d(TAG, "MediaControlChannel.onStatusUpdated");
						// If item has ended, clear metadata.
						MediaStatus mediaStatus = mMediaPlayer.getMediaStatus();
						if ((mediaStatus != null)
								&& (mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_IDLE)) {
							// clearMediaState();
						}

						updatePlaybackPosition();
						// updateStreamVolume();
						// updateButtonStates();
					}
				});

		mMediaPlayer
				.setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
					@Override
					public void onMetadataUpdated() {
						Log.d(TAG, "MediaControlChannel.onMetadataUpdated");
						String title = null;
						String artist = null;
						Uri imageUrl = null;

						MediaInfo mediaInfo = mMediaPlayer.getMediaInfo();
						if (mediaInfo != null) {
							MediaMetadata metadata = mediaInfo.getMetadata();
							if (metadata != null) {
								title = metadata
										.getString(MediaMetadata.KEY_TITLE);

								artist = metadata
										.getString(MediaMetadata.KEY_ARTIST);
								if (artist == null) {
									artist = metadata
											.getString(MediaMetadata.KEY_STUDIO);
								}

								List<WebImage> images = metadata.getImages();
								if ((images != null) && !images.isEmpty()) {
									WebImage image = images.get(0);
									imageUrl = image.getUrl();
								}
							}
							// setCurrentMediaMetadata(title, artist, imageUrl);
						}
					}
				});

		try {
			Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
					mMediaPlayer.getNamespace(), mMediaPlayer);
		} catch (IOException e) {
			Log.w(TAG, "Exception while launching application", e);
		}
	}

	private void updatePlaybackPosition() {
		if (mMediaPlayer == null) {
			return;
		}
		refreshPlaybackPosition(mMediaPlayer.getApproximateStreamPosition(),
				mMediaPlayer.getStreamDuration());
	}

	private void reattachMediaPlayer() {
		if ((mMediaPlayer != null) && (mApiClient != null)) {
			try {
				Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
						mMediaPlayer.getNamespace(), mMediaPlayer);
			} catch (IOException e) {
				Log.w(TAG, "Exception while launching application", e);
			}
		}
	}

	private void detachMediaPlayer() {
		if ((mMediaPlayer != null) && (mApiClient != null)) {
			try {
				Cast.CastApi.removeMessageReceivedCallbacks(mApiClient,
						mMediaPlayer.getNamespace());
			} catch (IOException e) {
				Log.w(TAG, "Exception while launching application", e);
			}
		}
		mMediaPlayer = null;
	}

	/*
	 * Begins playback of the currently selected video.
	 */
	private void playMedia(MediaInfo media) {
		Log.d(TAG, "playMedia: " + media);
		if (media == null) {
			return;
		}
		if (mMediaPlayer == null) {
			Log.e(TAG, "Trying to play a video with no active media session");
			return;
		}

		mMediaPlayer.load(mApiClient, media, true).setResultCallback(
				new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
					@Override
					public void onResult(MediaChannelResult result) {
						if (result.getStatus().isSuccess()) {
							if (mCastingFilm != null) {
								mCastingFilm.onCastingFilmSuccess();
							}
						} else {
							Log.e(TAG, "Failed to load media.");
						}
					}
				});
	}

	@Override
	protected void onRouteSelected(RouteInfo route) {
		super.onRouteSelected(route);

		Log.d(TAG, "onRouteSelected: " + route);

		CastDevice device = CastDevice.getFromBundle(route.getExtras());
		setSelectedDevice(device);
		// updateButtonStates();
	}

	@Override
	protected void onRouteUnselected(RouteInfo route) {
		super.onRouteUnselected(route);

		Log.d(TAG, "onRouteUnselected: " + route);

		setSelectedDevice(null);
		mAppMetadata = null;
		// clearMediaState();
		// updateButtonStates();
	}

	private class ConnectionCallbacks implements
			GoogleApiClient.ConnectionCallbacks {
		@Override
		public void onConnectionSuspended(int cause) {
			Log.d(TAG, "ConnectionCallbacks.onConnectionSuspended");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// TODO: need to disable all controls, and possibly display
					// a "reconnecting..." dialog or overlay
					detachMediaPlayer();
					// updateButtonStates();
					mWaitingForReconnect = true;
				}
			});
		}

		@Override
		public void onConnected(final Bundle connectionHint) {
			Log.e(TAG, "ConnectionCallbacks.onConnected");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mApiClient == null) {
						// We got disconnected while this runnable was pending
						// execution.
						return;
					}
					try {
						Cast.CastApi.requestStatus(mApiClient);
					} catch (IOException e) {
						Log.d(TAG, "error requesting status", e);
					}

					// TODO REMOVE
					Cast.CastApi.launchApplication(mApiClient,
							getReceiverApplicationId(), true)
							.setResultCallback(
									new ApplicationConnectionResultCallback(
											"LaunchApp"));

					// setDeviceVolumeControlsEnabled(true);
					// mLaunchAppButton.setEnabled(true);
					// mJoinAppButton.setEnabled(true);

					if (mWaitingForReconnect) {
						mWaitingForReconnect = false;
						if ((connectionHint != null)
								&& connectionHint
										.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
							mAppMetadata = null;
							// clearMediaState();
							// cancelRefreshTimer();
							// updateButtonStates();
						} else {
							reattachMediaPlayer();
						}
					}
				}
			});
		}
	}

	private class ConnectionFailedListener implements
			GoogleApiClient.OnConnectionFailedListener {
		@Override
		public void onConnectionFailed(ConnectionResult result) {
			Log.d(TAG, "onConnectionFailed");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// updateButtonStates();
					// clearMediaState();
					// cancelRefreshTimer();
					showErrorDialog(getString(R.string.error_no_device_connection));
				}
			});
		}
	}

	private class CastListener extends Cast.Listener {
		@Override
		public void onVolumeChanged() {
			// refreshDeviceVolume(Cast.CastApi.getVolume(mApiClient),
			// Cast.CastApi.isMute(mApiClient));
		}

		@Override
		public void onApplicationStatusChanged() {
			try {
				String status = Cast.CastApi.getApplicationStatus(mApiClient);
				Log.d(TAG, "onApplicationStatusChanged; status=" + status);
				// setApplicationStatus(status);
			} catch (Exception error) {
				error.printStackTrace();
			}
		}

		@Override
		public void onApplicationDisconnected(int statusCode) {
			Log.d(TAG, "onApplicationDisconnected: statusCode=" + statusCode);
			mAppMetadata = null;
			detachMediaPlayer();
			// clearMediaState();
			// updateButtonStates();
			if (statusCode != CastStatusCodes.SUCCESS) {
				// This is an unexpected disconnect.
				// setApplicationStatus(getString(R.string.status_app_disconnected));
			}
		}
	}

	private final class ApplicationConnectionResultCallback implements
			ResultCallback<Cast.ApplicationConnectionResult> {
		private final String mClassTag;

		public ApplicationConnectionResultCallback(String suffix) {
			mClassTag = TAG + "_" + suffix;
		}

		@Override
		public void onResult(ApplicationConnectionResult result) {
			Status status = result.getStatus();
			Log.e(mClassTag,
					"ApplicationConnectionResultCallback.onResult: statusCode "
							+ status.getStatusCode());
			if (status.isSuccess()) {
				ApplicationMetadata applicationMetadata = result
						.getApplicationMetadata();
				String sessionId = result.getSessionId();
				String applicationStatus = result.getApplicationStatus();
				boolean wasLaunched = result.getWasLaunched();
				Log.d(mClassTag,
						"application name: " + applicationMetadata.getName()
								+ ", status: " + applicationStatus
								+ ", sessionId: " + sessionId
								+ ", wasLaunched: " + wasLaunched);
				// setApplicationStatus(applicationStatus);
				attachMediaPlayer();
				mAppMetadata = applicationMetadata;
				// startRefreshTimer();
				// updateButtonStates();
				mShouldPlayMedia = true;
				Log.d(mClassTag, "mShouldPlayMedia is " + mShouldPlayMedia);
				if (mShouldPlayMedia) {
					mShouldPlayMedia = false;
					playMedia(mSelectedMedia);
				} else {
					// Synchronize with the receiver's state.
					Log.d(mClassTag, "requesting current media status");
					mMediaPlayer
							.requestStatus(mApiClient)
							.setResultCallback(
									new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
										@Override
										public void onResult(
												MediaChannelResult result) {
											Status status = result.getStatus();
											if (!status.isSuccess()) {
												Log.w(mClassTag,
														"Unable to request status: "
																+ status.getStatusCode());
											}
										}
									});
				}
			} else {
				showErrorDialog(getString(R.string.error_app_launch_failed));
			}
		}
	}

	protected final void showErrorDialog(String errorString) {
		if (!isFinishing()) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.error)
					.setMessage(errorString)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							}).create().show();
		}
	}

	private void setSelectedDevice(CastDevice device) {
		mSelectedDevice = device;
		// setCurrentDeviceName(mSelectedDevice != null ?
		// mSelectedDevice.getFriendlyName() : null);

		if (mSelectedDevice == null) {
			detachMediaPlayer();
			if ((mApiClient != null) && mApiClient.isConnected()) {
				mApiClient.disconnect();
			}
			mApiClient = null;
		} else {
			Log.d(TAG, "acquiring controller for " + mSelectedDevice);
			try {
				Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
						.builder(mSelectedDevice, mCastListener);

				mApiClient = new GoogleApiClient.Builder(this)
						.addApi(Cast.API, apiOptionsBuilder.build())
						.addConnectionCallbacks(mConnectionCallbacks)
						.addOnConnectionFailedListener(
								mConnectionFailedListener).build();
				mApiClient.connect();
			} catch (IllegalStateException e) {
				Log.w(TAG, "error while creating a device controller", e);
				showErrorDialog(getString(R.string.error_no_controller));
			}
		}
	}

	public interface CastingFilmSuccess {
		public void onCastingFilmSuccess();
	}

}
