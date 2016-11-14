//
//  NTESChatroomViewController.m
//  NIM
//
//  Created by chris on 15/12/11.
//  Copyright © 2015年 Netease. All rights reserved.
//

#import "NTESChatroomViewController.h"
#import "NTESChatroomConfig.h"
#import "NTESJanKenPonAttachment.h"
#import "NTESSessionMsgConverter.h"
#import "NTESChatroomManager.h"

@interface NTESChatroomViewController ()<NIMInputDelegate>
{
    BOOL _isRefreshing;
}

@property (nonatomic,strong) NTESChatroomConfig *config;

@property (nonatomic,strong) NIMChatroom *chatroom;

@end

@implementation NTESChatroomViewController

- (instancetype)initWithChatroom:(NIMChatroom *)chatroom
{
    self = [super initWithSession:[NIMSession session:chatroom.roomId type:NIMSessionTypeChatroom]];
    if (self) {
        _chatroom = chatroom;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.layoutManager.delegate = self;
}

- (id<NIMSessionConfig>)sessionConfig{
    return self.config;
}


- (void)onTapMediaItem:(NIMMediaItem *)item
{
    switch (item.tag) {
        case NTESMediaButtonJanKenPon:
        {
            NTESJanKenPonAttachment *attachment = [[NTESJanKenPonAttachment alloc] init];
            attachment.value = arc4random() % 3 + 1;
            [self sendMessage:[NTESSessionMsgConverter msgWithJenKenPon:attachment]];
            break;
        }
        default:
            break;
    }
}

- (void)sendMessage:(NIMMessage *)message
{
    NIMChatroomMember *member = [[NTESChatroomManager sharedInstance] myInfo:self.chatroom.roomId];
    message.remoteExt = @{@"type":@(member.type)};
    [super sendMessage:message];
}

#pragma mark - NIMInputDelegate
- (void)showInputView
{
    if ([self.delegate respondsToSelector:@selector(showInputView)]) {
        [self.delegate showInputView];
    }
}


- (void)hideInputView
{
    if ([self.delegate respondsToSelector:@selector(hideInputView)]) {
        [self.delegate hideInputView];
    }
}


#pragma mark - UIScrollViewDelegate
- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    [super scrollViewDidScroll:scrollView];
    CGFloat offset = 44.f;
    if (scrollView.contentOffset.y <= -offset && !_isRefreshing && self.tableView.isDragging) {
        _isRefreshing = YES;
        [self.refreshControl beginRefreshing];
        [self.refreshControl sendActionsForControlEvents:UIControlEventValueChanged];
        [scrollView endEditing:YES];
    }
    else if(scrollView.contentOffset.y >= 0)
    {
        _isRefreshing = NO;
    }
}


#pragma mark - Get
- (NTESChatroomConfig *)config{
    if (!_config) {
        _config = [[NTESChatroomConfig alloc] initWithChatroom:self.chatroom.roomId];
    }
    return _config;
}


@end
