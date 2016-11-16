package com.netease.nrtc.demo.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.netease.nrtc.demo.BuildConfig;
import com.netease.nrtc.demo.Config;
import com.netease.nrtc.demo.R;
import com.netease.nrtc.sdk.NRtc;
import com.netease.nrtc.sdk.NRtcVersion;


public class AboutFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.about_pref);

        NRtcVersion ver = NRtc.version();

        bindPreferenceSummaryToValue(findPreference(getString(R.string.about_nrtc_sdk_version_name_key)), ver.versionName);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.about_nrtc_sdk_version_code_key)), ver.versionCode + "");
        bindPreferenceSummaryToValue(findPreference(getString(R.string.about_nrtc_sdk_build_tag_key)), ver.buildRevision);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.about_nrtc_sdk_build_branch_key)), ver.buildBranch);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.about_nrtc_sdk_build_time_key)), ver.buildDate);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.about_nrtc_sdk_build_type_key)), ver.buildType);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.about_nrtc_sdk_version_server_env_key)), ver.serverEnv);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.about_nrtc_sdk_build_host_key)), ver.buildHost);


        bindPreferenceSummaryToValue(findPreference(getString(R.string.about_nrtc_demo_version_name_key)), BuildConfig.VERSION_NAME);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.about_nrtc_demo_version_code_key)), BuildConfig.VERSION_CODE + "");
        bindPreferenceSummaryToValue(findPreference(getString(R.string.about_nrtc_demo_version_server_env_key)), Config.SERVER_ENV);
    }


    private void bindPreferenceSummaryToValue(Preference preference, String summary) {

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(summary);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            preference.setSummary(summary);
        }
    }


}
