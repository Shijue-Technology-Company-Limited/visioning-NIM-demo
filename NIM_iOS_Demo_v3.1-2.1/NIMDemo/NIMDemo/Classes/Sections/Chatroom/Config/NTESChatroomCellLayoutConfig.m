//
//  NTESChatroomCellLayoutConfig.m
//  NIM
//
//  Created by chris on 16/1/13.
//  Copyright © 2016年 Netease. All rights reserved.
//

#import "NTESChatroomCellLayoutConfig.h"
#import "NIMBaseSessionContentConfig.h"
#import "NTESChatroomTextContentConfig.h"
#import "NTESCustomAttachmentDefines.h"
#import "NTESSessionCustomContentConfig.h"

@interface NTESChatroomCellLayoutConfig()

@property (nonatomic,strong) NTESChatroomTextContentConfig  *textConfig;

@property (nonatomic,strong) NTESSessionCustomContentConfig *customConfig;

@end

@implementation NTESChatroomCellLayoutConfig

- (instancetype)init{
    self = [super init];
    if (self) {
        _textConfig   = [NTESChatroomTextContentConfig new];
        _customConfig = [NTESSessionCustomContentConfig new];
    }
    return self;
}

- (BOOL)shouldShowAvatar:(NIMMessageModel *)model{
    return NO;
}


- (CGSize)contentSize:(NIMMessageModel *)model cellWidth:(CGFloat)cellWidth
{
    id<NIMSessionContentConfig> config = [self chatroomContentConfig:model];
    if (config) {
        return [config contentSize:cellWidth];
    }
    return [super contentSize:model cellWidth:cellWidth];
}

- (NSString *)cellContent:(NIMMessageModel *)model
{
    id<NIMSessionContentConfig> config = [self chatroomContentConfig:model];
    if (config) {
        return [config cellContent];
    }
    return [super cellContent:model];
}

- (UIEdgeInsets)cellInsets:(NIMMessageModel *)model
{
    id<NIMSessionContentConfig> config = [self chatroomContentConfig:model];
    if (config) {
        return UIEdgeInsetsZero;
    }
    return [super cellInsets:model];
}

- (UIEdgeInsets)contentViewInsets:(NIMMessageModel *)model
{
    id<NIMSessionContentConfig> config = [self chatroomContentConfig:model];
    if (config) {
        return [config contentViewInsets];
    }
    return [super contentViewInsets:model];
}

- (BOOL)shouldShowLeft:(NIMMessageModel *)model{
    if ([self chatroomContentConfig:model]) {
        return YES;
    }
    return [super shouldShowLeft:model];
}

- (BOOL)shouldShowNickName:(NIMMessageModel *)model{
    if ([self chatroomContentConfig:model]) {
        return YES;
    }
    return [super shouldShowNickName:model];
}


- (CGFloat)nickNameMargin:(NIMMessageModel *)model
{
    if ([self chatroomContentConfig:model]) {
        NSDictionary *ext = model.message.remoteExt;
        NIMChatroomMemberType type = [ext[@"type"] integerValue];
        switch (type) {
            case NIMChatroomMemberTypeManager:
            case NIMChatroomMemberTypeCreator:
                return 50.f;
            default:
                break;
        }
        return 15.f;
    }
    return [super shouldShowNickName:model];
}


- (NSArray *)customViews:(NIMMessageModel *)model
{
   if ([self chatroomContentConfig:model]) {
        NSDictionary *ext = model.message.remoteExt;
        NIMChatroomMemberType type = [ext[@"type"] integerValue];
        NSString *imageName;
        switch (type) {
            case NIMChatroomMemberTypeManager:
                imageName = @"chatroom_role_manager";
                break;
            case NIMChatroomMemberTypeCreator:
                imageName = @"chatroom_role_master";
                break;
            default:
                break;
        }
        UIImageView *imageView;
        if (imageName.length) {
            UIImage *image = [UIImage imageNamed:imageName];
            imageView = [[UIImageView alloc] initWithImage:image];
            CGFloat leftMargin = 15.f;
            CGFloat topMatgin  = 0.f;
            CGRect frame = imageView.frame;
            frame.origin = CGPointMake(leftMargin, topMatgin);
            imageView.frame = frame;
        }
        return imageView ? @[imageView] : nil;
    }
    return [super customViews:model];
}

- (id<NIMSessionContentConfig>)chatroomContentConfig:(NIMMessageModel *)model
{
    switch (model.message.messageType) {
        case NIMMessageTypeText:
            self.textConfig.message = model.message;
            return self.textConfig;
            
        case NIMMessageTypeCustom:{
            NIMCustomObject *object = model.message.messageObject;
            if (object.attachment) {
                self.customConfig.message = model.message;
                return self.customConfig;
            }
            break;
        }
        default:
            break;
    }
    
    return nil;
}




@end
