var argscheck = require('cordova/argscheck'),
    cordova = require('cordova'),
    exec = require('cordova/exec');

var VR = function () {

};

VR.prototype.playVideo = function (url, options, successCallback, errorCallback) {
    exec(successCallback, errorCallback, "VRViewer", "playVideo", [url, options]);
};

VR.prototype.startPanorama = function (url, options, successCallback, errorCallback) {
    exec(successCallback, errorCallback, "VRViewer", "startPano", [url, options]);
};

VR.prototype.stopVideo = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "VRViewer", "stopVideo", []);
};

var vrViewer = new VR();

module.exports = vrViewer;
