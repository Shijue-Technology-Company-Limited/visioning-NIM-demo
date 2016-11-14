//
//  NTESChatroomTextContentConfig.m
//  NIM
//
//  Created by chris on 16/1/13.
//  Copyright © 2016年 Netease. All rights reserved.
//

#import "NTESChatroomTextContentConfig.h"
#import "NIMAttributedLabel+NIMKit.h"
#import "NIMGlobalMacro.h"

@interface NTESChatroomTextContentConfig()

@property (nonatomic, strong) NIMAttributedLabel *label;

@end

@implementation NTESChatroomTextContentConfig

- (CGSize)contentSize:(CGFloat)cellWidth
{
    NSString *text = self.message.text;
    [self.label nim_setText:text];
    CGFloat msgBubbleMaxWidth    = (cellWidth - 130);
    CGFloat bubbleLeftToContent  = 15;
    CGFloat contentRightToBubble = 0;
    CGFloat msgContentMaxWidth = (msgBubbleMaxWidth - contentRightToBubble - bubbleLeftToContent);
    return [self.label sizeThatFits:CGSizeMake(msgContentMaxWidth, CGFLOAT_MAX)];
}

- (NSString *)cellContent
{
    return @"NTESChatroomTextContentView";
}

- (UIEdgeInsets)contentViewInsets
{
    return UIEdgeInsetsMake(20,15,10,0);
}

- (NIMAttributedLabel *)label
{
    if (!_label) {
        _label = [[NIMAttributedLabel alloc] initWithFrame:CGRectZero];
        _label.font = [UIFont systemFontOfSize:Chatroom_Message_Font_Size];
    }
    return _label;
}

@end
