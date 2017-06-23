//
//  DocumentScannerView.m
//  DocumentScanner
//
//  Created by Marc PEYSALE on 22/06/2017.
//  Copyright © 2017 Snapp'. All rights reserved.
//

#import "DocumentScannerView.h"

@interface DocumentScannerView() <IPDFCameraViewControllerDelegate>

@end

@implementation DocumentScannerView

RCT_EXPORT_VIEW_PROPERTY(onPictureTaken, RCTBubblingEventBlock)

- (void) awakeFromNib {
    [super awakeFromNib];
    self.delegate = self;
}


@end
