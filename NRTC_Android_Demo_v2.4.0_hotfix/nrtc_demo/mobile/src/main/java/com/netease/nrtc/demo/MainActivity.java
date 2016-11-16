package com.netease.nrtc.demo;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.netease.nrtc.demo.settings.AboutActivity;
import com.netease.nrtc.demo.settings.SettingActivity;
import com.netease.nrtc.sdk.NRtc;
import com.visionin.Visionin;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 10;

    private EditText mUid;
    private EditText mChannel;
    private SwitchCompat mVideoMode;
    private SwitchCompat mSafeMode;
    private SwitchCompat mMultiUser;
    private SwitchCompat mUserAudience;

    private Handler mHandler = new Handler();

    private ProgressDialog mDialog = null;

    private String mToken;

    //SDK支持安全模式和非安全模式
    //非安全模式下，客户端只需要传递APPKEY即可进行通话，需要保管好自己的APPKEY
    //安全模式下，客户端需要传递APPKEY和TOKEN来进行通话，APPKEY泄漏不影响程序安全性
    //默认情况下非安全模式关闭，需要联系客服开启
    private boolean safeMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Visionin.initialize(this, "293cd8f2fd5cdf0e403f535f2563b5b4", "44ce96297a8bcc10eaf095d216d045ec");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(true);


        mUid = (EditText) findViewById(R.id.uid);
        mChannel = (EditText) findViewById(R.id.channel);
        mVideoMode = (SwitchCompat) findViewById(R.id.type);
        mSafeMode = (SwitchCompat) findViewById(R.id.mode);
        mMultiUser = (SwitchCompat) findViewById(R.id.multi_user);
        mUserAudience = (SwitchCompat) findViewById(R.id.user_audience);
        findViewById(R.id.RTC).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRtc();
            }
        });
        mMultiUser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mMultiUser.isChecked()) {
                    mUserAudience.setEnabled(true);
                    mUserAudience.setChecked(true);
                } else {
                    mUserAudience.setEnabled(false);
                    mUserAudience.setChecked(false);
                }
            }
        });

        if(mMultiUser.isChecked()) {
            mUserAudience.setEnabled(true);
            mUserAudience.setChecked(true);
        } else {
            mUserAudience.setEnabled(false);
            mUserAudience.setChecked(false);
        }


        mUid.setText(new Random().nextInt(Integer.MAX_VALUE) + "");

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("正在获取密钥");

        mChannel.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                SettingActivity.launch(this);
                break;
            case R.id.action_about:
                AboutActivity.launch(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void startRtc() {

        String uid = mUid.getText().toString();
        String channel = mChannel.getText().toString();
        mToken = null;
        if(TextUtils.isEmpty(uid)) {
            mUid.setError("Required");
            return;
        }
        if(TextUtils.isEmpty(channel)) {
            mChannel.setError("Required");
            return;
        }

        final List<String> missed = NRtc.checkPermission(this);
        if(missed.size() != 0) {

            List<String> showRationale = new ArrayList<>();
            for(String permission : missed) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    showRationale.add(permission);
                }
            }

            if(showRationale.size() > 0) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("You need to allow some permission")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, missed.toArray(new String[missed.size()]),
                                        PERMISSION_REQUEST_CODE);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, missed.toArray(new String[missed.size()]), PERMISSION_REQUEST_CODE);
            }

            return;
        }

        chat(uid, channel);

    }

    private void chat(String uid, String channel) {
        safeMode = mSafeMode.isChecked();
        if(safeMode) {
            loadToken(uid, channel);
            mDialog.show();
        } else {
            doChat(uid, channel, false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if(NRtc.checkPermission(this).size() == 0) {
                    chat(mUid.getText().toString(), mChannel.getText().toString());
                } else {
                    Toast.makeText(MainActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    private void doChat(String uid, String channel, boolean safe) {

        if(safe && !isFinishing()) {
            mDialog.dismiss();
        }

        if(mMultiUser.isChecked()) {
            MultiChatActivity.launch(MainActivity.this, Long.parseLong(uid),
                    channel, mToken, mVideoMode.isChecked(), mMultiUser.isChecked(),
                    mUserAudience.isChecked());
        } else {
            ChatActivity.launch(MainActivity.this, Long.parseLong(uid),
                    channel, mToken, mVideoMode.isChecked(), mMultiUser.isChecked(),
                    mUserAudience.isChecked());
        }


    }

    private void loadTokenError() {
        if(!isFinishing())
            mDialog.dismiss();
        Toast.makeText(this, "load token error, try again!", Toast.LENGTH_SHORT).show();
    }


    //在线上环境中，token的获取需要放到您的应用服务端完成，然后由服务器通过安全通道把token传递给客户端
    //Demo中使用的URL仅仅是demoserver，不要在您的应用中使用
    //详细请参考: http://dev.netease.im/docs?doc=server
    private void loadToken(final String uid, final String channel) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String queryString = Config.DEMO_SERVER + "?uid=" +
                           uid + "&appkey=" + Config.APP_KEY;
                    URL requestedUrl = new URL(queryString);
                    HttpURLConnection connection = (HttpURLConnection) requestedUrl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(6000);
                    connection.setReadTimeout(6000);

                    if (connection.getResponseCode() == 200) {
                        String result = readFully(connection.getInputStream());
                        Log.d("Demo", result);
                        if(!TextUtils.isEmpty(result)) {
                            JSONObject object = new JSONObject(result);
                            int code = object.getInt("code");
                            if(code == 200) {
                                mToken = object.getString("checksum");
                                if(!TextUtils.isEmpty(mToken)) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            doChat(uid, channel, true);
                                        }
                                    });
                                    return;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadTokenError();
                    }
                });
            }
        }).start();
    }



    public static String readFully(InputStream inputStream) throws IOException {

        if(inputStream == null) {
            return "";
        }

        BufferedInputStream bufferedInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;

        try {
            bufferedInputStream = new BufferedInputStream(inputStream);
            byteArrayOutputStream = new ByteArrayOutputStream();

            final byte[] buffer = new byte[1024];
            int available = 0;

            while ((available = bufferedInputStream.read(buffer)) >= 0) {
                byteArrayOutputStream.write(buffer, 0, available);
            }

            return byteArrayOutputStream.toString();

        } finally {
            if(bufferedInputStream != null) {
                bufferedInputStream.close();
            }

        }
    }

}
