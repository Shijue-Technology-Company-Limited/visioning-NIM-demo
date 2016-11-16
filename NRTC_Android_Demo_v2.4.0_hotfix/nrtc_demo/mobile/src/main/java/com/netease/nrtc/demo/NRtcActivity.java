package com.netease.nrtc.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.netease.nrtc.sdk.NRtcNetworkProxy;
import com.netease.nrtc.sdk.NRtcParameters;

/**
 * Created by liuqijun on 6/14/16.
 */
public class NRtcActivity extends AppCompatActivity {

    //全局设置参数
    private boolean videoAutoCrop;
    private boolean videoAutoRotate;
    private int videoQuality;
    private boolean serverRecordAudio;
    private boolean serverRecordVideo;
    private boolean defaultFrontCamera;
    private boolean autoCallProximity;
    private int videoHwEncoderMode;
    private int videoHwDecoderMode;
    private boolean videoFpsReported;
    private int audioEffectAecMode;
    private int audioEffectAgcMode;
    private int audioEffectNsMode;
    private int videoMaxBitrate;
    private int deviceDefaultRotation;
    private int deviceRotationOffset;
    protected int scalingType;
    protected boolean live;
    protected String liveUrl;
    private int videoFrameRate;
    protected NRtcNetworkProxy networkProxy;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configFromPreference(PreferenceManager.getDefaultSharedPreferences(this));
    }


    private void configFromPreference(SharedPreferences preferences) {
        videoAutoCrop = preferences.getBoolean(getString(R.string.setting_vie_crop_key), true);
        videoAutoRotate = preferences.getBoolean(getString(R.string.setting_vie_rotation_key), true);
        videoQuality = Integer.parseInt(preferences.getString(getString(R.string.setting_vie_quality_key), 0 + ""));
        serverRecordAudio = preferences.getBoolean(getString(R.string.setting_other_server_record_audio_key), false);
        serverRecordVideo = preferences.getBoolean(getString(R.string.setting_other_server_record_video_key), false);
        defaultFrontCamera = preferences.getBoolean(getString(R.string.setting_vie_default_front_camera_key), true);
        autoCallProximity = preferences.getBoolean(getString(R.string.setting_voe_call_proximity_key), true);
        videoHwEncoderMode = Integer.parseInt(preferences.getString(getString(R.string.setting_vie_hw_encoder_key), 0 + ""));
        videoHwDecoderMode = Integer.parseInt(preferences.getString(getString(R.string.setting_vie_hw_decoder_key), 0 + ""));
        videoFpsReported = preferences.getBoolean(getString(R.string.setting_vie_fps_reported_key), true);
        audioEffectAecMode = Integer.parseInt(preferences.getString(getString(R.string.setting_voe_audio_aec_key), 2 + ""));
        audioEffectAgcMode = Integer.parseInt(preferences.getString(getString(R.string.setting_voe_audio_agc_key), 2 + ""));
        audioEffectNsMode = Integer.parseInt(preferences.getString(getString(R.string.setting_voe_audio_ns_key), 2 + ""));
        scalingType = Integer.parseInt(preferences.getString(getString(R.string.setting_vie_frame_scale_type_key), 0 + ""));
        live = preferences.getBoolean(getString(R.string.setting_live_status_key), false);
        liveUrl = preferences.getString(getString(R.string.setting_live_url_key), "");
        videoFrameRate = Integer.parseInt(preferences.getString(getString(R.string.setting_vie_frame_rate_key), 15 + ""));

        String value1 = preferences.getString(getString(R.string.setting_vie_max_bitrate_key), 0 + "");
        videoMaxBitrate = Integer.parseInt(TextUtils.isDigitsOnly(value1) && !TextUtils.isEmpty(value1) ? value1 : 0 + "");
        String value2 = preferences.getString(getString(R.string.setting_other_device_default_rotation_key), 0 + "");
        deviceDefaultRotation = Integer.parseInt(TextUtils.isDigitsOnly(value2) && !TextUtils.isEmpty(value2) ? value2 : 0 + "");
        String value3 = preferences.getString(getString(R.string.setting_other_device_rotation_fixed_offset_key), 0 + "");
        deviceRotationOffset = Integer.parseInt(TextUtils.isDigitsOnly(value3) && !TextUtils.isEmpty(value3) ? value3 : 0 + "");


        //网络代理
        boolean proxy = preferences.getBoolean(getString(R.string.setting_net_proxy_key), false);
        if(proxy) {
            String userName = preferences.getString(getString(R.string.setting_net_proxy_user_name_key), "");
            String password = preferences.getString(getString(R.string.setting_net_proxy_user_password_key), "");
            String host = preferences.getString(getString(R.string.setting_net_proxy_addr_ip_key), "");
            String port = preferences.getString(getString(R.string.setting_net_proxy_addr_port_key), "");

            networkProxy = new NRtcNetworkProxy();
            networkProxy.scheme = NRtcNetworkProxy.SOCKS5;
            if (TextUtils.isEmpty(host) || TextUtils.isEmpty(port) || !port.matches("[0-9]+")) {
                preferences.edit().putBoolean(getString(R.string.setting_net_proxy_key), false).apply();
                Toast.makeText(this, "host or port is wrong，无法启用代理", Toast.LENGTH_SHORT).show();
                return;
            }
            networkProxy.host = host;
            networkProxy.port = Integer.parseInt(port);
            networkProxy.userName = userName;
            networkProxy.userPassword = password;
        }
    }


    protected NRtcParameters getRtcParameters() {

        NRtcParameters parameters = new NRtcParameters();
        parameters.setInteger(NRtcParameters.KEY_VIDEO_QUALITY, videoQuality);
        parameters.setBoolean(NRtcParameters.KEY_VIDEO_DEFAULT_FRONT_CAMERA, defaultFrontCamera);
        parameters.setBoolean(NRtcParameters.KEY_AUDIO_CALL_PROXIMITY, autoCallProximity);
        parameters.setBoolean(NRtcParameters.KEY_SERVER_AUDIO_RECORD, serverRecordAudio);
        parameters.setBoolean(NRtcParameters.KEY_SERVER_VIDEO_RECORD, serverRecordVideo);
        parameters.setBoolean(NRtcParameters.KEY_VIDEO_CROP_BEFORE_SEND, videoAutoCrop);
        parameters.setBoolean(NRtcParameters.KEY_VIDEO_ROTATE_BEFORE_RENDING, videoAutoRotate);
        parameters.setBoolean(NRtcParameters.KEY_VIDEO_FPS_REPORTED, videoFpsReported);
        parameters.setInteger(NRtcParameters.KEY_DEVICE_DEFAULT_ROTATION, deviceDefaultRotation);
        parameters.setInteger(NRtcParameters.KEY_DEVICE_ROTATION_FIXED_OFFSET, deviceRotationOffset);
        parameters.setBoolean(NRtcParameters.KEY_SESSION_LIVE_MODE, live);
        parameters.setString(NRtcParameters.KEY_SESSION_LIVE_URL, liveUrl);
        parameters.setInteger(NRtcParameters.KEY_VIDEO_FRAME_RATE, videoFrameRate);

        switch (videoHwEncoderMode) {
            case 0:
                parameters.setString(NRtcParameters.KEY_VIDEO_ENCODER_MODE,
                        NRtcParameters.MEDIA_CODEC_AUTO);
                break;
            case 1:
                parameters.setString(NRtcParameters.KEY_VIDEO_ENCODER_MODE,
                        NRtcParameters.MEDIA_CODEC_SOFTWARE);
                break;
            case 2:
                parameters.setString(NRtcParameters.KEY_VIDEO_ENCODER_MODE,
                        NRtcParameters.MEDIA_CODEC_HARDWARE);
                break;
        }

        switch (videoHwDecoderMode) {
            case 0:
                parameters.setString(NRtcParameters.KEY_VIDEO_DECODER_MODE,
                        NRtcParameters.MEDIA_CODEC_AUTO);
                break;
            case 1:
                parameters.setString(NRtcParameters.KEY_VIDEO_DECODER_MODE,
                        NRtcParameters.MEDIA_CODEC_SOFTWARE);
                break;
            case 2:
                parameters.setString(NRtcParameters.KEY_VIDEO_DECODER_MODE,
                        NRtcParameters.MEDIA_CODEC_HARDWARE);
                break;
        }

        switch (audioEffectAecMode) {
            case 0:
                parameters.setString(NRtcParameters.KEY_AUDIO_EFFECT_ACOUSTIC_ECHO_CANCELER,
                        NRtcParameters.AUDIO_EFFECT_MODE_DISABLE);
                break;
            case 1:
                parameters.setString(NRtcParameters.KEY_AUDIO_EFFECT_ACOUSTIC_ECHO_CANCELER,
                        NRtcParameters.AUDIO_EFFECT_MODE_SDK_BUILTIN_PRIORITY);
                break;
            case 2:
                parameters.setString(NRtcParameters.KEY_AUDIO_EFFECT_ACOUSTIC_ECHO_CANCELER,
                        NRtcParameters.AUDIO_EFFECT_MODE_PLATFORM_BUILTIN_PRIORITY);
                break;
        }


        switch (audioEffectAgcMode) {
            case 0:
                parameters.setString(NRtcParameters.KEY_AUDIO_EFFECT_AUTOMATIC_GAIN_CONTROL,
                        NRtcParameters.AUDIO_EFFECT_MODE_DISABLE);
                break;
            case 1:
                parameters.setString(NRtcParameters.KEY_AUDIO_EFFECT_AUTOMATIC_GAIN_CONTROL,
                        NRtcParameters.AUDIO_EFFECT_MODE_SDK_BUILTIN_PRIORITY);
                break;
            case 2:
                parameters.setString(NRtcParameters.KEY_AUDIO_EFFECT_AUTOMATIC_GAIN_CONTROL,
                        NRtcParameters.AUDIO_EFFECT_MODE_PLATFORM_BUILTIN_PRIORITY);
                break;
        }


        switch (audioEffectNsMode) {
            case 0:
                parameters.setString(NRtcParameters.KEY_AUDIO_EFFECT_NOISE_SUPPRESSOR,
                        NRtcParameters.AUDIO_EFFECT_MODE_DISABLE);
                break;
            case 1:
                parameters.setString(NRtcParameters.KEY_AUDIO_EFFECT_NOISE_SUPPRESSOR,
                        NRtcParameters.AUDIO_EFFECT_MODE_SDK_BUILTIN_PRIORITY);
                break;
            case 2:
                parameters.setString(NRtcParameters.KEY_AUDIO_EFFECT_NOISE_SUPPRESSOR,
                        NRtcParameters.AUDIO_EFFECT_MODE_PLATFORM_BUILTIN_PRIORITY);
                break;
        }

        if (videoMaxBitrate > 0) {
            parameters.setInteger(NRtcParameters.KEY_VIDEO_MAX_BITRATE, videoMaxBitrate * 1024);
        }

        return parameters;
    }


}
