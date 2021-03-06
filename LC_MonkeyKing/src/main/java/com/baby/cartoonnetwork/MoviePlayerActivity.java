
package com.baby.cartoonnetwork;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaCodec.CryptoException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baby.chromecast.SdkCastPlayerActivity;
import com.baby.chromecast.SdkCastPlayerActivity.CastingFilmSuccess;
import com.baby.constant.Constants;
import com.baby.constant.GlobalSingleton;
import com.baby.dataloader.URLProvider;
import com.baby.dbdownloader.DownloaderUtils;
import com.baby.dbdownloader.MovieObject;
import com.baby.downloader.DownloadCache;
import com.baby.fragments.ExplosisDialog;
import com.baby.fragments.ExplosisFragment;
import com.baby.fragments.ExplosisListener;
import com.baby.fragments.StreamDialog;
import com.baby.fragments.StreamFragment;
import com.baby.fragments.StreamListener;
import com.baby.model.StreamObject;
import com.baby.model.StreamResult;
import com.baby.parselink.DecodeType;
import com.baby.parselink.LinkType;
import com.baby.parselink.StreamUtils;
import com.baby.utils.Debug;
import com.baby.utils.StringEscapeUtils;
import com.baby.utils.Utils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.VideoSurfaceView;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.common.images.WebImage;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoviePlayerActivity extends SdkCastPlayerActivity implements
        SurfaceHolder.Callback, ExoPlayer.Listener,
        MediaCodecVideoTrackRenderer.EventListener, OnTouchListener,
        OnClickListener, StreamListener, ExplosisListener, CastingFilmSuccess {

    private static final String TAG = "MoviePlayerActivity";

    public static final int TURN_OFF_ON = 1;
    public static final int ALLOW_ORIENTATION = 2;
    public static final int SHOW_VIDEO = 3;

    public static String chapterID = "";
    public String chapterTitle = "Unknown";
    public String chapterPoster = "Unknown";
    // private String url = "";

    private TextView startTime;
    // private SeekBar timeSeekbar;
    private TextView endTime;

    private ImageView playBtn;
    private ImageView fullscreenBtn;
    private ImageView headReturn;
    private ImageView explosisBtn;
    private ImageView streamBtn;
    private ImageView downloadBtn;

    private LinearLayout loadingData;
    private LinearLayout mContentLayout;
    private FrameLayout controlContainer;

    private FragmentTabHost mTabHost;

    public StreamResult streamObject;

    private int TIME_AUTO_OFF_SCREEN = 3000;

    private Boolean isLoading = false;
    private int mPosition = 0;
    private boolean landscape = false;

    private boolean mControllerShowingStatus = false;
    public static boolean mLoadingStatus = false;

    private int mProgress = 0;

    /**
     * Builds renderers for the player.
     */
    public interface RendererBuilder {
        void buildRenderers(RendererBuilderCallback callback);
    }

    public static final int RENDERER_COUNT = 2;
    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_AUDIO = 1;

    public static final int TYPE_DASH_VOD = 0;
    public static final int TYPE_SS_VOD = 1;
    public static final int TYPE_OTHER = 2;

    private Handler mainHandler;
    private VideoSurfaceView surfaceView;

    private ExoPlayer player;
    private RendererBuilder builder;
    private RendererBuilderCallback callback;
    private MediaCodecVideoTrackRenderer videoRenderer;

    private boolean autoPlay = true;
    private long playerPosition;

    private Uri contentUri;
    private String nowPlayingStream;
    private StreamObject lastStream;
    private int contentType;
    private String contentId;

    private boolean mIsUserSeeking;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_player_activity);

        Intent intent = getIntent();
        chapterID = intent.getStringExtra(Constants.CHAPTER_ID);
        chapterTitle = intent.getStringExtra(Constants.CHAPTER_TITLE);
        chapterPoster = intent.getStringExtra(Constants.CHAPTER_IMAGE);

        setCastingFilmSuccess(this);

        try {
            contentUri = Uri.parse(intent
                    .getStringExtra(DemoUtil.CONTENT_ID_EXTRA));
        } catch (NullPointerException error) {
            error.printStackTrace();
        }
        contentType = TYPE_DASH_VOD;
        contentId = intent.getStringExtra(DemoUtil.CONTENT_ID_EXTRA);

        mainHandler = new Handler(getMainLooper());
        // builder = getRendererBuilder();

        explosisBtn = (ImageView) findViewById(R.id.explosisBtn);
        headReturn = (ImageView) findViewById(R.id.head_return);
        playBtn = (ImageView) findViewById(R.id.head_play_all);
        playBtn.setOnClickListener(this);
        fullscreenBtn = (ImageView) findViewById(R.id.fullscreenBtn);
        fullscreenBtn.setOnClickListener(this);
        loadingData = (LinearLayout) findViewById(R.id.loadingData);
        mContentLayout = (LinearLayout) findViewById(R.id.player_activity_content_fragment);
        startTime = (TextView) findViewById(R.id.startTime);
        timeSeekbar = (SeekBar) findViewById(R.id.timeSeekbar);
        endTime = (TextView) findViewById(R.id.endTime);
        controlContainer = (FrameLayout) findViewById(R.id.controlContainer);
        streamBtn = (ImageView) findViewById(R.id.streamBtn);
        downloadBtn = (ImageView) findViewById(R.id.download_btn);

        streamBtn.setOnClickListener(this);
        headReturn.setOnClickListener(this);
        explosisBtn.setOnClickListener(this);
        downloadBtn.setOnClickListener(this);

        chapterID = getIntent().getStringExtra(Constants.CHAPTER_ID);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        mTabHost.addTab(
                mTabHost.newTabSpec("explosis").setIndicator(
                        getString(R.string.explosis)), ExplosisFragment.class,
                null);
        mTabHost.addTab(
                mTabHost.newTabSpec("stream_link").setIndicator(
                        getString(R.string.stream_link)), StreamFragment.class,
                null);

        if (!GlobalSingleton.getInstance().offline) {
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("ah", "get");
            client.get(URLProvider.getStreamLink(chapterID),
                    new TextHttpResponseHandler() {

                        @Override
                        public void onSuccess(int arg0, Header[] arg1,
                                String arg2) {
                            Debug.logData(TAG, arg2);

                            ObjectMapper objectMapper = new ObjectMapper();
                            objectMapper
                                    .configure(
                                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                            false);
                            try {
                                streamObject = objectMapper.readValue(arg2,
                                        StreamResult.class);

                                for (int i = 0; i < streamObject.data.size(); i++) {
                                    streamObject.data.get(i).stream = Utils
                                            .EncryptionKey(streamObject.data
                                                    .get(i).stream);
                                }

                                if (!TextUtils.isEmpty(streamObject.cf)) {
                                    streamObject.cf = Utils
                                            .EncryptionKey(streamObject.cf);
                                } else {
                                    streamObject.cf = GlobalSingleton
                                            .getInstance().cf;
                                }

                                loadVideoLink();

                            } catch (JsonParseException e) {
                                e.printStackTrace();
                            } catch (JsonMappingException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int arg0, Header[] arg1,
                                String arg2, Throwable arg3) {
                            finish();
                        }
                    });
        } else {
            contentType = TYPE_OTHER;
            builder = getRendererBuilder();

            explosisBtn.setVisibility(View.GONE);
            downloadBtn.setVisibility(View.GONE);
            streamBtn.setVisibility(View.GONE);
            fullscreenBtn.setVisibility(View.GONE);

            changeToLandScape();
        }

        /*
         * Seek bar's range is in seconds, to prevent possibility of user seeking to fractions of
         * seconds.
         */
        timeSeekbar
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mIsUserSeeking = false;
                        timeSeekbar.setSecondaryProgress(0);
                        player.seekTo(mProgress);
                        hideController();
                        onSeekBarMoved(TimeUnit.SECONDS.toMillis(seekBar
                                .getProgress()));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mIsUserSeeking = true;
                        timeSeekbar.setSecondaryProgress(seekBar.getProgress());
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                            int progress, boolean fromUser) {
                        mProgress = progress;
                    }
                });

        View root = findViewById(R.id.root);
        root.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    toggleControlsVisibility();
                }
                return true;
            }
        });

