var argscheck = require('cordova/argscheck'),
    cordova = require('cordova'),
    exec = require('cordova/exec');

var VR = function () {

};

VR.prototype.playVideo = function (url, inputType, inputFormat, successCallback, errorCallback) {
    exec(successCallback, errorCallback, "VRViewer", "playVideo", [url, inputType, inputFormat]);
};

VR.prototype.startPanorama = function (url, inputType, successCallback, errorCallback) {
    exec(successCallback, errorCallback, "VRViewer", "startPano", [url, inputType]);
};

VR.prototype.stopVideo = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "VRViewer", "stopVideo", []);
};

var vrViewer = new VR();

module.exports = vrViewer;
