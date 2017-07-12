
#import "RNPdfScannerManager.h"
#import "DocumentScannerView.h"

@implementation RNPdfScannerManager

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

RCT_EXPORT_VIEW_PROPERTY(onPictureTaken, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(overlayColor, UIColor)
RCT_EXPORT_VIEW_PROPERTY(enableTorch, BOOL)
RCT_EXPORT_VIEW_PROPERTY(saturation, float)
RCT_EXPORT_VIEW_PROPERTY(brightness, float)
RCT_EXPORT_VIEW_PROPERTY(contrast, float)

- (UIView*) view {
    return [[DocumentScannerView alloc] init];
}

@end
