package com.netease.nrtc.demo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nrtc.demo.settings.SettingActivity;
import com.netease.nrtc.effect.video.GPUImage;
import com.netease.nrtc.sdk.NRtc;
import com.netease.nrtc.sdk.NRtcCallback;
import com.netease.nrtc.sdk.NRtcConstants;
import com.netease.nrtc.sdk.NRtcParameters;
import com.netease.nrtc.sdk.NRtcSessionStats;
import com.netease.nrtc.sdk.NRtcVideoRender;
import com.netease.nrtc.sdk.common.NRtcAudioFrame;
import com.netease.nrtc.sdk.common.NRtcVideoFrame;
import com.visionin.core.VSRawBytesCallback;
import com.visionin.core.VSVideoFrame;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by liuqijun on 3/30/16.
 */
public class ChatActivity extends NRtcActivity implements SurfaceHolder.Callback, NRtcCallback, View.OnClickListener {

    private static final String TAG = "Chat";

    private static final int LOCAL_X = 72;
    private static final int LOCAL_Y = 3;
    private static final int LOCAL_WIDTH = 25;
    private static final int LOCAL_HEIGHT = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;

    public static void launch(Context context, long uid,
                              String channel, String token,
                              boolean videoEnabled, boolean multi, boolean audience) {

        Intent intent = new Intent();
        intent.putExtra("uid", uid);
        intent.putExtra("channel", channel);
        intent.putExtra("videoEnabled", videoEnabled);
        intent.putExtra("token", token);
        intent.putExtra("multi", multi);
        intent.putExtra("audience", audience);
        intent.setClass(context, ChatActivity.class);
        context.startActivity(intent);

    }

    //用户传递参数
    private long uid;
    private String channelName;
    private boolean videoEnabled;
    private String token;
    private boolean multi;
    private boolean audience;

    public Handler mHandler = new Handler();

    private TextView roomInfo;
    private TextView localFps;
    private TextView remoteFps;
    private TextView localResolution;
    private TextView remoteResolution;

    //layout
    private PercentFrameLayout smallPreview;
    private PercentFrameLayout largePreview;

    private Button muteAudio;
    private Button muteVideo;
    private Button switchCamera;
    private Button switchRender;
    private Button switchMode;
    private Button localRecord;
    private Button speaker;
    private Button setting;
    private Button videoFilter;
    private Button audioFilter;

    private SurfaceView surfaceView = null;
    SurfaceHolder surfaceHolder = null;

    private NRtc nrtc;

    private long remoteId;
    private NRtcVideoRender remoteRender;
    private NRtcVideoRender localRender;
    private SurfaceView captureView;

    private boolean joinedChannel = false;

    private long mSessionTimeMs;

    private View mControlPanel;
    private View mRootView;

    private VSVideoFrame videoFrame;

    private Map<Long, View> userRenders = new HashMap<>();

    private byte[] mVideoDate;

    private GPUImage mVideoEffect;

    private void setScreenOnFlag() {
        final int keepScreenOnFlag =
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        Window w = getWindow();
        w.getAttributes().flags |= keepScreenOnFlag;
        w.addFlags(keepScreenOnFlag);
    }

