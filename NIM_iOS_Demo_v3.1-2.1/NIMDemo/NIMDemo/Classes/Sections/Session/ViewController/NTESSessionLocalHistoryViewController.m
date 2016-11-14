//
//  NTESSessionLocalHistoryViewController.m
//  NIM
//
//  Created by chris on 15/7/8.
//  Copyright © 2015年 Netease. All rights reserved.
//

#import "NTESSessionLocalHistoryViewController.h"
#import "NTESSearchMessageEntraceCell.h"
#import "NTESSearchMessageContentCell.h"
#import "NTESSearchLocalHistoryObject.h"
#import "NTESBundleSetting.h"
#import "UIView+NTES.h"

#define EntranceCellIdentity @"entrance"
#define EntranceCellHeight   45

#define ContentCellIdentity  @"content"
#define ContentCellHeight    50

#define SearchLimit  10

@interface NTESSessionLocalHistoryViewController ()<UITableViewDataSource,UITableViewDelegate,UISearchBarDelegate,UISearchDisplayDelegate>

@property (nonatomic,strong) UISearchDisplayController *searchDisplayVC;

@property (nonatomic,strong) NTESSearchLocalHistoryObject *searchObject;

@property (nonatomic,copy)   NSString *keyWord;

@property (nonatomic,strong) NSMutableArray *data;

@property (nonatomic,strong) NIMSession *session;

@property (nonatomic,strong) NIMMessageSearchOption *lastOption;

@property (nonatomic,strong) NSArray *members;

@property (nonatomic,strong) UILabel *noResultTip;

@end

@implementation NTESSessionLocalHistoryViewController

- (instancetype)initWithSession:(NIMSession *)session{
    self = [super initWithNibName:nil bundle:nil];
    if (self) {
        _session = session;
        NTESSearchLocalHistoryObject *object = [[NTESSearchLocalHistoryObject alloc] init];
        _searchObject = object;
        _data = [[NSMutableArray alloc]init];
    }
    return self;
}


- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationItem.title = @"聊天记录";
    [self prepareMember];
    self.searchBar = [[UISearchBar alloc] initWithFrame:CGRectZero];
    self.tableView.tableHeaderView = self.searchBar;
    self.tableView.tableFooterView = [[UIView alloc] init];
    self.searchBar.delegate = self;
    self.searchDisplayVC = [[UISearchDisplayController alloc] initWithSearchBar:self.searchBar contentsController:self];
    self.searchDisplayVC.searchResultsTableView.tableFooterView = [[UIView alloc] init];
    self.searchDisplayVC.searchResultsDataSource = self;
    self.searchDisplayVC.searchResultsDelegate = self;
    self.tableView.dataSource = self;
    self.tableView.delegate   = self;
    self.refreshControl = [[UIRefreshControl alloc] init];
    [self.refreshControl addTarget:self action:@selector(onPull2Refresh:) forControlEvents:UIControlEventValueChanged];
    [self.searchDisplayVC.searchResultsTableView registerClass:[NTESSearchMessageEntraceCell class] forCellReuseIdentifier:EntranceCellIdentity];
    [self.tableView registerClass:[NTESSearchMessageContentCell class] forCellReuseIdentifier:ContentCellIdentity];
    
    self.noResultTip = [[UILabel alloc] initWithFrame:CGRectZero];
    self.noResultTip.text = @"无结果";
    self.noResultTip.hidden = YES;
    [self.noResultTip sizeToFit];
    [self.tableView addSubview:self.noResultTip];
}

- (void)viewDidLayoutSubviews{
    self.noResultTip.centerX = self.tableView.width * .5f;
    self.noResultTip.centerY = self.tableView.height * .5f;
}

- (void)prepareMember{
    if (self.session.sessionType == NIMSessionTypeTeam) {
        __weak typeof(self) wself = self;
        [[NIMSDK sharedSDK].teamManager fetchTeamMembers:self.session.sessionId completion:^(NSError *error, NSArray *members) {
            NSMutableArray *data = [[NSMutableArray alloc] init];
            for (NIMTeamMember *member in members) {
                if(member.userId.length){
                    [data addObject:member.userId];
                }
            }
            wself.members = data;
        }];
    }else{
        NSString *me = [[NIMSDK sharedSDK].loginManager currentAccount];
        self.members = @[self.session.sessionId,me];
    }
}


#pragma mark - UISearchBarDelegate
- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar{
    self.noResultTip.hidden = YES;
}

- (void)searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)searchText{
    self.keyWord = searchText;
}

#pragma mark - UITableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
    return [[self fetchData:tableView] count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    NSString *identity;
    NSArray *data = [self fetchData:tableView];
    NTESSearchLocalHistoryObject *object = data[indexPath.row];
    if (object.type == SearchLocalHistoryTypeEntrance) {
        identity = EntranceCellIdentity;
    }else{
        identity = ContentCellIdentity;
    }
    UITableViewCell<NTESSearchObjectRefresh> *cell = [tableView dequeueReusableCellWithIdentifier:identity];
    [cell refresh:object];
    return cell;
}

#pragma mark - UITableViewDelegate
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath{
    NSArray *data = [self fetchData:tableView];
    NTESSearchLocalHistoryObject *object = data[indexPath.row];
    return object.uiHeight;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    NSArray *data = [self fetchData:tableView];
    NTESSearchLocalHistoryObject *object = data[indexPath.row];
    if (object.type == SearchLocalHistoryTypeEntrance) {
        [self.data removeAllObjects];
        NIMMessageSearchOption *option = [[NIMMessageSearchOption alloc] init];
        option.searchContent = self.keyWord;
        NSArray *uids = [self searchUsersByKeyword:self.keyWord users:self.members];
        option.fromIds       = uids;
        option.limit         = SearchLimit;
        option.order         = [NTESBundleSetting sharedConfig].localSearchOrderByTimeDesc? NIMMessageSearchOrderDesc: NIMMessageSearchOrderAsc;
        self.lastOption      = option;
        [self.searchDisplayVC setActive:NO];
        [self showSearchData:option loadMore:YES];
    }
}

