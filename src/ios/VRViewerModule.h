#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import "GVRVideoView.h"
#import "GVRPanoramaView.h"

@interface VRViewerModule : CDVPlugin <GVRWidgetViewDelegate>

@property (nonatomic, strong) GVRVideoView *videoView;
@property (nonatomic, strong) GVRPanoramaView *panoView;
@property (nonatomic) BOOL isPaused;
@property (nonatomic) BOOL isVideo;
@property (nonatomic, strong) NSString* currentCallbackId;

- (void)playVideo: (CDVInvokedUrlCommand*)command;
- (void)stopVideo: (CDVInvokedUrlCommand*)command;
- (void)startPano: (CDVInvokedUrlCommand*)command;

@end