    private boolean useSystemPreviewDisplay() {
        return captureView != null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenOnFlag();
        setContentView(R.layout.chat_layout);

        configFromIntent(getIntent());

        componentFromLayout();

        resetControl();



        surfaceView = (SurfaceView) findViewById(R.id.camera_surfaceView);
        surfaceHolder = surfaceView.getHolder();

//        mVideoEffect = GPUImage.create(this);
//        GPUImageFilterGroup filters = new GPUImageFilterGroup();
//        filters.addFilter(new GPUImageBilateralFilter(6f));
//        filters.addFilter(new GPUImageBrightnessFilter(0.1f));
//        filters.addFilter(new GPUImageSaturationFilter(1.0f));
//        filters.addFilter(new GPUImageContrastFilter(1.5f)); //1.2f ?
//        mVideoEffect.setFilter(filters);
        //filters.addFilter(new GPUImageBrightnessFilter(0.1f));  //0.1f ?
        //filters.addFilter(new GPUImageSaturationFilter(1.1f));  //1.1f ?
        //filters.addFilter(new GPUImageContrastFilter(1.2f)); //1.2f ?

        nrtc = NRtc.create(this, this);

        NRtcParameters parameters = getRtcParameters();
        parameters.setBoolean(NRtcParameters.KEY_SESSION_MULTI_MODE, multi);
        parameters.setInteger(NRtcParameters.KEY_SESSION_MULTI_MODE_USER_ROLE,
                audience ? NRtcConstants.UserRole.AUDIENCE : NRtcConstants.UserRole.NORMAL);
        nrtc.setParameters(parameters);


        parameters.clear();
        parameters.setRequestKey("key_monster_supported");
        parameters = nrtc.getParameters(parameters);
        if (parameters.getBoolean("key_monster_supported")) {
            parameters.clear();
            parameters.setBoolean("key_monster_mode", true);
            nrtc.setParameters(parameters);
        }

        parameters.clear();
        parameters.setRequestKey("key_video_system_preview");
        parameters = nrtc.getParameters(parameters);
        if (parameters.getBoolean("key_video_system_preview")) {
            captureView = new SurfaceView(this);
            captureView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            parameters.clear();
            parameters.setObject("key_video_preview_display", captureView);
            nrtc.setParameters(parameters);
        }


        nrtc.setNetworkProxy(networkProxy);

        nrtc.joinChannel(Config.APP_KEY, token, channelName, uid,
                videoEnabled ? NRtcConstants.RtcMode.VIDEO : NRtcConstants.RtcMode.AUDIO);

        mSessionTimeMs = SystemClock.elapsedRealtime();

        updateRoomInfo();
    }

