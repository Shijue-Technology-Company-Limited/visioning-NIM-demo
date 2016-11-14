//
//  NTESSessionCustomConfig.h
//  NIM
//
//  Created by chris on 15/7/24.
//  Copyright (c) 2015年 Netease. All rights reserved.
//

#import "NIMCellConfig.h"

@interface NTESSessionCustomLayoutConfig : NSObject<NIMCellLayoutConfig>

+ (BOOL)supportMessage:(NIMMessage *)message;

@end
