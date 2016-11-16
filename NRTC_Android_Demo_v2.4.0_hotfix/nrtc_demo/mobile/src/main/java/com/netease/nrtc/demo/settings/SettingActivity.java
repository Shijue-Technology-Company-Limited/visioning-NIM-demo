package com.netease.nrtc.demo.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.netease.nrtc.demo.R;

import java.util.HashMap;


public class SettingActivity extends AppCompatActivity {

    public static final int requestCode = 10000;

    public static void launch(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, SettingActivity.class);
        intent.putExtra("runtime", false);
        context.startActivity(intent);
    }


    public static void launch(Activity context,
                              int videoEncodeMode,
                              int videoDecodeMode,
                              int videoQuality,
                              int videoMaxBitrate) {
        Intent intent = new Intent();
        intent.setClass(context, SettingActivity.class);
        intent.putExtra("runtime", true);
        intent.putExtra("videoEncodeMode", videoEncodeMode);
        intent.putExtra("videoDecodeMode", videoDecodeMode);
        intent.putExtra("videoQuality", videoQuality);
        intent.putExtra("videoMaxBitrate", videoMaxBitrate);
        context.startActivityForResult(intent, requestCode);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.setting_title);

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

}
