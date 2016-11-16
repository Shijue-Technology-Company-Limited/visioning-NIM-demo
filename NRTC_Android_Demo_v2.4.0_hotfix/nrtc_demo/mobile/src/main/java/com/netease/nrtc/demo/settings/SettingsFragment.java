package com.netease.nrtc.demo.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import com.netease.nrtc.demo.R;
import com.netease.nrtc.sdk.NRtcParameters;

import java.util.HashMap;


public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {


    boolean runtime = false;

    private HashMap<String, Object> newSettings = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.setting_pref);

        findPreference(getString(R.string.setting_vie_crop_key)).setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.setting_vie_rotation_key)).setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.setting_vie_fps_reported_key)).setOnPreferenceChangeListener(this);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_vie_quality_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_vie_hw_encoder_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_vie_hw_decoder_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_voe_audio_aec_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_voe_audio_agc_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_voe_audio_ns_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_vie_max_bitrate_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_other_device_default_rotation_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_other_device_rotation_fixed_offset_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_vie_frame_scale_type_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_live_url_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_vie_frame_rate_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_net_proxy_user_name_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_net_proxy_user_password_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_net_proxy_addr_ip_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_net_proxy_addr_port_key)));

        update();
    }


    private void update() {

        runtime = getActivity().getIntent().getBooleanExtra("runtime", false);
        int videoEncodeMode = getActivity().getIntent().getIntExtra("videoEncodeMode", 0);
        int videoDecodeMode = getActivity().getIntent().getIntExtra("videoDecodeMode", 0);
        int videoQuality = getActivity().getIntent().getIntExtra("videoQuality", 0);
        int videoMaxBitrate = getActivity().getIntent().getIntExtra("videoMaxBitrate", 0);

        if (runtime) {
            findPreference(getString(R.string.setting_voe_call_proximity_key)).setEnabled(false);
            findPreference(getString(R.string.setting_voe_audio_aec_key)).setEnabled(false);
            findPreference(getString(R.string.setting_voe_audio_agc_key)).setEnabled(false);
            findPreference(getString(R.string.setting_voe_audio_ns_key)).setEnabled(false);
            findPreference(getString(R.string.setting_vie_default_front_camera_key)).setEnabled(false);
            findPreference(getString(R.string.setting_vie_frame_scale_type_key)).setEnabled(false);
            findPreference(getString(R.string.setting_live_status_key)).setEnabled(false);
            findPreference(getString(R.string.setting_other_device_default_rotation_key)).setEnabled(false);
            findPreference(getString(R.string.setting_other_device_rotation_fixed_offset_key)).setEnabled(false);
            findPreference(getString(R.string.setting_other_server_record_audio_key)).setEnabled(false);
            findPreference(getString(R.string.setting_other_server_record_video_key)).setEnabled(false);
            findPreference(getString(R.string.setting_net_proxy_key)).setEnabled(false);
            findPreference(getString(R.string.setting_net_proxy_user_name_key)).setEnabled(false);
            findPreference(getString(R.string.setting_net_proxy_user_password_key)).setEnabled(false);
            findPreference(getString(R.string.setting_net_proxy_addr_ip_key)).setEnabled(false);
            findPreference(getString(R.string.setting_net_proxy_addr_port_key)).setEnabled(false);

            switch (videoEncodeMode) {
                case -1:
                    findPreference(getString(R.string.setting_vie_hw_encoder_key)).setEnabled(false);
                    break;
                case 0:
                    findPreference(getString(R.string.setting_vie_hw_encoder_key)).setSummary("自动");
                    break;
                case 1:
                    findPreference(getString(R.string.setting_vie_hw_encoder_key)).setSummary("软件");
                    break;
                case 2:
                    findPreference(getString(R.string.setting_vie_hw_encoder_key)).setSummary("硬件");
                    break;
            }

            switch (videoDecodeMode) {
                case -1:
                    findPreference(getString(R.string.setting_vie_hw_decoder_key)).setEnabled(false);
                    break;
                case 0:
                    findPreference(getString(R.string.setting_vie_hw_decoder_key)).setSummary("自动");
                    break;
                case 1:
                    findPreference(getString(R.string.setting_vie_hw_decoder_key)).setSummary("软件");
                    break;
                case 2:
                    findPreference(getString(R.string.setting_vie_hw_decoder_key)).setSummary("硬件");
                    break;
            }

            switch (videoQuality) {
                case 0:
                    findPreference(getString(R.string.setting_vie_quality_key)).setSummary("Default");
                    break;
                case 1:
                    findPreference(getString(R.string.setting_vie_quality_key)).setSummary("LOW");
                    break;
                case 2:
                    findPreference(getString(R.string.setting_vie_quality_key)).setSummary("MEDIUM");
                    break;
                case 3:
                    findPreference(getString(R.string.setting_vie_quality_key)).setSummary("HIGH");
                    break;
                case 4:
                    findPreference(getString(R.string.setting_vie_quality_key)).setSummary("480P");
                    break;
                case 5:
                    findPreference(getString(R.string.setting_vie_quality_key)).setSummary("720P");
                    break;
            }

            findPreference(getString(R.string.setting_vie_max_bitrate_key)).setSummary(videoMaxBitrate / 1024 + "");

        }


    }


    private void bindPreferenceSummaryToValue(Preference preference) {

        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue = newValue.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof SwitchPreference){
            //ignore
        } else {
            preference.setSummary(stringValue);
        }


        if (runtime) {
            String key = preference.getKey();
            if (key.equals(getString(R.string.setting_vie_crop_key))) {
                newSettings.put(NRtcParameters.KEY_VIDEO_CROP_BEFORE_SEND, newValue);
            } else if (key.equals(getString(R.string.setting_vie_rotation_key))) {
                newSettings.put(NRtcParameters.KEY_VIDEO_ROTATE_BEFORE_RENDING, newValue);
            } else if (key.equals(getString(R.string.setting_vie_fps_reported_key))) {
                newSettings.put(NRtcParameters.KEY_VIDEO_FPS_REPORTED, newValue);
            } else if (key.equals(getString(R.string.setting_vie_max_bitrate_key))) {
                newSettings.put(NRtcParameters.KEY_VIDEO_MAX_BITRATE, 1024 * Integer.parseInt(newValue.toString()));
            } else if (key.equals(getString(R.string.setting_vie_hw_encoder_key))) {
                switch (Integer.parseInt(newValue.toString())) {
                    case 0:
                        newSettings.put(NRtcParameters.KEY_VIDEO_ENCODER_MODE,
                                NRtcParameters.MEDIA_CODEC_AUTO);
                        break;
                    case 1:
                        newSettings.put(NRtcParameters.KEY_VIDEO_ENCODER_MODE,
                                NRtcParameters.MEDIA_CODEC_SOFTWARE);
                        break;
                    case 2:
                        newSettings.put(NRtcParameters.KEY_VIDEO_ENCODER_MODE,
                                NRtcParameters.MEDIA_CODEC_HARDWARE);
                        break;
                }
            } else if (key.equals(getString(R.string.setting_vie_hw_decoder_key))) {
                switch (Integer.parseInt(newValue.toString())) {
                    case 0:
                        newSettings.put(NRtcParameters.KEY_VIDEO_DECODER_MODE,
                                NRtcParameters.MEDIA_CODEC_AUTO);
                        break;
                    case 1:
                        newSettings.put(NRtcParameters.KEY_VIDEO_DECODER_MODE,
                                NRtcParameters.MEDIA_CODEC_SOFTWARE);
                        break;
                    case 2:
                        newSettings.put(NRtcParameters.KEY_VIDEO_DECODER_MODE,
                                NRtcParameters.MEDIA_CODEC_HARDWARE);
                        break;
                }
            } else if (key.equals(getString(R.string.setting_vie_quality_key))) {
                newSettings.put(NRtcParameters.KEY_VIDEO_QUALITY, Integer.parseInt(newValue.toString()));
            } else if (key.equals(getString(R.string.setting_live_url_key))) {
                newSettings.put(NRtcParameters.KEY_SESSION_LIVE_URL, newValue.toString());
            }

            Intent intent = new Intent();
            intent.putExtra("settings", newSettings);
            getActivity().setResult(Activity.RESULT_OK, intent);
        }

        return true;
    }

}
