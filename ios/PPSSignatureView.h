#import <UIKit/UIKit.h>
#import <GLKit/GLKit.h>

@protocol PPSSignatureViewDelegate <NSObject>

- (void) onPan;

@end

@interface PPSSignatureView : GLKView

@property (assign, nonatomic) UIColor *strokeColor;
@property (assign, nonatomic) BOOL hasSignature;
@property (strong, nonatomic) UIImage *signatureImage;
@property (strong, nonatomic) id<PPSSignatureViewDelegate> signViewDelegate;

- (void)erase;

- (UIImage *) signatureImage;
- (UIImage *) signatureImage: (BOOL) rotatedImage;
- (UIImage *) signatureImage: (BOOL) rotatedImage withSquare:(BOOL)square;

@end