#pragma mark - Action
- (void)onPull2Refresh:(id)sender{
    if (!self.keyWord.length) {
        NTESSearchLocalHistoryObject *obj  = self.data.firstObject;
        if ([NTESBundleSetting sharedConfig].localSearchOrderByTimeDesc == NIMMessageSearchOrderDesc) {
            self.lastOption.startTime      = 0;
            self.lastOption.endTime        = obj.message.timestamp;
        }else{
            self.lastOption.startTime      = obj.message.timestamp;
            self.lastOption.endTime        = 0;
        }
        [self showSearchData:self.lastOption loadMore:NO];
    }
}

- (void)loadMore:(id)sender{
    if (!self.keyWord.length) {
        NTESSearchLocalHistoryObject *obj  = self.data.lastObject;
        if ([NTESBundleSetting sharedConfig].localSearchOrderByTimeDesc == NIMMessageSearchOrderDesc) {
            self.lastOption.startTime      = obj.message.timestamp;
            self.lastOption.endTime        = 0;
        }else{
            self.lastOption.startTime      = 0;
            self.lastOption.endTime        = obj.message.timestamp;
        }
        [self showSearchData:self.lastOption loadMore:YES];
    }
}

#pragma mark - Private
- (NSArray *)fetchData:(UITableView *)tableView{
    if (tableView == self.searchDisplayVC.searchResultsTableView) {
        NTESSearchLocalHistoryObject *obj = [[NTESSearchLocalHistoryObject alloc] init];
        obj.content  = [NSString stringWithFormat:@"搜索：“%@”",self.keyWord];
        obj.type     = SearchLocalHistoryTypeEntrance;
        obj.uiHeight = EntranceCellHeight;
        return [@[obj] mutableCopy];
    }else{
        return self.data;
    }
}

- (void)showSearchData:(NIMMessageSearchOption *)option loadMore:(BOOL)loadMore{
    __weak typeof(self) wself = self;
    [[NIMSDK sharedSDK].conversationManager searchMessages:self.session option:option result:^(NSError *error, NSArray *messages) {
        messages = [NTESBundleSetting sharedConfig].localSearchOrderByTimeDesc == NIMMessageSearchOrderAsc ? messages.reverseObjectEnumerator.allObjects : messages;
        NSMutableArray *array = [[NSMutableArray alloc]init];
        for (NIMMessage *message in messages) {
            NTESSearchLocalHistoryObject *obj = [[NTESSearchLocalHistoryObject alloc] initWithMessage:message];
            obj.type     = SearchLocalHistoryTypeContent;
            [array addObject:obj];
        }
        if (loadMore) {
            [wself.data addObjectsFromArray:array];
            wself.tableView.tableFooterView = array.count == SearchLimit? wself.tableFooterView : [[UIView alloc]init];
            wself.noResultTip.hidden = wself.data.count != 0;
        }else{
            [array addObjectsFromArray:self.data];
            wself.data = array;
        }
        wself.keyWord = nil;
        [wself.tableView reloadData];
        [wself.refreshControl endRefreshing];
    }];
}

- (UIView *)tableFooterView{
    UIButton *btn   = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, UIScreenWidth, ContentCellHeight)];
    [btn addTarget:self action:@selector(loadMore:) forControlEvents:UIControlEventTouchUpInside];
    UILabel *lable = [[UILabel alloc] initWithFrame:CGRectZero];
    lable.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin;
    lable.text = @"点击加载更多";
    [lable sizeToFit];
    [btn addSubview:lable];
    lable.centerX = btn.width  * .5f;
    lable.centerY = btn.height * .5f;
    UIView *sep = [[UIView alloc] initWithFrame:CGRectMake(0, 0, UIScreenWidth, 0.5f)];
    sep.backgroundColor = UIColorFromRGB(0xebebeb);
    [btn addSubview:sep];
    return btn;
}


- (NSArray *)searchUsersByKeyword:(NSString *)keyword users:(NSArray *)users{
    NSMutableArray *data = [[NSMutableArray alloc] init];
    for (NSString *uid in users) {
        NIMKitInfo *info = [[NIMKit sharedKit] infoByUser:uid];
        [data addObject:info];
    }
    NSPredicate *predicate = [NSPredicate predicateWithFormat: @"SELF.showName CONTAINS[cd] %@",keyword];
    NSArray *array = [data filteredArrayUsingPredicate:predicate];
    NSMutableArray *output = [[NSMutableArray alloc] init];
    for (NIMKitInfo *info in array) {
        [output addObject:info.infoId];
    }
    return output;
}

#pragma mark - 旋转处理 (iOS7)
- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
                                duration:(NSTimeInterval)duration
{
    if (self.searchDisplayVC.isActive) {
        [self.searchDisplayVC setActive:NO animated:YES];
    }
}

#pragma mark - 旋转处理 (iOS8 or above)
- (void)viewWillTransitionToSize:(CGSize)size
       withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator
{
    [super viewWillTransitionToSize:size withTransitionCoordinator:coordinator];
    if (self.searchDisplayVC.isActive) {
        [self.searchDisplayVC setActive:NO animated:YES];
    }
}



@end
