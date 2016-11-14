//
//  NTESAudioChatViewController.m
//  NIM
//
//  Created by chris on 15/5/12.
//  Copyright (c) 2015年 Netease. All rights reserved.
//

#import "NTESAudioChatViewController.h"
#import "NTESVideoChatViewController.h"
#import "NTESTimerHolder.h"
#import "NetCallChatInfo.h"
#import "NTESMainTabController.h"
#import "NTESSessionUtil.h"
#import "UIView+Toast.h"
#import "UIAlertView+NTESBlock.h"
#import "NTESVideoChatNetStatusView.h"

@interface NTESAudioChatViewController ()

@end

@implementation NTESAudioChatViewController
- (instancetype)initWithCallInfo:(NetCallChatInfo *)callInfo{
    self = [self initWithNibName:nil bundle:nil];
    if (self) {
        self.callInfo = callInfo;
        self.callInfo.isMute = NO;
        self.callInfo.disableCammera = NO;
        self.callInfo.useSpeaker = NO;
        [[NIMSDK sharedSDK].netCallManager switchType:NIMNetCallTypeAudio];
    }
    return self;
}

- (instancetype)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        self.callInfo.callType = NIMNetCallTypeAudio;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self initUI];
}

- (void)initUI {
    self.localRecordingView.layer.cornerRadius = 10.0;
    self.localRecordingRedPoint.layer.cornerRadius = 4.0;
    self.lowMemoryView.layer.cornerRadius = 10.0;
    self.lowMemoryRedPoint.layer.cornerRadius = 4.0;
    self.refuseBtn.exclusiveTouch = YES;
    self.acceptBtn.exclusiveTouch = YES;
}

#pragma mark - Call Life
- (void)startByCaller{
    [super startByCaller];
    [self startInterface];
}

- (void)startByCallee{
    [super startByCallee];
    [self waitToCallInterface];
}

- (void)onCalling{
    [super onCalling];
    [self audioCallingInterface];
}

- (void)waitForConnectiong{
    [super onCalling];
    [self connectingInterface];
}

#pragma mark - Interface
//正在接听中界面
- (void)startInterface{
    self.hangUpBtn.hidden  = NO;
    self.muteBtn.hidden    = YES;
    self.speakerBtn.hidden = YES;
    self.localRecordBtn.hidden = YES;
    self.localRecordingView.hidden = YES;
    self.lowMemoryView.hidden = YES;
    self.durationLabel.hidden   = YES;
    self.switchVideoBtn.hidden  = YES;
    self.connectingLabel.hidden = NO;
    self.connectingLabel.text   = @"正在呼叫，请稍候...";
    self.refuseBtn.hidden = YES;
    self.acceptBtn.hidden = YES;
}

//选择是否接听界面
- (void)waitToCallInterface{
    self.hangUpBtn.hidden  = YES;
    self.muteBtn.hidden    = YES;
    self.speakerBtn.hidden = YES;
    self.localRecordBtn.hidden = YES;
    self.localRecordingView.hidden = YES;
    self.lowMemoryView.hidden = YES;
    self.durationLabel.hidden   = YES;
    self.switchVideoBtn.hidden  = YES;
    self.connectingLabel.hidden = NO;
    NSString *nick = [NTESSessionUtil showNick:self.callInfo.caller inSession:nil];
    self.connectingLabel.text = [nick stringByAppendingString:@"的来电"];
    self.refuseBtn.hidden = NO;
    self.acceptBtn.hidden = NO;
}

//连接对方界面
- (void)connectingInterface{
    self.hangUpBtn.hidden  = NO;
    self.muteBtn.hidden    = YES;
    self.speakerBtn.hidden = YES;
    self.localRecordBtn.hidden = YES;
    self.localRecordingView.hidden = YES;
    self.lowMemoryView.hidden = YES;
    self.durationLabel.hidden   = YES;
    self.switchVideoBtn.hidden  = YES;
    self.connectingLabel.hidden = NO;
    self.connectingLabel.text   = @"正在连接对方...请稍后...";
    self.refuseBtn.hidden = YES;
    self.acceptBtn.hidden = YES;
}

//接听中界面(音频)
- (void)audioCallingInterface{
    NIMNetCallNetStatus status = [NIMSDK sharedSDK].netCallManager.netStatus;
    [self.netStatusView refreshWithNetState:status];
    self.hangUpBtn.hidden  = NO;
    self.muteBtn.hidden    = NO;
    self.localRecordBtn.hidden = NO;
    self.speakerBtn.hidden = NO;
    self.durationLabel.hidden   = NO;
    self.switchVideoBtn.hidden  = NO;
    self.connectingLabel.hidden = YES;
    self.refuseBtn.hidden = YES;
    self.acceptBtn.hidden = YES;
    self.muteBtn.selected    = self.callInfo.isMute;
    self.speakerBtn.selected = self.callInfo.useSpeaker;
    self.localRecordBtn.selected = self.callInfo.localRecording;
    self.localRecordingView.hidden = !self.callInfo.localRecording;
    self.lowMemoryView.hidden = YES;
}

//切换接听中界面(视频)
- (void)videoCallingInterface{
    NTESVideoChatViewController *vc = [[NTESVideoChatViewController alloc] initWithCallInfo:self.callInfo];
    [UIView  beginAnimations:nil context:NULL];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    [UIView setAnimationDuration:0.75];
    [self.navigationController pushViewController:vc animated:NO];
    [UIView setAnimationTransition:UIViewAnimationTransitionFlipFromRight forView:self.navigationController.view cache:NO];
    [UIView commitAnimations];
    NSMutableArray * vcs = [self.navigationController.viewControllers mutableCopy];
    [vcs removeObject:self];
    self.navigationController.viewControllers = vcs;
}

