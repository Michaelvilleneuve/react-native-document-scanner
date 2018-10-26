#import "DocumentScannerView.h"
#import "IPDFCameraViewController.h"
#import "TOCropViewController.h"

@implementation DocumentScannerView

#pragma mark - SCANNER -

- (instancetype)init {
    self = [super init];
    if (self) {
        [self setEnableBorderDetection:YES];
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
    
    if (self.stableCounter > self.detectionCountBeforeCapture) {
        [self capture];
    }
}

- (void) capture {
    [self captureImageWithCompletionHander:^(UIImage *croppedImage, UIImage *initialImage, CIRectangleFeature *rectangleFeature) {
        if (self.onPictureTaken) {
            if (self.onCrop) {
                [self presentCropViewController:initialImage withRect:CGRectMake(rectangleFeature.bottomLeft.y, rectangleFeature.bottomLeft.x, rectangleFeature.bounds.size.height, rectangleFeature.bounds.size.width)];
            }
            
            NSData *croppedImageData = UIImageJPEGRepresentation(croppedImage, self.quality);
            
            if (initialImage.imageOrientation != UIImageOrientationUp) {
                UIGraphicsBeginImageContextWithOptions(initialImage.size, false, initialImage.scale);
                [initialImage drawInRect:CGRectMake(0, 0, initialImage.size.width
                                                    , initialImage.size.height)];
                initialImage = UIGraphicsGetImageFromCurrentImageContext();
                UIGraphicsEndImageContext();
            }
            NSData *initialImageData = UIImageJPEGRepresentation(initialImage, self.quality);
            
            /*
             RectangleCoordinates expects a rectanle viewed from portrait,
             while rectangleFeature returns a rectangle viewed from landscape, which explains the nonsense of the mapping below.
             Sorry about that.
             */
            NSDictionary *rectangleCoordinates = rectangleFeature ? @{
                                                                      @"topLeft": @{ @"y": @(rectangleFeature.bottomLeft.x + 30), @"x": @(rectangleFeature.bottomLeft.y)},
                                                                      @"topRight": @{ @"y": @(rectangleFeature.topLeft.x + 30), @"x": @(rectangleFeature.topLeft.y)},
                                                                      @"bottomLeft": @{ @"y": @(rectangleFeature.bottomRight.x), @"x": @(rectangleFeature.bottomRight.y)},
                                                                      @"bottomRight": @{ @"y": @(rectangleFeature.topRight.x), @"x": @(rectangleFeature.topRight.y)},
                                                                      } : [NSNull null];
            if (self.useBase64) {
                self.onPictureTaken(@{
                                      @"croppedImage": [croppedImageData base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength],
                                      @"initialImage": [initialImageData base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength],
                                      @"rectangleCoordinates": rectangleCoordinates });
            }
            else {
                NSString *dir = [self getSaveDirectory];
                NSString *croppedFilePath = [dir stringByAppendingPathComponent:[NSString stringWithFormat:@"cropped_img_%i.jpeg",(int)[NSDate date].timeIntervalSince1970]];
                NSString *initialFilePath = [dir stringByAppendingPathComponent:[NSString stringWithFormat:@"initial_img_%i.jpeg",(int)[NSDate date].timeIntervalSince1970]];
                
                [croppedImageData writeToFile:croppedFilePath atomically:YES];
                [initialImageData writeToFile:initialFilePath atomically:YES];
                
                self.onPictureTaken(@{
                                      @"croppedImage": croppedFilePath,
                                      @"initialImage": initialFilePath,
                                      @"rectangleCoordinates": rectangleCoordinates });
            }
        }
        
        if (!self.captureMultiple) {
            [self stop];
        }
    }];
    
}

#pragma mark - CROPPER -

- (void)presentCropViewController: (UIImage *)img withRect:(CGRect)rect
{
    UIImage *image = img;
    TOCropViewController *cropViewController = [[TOCropViewController alloc] initWithImage:image];
    [cropViewController setImageCropFrame:rect];
    NSString *doneButtonTitle = [self.cropperOpts valueForKey:@"doneButtonTitle"];
    if ([doneButtonTitle length] > 0) {
        [cropViewController setDoneButtonTitle:doneButtonTitle];
    }
    NSString *cancelButtonTitle = [self.cropperOpts valueForKey:@"cancelButtonTitle"];
    if ([cancelButtonTitle length] > 0) {
        [cropViewController setCancelButtonTitle:cancelButtonTitle];
    }
    cropViewController.delegate = self;
    UIViewController *viewController = [self getMainViewController];
    [viewController presentViewController:cropViewController animated:YES completion:^{}];
}

- (void)cropViewController:(TOCropViewController *)cropViewController didCropToImage:(UIImage *)image withRect:(CGRect)cropRect angle:(NSInteger)angle {
    NSMutableDictionary* response = [NSMutableDictionary dictionary];
    NSData *croppedImageData = UIImageJPEGRepresentation(image, self.quality);
    
    // If should save file
    BOOL saveFile = [[self.cropperOpts valueForKey:@"saveFile"]boolValue];
    if (saveFile) {
        NSString *dir = [self getSaveDirectory];
        NSString *directory = [self.cropperOpts valueForKey:@"directory"];
        if ([directory length] > 0) {
            dir = [dir stringByAppendingPathComponent:directory];
            [[NSFileManager defaultManager] createDirectoryAtPath:dir withIntermediateDirectories:YES attributes:nil error:nil];
        }
        NSString *fileName = [self.cropperOpts valueForKey:@"fileName"];
        NSString *croppedFilePath = @"";
        if ([fileName length] > 0) {
            croppedFilePath = [dir stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.jpeg", fileName]];
        } else {
            croppedFilePath = [dir stringByAppendingPathComponent:[NSString stringWithFormat:@"to_cropped_img_%i.jpeg",(int)[NSDate date].timeIntervalSince1970]];
        }
        [croppedImageData writeToFile:croppedFilePath atomically:YES];
        
        [response setValue:croppedFilePath forKey:@"croppedFilePath"];
    }
    
    // If should return base64
    BOOL base64 = [[self.cropperOpts valueForKey:@"base64"]boolValue];
    if (base64) {
        [response setValue:[croppedImageData base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength] forKey:@"base64"];
    }
    UIViewController *viewController = [self getMainViewController];
    [viewController dismissViewControllerAnimated:true completion:^{}];
    self.onCrop(response);
}

#pragma mark - UTILS -

- (UIViewController *)getMainViewController {
    UIViewController *viewController = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
    if ( viewController.presentedViewController && !viewController.presentedViewController.isBeingDismissed ) {
        viewController = viewController.presentedViewController;
    }
    return viewController;
}

- (NSString *)getSaveDirectory {
    NSString *dir = NSTemporaryDirectory();
    if (self.saveInAppDocument) {
        dir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject];
    }
    return dir;
}

@end
