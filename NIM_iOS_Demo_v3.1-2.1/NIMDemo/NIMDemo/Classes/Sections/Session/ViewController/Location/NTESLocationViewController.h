//
//  NTESLocationViewController.h
//  NIM
//
//  Created by chris on 15/2/28.
//  Copyright (c) 2015年 Netease. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
@class NTESLocationPoint;
@protocol NTESLocationViewControllerDelegate <NSObject>
- (void)onSendLocation:(NTESLocationPoint*)locationPoint;
@end

@interface NTESLocationViewController : UIViewController<MKMapViewDelegate>

@property(nonatomic,strong) IBOutlet MKMapView *mapView;

@property(nonatomic,weak)  id<NTESLocationViewControllerDelegate> delegate;

- (instancetype)initWithLocationPoint:(NTESLocationPoint*)locationPoint;

@end
