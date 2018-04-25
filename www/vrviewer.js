var argscheck = require('cordova/argscheck'),
    cordova = require('cordova'),
    exec = require('cordova/exec');

var VR = function() {},
    emptyFn = function() {};

function checkOptionsAndExec(fnName, url, options, successCallback, errorCallback) {
    if (typeof options === 'function') {
        errorCallback = successCallback;
        successCallback = options;
        options = {};
    }
    options = options || {};
    successCallback = successCallback || emptyFn;
    errorCallback = errorCallback || emptyFn;

    if(options.inputType === undefined) {
        options.inputType = 'TYPE_MONO';
    }

    if(options.inputFormat === undefined) {
        options.inputFormat = 'FORMAT_DEFAULT';
    }

    if(options.fromAsset === undefined) {
        options.fromAsset = false;
    }

    var args = [url, options.inputType, options.inputFormat, options.fromAsset];

    exec(successCallback, errorCallback, "VRViewer", fnName, args);
}

VR.prototype.playVideo = function(url, options, successCallback, errorCallback) {
    checkOptionsAndExec("playVideo", url, options, successCallback, errorCallback);
};

VR.prototype.startPanorama = function(url, options, successCallback, errorCallback) {
    checkOptionsAndExec("startPano", url, options, successCallback, errorCallback);
};

VR.prototype.stopVideo = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback, "VRViewer", "stopVideo", []);
};

var vrViewer = new VR();

module.exports = vrViewer;