//        surfaceView = (VideoSurfaceView) findViewById(R.id.surface_view);
//        surfaceView.getHolder().addCallback(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        // Setup the player
        player = ExoPlayer.Factory.newInstance(RENDERER_COUNT, 1000, 5000);
        player.addListener(this);
        player.seekTo(playerPosition);
        // Request the renderers
        callback = new RendererBuilderCallback();
        if (builder != null) {
            builder.buildRenderers(callback);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Release the player
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
        }
        callback = null;
        videoRenderer = null;
    }

    public Handler getMainHandler() {
        return mainHandler;
    }

    // Internal methods

    private void toggleControlsVisibility() {
        if (mControllerShowingStatus) {
            hideController();
        } else {
            showController();
        }
    }

    private RendererBuilder getRendererBuilder() {
        String userAgent = DemoUtil.getUserAgent(this);
        switch (contentType) {
            case DemoUtil.TYPE_SS:
                return new SmoothStreamingRendererBuilder(this, userAgent,
                        contentUri.toString(), contentId);
            case DemoUtil.TYPE_DASH:
                return new DashRendererBuilder(this, userAgent,
                        contentUri.toString(), contentId);
            default:
                return new DefaultRendererBuilder(this, contentUri);
        }
    }

    private void onRenderers(RendererBuilderCallback callback,
            MediaCodecVideoTrackRenderer videoRenderer,
            MediaCodecAudioTrackRenderer audioRenderer) {
        if (this.callback != callback) {
            return;
        }
        this.callback = null;
        player.stop();
        this.videoRenderer = videoRenderer;
        player.prepare(videoRenderer, audioRenderer);
        maybeStartPlayback();
    }

    private void maybeStartPlayback() {
        Surface surface = surfaceView.getHolder().getSurface();
        if (videoRenderer == null || surface == null || !surface.isValid()) {
            // We're not ready yet.
            return;
        }
        player.sendMessage(videoRenderer,
                MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
        if (autoPlay) {
            player.setPlayWhenReady(true);
            autoPlay = false;
        }
    }

    private void onRenderersError(RendererBuilderCallback callback, Exception e) {
        if (this.callback != callback) {
            return;
        }
        this.callback = null;
        onError(e);
    }

    private void onError(Exception e) {
        // Debug.logData(TAG, e.getCause().toString());
        if (mPosition < streamObject.data.size()) {
            StreamObject current = streamObject.data.get(mPosition);
            if (current.sourceParsed != null && current.sourceParsed.length() > 0) {
                int i = 0;
                String orgStream = current.sourceParsed;
                while (i < streamObject.data.size())
                {
                    StreamObject obj = streamObject.data.get(i);
                    if ((obj.sourceParsed != null && obj.sourceParsed.equals(orgStream))
                            || obj.stream.equals(orgStream)) {
                        streamObject.data.remove(i);
                        Debug.logData(TAG, "remove index: " + i);
                    }
                    else
                    {
                        i++;
                    }
                }
                StreamFragment.updateData(streamObject.data);
            }
            else
            {
                streamObject.data.remove(mPosition);
                Debug.logData(TAG, "remove index: " + mPosition);
                StreamFragment.updateData(streamObject.data);
            }
        }

        if (streamObject.data.size() > 0) {
            mPosition = 0;
            loadVideoLink();
        } else {
            showDialog();
        }
    }

    // ExoPlayer.Listener implementation

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        // Do nothing.
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        // Do nothing.
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        onError(e);
    }

    // MediaCodecVideoTrackRenderer.Listener
    @Override
    public void onVideoSizeChanged(int width, int height,
            float pixelWidthHeightRatio) {
        surfaceView.setVideoWidthHeightRatio(height == 0 ? 1
                : (pixelWidthHeightRatio * width) / height);
    }

    @Override
    public void onDrawnToSurface(Surface surface) {
        loadingData.setVisibility(View.GONE);
        hideController();
        GlobalSingleton.getInstance().countFilm++;
        playBtn.setImageResource(R.drawable.lock_screen_pause);
        mMessageSeekbar.sendEmptyMessage(0);
    }

    @Override
    public void onDroppedFrames(int count, long elapsed) {
    }

    @Override
    public void onDecoderInitializationError(DecoderInitializationException e) {
        // This is for informational purposes only. Do nothing.
    }

    @Override
    public void onCryptoError(CryptoException e) {
        // This is for informational purposes only. Do nothing.
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        maybeStartPlayback();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (videoRenderer != null) {
            player.blockingSendMessage(videoRenderer,
                    MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, null);
        }
    }

    final class RendererBuilderCallback {

        public void onRenderers(MediaCodecVideoTrackRenderer videoRenderer,
                MediaCodecAudioTrackRenderer audioRenderer) {
            MoviePlayerActivity.this.onRenderers(this, videoRenderer,
                    audioRenderer);
        }

        public void onRenderersError(Exception e) {
            MoviePlayerActivity.this.onRenderersError(this, e);
        }

    }

    private void loadVideoLink()
    {

        if (streamObject.data != null && streamObject.data.size() > 0)
        {
            // ncnlam
            mPosition = streamObject.data.size() - 1;

            // init link stream
            nowPlayingStream = "";

            Debug.logData(TAG, "loadVideoLink");
            showLoading();
            final Handler handler = new Handler();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {

                        Map<Integer, JSONObject> data = StreamUtils
                                .parseFilm(StreamUtils
                                        .getJSONObject(streamObject.cf));
                        if (streamObject.data.get(mPosition).parseType == 0) {
                            // url = streamObject.data.get(mPosition).stream;
                            // Debug.logData(TAG, url);

                            mMessageSeekbar.sendEmptyMessage(0);

                            nowPlayingStream = streamObject.data
                                    .get(mPosition).stream;
                            lastStream = streamObject.data
                                    .get(mPosition);
                            playVideo();
                        } else {
                            ArrayList<StreamObject> values = getLinks(
                                    streamObject.data.get(mPosition), data);
                            if (values != null && values.size() > 0) {

                                if (values.size() == 1) {
                                    nowPlayingStream = values.get(0).stream;
                                    lastStream = streamObject.data
                                            .get(mPosition);
                                    streamObject.data.get(mPosition).quality = values.get(0).quality;
                                } else {
                                    // remove old parsed
                                    String orgStream = streamObject.data.get(mPosition).stream;
                                    int i = 0;
                                    while (i < streamObject.data.size())
                                    {
                                        StreamObject obj = streamObject.data.get(i);
                                        if (obj.sourceParsed != null
                                                && obj.sourceParsed.equals(orgStream)) {
                                            streamObject.data.remove(i);
                                        }
                                        else
                                        {
                                            i++;
                                        }
                                    }
                                    streamObject.data.addAll(mPosition + 1, values);

                                    StreamObject bestObject = StreamUtils.findBestStream(values,
                                            lastStream);
                                    lastStream = bestObject;
                                    nowPlayingStream = bestObject.stream;
                                    mPosition = streamObject.data.indexOf(bestObject);

                                    handler.post(new Runnable() {
                                        public void run() {
                                            StreamFragment.updateData(streamObject.data);
                                        }
                                    });
                                }
                                // Debug.logData(TAG, url);
                                playVideo();
                                mMessageSeekbar.sendEmptyMessage(0);
                            } else {
                                streamObject.data.remove(mPosition);
                                handler.post(new Runnable() {
                                    public void run() {
                                        StreamFragment.updateData(streamObject.data);
                                    }
                                });

                                if (streamObject.data.size() == 0) {
                                    showDialogNoLink().show();
                                }
                                else
                                {
                                    mPosition = 0;
                                    loadVideoLink();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }).start();
        } else {
            showDialogNoLink().show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void playVideo()
    {
        // Intent intent = new Intent(Intent.ACTION_VIEW);
        // intent.setDataAndType(Uri.parse(nowPlayingStream), "video/*");
        // startActivity(intent);
        // ncnlam
        contentUri = Uri.parse(nowPlayingStream);
        contentType = TYPE_OTHER;
        builder = getRendererBuilder();
        if (callback == null) {
            callback = new RendererBuilderCallback();
        }
        if (builder != null) {
            builder.buildRenderers(callback);
        }

        // String videoUrl = streamObject.data.get(mPosition).stream;
        Debug.logData(TAG, "url: " + nowPlayingStream);
        String imageurl = chapterPoster;
        String bigImageurl = chapterPoster;
        String title = chapterTitle;
        String studio = chapterTitle;
        List<MediaTrack> tracks = null;
        // if (video.has(TAG_TRACKS)) {
        // JSONArray tracksArray = video.getJSONArray(TAG_TRACKS);
        // if (tracksArray != null) {
        tracks = new ArrayList<MediaTrack>();
        // for (int k = 0; k < tracksArray.length(); k++) {
        // JSONObject track = tracksArray.getJSONObject(k);
        tracks.add(buildTrack(1, "video", "SUBTYPE", "CONTENT_ID", "NAME",
                "ENGLISH"));
        // }
        // }
        // }

        mSelectedMedia = buildMediaInfo(title, "", studio, nowPlayingStream, imageurl,
                bigImageurl, tracks);
    }

    protected AlertDialog showDialogNoLink() {
        return new AlertDialog.Builder(this)
                .setTitle("Information")
                .setMessage("I'm sorry, we can't get the link to play.")
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                finish();
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    private void showVideo() {
        loadingData.setVisibility(View.GONE);
        hideController();
    }

    private void hideController() {
        mControllerShowingStatus = false;
        controlContainer.setVisibility(View.GONE);
    }

    private void showController() {
        mControllerShowingStatus = true;
        controlContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onExplosisListener(String chapterid) {
        StreamFragment streamFragment = (StreamFragment) getSupportFragmentManager()
                .findFragmentByTag("stream_link");
        chapterID = chapterid;
        if (streamFragment != null) {
            streamFragment.refreshStream(chapterID);
        }
    }

    @Override
    public void onStreamChangeListener(int position) {
        Debug.logData(TAG, "onStreamChangeListener");
        nowPlayingStream = "";
        mPosition = position;
        loadVideoLink();
    }

    private JSONObject emptyJson;

    private final String baseLink = Constants.SERVICE_URL
            + "type=directlink&id=%s";

    public ArrayList<StreamObject> getLinks(StreamObject stream,
            Map<Integer, JSONObject> data) throws JSONException {
        emptyJson = StreamUtils.getJSONObject("{}");
        JSONObject dic = data.get(stream.parseType);
        ArrayList<String> result = new ArrayList<String>();
        parseLink(stream.stream, dic, result);

        return StreamUtils.parserStream(result, stream.quality, stream.server, stream.stream);
    }

    public Collection<String> stringMatcherRegex(String data, String regex,
            int matchGroup, int type) {
        Set<String> result = new HashSet<String>();
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(data);
            while (matcher.find()) {
                result.add(decode(matcher.group(matchGroup), type));
            }
        } catch (Exception e) {
        }
        return result;
    }

    public String decode(String data, int type) {
        switch (type) {
            case DecodeType.BASE64_DECODE:
                data = new String(Base64.decode(data, Base64.DEFAULT));
                Debug.logData(TAG, data);
                break;
            case DecodeType.ESCAPTE_HTML:
                data = StringEscapeUtils.escapeHtml(data);
                break;
            case DecodeType.ESCAPTE_HTML_NUMBER:
                data = StringEscapeUtils.replaceHtmlEscapeNumber(data);
                break;
            case DecodeType.URL_DECODE:
                try {
                    data = URLDecoder.decode(data, "UTF-8");
                    Debug.logData(TAG, data);
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                break;
            case DecodeType.URL_ENCODE:
                try {
                    data = URLEncoder.encode(data, "UTF-8");
                    Debug.logData(TAG, data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
        }
        return data;
    }

    private void getStreamObjectFromString(String string, List<String> result)
    {
        if (string == null || string.isEmpty()) {
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            StreamResult currResult = objectMapper.readValue(string, StreamResult.class);
            if (currResult == null || currResult.data == null) {
                return;
            }
            for (StreamObject item : currResult.data) {
                result.add(item.stream + "#" + item.quality);
            }

        } catch (Exception e) {
        }

    }

    private void parseLink(String link, JSONObject json, List<String> result)
            throws JSONException {
        if (json == null || json.equals(emptyJson)) {
            return;
        }
        int l = json.getInt("l");
        String data = "";
        switch (l) {
            case LinkType.DIRECT:
                data = link;
                break;
            case LinkType.MAKE_GET_REQUEST:
                data = StreamUtils.getTextFromLink(link);
                break;
            case LinkType.MAKE_HEAD_REQUEST:
                data = StreamUtils.getHeadFromLink(link);
                break;
            case LinkType.BASE64_REQUEST_SERVER:
                String dataid = Base64.encodeToString(link.getBytes(), Base64.DEFAULT);
                try {
                    dataid = URLEncoder.encode(dataid, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                getStreamObjectFromString(StreamUtils.getTextFromLink(String.format(baseLink,
                        dataid)), result);
                return;
        }

        String rp = json.getString("rp");
        String r = json.getString("r");
        int i = json.getInt("i");
        int d = json.getInt("d");
        int ir = json.getInt("ir");

        if (!TextUtils.isEmpty(rp)) {
            result.add(decode(data.replaceAll(r, rp), d));
        } else {
            result.addAll(stringMatcherRegex(data, r, ir, d));
        }

        if (result.isEmpty()) {
            return;
        }

        json = json.getJSONObject("p");

        if (json.length() == 0) {
            if (i < 0) {
                return;
            }
            int index = result.size() > i ? i : result.size() - 1;
            String tmp = result.get(index);
            result.clear();
            result.add(tmp);
            return;
        }

        int index = i < 0 ? 0 : result.size() > i ? i : result.size() - 1;
        link = result.get(index);
        result.clear();
        parseLink(link, json, result);

    }

    private void changeToPortrait() {
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        fullscreenBtn.setImageResource(R.drawable.fullscreen_button);
        closeLayout(View.VISIBLE);
        getSupportActionBar().show();
    }

    private void changeToLandScape() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        fullscreenBtn.setImageResource(R.drawable.scale_button);
        closeLayout(View.GONE);
        getSupportActionBar().hide();
    }

    private void closeLayout(int visibility) {
        mContentLayout.setVisibility(visibility);
        if (visibility == View.GONE) {
            // linearSetting.setVisibility(View.VISIBLE);
            // relativeButtonSetting.setVisibility(View.VISIBLE);
        } else {
            // linearSetting.setVisibility(View.GONE);
            // relativeButtonSetting.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (GlobalSingleton.getInstance().offline)
            return;

        int visibility = View.VISIBLE;
        landscape = (Configuration.ORIENTATION_LANDSCAPE == newConfig.orientation);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            visibility = View.GONE;
        }

        closeLayout(visibility);

        if (landscape) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            fullscreenBtn.setImageResource(R.drawable.scale_button);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            fullscreenBtn.setImageResource(R.drawable.fullscreen_button);
        }
    }

    private void pauseVideo() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }

        playBtn.setImageResource(R.drawable.lock_screen_play);
        mMessageSeekbar.removeMessages(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_play_all:
                if (player.getPlayWhenReady()) {
                    pauseVideo();
                    onPauseClicked();
                } else {
                    if (android.provider.Settings.System.getInt(
                            getContentResolver(),
                            Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
                        // ON
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    } else {
                    }
                    onPlayClicked();
                    if (player != null) {
                        player.setPlayWhenReady(true);
                    }
                    playBtn.setImageResource(R.drawable.lock_screen_pause);
                    mMessageSeekbar.sendEmptyMessage(0);
                    hideController();
                }
                break;
            case R.id.fullscreenBtn:
                if (mContentLayout.getVisibility() == View.GONE) {
                    changeToPortrait();
                } else {
                    changeToLandScape();
                }
                break;
            case R.id.head_return:
                pauseVideo();
                finish();
            case R.id.explosisBtn:
                showExplosisDialog();
                break;
            case R.id.streamBtn:
                showStreamDialog();
                break;
            case R.id.download_btn:
                if (nowPlayingStream.length() > 0)
                {
                    MovieObject movieObject = new MovieObject();
                    movieObject.setMovieId(DetailActivity.detailObject.data.id);
                    movieObject.setMovieTitle(DetailActivity.detailObject.data.title);
                    movieObject.setPath("");
                    movieObject.setPoster(DetailActivity.poster);
                    movieObject.setStatus(DownloadCache.DOWNLOAD_UNSUCCESSFUL);
                    movieObject.setURLLink(nowPlayingStream);
                    DownloaderUtils.downloadMovie(getBaseContext(), movieObject);
                    Toast.makeText(
                            MoviePlayerActivity.this,
                            getString(R.string.download_movie) + " "
                                    + DetailActivity.detailObject.data.title, Toast.LENGTH_SHORT)
                            .show();
                }
                break;

        }
    }

    void showStreamDialog() {

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = StreamDialog.newInstance(1);
        newFragment.show(ft, "dialog");
    }

    void showExplosisDialog() {

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager()
                .findFragmentByTag("dialog1");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = ExplosisDialog.newInstance(1);
        newFragment.show(ft, "dialog1");
    }

    private void showControllerAutoTurnoff() {
        mControllerShowingStatus = true;
        controlContainer.setVisibility(View.VISIBLE);
        mMessageHandler.sendEmptyMessageDelayed(TURN_OFF_ON,
                TIME_AUTO_OFF_SCREEN);
        if (landscape) {
            // relativeButtonSetting.setVisibility(View.VISIBLE);
        }
    }

    private Handler mMessageSeekbar = new Handler() {
        public void handleMessage(android.os.Message msg) {
            setTimeMovie();
            mMessageSeekbar.sendEmptyMessageDelayed(0, 800);
        };
    };

    private void setTimeMovie() {
        if (player != null && player.isPlayWhenReadyCommitted()) {
            startTime.setText(Utils.makeTimeString(MoviePlayerActivity.this,
                    player.getCurrentPosition() / 1000));
            endTime.setText(Utils.makeTimeString(MoviePlayerActivity.this,
                    (player.getDuration() - player.getCurrentPosition()) / 1000));
            timeSeekbar.setMax((int) player.getDuration());
            timeSeekbar.setProgress((int) player.getCurrentPosition());

            int max = (int) player.getDuration();
            int percent = Utils.getProgressPercentage(
                    player.getBufferedPercentage(), max);
            timeSeekbar.setSecondaryProgress(percent);

        }
    }

    private void showLoading() {
        isLoading = true;
        loadingData.setVisibility(View.VISIBLE);
        hideController();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (player != null && !isLoading) {
                if (!mControllerShowingStatus) {
                    showControllerAutoTurnoff();
                } else {
                    mMessageHandler.removeCallbacksAndMessages(null);
                    hideController();
                }
            }
            return true;
        }
        return false;
    }

    private Handler mMessageHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case TURN_OFF_ON:
                    hideController();
                    break;
                // case ALLOW_ORIENTATION:
                // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                // break;
                case SHOW_VIDEO:
                    showVideo();
                    break;
                default:
                    break;
            }
        };
    };

    protected AlertDialog showDialog() {
        return new AlertDialog.Builder(MoviePlayerActivity.this)
                .setTitle("Information")
                .setMessage(
                        "Can't get link to play. Please choose other quality to play. Thank you")
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    private String getMediaType() {
        return "video/mp4";
    }

    private MediaInfo buildMediaInfo(String title, String subTitle,
            String studio, String url, String imgUrl, String bigImageUrl,
            List<MediaTrack> tracks) {
        MediaMetadata movieMetadata = new MediaMetadata(
                MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        movieMetadata.putString(MediaMetadata.KEY_STUDIO, studio);
        movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
        movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(getMediaType()).setMetadata(movieMetadata)
                .setMediaTracks(tracks).build();
    }

    private MediaTrack buildTrack(long id, String type, String subType,
            String contentId, String name, String language) {
        int trackType = MediaTrack.TYPE_UNKNOWN;
        if ("text".equals(type)) {
            trackType = MediaTrack.TYPE_TEXT;
        } else if ("video".equals(type)) {
            trackType = MediaTrack.TYPE_VIDEO;
        } else if ("audio".equals(type)) {
            trackType = MediaTrack.TYPE_AUDIO;
        }

        int trackSubType = MediaTrack.SUBTYPE_NONE;
        if (subType != null) {
            if ("captions".equals(type)) {
                trackSubType = MediaTrack.SUBTYPE_CAPTIONS;
            } else if ("subtitle".equals(type)) {
                trackSubType = MediaTrack.SUBTYPE_SUBTITLES;
            }
        }

        return new MediaTrack.Builder(id, trackType).setName(name)
                .setSubtype(trackSubType).setContentId(contentId)
                .setLanguage(language).build();
    }

    @Override
    public void onCastingFilmSuccess() {
        Debug.logData(TAG, "onCastingFilmSuccess");
        pauseVideo();
    }

}
