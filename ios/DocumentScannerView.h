#import "IPDFCameraViewController.h"
#import "TOCropViewController.h"
#import <React/RCTViewManager.h>

@interface DocumentScannerView : IPDFCameraViewController <IPDFCameraViewControllerDelegate, TOCropViewControllerDelegate>

@property (nonatomic, copy) RCTBubblingEventBlock onPictureTaken;
@property (nonatomic, copy) RCTBubblingEventBlock onRectangleDetect;
@property (nonatomic, copy) RCTBubblingEventBlock onCrop;
@property (nonatomic, copy) NSDictionary * cropperOpts;
@property (nonatomic, assign) NSInteger detectionCountBeforeCapture;
@property (assign, nonatomic) NSInteger stableCounter;
@property (nonatomic, assign) float quality;
@property (nonatomic, assign) BOOL useBase64;
@property (nonatomic, assign) BOOL captureMultiple;
@property (nonatomic, assign) BOOL saveInAppDocument;

- (void) capture;

@end