    private void updateRoomInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("channel: ").append(channelName).append("\n");
        sb.append("uid: ").append(uid);
        roomInfo.setText(sb.toString());
    }

    private void updateLocalResolution(String string) {
        if (localResolution.getVisibility() == View.GONE) {
            localResolution.setVisibility(View.VISIBLE);
        }
        localResolution.setText("local res: " + string);
    }


    private void updateRemoteResolution(String string) {
        if (remoteResolution.getVisibility() == View.GONE) {
            remoteResolution.setVisibility(View.VISIBLE);
        }
        remoteResolution.setText("remote res: " + string);
    }

    private void updateLocalFps(int fps) {
        if (localFps.getVisibility() == View.GONE) {
            localFps.setVisibility(View.VISIBLE);
        }
        localFps.setText("local fps: " + fps);
    }

    private void updateRemoteFps(int fps) {
        if (remoteFps.getVisibility() == View.GONE) {
            remoteFps.setVisibility(View.VISIBLE);
        }
        remoteFps.setText("remote fps: " + fps);
    }

    @Override
    protected void onResume() {
        super.onResume();
        surfaceHolder.addCallback(this);
        largePreview.setPosition(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT);
        smallPreview.setPosition(LOCAL_X, LOCAL_Y, LOCAL_WIDTH, LOCAL_HEIGHT);
    }

    private void resetControl() {
        muteAudio.setEnabled(true);
        muteVideo.setEnabled(videoEnabled);
        switchCamera.setEnabled(false);
        switchRender.setEnabled(false);
        switchMode.setEnabled(false);
        localRecord.setEnabled(false);
        speaker.setEnabled(!videoEnabled);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void configFromIntent(Intent intent) {
        uid = intent.getLongExtra("uid", 100);
        channelName = intent.getStringExtra("channel");
        videoEnabled = intent.getBooleanExtra("videoEnabled", false);
        token = intent.getStringExtra("token");
        multi = intent.getBooleanExtra("multi", false);
        audience = intent.getBooleanExtra("audience", false);
    }


    private boolean mControlPanelShow = true;


    private void componentFromLayout() {

        roomInfo = (TextView) findViewById(R.id.room_uid);
        localFps = (TextView) findViewById(R.id.local_fps);
        remoteFps = (TextView) findViewById(R.id.remote_fps);
        localResolution = (TextView) findViewById(R.id.local_resolution);
        remoteResolution = (TextView) findViewById(R.id.remote_resolution);

        remoteRender = new NRtcVideoRender(this);
        localRender = new NRtcVideoRender(this);

        remoteRender.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        localRender.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        mControlPanel = findViewById(R.id.control_panel);
        mRootView = findViewById(R.id.root_view);

        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mControlPanelShow) {
                    mControlPanel.animate().translationY(mControlPanel.getHeight());
                    mControlPanelShow = false;
                } else {
                    mControlPanel.animate().translationY(0);
                    mControlPanelShow = true;
                }

            }
        });

        smallPreview = (PercentFrameLayout) findViewById(R.id.small_size_preview);
        largePreview = (PercentFrameLayout) findViewById(R.id.large_size_preview);
        muteAudio = (Button) findViewById(R.id.mute_audio_btn);
        muteVideo = (Button) findViewById(R.id.mute_video_btn);
        switchCamera = (Button) findViewById(R.id.switch_camera_btn);
        switchRender = (Button) findViewById(R.id.switch_render_btn);
        switchMode = (Button) findViewById(R.id.switch_mode_btn);
        speaker = (Button) findViewById(R.id.speaker_btn);
        localRecord = (Button) findViewById(R.id.recorder_btn);
        setting = (Button) findViewById(R.id.settings);
        videoFilter = (Button) findViewById(R.id.video_filter_btn);
        audioFilter = (Button) findViewById(R.id.audio_filter_btn);

        setting.setOnClickListener(this);
        muteAudio.setOnClickListener(this);
        muteVideo.setOnClickListener(this);
        switchCamera.setOnClickListener(this);
        switchRender.setOnClickListener(this);
        switchMode.setOnClickListener(this);
        speaker.setOnClickListener(this);
        localRecord.setOnClickListener(this);
        videoFilter.setOnClickListener(this);
        audioFilter.setOnClickListener(this);

    }


    private void log(int level, String message) {

        switch (level) {
            case Log.DEBUG:
                Log.d(TAG, message);
                break;
            case Log.INFO:
                Log.i(TAG, message);
                break;
            case Log.WARN:
                Log.w(TAG, message);
                break;
            case Log.ERROR:
                Log.e(TAG, message);
                break;
        }

    }


    private void leave() {
        nrtc.leaveChannel();
    }

    @Override
    public void onBackPressed() {
        if (joinedChannel)
            leave();
        else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {

        Map<String, Object> attributes = new HashMap<>();
        long sessionLength = SystemClock.elapsedRealtime() - mSessionTimeMs;
        attributes.put("Duration", (int) (sessionLength / 1000));
        LogEvent.logEvent(this, "Call", attributes);

        super.onDestroy();

        //mVideoEffect.dispose();
        try {
            nrtc.dispose();
        } catch (Exception e) {
            //调用流程出现了问题
            e.printStackTrace();
        }

    }

    @Override
    public void onJoinedChannel(long channelId, String videoFile, String audioFile) {
        log(Log.INFO, "onJoinedChannel->" + channelId);
        joinedChannel = true;
    }

    @Override
    public void onLeftChannel(NRtcSessionStats stats) {

        log(Log.INFO, "onLeaveChannel. " + "RX->" + stats.trafficStat.RX + ", TX->" + stats.trafficStat.TX);

        finish();
    }

    @Override
    public void onError(int event, int code) {


        log(Log.ERROR, "error->" + event + "#" + code);

        Toast.makeText(this, event + "#" + code, Toast.LENGTH_SHORT).show();

        leave();

    }

    @Override
    public void onDeviceEvent(int event, String desc) {
        log(Log.WARN, "device->" + "(" + event + "," + desc + ")");
    }

    @Override
    public void onVideoCapturerStarted(boolean success) {

    }

    @Override
    public void onVideoCapturerStopped() {

    }

    @Override
    public void onCallEstablished() {
        log(Log.INFO, "onCallEstablished");

        if (useSystemPreviewDisplay()) {
            userRenders.put(uid, captureView);
        } else {
            userRenders.put(uid, localRender);
            nrtc.setupVideoCanvas(uid, (NRtcVideoRender) userRenders.get(uid), false, scalingType);
        }

        if (nrtc.getChannelMode() == NRtcConstants.RtcMode.VIDEO) {

            if (nrtc.hasMultipleCameras()) {
                switchCamera.setEnabled(true);
            }

            SurfaceView view = (SurfaceView) userRenders.get(uid);
            smallPreview.removeAllViews();
            smallPreview.addView(view);
            view.setZOrderMediaOverlay(true);
            view.setZOrderOnTop(true);
            view.setTag(smallPreview);

        }

        localRecord.setEnabled(true);
        switchMode.setEnabled(true);

    }

    @Override
    public void onUserJoined(long uid) {

        if (remoteId == uid) return;

        remoteId = uid;

        userRenders.put(uid, remoteRender);

        nrtc.setupVideoCanvas(uid, (NRtcVideoRender) userRenders.get(uid), false, scalingType);

        if (nrtc.getChannelMode() == NRtcConstants.RtcMode.VIDEO) {

            largePreview.removeAllViews();
            largePreview.addView(remoteRender);
            remoteRender.setZOrderOnTop(false);
            remoteRender.setZOrderMediaOverlay(false);
            remoteRender.setTag(largePreview);

            if (useSystemPreviewDisplay()) {
                switchRender.setEnabled(false);
            } else {
                switchRender.setEnabled(true);
            }

        }
    }

    @Override
    public void onUserLeft(long uid, int event) {
        remoteId = 0;

        switchRender.setEnabled(false);
        PercentFrameLayout view = (PercentFrameLayout) remoteRender.getTag();
        if (view != null)
            view.removeAllViews();

    }

    @Override
    public void onNetworkQuality(long user, int quality) {
    }

    @Override
    public void onUserMuteAudio(long uid, boolean muted) {
        log(Log.WARN, "audio muted-> " + uid + "#" + muted);
    }

    @Override
    public void onUserMuteVideo(long uid, boolean muted) {
        log(Log.WARN, "video muted->" + uid + "#" + muted);
    }

    @Override
    public void onUserChangeMode(long uid, int mode) {
        log(Log.WARN, "mode change->" + uid + "#" + mode);
    }

    @Override
    public void onConnectionTypeChanged(int newConnectionType) {
        log(Log.WARN, "connection change : " + newConnectionType);
    }

    @Override
    public void onUserRecordStatusChange(long uid, boolean on) {
        log(Log.WARN, "record change->" + uid + "->" + on);
    }

    @Override
    public void onRecordEnd(String[] files, int event) {
        log(Log.WARN, "record end ->" + event);
    }

    @Override
    public void onFirstVideoFrameAvailable(long channel) {
        log(Log.WARN, "first video available :" + channel);
    }

    private int videoEncodeMode() {
        NRtcParameters parameters = new NRtcParameters();
        parameters.setRequestKey(NRtcParameters.KEY_VIDEO_SUPPORTED_HW_ENCODER);
        parameters = nrtc.getParameters(parameters);
        if (parameters.getBoolean(NRtcParameters.KEY_VIDEO_SUPPORTED_HW_ENCODER)) {
            parameters.clear();
            parameters.setRequestKey(NRtcParameters.KEY_VIDEO_ENCODER_MODE);
            parameters = nrtc.getParameters(parameters);
            if (NRtcParameters.MEDIA_CODEC_SOFTWARE.equals(parameters.getString(NRtcParameters.KEY_VIDEO_ENCODER_MODE))) {
                return 1;
            } else {
                return 2;
            }
        } else {
            return -1;
        }
    }


    private int videoDecodeMode() {
        NRtcParameters parameters = new NRtcParameters();
        parameters.setRequestKey(NRtcParameters.KEY_VIDEO_SUPPORTED_HW_DECODER);
        parameters = nrtc.getParameters(parameters);
        if (parameters.getBoolean(NRtcParameters.KEY_VIDEO_SUPPORTED_HW_DECODER)) {

            parameters.clear();
            parameters.setRequestKey(NRtcParameters.KEY_VIDEO_DECODER_MODE);
            parameters = nrtc.getParameters(parameters);
            if (NRtcParameters.MEDIA_CODEC_SOFTWARE.equals(parameters.getString(NRtcParameters.KEY_VIDEO_DECODER_MODE))) {
                return 1;
            } else {
                return 2;
            }
        } else {
            return -1;
        }
    }

    private int videoQuality() {
        NRtcParameters parameters = new NRtcParameters();
        parameters.setRequestKey(NRtcParameters.KEY_VIDEO_QUALITY);
        parameters = nrtc.getParameters(parameters);
        return parameters.getInteger(NRtcParameters.KEY_VIDEO_QUALITY);
    }

    private int videoMaxBitrate() {
        NRtcParameters parameters = new NRtcParameters();
        parameters.setRequestKey(NRtcParameters.KEY_VIDEO_MAX_BITRATE);
        parameters = nrtc.getParameters(parameters);
        return parameters.getInteger(NRtcParameters.KEY_VIDEO_MAX_BITRATE);
    }

    @Override
    public void onVideoFpsReported(long channel, int fps) {
        if (channel == uid) {
            updateLocalFps(fps);
        } else {
            updateRemoteFps(fps);
        }
    }

    @Override
    public void onFirstVideoFrameRendered(long uid) {

    }

    @Override
    public void onVideoFrameResolutionChanged(long uid, int videoWidth, int videoHeight, int rotation) {
        log(Log.DEBUG, "onVideoFrameResolutionChanged->" + videoWidth + "x" + videoHeight);
        if (this.uid == uid) {
            updateLocalResolution(videoWidth + "x" + videoHeight);
        } else {
            updateRemoteResolution(videoWidth + "x" + videoHeight);
        }
    }

    @Override
    public void onTakeSnapshotResult(long channel, boolean success, String file) {
        log(Log.DEBUG, "snapshot->" + channel + "#" + success);
    }

    @Override
    public int onVideoFrameFilter(NRtcVideoFrame frame) {
        //默认都是转换成I420
        frame.format = NRtcVideoFrame.ImageFormat.NV21;
        processDate(frame);
        if(mVideoDate!=null) {
            frame.data.flip();
            frame.data.put(mVideoDate);
        }
        return 0;
    }

    @Override
    public int onAudioFrameFilter(NRtcAudioFrame frame) {
        return 0;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.settings: {
                SettingActivity.launch(this,
                        videoEncodeMode(),
                        videoDecodeMode(),
                        videoQuality(),
                        videoMaxBitrate());
            }
            break;
            case R.id.mute_audio_btn: {
                nrtc.muteAudioStream(uid, !nrtc.audioStreamMuted(uid));

                muteAudio.setText(nrtc.audioStreamMuted(uid) ? "打开语音" : "关闭语音");
            }
            break;
            case R.id.mute_video_btn: {

                nrtc.muteVideoStream(uid, !nrtc.videoStreamMuted(uid));

                muteVideo.setText(nrtc.videoStreamMuted(uid) ? "打开视频" : "关闭视频");

            }
            break;
            case R.id.switch_camera_btn: {

                nrtc.switchCamera();

            }
            break;
            case R.id.switch_render_btn: {
                switchRender();
            }
            break;
            case R.id.switch_mode_btn: {

                switchMode(nrtc.getChannelMode() == NRtcConstants.RtcMode.VIDEO ?
                        NRtcConstants.RtcMode.AUDIO : NRtcConstants.RtcMode.VIDEO);

            }
            break;
            case R.id.speaker_btn: {

                nrtc.setSpeaker(!nrtc.speakerEnabled());

                speaker.setText(nrtc.speakerEnabled() ? "关闭扬声器" : "打开扬声器");

            }
            break;
            case R.id.recorder_btn: {

                if (!nrtc.isLocalRecording()) {
                    nrtc.startLocalRecording();
                } else {
                    nrtc.stopLocalRecording();
                }

                localRecord.setText(nrtc.isLocalRecording() ? "关闭录制" : "打开录制");

            }
            break;
            case R.id.video_filter_btn: {

                boolean on = false;
                NRtcParameters parameters = new NRtcParameters();
                parameters.setRequestKey(NRtcParameters.KEY_VIDEO_FRAME_FILTER);
                parameters = nrtc.getParameters(parameters);
                if (parameters != null && parameters.containsKey(NRtcParameters.KEY_VIDEO_FRAME_FILTER)) {
                    on = parameters.getBoolean(NRtcParameters.KEY_VIDEO_FRAME_FILTER);
                    parameters.clear();
                    parameters.setBoolean(NRtcParameters.KEY_VIDEO_FRAME_FILTER, !on);
                    nrtc.setParameters(parameters);
                }

                videoFilter.setText(on ? "打开美颜" : "关闭美颜");


            }
            break;
            case R.id.audio_filter_btn: {
                boolean on = false;
                NRtcParameters parameters = new NRtcParameters();
                parameters.setRequestKey(NRtcParameters.KEY_AUDIO_FRAME_FILTER);
                parameters = nrtc.getParameters(parameters);
                if (parameters != null && parameters.containsKey(NRtcParameters.KEY_AUDIO_FRAME_FILTER)) {
                    on = parameters.getBoolean(NRtcParameters.KEY_AUDIO_FRAME_FILTER);
                    parameters.clear();
                    parameters.setBoolean(NRtcParameters.KEY_AUDIO_FRAME_FILTER, !on);
                    nrtc.setParameters(parameters);
                }

                audioFilter.setText(on ? "打开变声" : "关闭变声");
            }
            break;
        }


    }

    private void switchRender() {
        if (useSystemPreviewDisplay()) {
            return;
        }
        //先在本地Map中交换
        NRtcVideoRender render = (NRtcVideoRender) userRenders.get(uid);
        userRenders.put(uid, userRenders.get(remoteId));
        userRenders.put(remoteId, render);
        //断开SDK视频绘制画布
        nrtc.setupVideoCanvas(uid, null, false, 0);
        nrtc.setupVideoCanvas(remoteId, null, false, 0);
        //重新关联上画布
        nrtc.setupVideoCanvas(uid, (NRtcVideoRender) userRenders.get(uid), false, scalingType);
        nrtc.setupVideoCanvas(remoteId, (NRtcVideoRender) userRenders.get(remoteId), false, scalingType);
    }

    private void switchMode(int mode) {

        if (nrtc.getChannelMode() == mode) return;

        nrtc.setChannelMode(mode);

        if (nrtc.getChannelMode() == NRtcConstants.RtcMode.VIDEO) {

            smallPreview.removeAllViews();
            smallPreview.addView(localRender);
            localRender.setTag(smallPreview);

            largePreview.removeAllViews();
            largePreview.addView(remoteRender);
            remoteRender.setTag(largePreview);

            muteAudio.setEnabled(true);
            muteVideo.setEnabled(true);
            if (nrtc.hasMultipleCameras()) {
                switchCamera.setEnabled(true);
            }
            if (useSystemPreviewDisplay()) {
                switchRender.setEnabled(false);
            } else {
                switchRender.setEnabled(true);
            }
            switchMode.setEnabled(true);
            localRecord.setEnabled(true);
            speaker.setEnabled(false);


        } else {
            smallPreview.removeAllViews();
            largePreview.removeAllViews();
            localRender.setTag(null);
            remoteRender.setTag(null);

            muteAudio.setEnabled(true);
            muteVideo.setEnabled(false);
            switchCamera.setEnabled(false);
            switchRender.setEnabled(false);
            switchMode.setEnabled(true);
            localRecord.setEnabled(true);
            speaker.setEnabled(true);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SettingActivity.requestCode) {
            if (data != null) {
                Map<String, Object> newSettings = (Map<String, Object>) data.getSerializableExtra("settings");
                if (newSettings != null && nrtc != null) {
                    NRtcParameters parameters = new NRtcParameters();
                    for (Map.Entry<String, Object> entry : newSettings.entrySet()) {
                        parameters.setObject(entry.getKey(), entry.getValue());
                    }
                    nrtc.setParameters(parameters);
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            videoFrame = new VSVideoFrame(surfaceHolder.getSurface());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        videoFrame.setCameraPosition(VSVideoFrame.CAMERA_FACING_FRONT);
        videoFrame.setMirrorFrontVideo(true);
        videoFrame.setMirrorFrontPreview(true);
        videoFrame.setSmoothLevel(1.0f);
        videoFrame.setBrightenLevel(1.0f);
        videoFrame.setToningLevel(1.0f);
        videoFrame.setNV12Callback(new VSRawBytesCallback() {
            @Override
            public void outputBytes(byte[] bytes) {
                mVideoDate = bytes;
            }
        });

        videoFrame.start(/*getAssets()*/);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void processDate(final NRtcVideoFrame frame){
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                videoFrame.setOutputSize(frame.width,frame.height);
                videoFrame.setVideoSize(frame.width,frame.height);
                videoFrame.makeCurrent();
                videoFrame.processBytes(frame.data.array(),frame.width,frame.height,videoFrame.GPU_NV21);

            }
        });

    }
}