- (void)udpateLowSpaceWarning:(BOOL)show {
    self.lowMemoryView.hidden = !show;
    self.localRecordingView.hidden = show;
}


#pragma mark - IBAction
- (IBAction)hangup:(id)sender{
    [self hangup];
}

- (IBAction)acceptToCall:(id)sender{
    BOOL accept = (sender == self.acceptBtn);
    [self response:accept];
}

- (IBAction)mute:(id)sender{
    self.callInfo.isMute  = !self.callInfo.isMute;
    self.muteBtn.selected = self.callInfo.isMute;
    [[NIMSDK sharedSDK].netCallManager setMute:self.callInfo.isMute];
}

- (IBAction)userSpeaker:(id)sender{
    self.callInfo.useSpeaker = !self.callInfo.useSpeaker;
    self.speakerBtn.selected = self.callInfo.useSpeaker;
    [[NIMSDK sharedSDK].netCallManager setSpeaker:self.callInfo.useSpeaker];
}

- (IBAction)switchToVideoMode:(id)sender {
    [self.view makeToast:@"已发送转换请求，请等待对方应答..."
                duration:2
                position:CSToastPositionCenter];
    [[NIMSDK sharedSDK].netCallManager control:self.callInfo.callID type:NIMNetCallControlTypeToVideo];
}

- (IBAction)localRecord:(id)sender {
    
    if (self.callInfo.localRecording) {
        if (![self stopLocalRecording]) {
            [self.view makeToast:@"无法结束录制"
                        duration:3
                        position:CSToastPositionCenter];
        }
    }
    else {
        NSString *toastText;
        if ([self startLocalRecording]) {
            toastText = @"仅录制你说话的内容";
        }
        else {
            toastText = @"无法开始录制";
        }
        [self.view makeToast:toastText
                    duration:3
                    position:CSToastPositionCenter];
    }

}


#pragma mark - NIMNetCallManagerDelegate

- (void)onControl:(UInt64)callID
             from:(NSString *)user
             type:(NIMNetCallControlType)control{
    [super onControl:callID from:user type:control];
    switch (control) {
        case NIMNetCallControlTypeToVideo:
            [self onResponseVideoMode];
            break;
        case NIMNetCallControlTypeAgreeToVideo:
            [self videoCallingInterface];
            break;
        case NIMNetCallControlTypeRejectToVideo:
            [self.view makeToast:@"对方拒绝切换到视频模式"
                        duration:2
                        position:CSToastPositionCenter];
            break;
        default:
            break;
    }
}

- (void)onCall:(UInt64)callID status:(NIMNetCallStatus)status{
    if (self.callInfo.callID != callID) {
        return;
    }
    [super onCall:callID status:status];
    //记时
    switch (status) {
        case NIMNetCallStatusConnect:
            self.durationLabel.hidden = NO;
            self.durationLabel.text = self.durationDesc;
            break;
        default:
            break;
    }
}

- (void)onCall:(UInt64)callID
     netStatus:(NIMNetCallNetStatus)status{
    [self.netStatusView refreshWithNetState:status];
}


- (void)onLocalRecordStarted:(UInt64)callID fileURL:(NSURL *)fileURL
{
    [super onLocalRecordStarted:callID fileURL:fileURL];
    if (self.callInfo.callID == callID) {
        self.localRecordBtn.selected = YES;
        self.localRecordingView.hidden = NO;
        self.lowMemoryView.hidden = YES;
    }
}


- (void)onLocalRecordError:(NSError *)error
                    callID:(UInt64)callID
{
    [super onLocalRecordError:error callID:callID];
    if (self.callInfo.callID == callID) {
        self.localRecordBtn.selected = NO;
        self.localRecordingView.hidden = YES;
        self.lowMemoryView.hidden = YES;
    }
}

- (void) onLocalRecordStopped:(UInt64)callID
                      fileURL:(NSURL *)fileURL
{
    [super onLocalRecordStopped:callID fileURL:fileURL];
    if (self.callInfo.callID == callID) {
        self.localRecordBtn.selected = NO;
        self.localRecordingView.hidden = YES;
        self.lowMemoryView.hidden = YES;
    }
}


#pragma mark - M80TimerHolderDelegate
- (void)onNTESTimerFired:(NTESTimerHolder *)holder{
    [super onNTESTimerFired:holder];
    self.durationLabel.text = self.durationDesc;
}

#pragma mark -  Misc
- (NSString*)durationDesc{
    if (!self.callInfo.startTime) {
        return @"";
    }
    NSTimeInterval time = [NSDate date].timeIntervalSince1970;
    NSTimeInterval duration = time - self.callInfo.startTime;
    return [NSString stringWithFormat:@"%02d:%02d",(int)duration/60,(int)duration%60];
}


- (void)onResponseVideoMode{
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"" message:@"对方请求切换为视频模式" delegate:nil cancelButtonTitle:@"拒绝" otherButtonTitles:@"接受", nil];
    [alert showAlertWithCompletionHandler:^(NSInteger idx) {
        switch (idx) {
            case 0:
                [[NIMSDK sharedSDK].netCallManager control:self.callInfo.callID type:NIMNetCallControlTypeRejectToVideo];
                [self.view makeToast:@"已拒绝"
                            duration:2
                            position:CSToastPositionCenter];
                break;
            case 1:
                [[NIMSDK sharedSDK].netCallManager control:self.callInfo.callID type:NIMNetCallControlTypeAgreeToVideo];
                [self videoCallingInterface];
                break;
            default:
                break;
        }
    }];
}


@end
