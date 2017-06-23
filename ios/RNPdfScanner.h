
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#import <React/RCTViewManager.h>
#endif

@interface RNPdfScanner : RCTViewManager <RCTBridgeModule>

@end
