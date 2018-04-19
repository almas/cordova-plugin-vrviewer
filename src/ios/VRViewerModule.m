#import "VRViewerModule.h"

@implementation VRViewerModule

@synthesize videoView;
@synthesize panoView;
@synthesize isPaused;
@synthesize isVideo;
@synthesize currentCallbackId;

- (void)pluginInitialize {
}

- (void) stopVideo: (CDVInvokedUrlCommand*)command {
    [self stopVideo];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

- (void) playVideo: (CDVInvokedUrlCommand*)command {
    isVideo = YES;
    NSString* playUrl = [command.arguments objectAtIndex: 0];
    NSString* inputTypeStr = [command.arguments objectAtIndex: 1];
    NSString* inputFormatStr = [command.arguments objectAtIndex: 2];

    if (playUrl == nil) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"arg was null"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    self.currentCallbackId = command.callbackId;

    NSLog(@"VRViewerModule playVideo = %@", playUrl);

    self.videoView  = [[GVRVideoView alloc] initWithFrame:CGRectMake(0, 0, 0, 0)];
    self.videoView.delegate = self;
    self.videoView.enableFullscreenButton = YES;
    self.videoView.enableCardboardButton = YES;
    self.videoView.enableInfoButton = NO;
    self.videoView.hidesTransitionView = YES;
    self.videoView.enableTouchTracking = YES;
    self.videoView.displayMode = kGVRWidgetDisplayModeFullscreen;
    [self.viewController.view addSubview:self.videoView];

    self.isPaused = NO;

    GVRVideoType inputType = kGVRVideoTypeStereoOverUnder;
    if(inputTypeStr == "TYPE_MONO") {
        inputType = kGVRVideoTypeMono;
    }

    if ([playUrl hasPrefix:@"http"]) {
        [self.videoView loadFromUrl:[[NSURL alloc] initWithString:playUrl] ofType:inputType];
    } else {
        [self.videoView loadFromUrl:[[NSURL alloc] initFileURLWithPath:playUrl] ofType:inputType];
    }
}


- (void) startPano: (CDVInvokedUrlCommand*)command {
    NSString* imageUrl = [command.arguments objectAtIndex: 0];
    NSString* inputTypeStr = [command.arguments objectAtIndex: 1];

    if (imageUrl == nil) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"arg was null"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    self.currentCallbackId = command.callbackId;

    NSLog(@"VRViewerModule startPano = %@", imageUrl);

    self.panoView  = [[GVRPanoramaView alloc] initWithFrame:CGRectMake(0, 0, 0, 0)];
    self.panoView.delegate = self;
    self.panoView.enableFullscreenButton = YES;
    self.panoView.enableCardboardButton = YES;
    self.panoView.enableInfoButton = NO;
    self.panoView.hidesTransitionView = YES;
    self.panoView.enableTouchTracking = YES;
    self.panoView.displayMode = kGVRWidgetDisplayModeFullscreen;
    [self.viewController.view addSubview:self.panoView];

    GVRPanoramaImageType inputType = kGVRPanoramaImageTypeStereoOverUnder;
    if(inputTypeStr == "TYPE_MONO") {
        inputType = kGVRPanoramaImageTypeMono;
    }

    UIImage *image = [UIImage imageWithContentsOfFile:imageUrl];
    if ([imageUrl hasPrefix:@"http"]) {
        NSURL *url = [NSURL URLWithString:imageUrl];
        NSData *data = [NSData dataWithContentsOfURL:url];
        image = [[[UIImage alloc] initWithData:data] autorelease];
    }

    [self.panoView loadImage:image ofType:inputType];
}


#pragma mark - GVRVideoViewDelegate

- (void)widgetViewDidTap:(GVRWidgetView *)widgetView {
    if(!isVideo) {
        return;
    }
    if (self.isPaused) {
        [self.videoView play];
    } else {
        [self.videoView pause];
    }
    self.isPaused = !self.isPaused;
}

- (void)widgetView:(GVRWidgetView *)widgetView didLoadContent:(id)content {
    if(isVideo) {
        [self.videoView play];
    }
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.currentCallbackId];
}


- (void)widgetView:(GVRWidgetView *)widgetView
didChangeDisplayMode:(GVRWidgetDisplayMode)displayMode{
    if (displayMode != kGVRWidgetDisplayModeFullscreen && displayMode != kGVRWidgetDisplayModeFullscreenVR){
        if(isVideo) {
            // Full screen closed, closing the view
            [self stopVideo];
        }
    }
}

- (void)widgetView:(GVRWidgetView *)widgetView
didFailToLoadContent:(id)content
  withErrorMessage:(NSString *)errorMessage {
    NSLog(@"Failed to load video: %@", errorMessage);
    if(isVideo) {
        [self stopVideo];
    }
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Failed to load video"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.currentCallbackId];
}

- (void)videoView:(GVRVideoView*)videoView didUpdatePosition:(NSTimeInterval)position {
    if(!isVideo) {
        return;
    }
    // Loop the video when it reaches the end.
    if (position == videoView.duration) {
        NSLog(@"videoView didUpdatePosition: %f", position);
//        [self stopVideo];
         [self.videoView seekTo:0];
//         [self.videoView play];
        [self.videoView pause];
        self.isPaused = true;
    }
}

- (void)stopVideo {
    if (self.videoView != nil) {
        [self.videoView stop];
    }
}

@end
