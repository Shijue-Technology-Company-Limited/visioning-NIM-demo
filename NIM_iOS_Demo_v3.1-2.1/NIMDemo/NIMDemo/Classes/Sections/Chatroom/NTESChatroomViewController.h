//
//  NTESChatroomViewController.h
//  NIM
//
//  Created by chris on 15/12/11.
//  Copyright © 2015年 Netease. All rights reserved.
//

#import "NIMSessionViewController.h"

@interface NTESChatroomViewController : NIMSessionViewController

@property (nonatomic,weak) id<NIMInputDelegate> delegate;

- (instancetype)initWithChatroom:(NIMChatroom *)chatroom;

@end
