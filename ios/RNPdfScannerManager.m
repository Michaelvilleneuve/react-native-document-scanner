
#import "RNPdfScannerManager.h"
#import "DocumentScannerView.h"

@implementation RNPdfScannerManager

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

- (UIView*) view {
    return [[DocumentScannerView alloc] init];
}

@end
