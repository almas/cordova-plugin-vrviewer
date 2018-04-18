package com.cordova.plugin.vrviewer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.vr.sdk.widgets.common.VrWidgetView;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.google.vr.sdk.widgets.video.VrVideoView.Options;

import java.io.IOException;

import org.apache.cordova.CordovaActivity;
import org.json.JSONObject;

public class VideoActivity extends CordovaActivity {

    private static final String TAG = VideoActivity.class.getSimpleName();

    protected VrVideoView videoWidgetView;
    private boolean isPaused = false;
    private CordovaActivity that = null;

    /**
     * Preserve the video's state when rotating the phone.
     */
    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_PROGRESS_TIME = "progressTime";
    /**
     * The video duration doesn't need to be preserved, but it is saved in this example. This allows
     * the seekBar to be configured during {@link #onRestoreInstanceState(Bundle)} rather than waiting
     * for the video to be reloaded and analyzed. This avoid UI jank.
     */
    private static final String STATE_VIDEO_DURATION = "videoDuration";

    public static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
    public static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
    public static final int LOAD_VIDEO_STATUS_ERROR = 2;

    private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

    /**
     * Tracks the file to be loaded across the lifetime of this app.
     **/
    private Uri fileUri;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getApplication().getResources().getIdentifier("vr_viewer_main","layout",getApplication().getPackageName()));

        videoWidgetView = (VrVideoView) findViewById(getApplication().getResources().getIdentifier("vr_view","id",getApplication().getPackageName()));
        videoWidgetView.setDisplayMode(VrVideoView.DisplayMode.FULLSCREEN_MONO);
        videoWidgetView.setVisibility(View.INVISIBLE);
        videoWidgetView.setInfoButtonEnabled(false);
        videoWidgetView.setTransitionViewEnabled(false);
        videoWidgetView.setEventListener(new ActivityEventListener());
        loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

        that = this;
        handleIntent(getIntent());
    }

    /**
     * Called when the Activity is already running and it's given a new intent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, this.hashCode() + ".onNewIntent()");
        // Save the intent. This allows the getIntent() call in onCreate() to use this new Intent during
        // future invocations.
        setIntent(intent);
        // Load the new image.
        handleIntent(intent);
    }

    public int getLoadVideoStatus() {
        return loadVideoStatus;
    }

    private void handleIntent(Intent intent) {
        String url = intent.getStringExtra("url");

        String optionsRaw = intent.getStringExtra("options");
        String inputTypeString = null;
        String inputFormatString = null;
        try {
            JSONObject optionsJSON = new JSONObject(optionsRaw);
            inputTypeString = optionsJSON.getString("inputType");
            inputFormatString = optionsJSON.getString("inputFormat");
        } catch(Exception e) {
            Log.e(TAG, "JSON error: " + e.toString());
        }

        if (url != null) {
            fileUri = Uri.parse(url);
            Log.d(TAG, "Using file " + fileUri.toString());
        } else {
            fileUri = null;
            Toast.makeText(VideoActivity.this, "Video file does not exist", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String subString = fileUri.toString().substring(0, 4);
                    Log.d(TAG, subString);

                    Options options = new Options();

                    switch(inputTypeString) {
                        case "TYPE_STEREO_OVER_UNDER":
                            options.inputFormat = Options.TYPE_STEREO_OVER_UNDER;
                        break;
                        default:
                            options.inputType = Options.TYPE_MONO;
                    }

                    if (subString.equalsIgnoreCase("http")) {

                        switch(inputFormatString) {
                            case "FORMAT_DASH":
                                options.inputFormat = Options.FORMAT_DASH;
                            break;
                            case "FORMAT_HLS":
                                options.inputFormat = Options.FORMAT_HLS;
                            break;
                            default:
                                options.inputFormat = Options.FORMAT_DEFAULT;
                        }

                        videoWidgetView.loadVideo(fileUri, options);
                    } else {
                        videoWidgetView.loadVideoFromAsset(fileUri.toString(), options);
                    }
                } catch (IOException e) {
                    // An error here is normally due to being unable to locate the file.
                    loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
                    // Since this is a background thread, we need to switch to the main thread to show a toast.
                    videoWidgetView.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VideoActivity.this, "Unable to open video file", Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                    Log.e(TAG, "Could not open video: " + e);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(STATE_PROGRESS_TIME, videoWidgetView.getCurrentPosition());
        savedInstanceState.putLong(STATE_VIDEO_DURATION, videoWidgetView.getDuration());
        savedInstanceState.putBoolean(STATE_IS_PAUSED, isPaused);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
        videoWidgetView.seekTo(progressTime);

        isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
        if (isPaused) {
            videoWidgetView.pauseVideo();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the 3D rendering.
        videoWidgetView.resumeRendering();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Prevent the view from rendering continuously when in the background.
        videoWidgetView.pauseRendering();
        // If the video is playing when onPause() is called, the default behavior will be to pause
        // the video and keep it paused when onResume() is called.
        isPaused = true;
    }

    @Override
    public void onDestroy() {
        videoWidgetView.shutdown();
        super.onDestroy();
    }

    private void togglePause() {
        if (isPaused) {
            videoWidgetView.playVideo();
        } else {
            videoWidgetView.pauseVideo();
        }
        isPaused = !isPaused;
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrVideoEventListener {
        /**
         * Called by video widget on the UI thread when it's done loading the video.
         */
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Sucessfully loaded video " + videoWidgetView.getDuration());
            loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
        }

        @Override
        public void onDisplayModeChanged(int newDisplayMode) {
            if (newDisplayMode != VrWidgetView.DisplayMode.FULLSCREEN_STEREO &&
                    newDisplayMode != VrWidgetView.DisplayMode.FULLSCREEN_MONO){
                videoWidgetView.setVisibility(View.INVISIBLE);
                that.finish();
            }
        }
        /**
         * Called by video widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            // An error here is normally due to being unable to decode the video format.
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            Toast.makeText(
                    VideoActivity.this, "Error loading video file", Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading video: " + errorMessage);
        }

        @Override
        public void onClick() {
            togglePause();
        }

        /**
         * Make the video play in a loop. This method could also be used to move to the next video in
         * a playlist.
         */
        @Override
        public void onCompletion() {
            videoWidgetView.seekTo(0);
        }
    }
}
