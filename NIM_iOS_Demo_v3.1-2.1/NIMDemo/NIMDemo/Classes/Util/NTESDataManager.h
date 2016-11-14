//
//  NTESDataManager.h
//  NIM
//
//  Created by amao on 8/13/15.
//  Copyright (c) 2015 Netease. All rights reserved.
//

#import "NTESService.h"

@interface NTESDataManager : NSObject<NIMKitDataProvider>

+ (instancetype)sharedInstance;

@property (nonatomic,strong) UIImage *defaultUserAvatar;

@property (nonatomic,strong) UIImage *defaultTeamAvatar;


@end
