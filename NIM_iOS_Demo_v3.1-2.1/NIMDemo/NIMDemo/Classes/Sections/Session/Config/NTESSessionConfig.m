//
//  NTESSessionConfig.m
//  NIM
//
//  Created by amao on 8/11/15.
//  Copyright (c) 2015 Netease. All rights reserved.
//

#import "NTESSessionConfig.h"
#import "NTESSessionCustomLayoutConfig.h"
#import "NIMMediaItem.h"
#import "NTESBundleSetting.h"
#import "NTESSnapchatAttachment.h"
#import "NTESWhiteboardAttachment.h"
#import "NTESBundleSetting.h"

@interface NTESSessionConfig()

@end

@implementation NTESSessionConfig

- (NSArray *)mediaItems
{
    return @[[NIMMediaItem item:NTESMediaButtonPicture
                    normalImage:[UIImage imageNamed:@"bk_media_picture_normal"]
                  selectedImage:[UIImage imageNamed:@"bk_media_picture_nomal_pressed"]
                          title:@"相册"],
             
             [NIMMediaItem item:NTESMediaButtonShoot
                    normalImage:[UIImage imageNamed:@"bk_media_shoot_normal"]
                  selectedImage:[UIImage imageNamed:@"bk_media_shoot_pressed"]
                          title:@"拍摄"],
             
             [NIMMediaItem item:NTESMediaButtonLocation
                    normalImage:[UIImage imageNamed:@"bk_media_position_normal"]
                  selectedImage:[UIImage imageNamed:@"bk_media_position_pressed"]
                          title:@"位置"],
             
             [NIMMediaItem item:NTESMediaButtonJanKenPon
                    normalImage:[UIImage imageNamed:@"icon_jankenpon_normal"]
                  selectedImage:[UIImage imageNamed:@"icon_jankenpon_pressed"]
                          title:@"石头剪刀布"],
             
             [NIMMediaItem item:NTESMediaButtonAudioChat
                    normalImage:[UIImage imageNamed:@"btn_media_telphone_message_normal"]
                  selectedImage:[UIImage imageNamed:@"btn_media_telphone_message_pressed"]
                          title:@"实时语音"],
             
             [NIMMediaItem item:NTESMediaButtonVideoChat
                    normalImage:[UIImage imageNamed:@"btn_bk_media_video_chat_normal"]
                  selectedImage:[UIImage imageNamed:@"btn_bk_media_video_chat_pressed"]
                          title:@"视频聊天"],
             
             [NIMMediaItem item:NTESMediaButtonFileTrans
                    normalImage:[UIImage imageNamed:@"icon_file_trans_normal"]
                  selectedImage:[UIImage imageNamed:@"icon_file_trans_pressed"]
                          title:@"文件传输"],
             
             [NIMMediaItem item:NTESMediaButtonSnapchat
                    normalImage:[UIImage imageNamed:@"bk_media_snap_normal"]
                  selectedImage:[UIImage imageNamed:@"bk_media_snap_pressed"]
                          title:@"阅后即焚"],
             
             [NIMMediaItem item:NTESMediaButtonWhiteBoard
                    normalImage:[UIImage imageNamed:@"btn_whiteboard_invite_normal"]
                  selectedImage:[UIImage imageNamed:@"btn_whiteboard_invite_pressed"]
                          title:@"白板"],
             
             [NIMMediaItem item:NTESMediaButtonTip
                    normalImage:[UIImage imageNamed:@"bk_media_tip_normal"]
                  selectedImage:[UIImage imageNamed:@"bk_media_tip_pressed"]
                          title:@"提醒消息"]];
    
    
}



- (BOOL)shouldHideItem:(NIMMediaItem *)item
{
    BOOL hidden = NO;
    BOOL isMe   = _session.sessionType == NIMSessionTypeP2P
                          && [_session.sessionId isEqualToString:[[NIMSDK sharedSDK].loginManager currentAccount]];
    if (_session.sessionType == NIMSessionTypeTeam || isMe) {
        hidden = item.tag == NTESMediaButtonAudioChat ||
                 item.tag == NTESMediaButtonVideoChat ||
                 item.tag == NTESMediaButtonWhiteBoard||
                 item.tag == NTESMediaButtonSnapchat; 
    }
    return hidden;
}


- (id<NIMCellLayoutConfig>)layoutConfigWithMessage:(NIMMessage *)message{
    id<NIMCellLayoutConfig> config;
    switch (message.messageType) {
        case NIMMessageTypeCustom:{
            if ([NTESSessionCustomLayoutConfig supportMessage:message]) {
               config = [[NTESSessionCustomLayoutConfig alloc] init];
               break;
            }
        }
        default:
        //其他类型的Cell采用默认实现就返回nil即可。
            break;
    }
    return config;
}

- (BOOL)disableProximityMonitor{
    return [NTESBundleSetting sharedConfig].disableProximityMonitor;
}


- (BOOL)shouldHandleReceipt{
    return YES;
}

- (BOOL)shouldHandleReceiptForMessage:(NIMMessage *)message
{
    //文字，语音，图片，视频，文件，地址位置和自定义消息都支持已读回执，其他的不支持
    NIMMessageType type = message.messageType;
    if (type == NIMMessageTypeCustom) {
        NIMCustomObject *object = (NIMCustomObject *)message.messageObject;
        id attachment = object.attachment;
        
        if ([attachment isKindOfClass:[NTESWhiteboardAttachment class]]) {
            return NO;
        }
    }
    
    
    
    return type == NIMMessageTypeText ||
           type == NIMMessageTypeAudio ||
           type == NIMMessageTypeImage ||
           type == NIMMessageTypeVideo ||
           type == NIMMessageTypeFile ||
           type == NIMMessageTypeLocation ||
           type == NIMMessageTypeCustom;
}

- (NIMAudioType)recordType
{
    return [[NTESBundleSetting sharedConfig] usingAmr] ? NIMAudioTypeAMR : NIMAudioTypeAAC;
}

@end
