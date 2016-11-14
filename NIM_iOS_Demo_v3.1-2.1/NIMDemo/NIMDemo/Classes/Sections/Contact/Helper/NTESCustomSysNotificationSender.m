//
//  NTESCustomSysNotiSender.m
//  NIM
//
//  Created by chris on 15/5/26.
//  Copyright (c) 2015年 Netease. All rights reserved.
//

#import "NTESCustomSysNotificationSender.h"

@interface NTESCustomSysNotificationSender ()
@property (nonatomic,strong)    NSDate *lastTime;
@end

@implementation NTESCustomSysNotificationSender

- (void)sendCustomContent:(NSString *)content toSession:(NIMSession *)session{
    if (!content) {
        return;
    }
    NSDictionary *dict = @{
                           NTESNotifyID : @(NTESCustom),
                           NTESCustomContent : content,
                           };
    NSData *data = [NSJSONSerialization dataWithJSONObject:dict
                                                   options:0
                                                     error:nil];
    NSString *json = [[NSString alloc] initWithData:data
                                              encoding:NSUTF8StringEncoding];
    
    NIMCustomSystemNotification *notification = [[NIMCustomSystemNotification alloc] initWithContent:json];
    notification.apnsContent = content;
    notification.sendToOnlineUsersOnly = NO;
    NIMCustomSystemNotificationSetting *setting = [[NIMCustomSystemNotificationSetting alloc] init];
    setting.apnsEnabled = YES;
    notification.setting = setting;
    [[[NIMSDK sharedSDK] systemNotificationManager] sendCustomNotification:notification
                                                                 toSession:session
                                                                completion:nil];
}


- (void)sendTypingState:(NIMSession *)session
{
    NSString *currentAccount = [[[NIMSDK sharedSDK] loginManager] currentAccount];
    if (session.sessionType != NIMSessionTypeP2P ||
        [session.sessionId isEqualToString:currentAccount])
    {
        return;
    }
    
    NSDate *now = [NSDate date];
    if (_lastTime == nil ||
        [now timeIntervalSinceDate:_lastTime] > 3)
    {
        _lastTime = now;
        
        NSDictionary *dict = @{NTESNotifyID : @(NTESCommandTyping)};
        NSData *data = [NSJSONSerialization dataWithJSONObject:dict
                                                       options:0
                                                         error:nil];
        NSString *content = [[NSString alloc] initWithData:data
                                                  encoding:NSUTF8StringEncoding];
        
        NIMCustomSystemNotification *notification = [[NIMCustomSystemNotification alloc] initWithContent:content];
        notification.sendToOnlineUsersOnly = YES;
        NIMCustomSystemNotificationSetting *setting = [[NIMCustomSystemNotificationSetting alloc] init];
        setting.apnsEnabled  = NO;
        notification.setting = setting;
        [[[NIMSDK sharedSDK] systemNotificationManager] sendCustomNotification:notification
                                                                     toSession:session
                                                                    completion:nil];
    }

}

@end
