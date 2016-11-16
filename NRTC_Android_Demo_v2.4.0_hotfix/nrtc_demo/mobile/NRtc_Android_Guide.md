# 网易实时语音视频通话SDK开发指南

## <span id="SDK 概述">SDK 概述</span>

网易实时语音视频通话 SDK 为移动应用提供一个完善的网络语音视频通话开发框架，屏蔽掉复杂的系统细节，对外提供简洁的 API，支持任何帐号体系接入，方便第三方应用快速集成网络通话功能。 SDK 提供能力如下：

* 实时网络语音通话(双人模式，多人模式)
* 实时网络视频通话(双人模式，多人模式)
* 互动直播(多人模式)


## <span id="开发准备">开发准备</span>

首先从[网易云信官网](http://netease.im/?page=download  "target=_blank")下载 SDK。开发者可以根据实际需求，配置类库。

**<code>NRTC</code> 需要 Android 4.0 或更新的系统**


### <span id="集成SDK">集成SDK</span>

SDK 完整版包含了 IM，音视频通话等功能。 如果单独使用音视频仅需关注<code>nrtc</code>相关的类库， 主要包含了<code>nrtc-sdk.jar</code>, <code>libnrtc_engine.so</code> 和 <code>libnrtc_network.so</code>。

将 jar 文件拷贝到你的工程的 libs 目录下，JNI 库选择以下两种方式中的任何一个拷贝到相应的目录即可完成配置。
```
jniLibs(libs)
├── armeabi
│   ├── libnrtc_engine.so
│   └── libnrtc_network.so
├── armeabi-v7a
│   ├── libnrtc_engine.so
│   └── libnrtc_network.so
└── x86
    ├── libnrtc_engine.so
    └── libnrtc_network.so
```

```
jniLibs(libs)
└── armeabi
    ├── libnrtc_engine_armeabi.so
    ├── libnrtc_network_armeabi.so
    ├── libnrtc_engine_armeabi-v7a.so
    └── libnrtc_network_armeabi-v7a.so
```

### <span id="通过Gradle集成SDK">通过Gradle集成SDK</span>

NRTC支持 Gradle 集成。

首先，在整个工程的 build.gradle 文件中，配置repositories，使用 jcenter 或者 maven ，二选一即可，如下：

```
allprojects {
    repositories {
        jcenter() // 或者 mavenCentral()
    }
}
```

第二步，在主工程的 build.gradle 文件中，添加 dependencies。根据自己项目的需求，添加不同的依赖即可。
> 版本号: 集成最新的NRTC需要使用云信SDK整体的版本号,不要依赖NRTC的版本号。

```
dependencies {

    compile fileTree(dir: 'libs', include: '*.jar')

    compile 'com.netease.nimlib:nrtc:2.7.0'
}
```


### <span id="权限">权限</span>

在 `AndroidManifest.xml` 中加入以下配置:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />
    <uses-feature android:glEsVersion="0x00020000" android:required="true" />

	<!-- 访问网络状态-->
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    
	<!-- 音频和视频权限 -->
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    
    <!-- 外置存储存取权限 -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>

    <!-- 其他权限 -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

</manifest>
```


### <span id="混淆配置">混淆配置</span>

如果你的 apk 最终会经过代码混淆，请在 proguard 配置文件中加入以下代码:

```
-dontwarn com.netease.nrtc.**
-keep class com.netease.nrtc.** {*;}
```

### <span id="SDK 数据目录">SDK 数据目录</span>

SDK 在运行过程中会产生部分数据，默认存储路径为 “/{外卡根目录}/Android/data/{app_package_name}/files" 。
* **日志数据**
  <p>日志文件保存在log目录下，主要包含 <code>nrtc_engine.log</code> 以及 <code>nrtc_net.log</code> 等。</p>
* **视频数据**
  <p>视频文件保存在record目录下，用户在开启本地录制功能后会生成相应的MP4文件。</p>
* **图像数据**
  <p>图像文件保存在snapshot目录下，用户在使用SDK截图功能时会生成相应的图像文件。</p>
    



## <span id="开发流程">开发流程</span>

开发流程主要分为基础流程，语音通话流程，视频通话流程，事件通知流程以及其他流程。

### <span id="基础流程">基础流程</span>

SDK 主要包含了以下几个基础流程:

* 权限检查

    ```java
    public static List<String> checkPermission(Context context);
    ```
    
   在Android 6.0 及以上设备运行SDK前可以检测缺失权限，请确保在创建SDK前已经获取了这些权限。


* 创建 NRTC 实例

    ```java
    public static NRtc create(final Context context, NRtcCallback callback)
            throws UnsatisfiedLinkError, RuntimeException ;
    ```

    ```java
    public static NRtc create(final Context context, String logDir, NRtcCallback callback)
            throws UnsatisfiedLinkError, RuntimeException ;
    ```

    创建 <code>NRtc</code> 实例。创建时可能会出现 <code>RuntimeException</code> , <code>UnsatisfiedLinkError</code> 异常，
    <code>create</code> 创建实例后需要通过 <code>dispose</code>释放资源。
    
    主要参数：
    
    <code>callback</code> 主要接收通话过程中所有的事件通知。

    <code>logDir</code> 日志路径。
    
    

* 加入频道

    ```java
    public abstract int joinChannel(String appKey, String token, String channelName, long uid, int mode);
    ```

    加入频道支持安全模式和非安全模式两种方式:
    
    > `安全模式`: 客户端需要 appKey 和 token 来完成认证进行实时通话。 其中 token 需要第三方服务器从云信服务器获取，详细服务器接口请参考 [网易云信Sever Http API接口文档](http://dev.netease.im/docs?doc=server)的`音视频通话`部分。
    
    > `非安全模式`: 客户端只需要 appKey 即可完成认证进行实时通话，不需要传入 token，这种情况下用户需要保管好 appKey，防止泄露。默认情况下非安全模式处于关闭状态，如需开启请联系我们的技术支持。为了你的账号安全，我们推荐使用安全模式。
    
    主要参数：
    
    <code>appKey</code> 开发者申请的云信AppKey。
    
    <code>token</code> 参考安全模式，开发者服务器从云信服务器获取的动态token，默认情况下为了提高安全性采用安全模式来完成SDK的验证。
    
    <code>channelName</code> 同一AppKey下的客户端通过频道名加入会话。
    
    <code>uid</code> 加入会话用户ID(非零值)，便于其他客户端通知用户的加入。
    
    <code>mode</code> 用户加入频道的模式(音频，视频)，在多人会话时不能动态切换模式。


* 离开频道

    ```java
    int leaveChannel();
    ```
    离开频道后会有相应的 <code>NRtcCallback</code> 通知。



* 销毁 NRTC 实例

    ```java
    public void dispose();
    ```

    成功创建 <code>NRtc</code> 实例后，需要销毁实例。
    



### <span id="语音操作">语音操作</span>

语音流程主要在基础流程上添加了语音相关的操作。

* 可选参数设置与获取接口

    ```java
    public abstract void setParameters(NRtcParameters params);
    ```

    ```java
    public abstract NRtcParameters getParameters(NRtcParameters params);
    ```

    设置，获取语音相关的参数。

    参数主要分为两类:

    **静态参数: 仅允许在加入频道前设置获取**

    **动态参数: 支持加入频道前设置获取，同时也支持通话过程中设置与获取**

    <code>NRtcParameters</code> 语音参数:

    * <code>KEY_AUDIO_EFFECT_AUTOMATIC_GAIN_CONTROL</code> 语音自动增益模式, 静态参数。
    * <code>KEY_AUDIO_EFFECT_NOISE_SUPPRESSOR</code> 语音降噪模式, 静态参数。
    * <code>KEY_AUDIO_EFFECT_ACOUSTIC_ECHO_CANCELER</code> 语音回音抑制模式, 静态参数。
    * <code>KEY_SERVER_AUDIO_RECORD</code> 服务器语音录制, 静态参数。
    * <code>KEY_AUDIO_CALL_PROXIMITY</code> 通话时距离感应, 静态参数。
    * <code>KEY_AUDIO_FRAME_FILTER</code> 外部语音数据处理, 运行时参数。



* 设置静音

    ```java
    public abstract int muteAudioStream(long uid, boolean muted);
    ```

   设置语音静音或者取消语音静音。
   根据 <code>uid</code> 区分为两种：
   
   * 当 <code>uid</code> 为自己时，将设置本地发送语音是否静音。**注意：不影响本地语音数据发送。**
   
   * 当 <code>uid</code> 为远端用户时，将设置是否播放其他用户的语音数据。**注意：不影响远端语音数据接收。**
   
   
  
 * 获取是否静音 
 
    ```java
    public abstract boolean audioStreamMuted(long uid);
    ```
    
   获取 <code>uid</code> 是否静音。
   


* 设置扬声器

    ```java
    public int setSpeaker(boolean speakerOn);
    ```

    设置当前语音播放是否采用扬声器或者听筒。
    
    
* 获取当前语音播放的方式

    ```java
    public boolean speakerEnabled();
    ```

    获取当前语音播放是否为扬声器或者听筒。
    
        


### <span id="视频操作">视频操作</span>

* 可选参数设置与获取接口

    ```java
    public abstract void setParameters(NRtcParameters params);
    ```

    ```java
    public abstract NRtcParameters getParameters(NRtcParameters params);
    ```

    设置，获取视频相关的参数。

    参数主要分为两类:

    **静态参数: 仅允许在加入频道前设置获取**

    **动态参数: 支持加入频道前设置获取，同时也支持通话过程中设置与获取**

    <code>NRtcParameters</code> 视频参数:

    * <code>KEY_DEVICE_DEFAULT_ROTATION</code> 默认情况下设备顺时针旋转角度, 静态参数。
    * <code>KEY_DEVICE_ROTATION_FIXED_OFFSET</code> 设备旋转角度修正偏移量, 静态参数。
    * <code>KEY_VIDEO_ENCODER_MODE</code> 视频编码模式, 运行时参数。
    * <code>KEY_VIDEO_DECODER_MODE</code> 视频解码模式, 运行时参数。
    * <code>KEY_VIDEO_SUPPORTED_HW_ENCODER</code> 视频是否支持硬件编码, 运行时参数。
    * <code>KEY_VIDEO_SUPPORTED_HW_DECODER</code> 视频是否支持硬件解码, 运行时参数。
    * <code>KEY_SERVER_VIDEO_RECORD</code> 视频服务器录制, 静态参数。
    * <code>KEY_VIDEO_QUALITY</code> 视频清晰度, 运行时参数。
    * <code>KEY_VIDEO_CROP_BEFORE_SEND</code> 视频发送前裁剪, 运行时参数。
    * <code>KEY_VIDEO_ROTATE_BEFORE_RENDING</code> 视频绘制前旋转, 运行时参数。
    * <code>KEY_VIDEO_DEFAULT_FRONT_CAMERA</code> 视频默认摄像头, 静态参数。
    * <code>KEY_VIDEO_FPS_REPORTED</code> 视频帧率汇报, 运行时参数。
    * <code>KEY_VIDEO_FRAME_RATE</code> 视频帧率, 静态参数。
    * <code>KEY_VIDEO_MAX_BITRATE</code> 视频最大码率, 运行时参数。
    * <code>KEY_VIDEO_FRAME_FILTER</code> 外部视频数据处理, 运行时参数。



* 设置视频画布

    ```java
    public abstract void setupVideoCanvas(long uid,
                                          NRtcVideoRender render,
                                          boolean mirror,
                                          int scalingType);
    ```

    动态设置视频的画布
 

* 切换自己的前置和后置摄像头

    ```java
    public int switchCamera();
    ```
    
   切换用户的摄像头。
   
   
* 获取当前设备是否拥有多个Camera

    ```java
    public boolean hasMultipleCameras();
    ```
    
    
* 获取当前会话是否正在使用前置摄像头

    ```java
    public boolean frontCameraIsUsing();
    ```


* 设置视频数据的发送和绘制

    ```java
    public int muteVideoStream(long uid, boolean muted);
    ```

    设置视频数据的发送和绘制。
    根据 <code>uid</code> 区分为两种：
   
    * 当 <code>uid</code> 为自己时，设置本地视频数据是否发送。**注意：不影响本地视频数据采集绘制。**
   
    * 当 <code>uid</code> 为远端用户时，将设置是否绘制远端用户的视频数据。**注意：不影响远端视频数据接收。**
    
  
  
* 获取视频是否发送和播放
 
    ```java
    public boolean videoStreamMuted(long uid);
    ```
    
   获取 <code>uid</code> 是否发送和绘制视频数据。  
   
   
   
* 视频时截图
 
    ```java
    public abstract boolean takeSnapshot(long uid);
    ```
    
   截取用户 <code>uid</code> 的画面。 **注意：视频卡顿会影响图像截取。**



### <span id="事件通知">事件通知</span>

用户需要在 <code>create</code> 中注册事件监听接口 <code>NRtcCallback</code> 。
    
* 自己成功加入频道

    ```java
    void onJoinedChannel(long channelId, String videoFile, String audioFile);
    ```

    用户 <code>joinChannel</code> 成功后，系统会通知成功加入频道。
    
    成功加入频道系统会返回分配给你的唯一 <code>channelId</code>， 如果开启了服务器录制语音功能，同时也会返回服务器录制视频和音频文件名。
    


* 自己成功离开频道

    ```java
    void onLeftChannel(SessionStats stats);
    ```  
    
    用户 <code>leaveChannel</code> 成功后，系统会通知成功离开频道。
    
    成功离开频道系统会返回此次通话的相关统计数据，现阶段主要包含客户端上下行流量(此流量和服务器统计流量不一样)。
  


* 会话成功建立

    ```java
    void onCallEstablished();
    ``` 
    
    会话建立，SDK即将开始发送语音或者视频数据。用户需要在回调触发时关闭所有的其他铃声等。
 


* 用户加入频道

    ```java
    void onUserJoined(long uid);
    ``` 
    
    当远端用户成功加入频道后通知。
    
    如果开启了视频通话，需要在回调中通过 <coed>getSurfaceRender</code> 回去用户对应的画布，然后动态添加到布局中。
  


* 用户离开频道

    ```java
    void onUserLeft(long uid, int event);
    ``` 
    
    远端用户离开了频道。
    
    重要参数：
    
    <code>event</code> 参考 <code>UserQuitType</code> ，主要分为用户正常退出和异常退出。
  


* 网络状态汇报

    ```java
    void onNetworkQuality(long user， int quality);
    ``` 

    系统在网络状态发生改变时会汇报。


* 其他用户静音通知

    ```java
    void onUserMuteAudio(long uid, boolean muted);
    ```  

    当远端用户语音静音时，SDK会通知。
   


* 其他用户关闭视频通知

    ```java
    void onUserMuteVideo(long uid, boolean muted);
    ``` 

    当远端用户设置视频数据是否发送时，SDK会通知。

 

* 其他用户通话模式改变通知

    ```java
    void onUserChangeMode(long uid, int mode);
    ```  
    
    当其他用户通话模式改变时(音频或者视频), 系统会通知。**注意：多人模式下忽略。**



* 系统网络断开或者重连通知

    ```java
    void onConnectionTypeChanged(int netType);
    ```  
    
   当系统网络连接状态发生改变时通知。



* 错误通知

    ```java
    void onError(int event, int code);
    ```  
    
    当 <code>NRTC</code> 内部出现错误时用户会收到此通知。
    
    <code>event</code> :
    
    <code>EVENT_RESERVE</code> 当用户 <code>joinChannel</code> 后可能会出现频道分配错误。
    
    <code>EVENT_JOIN</code> 当用户 <code>joinChannel</code> 后可能会出现连接频道服务器错误。
    
    <code>EVENT_LOCAL</code> 客户端本地错误。
    
   
* 设置状态通知

    ```java
    void onDeviceEvent(long channel, int event, String desc);
    ```  
    
    主要监听语音采集设备，视频采集设备。
    
    通过监听设备的状态来跟踪一些设备权限异常的情况。
    
    
* 远端用户本地录制通知

    ```java
    void onUserRecordStatusChange(long uid, boolean on);
    ```  
    
    当远端用户开启或者关闭数据录制后会收到通知。 **注意：远端用户仅仅录制他发送的本地数据。**
    
    
* 自己本地录制结束通知
  
    ```java
    void onRecordEnd(String[] files, int event);
    ```  

    当自己结束本地录制后，会收到此通知。
    
 
* 自己本地录制结束通知   

    ```java
    void onFirstVideoFrameAvailable(long uid);
    ```  

   当用户 <code>uid</code> 第一帧数据开始绘制前通知。
    
 
 
 * 绘制帧率通知 

    ```java
    void onVideoFpsReported(long uid, int fps);
    ```  

   用户 <code>uid</code> 的绘制帧率汇报。   
   
   
* 截图操作

    ```java
       void onTakeSnapshotResult(long uid, boolean success, String file);
    ```  

   截取用户 <code>uid</code> 图像后的结果通知。


* 采集视频数据回调

    ```java
       int onVideoFrameFilter(NRtcVideoFrame frame);
    ```  

   当用户开始外部视频处理后,采集到的视频数据通过次回调通知。 用户可以对视频数据做相应的美颜等不同的处理。 需要通过<code>setParameters</code>开启视频数据处理。


* 采集语音数据回调

    ```java
       int onAudioFrameFilter(NRtcAudioFrame frame);
    ```  

   当用户开始外部语音处理后,采集到的语音数据通过次回调通知。 用户可以对语音数据做相应的变声等不同的处理。需要通过<code>setParameters</code>开启语音数据处理。




### <span id="多人通话">多人通话</span>      

默认情况下<code>NRTC</code>按照双人模式运行，通过相关参数设置即可支持多人通话。

* 设置获取多人通话模式

    ```java
    public abstract void setParameters(NRtcParameters params);
    ```

    ```java
    public abstract NRtcParameters getParameters(NRtcParameters params);
    ```

    参数:

    * <code>KEY_SESSION_MULTI_MODE</code> 是否多人通话模式, 静态参数。


* 用户角色设置

    ```java
    public boolean setRole(int role);
    ```

    ```java
    public abstract void setParameters(NRtcParameters params);
    ```

    <code>NRtcParameters</code>参数:

    * <code>KEY_SESSION_MULTI_MODE_USER_ROLE</code> 用户角色, 静态参数。

    用户角色身份主要有两种:
    
    > `普通角色`: 用户为普通角色时，能够正常的发送和接收数据。
    
    > `观众角色`: 用户为观众角色时，所有的语音和视频数据的采集和发送会关闭，允许接收和播放远端其他用户的数据。
    
    角色设置仅在多人会话时生效。**注意:用户角色和Mute区别。观众角色会影响本地数据的采集、处理和发送等; 本地的 <code>Mute</code>只会影响发送。 **
    
   
* 获取自己的频道角色

    ```java
    public int getRole();
    ```

    ```java
    public abstract NRtcParameters getParameters(NRtcParameters params);
    ```

    <code>NRtcParameters</code>参数:

    * <code>KEY_SESSION_MULTI_MODE_USER_ROLE</code> 用户角色, 静态参数。
 


### <span id="互动直播">互动直播</span>    

在多人通话模式下，<code>NRTC</code>支持互动直播功能，多人模式下的操作在互动直播中都适用。

目前互动直播支持一个主播和一个连麦观众。


* 在多人模式设置获取互动直播参数

    ```java
    public abstract void setParameters(NRtcParameters params);
    ```

    ```java
    public abstract NRtcParameters getParameters(NRtcParameters params);
    ```

    <code>NRtcParameters</code>参数:

    * <code>KEY_SESSION_LIVE_MODE</code> 进入房间开启互动直播需要设置此参数，不论是主播还是连麦观众都需要设置。


* 设置和更新推流地址

    ```java
    public abstract void setParameters(NRtcParameters params);
    ```

    <code>NRtcParameters</code>参数:

    * <code>KEY_SESSION_LIVE_URL</code> 直播推流url。如果作为主播，必须设置推流url， 如果是连麦观众，则不能设置 url。



### <span id="其他">其他</span>


* 网络代理设置

    ```java
    public void setNetworkProxy(NRtcNetworkProxy proxy);
    ```

   网络代理设置,目前仅支持SOCKS5代理。


* 通话模式切换

    ```java
    public int setChannelMode(int mode);
    ```

   SDK 支持用户从视频通话切换到语音通话，同时也能够从一通语音通话切换到视频通话。**注意：多人会话不能切换模式。**


* 打开本地发送数据录制

    ```java
    public boolean startLocalRecording();
    ```

   录制本地发送的语音和视频数据。**注意：录制双方数据可以采用服务器录制。**
    
    
* 关闭本地发送数据录制

    ```java
    public void stopLocalRecording();
    ```

   关闭本地发送数据录制。
   
   
* 获取是否正在录制本地发送数据

    ```java
    public boolean isLocalRecording();
    ```

* SDK 版本

    ```java
    public static NRtcVersion version(); 
    ```
    
    
* SDK 权限检查

    ```java
    public static List<String> checkPermission(Context context);
    ```
    
   在Android 6.0 及以上设备运行SDK前可以检测缺失权限。
   
   
   
   
## <span id="API文档">API 文档</span>

* [在线文档](http://dev.netease.im/doc/android_nrtc/ "target=_blank")
    
    
    