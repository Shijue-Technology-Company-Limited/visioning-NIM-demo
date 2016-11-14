//
//  NTESSessionCustomContentConfig.m
//  NIM
//
//  Created by chris on 16/1/14.
//  Copyright © 2016年 Netease. All rights reserved.
//

#import "NTESSessionCustomContentConfig.h"
#import "NTESCustomAttachmentDefines.h"

@interface NTESSessionCustomContentConfig()

@property (nonatomic, strong) id<NTESCustomAttachmentInfo> attachmentInfo;

@end

@implementation NTESSessionCustomContentConfig

- (void)setMessage:(NIMMessage *)message
{
    NIMCustomObject *object = message.messageObject;
    _message = message;
    _attachmentInfo = (id<NTESCustomAttachmentInfo>)object.attachment;
}

- (CGSize)contentSize:(CGFloat)cellWidth
{
    return [self.attachmentInfo contentSize:self.message cellWidth:cellWidth];
}

- (NSString *)cellContent
{
    return [self.attachmentInfo cellContent:self.message];
}

- (UIEdgeInsets)contentViewInsets
{
    return [self.attachmentInfo contentViewInsets:self.message];
}

@end
