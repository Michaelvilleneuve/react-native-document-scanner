//
//  DocumentScannerView.h
//  DocumentScanner
//
//  Created by Marc PEYSALE on 22/06/2017.
//  Copyright © 2017 Snapp'. All rights reserved.
//

#import "IPDFCameraViewController.h"
#import <React/RCTViewManager.h>

@interface DocumentScannerView : IPDFCameraViewController <IPDFCameraViewControllerDelegate>

@property (nonatomic, copy) RCTBubblingEventBlock onPictureTaken;



@end
