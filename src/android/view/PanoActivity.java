package com.cordova.plugin.vrviewer;

import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.graphics.BitmapFactory;

import com.google.vr.sdk.widgets.common.VrWidgetView;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.pano.VrPanoramaView.Options;
import com.google.zxing.common.BitArray;

import java.lang.reflect.Array;
import java.net.URL;

import org.apache.cordova.CordovaActivity;

public class PanoActivity extends CordovaActivity {

    private static final String TAG = PanoActivity.class.getSimpleName();

    protected VrPanoramaView panoWidgetView;
    private CordovaActivity that = null;

    private Uri fileUri;

    private class DownloadFilesTask extends AsyncTask<String, Integer, Bitmap> {

        protected Bitmap doInBackground(String... url) {
            Bitmap img;
            try {
                img = BitmapFactory.decodeStream(new URL(url[0]).openStream());
            } catch (IOException e) {
                Log.e(TAG, "Could not download image");
                img = null;
            }

            return img;
        }

        protected void onPostExecute(Bitmap result) {
            Log.i(TAG, "Loaded Successfully");
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getApplication().getResources().getIdentifier("vr_viewer_pano", "layout",
                getApplication().getPackageName()));

        panoWidgetView = (VrPanoramaView) findViewById(
                getApplication().getResources().getIdentifier("vr_view", "id", getApplication().getPackageName()));
        panoWidgetView.setDisplayMode(VrPanoramaView.DisplayMode.FULLSCREEN_MONO);
        panoWidgetView.setVisibility(View.INVISIBLE);
        panoWidgetView.setInfoButtonEnabled(false);
        panoWidgetView.setTransitionViewEnabled(false);
        panoWidgetView.setEventListener(new ActivityEventListener());

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

    private void handleIntent(Intent intent) {
        String url = intent.getStringExtra("url");

        final String inputTypeString = intent.getStringExtra("inputType");

        if (url != null) {
            fileUri = Uri.parse(url);
            Log.d(TAG, "Using file " + fileUri.toString());
        } else {
            fileUri = null;
            Toast.makeText(PanoActivity.this, "Image file does not exist", Toast.LENGTH_LONG).show();
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String subString = fileUri.toString().substring(0, 4);
                    Log.d(TAG, subString);

                    Options options = new Options();
                    if (inputTypeString.equals("TYPE_STEREO_OVER_UNDER")) {
                        options.inputType = Options.TYPE_STEREO_OVER_UNDER;
                    } else {
                        options.inputType = Options.TYPE_MONO;
                    }
                    if (subString.equalsIgnoreCase("http")) {
                        panoWidgetView.loadImageFromBitmap(new DownloadFilesTask().execute(fileUri.toString()).get(), options);
                    } else {
                        panoWidgetView.loadImageFromBitmap(BitmapFactory.decodeFile(fileUri.getPath()), options);
                    }
                } catch (Exception e) {
                    // Since this is a background thread, we need to switch to the main thread to show a toast.
                    panoWidgetView.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PanoActivity.this, "Unable to open panorama image", Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                    Log.e(TAG, "Could not open pano: " + e);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the 3D rendering.
        panoWidgetView.resumeRendering();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Prevent the view from rendering continuously when in the background.
        panoWidgetView.pauseRendering();
    }

    @Override
    public void onDestroy() {
        panoWidgetView.shutdown();
        super.onDestroy();
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrPanoramaEventListener {
        /**
         * Called by pano widget on the UI thread when it's done loading the pano.
         */
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Sucessfully loaded panorama image");
        }

        @Override
        public void onDisplayModeChanged(int newDisplayMode) {
            if (newDisplayMode != VrWidgetView.DisplayMode.FULLSCREEN_STEREO
                    && newDisplayMode != VrWidgetView.DisplayMode.FULLSCREEN_MONO) {
                panoWidgetView.setVisibility(View.INVISIBLE);
                that.finish();
            }
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            // An error here is normally due to being unable to decode the pano format.
            Toast.makeText(PanoActivity.this, "Error loading panorama image", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error loading panorama image: " + errorMessage);
        }

    }
}
