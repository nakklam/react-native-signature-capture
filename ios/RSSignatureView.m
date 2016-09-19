#import "RSSignatureView.h"
#import "RCTConvert.h"
#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>
#import "PPSSignatureView.h"
#import "RSSignatureViewManager.h"

#define DEGREES_TO_RADIANS(x) (M_PI * (x) / 180.0)
#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

@interface RSSignatureView () <PPSSignatureViewDelegate>

@end

@implementation RSSignatureView {
	CAShapeLayer *_border;
	BOOL _loaded;
	EAGLContext *_context;
	UIButton *saveButton;
	UIButton *clearButton;
	UILabel *titleLabel;
	BOOL _rotateClockwise;
	BOOL _square;
}

@synthesize sign;
@synthesize manager;

- (instancetype)init
{
	if ((self = [super init])) {
		_border = [CAShapeLayer layer];
		_border.strokeColor = [UIColor blackColor].CGColor;
		_border.fillColor = nil;
		_border.lineDashPattern = @[@4, @2];
		
		//[self.layer addSublayer:_border];
	}
	
	return self;
}

- (void) didRotate:(NSNotification *)notification {
	int ori=1;
	UIDeviceOrientation currOri = [[UIDevice currentDevice] orientation];
	if ((currOri == UIDeviceOrientationLandscapeLeft) || (currOri == UIDeviceOrientationLandscapeRight)) {
		ori=0;
	}
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    if (!_loaded) {
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didRotate:)
                                                     name:UIDeviceOrientationDidChangeNotification object:nil];
        
        _context = [[EAGLContext alloc] initWithAPI:kEAGLRenderingAPIOpenGLES2];
        
        CGSize screen = self.bounds.size;
        
        sign = [[PPSSignatureView alloc]
                initWithFrame: CGRectMake(0, 0, screen.width, screen.height)
                context: _context];
        
        [self addSubview:sign];
        [sign setSignViewDelegate:self];
        
        CGFloat buttonViewHeight = 70;
        UIView *buttonView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.bounds.size.width, buttonViewHeight)];
        [buttonView setCenter:CGPointMake(self.bounds.size.width/2, self.bounds.size.height-(buttonViewHeight/2))];
        [buttonView setBackgroundColor:UIColorFromRGB(0xf5f5f6)];
        [sign addSubview:buttonView];
        
        
        titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, self.bounds.size.width-80, self.bounds.size.height - 80)];
        [titleLabel setCenter:CGPointMake(self.bounds.size.width/2, self.bounds.size.height-90)];
        [titleLabel setText:@"เขียนชื่อที่นี่"];
        [titleLabel setLineBreakMode:NSLineBreakByClipping];
        [titleLabel setTextAlignment: NSTextAlignmentCenter];
        [titleLabel setTextColor:[UIColor colorWithRed:200/255.f green:200/255.f blue:200/255.f alpha:1.f]];
        [sign addSubview:titleLabel];
        
        
        
        //Save button
        saveButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
        [saveButton setLineBreakMode:NSLineBreakByClipping];
        [saveButton addTarget:self action:@selector(onSaveButtonPressed)
             forControlEvents:UIControlEventTouchUpInside];
        [saveButton setTitle:@"บันทึก" forState:UIControlStateNormal];
        
        CGSize buttonSize = CGSizeMake(100, 55); //Width/Height is swapped
        CGFloat margin = 15;
        saveButton.frame = CGRectMake(buttonView.bounds.size.width - buttonSize.width, buttonView.bounds.size.height-buttonSize.height, buttonSize.width-margin, buttonSize.height-margin);
        [saveButton setBackgroundColor:UIColorFromRGB(0xfec221)];
        [saveButton setTitleColor:UIColorFromRGB(0x58585b) forState:UIControlStateNormal];
        [buttonView addSubview:saveButton];
        
        //Clear button
        clearButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
        [clearButton setLineBreakMode:NSLineBreakByClipping];
        [clearButton addTarget:self action:@selector(onClearButtonPressed)
              forControlEvents:UIControlEventTouchUpInside];
        [clearButton setTitle:@"ลบ" forState:UIControlStateNormal];
        
        clearButton.frame = CGRectMake(margin, buttonView.bounds.size.height-buttonSize.height, buttonSize.width, buttonSize.height-margin);
        [clearButton setBackgroundColor:UIColorFromRGB(0xfec221)];
        [clearButton setTitleColor:UIColorFromRGB(0x58585b) forState:UIControlStateNormal];
        [buttonView addSubview:clearButton];
    }
    
    _loaded = true;
    _border.path = [UIBezierPath bezierPathWithRect:self.bounds].CGPath;
    _border.frame = self.bounds;
}

- (void)setRotateClockwise:(BOOL)rotateClockwise {
	_rotateClockwise = rotateClockwise;
}

- (void)setSquare:(BOOL)square {
	_square = square;
}

-(void) onSaveButtonPressed {
	saveButton.hidden = YES;
	clearButton.hidden = YES;
	UIImage *signImage = [self.sign signatureImage: _rotateClockwise withSquare:_square];
	
	saveButton.hidden = NO;
	clearButton.hidden = NO;
	
	NSError *error;
	
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = [paths firstObject];
	NSString *tempPath = [documentsDirectory stringByAppendingFormat:@"/signature.png"];
	
	//remove if file already exists
	if ([[NSFileManager defaultManager] fileExistsAtPath:tempPath]) {
		[[NSFileManager defaultManager] removeItemAtPath:tempPath error:&error];
		if (error) {
			NSLog(@"Error: %@", error.debugDescription);
		}
	}
	
	// Convert UIImage object into NSData (a wrapper for a stream of bytes) formatted according to PNG spec
	NSData *imageData = UIImagePNGRepresentation(signImage);
	BOOL isSuccess = [imageData writeToFile:tempPath atomically:YES];
	if (isSuccess) {
		NSFileManager *man = [NSFileManager defaultManager];
		NSDictionary *attrs = [man attributesOfItemAtPath:tempPath error: NULL];
		//UInt32 result = [attrs fileSize];
		
		NSString *base64Encoded = [imageData base64EncodedStringWithOptions:0];
		[self.manager saveImage: tempPath withEncoded:base64Encoded];
	}
}

-(void) onPan{
    if(![titleLabel isHidden]){
        [titleLabel setHidden:YES];
    }
}

-(void) onClearButtonPressed {
    if([titleLabel isHidden]){
        [titleLabel setHidden:NO];
    }
    
	[self.sign erase];
}

@end
