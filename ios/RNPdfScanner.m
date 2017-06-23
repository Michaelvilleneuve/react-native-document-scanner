
#import "RNPdfScanner.h"
#import "DocumentScannerView.h"

@implementation RNPdfScanner

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

- (UIView*) view {
    return [[DocumentScannerView alloc] init];
}

@end
  
