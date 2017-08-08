//
//  DocumentScannerView.m
//  DocumentScanner
//
//  Created by Marc PEYSALE on 22/06/2017.
//  Copyright © 2017 Snapp'. All rights reserved.
//

#import "DocumentScannerView.h"
#import "IPDFCameraViewController.h"

@implementation DocumentScannerView

- (instancetype)init {
    self = [super init];
    if (self) {
        [self setupCameraView];
        [self setEnableBorderDetection:YES];


        [self setOverlayColor: self.overlayColor];
        [self setEnableTorch: self.enableTorch];

        [self setContrast: self.contrast];
        [self setBrightness: self.brightness];
        [self setSaturation: self.saturation];

        NSLog(@"detectionCountBeforeCapture:  %ld", (long)self.detectionCountBeforeCapture);
        NSLog(@"detectionRefreshRateInMS:  %ld", (long)self.detectionRefreshRateInMS);

        [self start];
        [self setDelegate: self];
    }

    return self;
}


- (void) didDetectRectangle:(CIRectangleFeature *)rectangle withType:(IPDFRectangeType)type {
    switch (type) {
        case IPDFRectangeTypeGood:
            self.stableCounter ++;
            break;
        default:
            self.stableCounter = 0;
            break;
    }
    if (self.onRectangleDetect) {
        self.onRectangleDetect(@{@"stableCounter": @(self.stableCounter), @"lastDetectionType": @(type)});
    }

    if (self.stableCounter > self.detectionCountBeforeCapture){
        [self captureImageWithCompletionHander:^(id data) {
           if (self.onPictureTaken) {
               NSData *imageData = UIImageJPEGRepresentation(data, self.quality);
               self.onPictureTaken(@{@"image": [imageData base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength]});
               [self stop];
           }
        }];
    }
}


@end
