//
//  NTESSessionMsgHelper.h
//  NIMDemo
//
//  Created by ght on 15-1-28.
//  Copyright (c) 2015年 Netease. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "NIMSDK.h"

@class NTESLocationPoint;
@class NTESJanKenPonAttachment;
@class NTESSnapchatAttachment;
@class NTESChartletAttachment;
@class NTESWhiteboardAttachment;

@interface NTESSessionMsgConverter : NSObject

+ (NIMMessage *)msgWithText:(NSString*)text;

+ (NIMMessage *)msgWithImage:(UIImage*)image;

+ (NIMMessage *)msgWithAudio:(NSString*)filePath;

+ (NIMMessage *)msgWithVideo:(NSString*)filePath;

+ (NIMMessage *)msgWithLocation:(NTESLocationPoint*)locationPoint;

+ (NIMMessage *)msgWithJenKenPon:(NTESJanKenPonAttachment *)attachment;

+ (NIMMessage *)msgWithSnapchatAttachment:(NTESSnapchatAttachment *)attachment;

+ (NIMMessage *)msgWithChartletAttachment:(NTESChartletAttachment *)attachment;

+ (NIMMessage *)msgWithWhiteboardAttachment:(NTESWhiteboardAttachment *)attachment;

+ (NIMMessage *)msgWithFilePath:(NSString*)path;

+ (NIMMessage *)msgWithFileData:(NSData*)data extension:(NSString*)extension;

+ (NIMMessage *)msgWithTip:(NSString *)tip;

@end