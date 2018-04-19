package com.cordova.plugin.vrviewer;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

public class VRViewer extends CordovaPlugin {

    private static final String TAG = VRViewer.class.getSimpleName();
    public CallbackContext callbackContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        try {
            if (action.equals("playVideo") || action.equals("startPano")) {
                Intent intent;
                if (action.equals("playVideo")) {
                    intent = new Intent(cordova.getActivity(), VideoActivity.class);
                } else {
                    intent = new Intent(cordova.getActivity(), PanoActivity.class);
                }
                intent.putExtra("url", args.getString(0));
                intent.putExtra("inputType", args.getString(1));
                if (action.equals("playVideo")) {
                    intent.putExtra("inputFormat", args.getString(2));
                }
                cordova.getActivity().startActivity(intent);
                this.callbackContext.success(action);
            } else if (action.equals("stopVideo")) {
                this.callbackContext.success("stopVideo");
            } else {
                this.callbackContext.error("Invalid action");
                return false;
            }
        } catch (Exception e) {
            this.callbackContext.error("Exception occurred: ".concat(e.getMessage()));
            return false;
        }
        return true;
    }

}
