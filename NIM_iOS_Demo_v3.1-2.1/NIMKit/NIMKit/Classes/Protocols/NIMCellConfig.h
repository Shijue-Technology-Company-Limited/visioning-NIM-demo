//
//  NIMCellConfig.h
//  NIMKit
//
//  Created by chris.
//  Copyright (c) 2015年 NetEase. All rights reserved.
//

#import <UIKit/UIKit.h>

@class NIMSessionMessageContentView;
@class NIMMessageModel;

@protocol NIMCellLayoutConfig <NSObject>

@optional

/**
 * @return 返回message的内容大小
 */
- (CGSize)contentSize:(NIMMessageModel *)model cellWidth:(CGFloat)width;


/**
 *  需要构造的cellContent类名
 */
- (NSString *)cellContent:(NIMMessageModel *)model;

/**
 *  左对齐的气泡，cell气泡距离整个cell的内间距
 */
- (UIEdgeInsets)cellInsets:(NIMMessageModel *)model;

/**
 *  左对齐的气泡，cell内容距离气泡的内间距，
 */
- (UIEdgeInsets)contentViewInsets:(NIMMessageModel *)model;

/**
 *  是否显示头像
 */
- (BOOL)shouldShowAvatar:(NIMMessageModel *)model;


/**
 *  左对齐的气泡，头像到左边的距离
 */
- (CGFloat)avatarMargin:(NIMMessageModel *)model;

/**
 *  是否显示姓名
 */
- (BOOL)shouldShowNickName:(NIMMessageModel *)model;

/**
 *  左对齐的气泡，昵称到左边的距离
 */
- (CGFloat)nickNameMargin:(NIMMessageModel *)model;


/**
 *  消息显示在左边
 */
- (BOOL)shouldShowLeft:(NIMMessageModel *)model;


/**
 *  需要添加到Cell上的自定义视图
 */
- (NSArray *)customViews:(NIMMessageModel *)model;


@end