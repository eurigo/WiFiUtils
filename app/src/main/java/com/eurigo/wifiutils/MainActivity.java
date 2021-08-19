package com.eurigo.wifiutils;

import static android.os.Build.VERSION_CODES.Q;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.eurigo.wifilib.WifiReceiver;
import com.eurigo.wifilib.WifiUtils;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
        , WifiReceiver.WifiStateListener {

    private static final String TAG = "MainActivity";
    private static final String AP_SSID = "TEST_AP";
    private static final String AP_PWD = "TEST_PWD";
    private static final String WIFI_SSID = "TEST_WIFI_SSID";
    private static final String WIFI_PWD = "TEST_WIFI_PWD";

    private LogAdapter mAdapter;

    private EditText etSsid, etPwd;
    private MaterialButton btnAp, btnWiFi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btnAp = findViewById(R.id.btn_use_ap);
        btnWiFi = findViewById(R.id.btn_use_wifi);
        btnAp.setOnClickListener(this);
        btnWiFi.setOnClickListener(this);
        findViewById(R.id.btn_connect).setOnClickListener(this);
        etSsid = findViewById(R.id.et_wifi_ssid);
        etPwd = findViewById(R.id.et_wifi_pwd);
        etSsid.setText(WIFI_SSID);
        etPwd.setText(WIFI_PWD);
        btnAp.setText(WifiUtils.getInstance().isApEnable(this) ? "关闭热点" : "打开热点");
        btnWiFi.setText(WifiUtils.getInstance().isWifiEnable(this) ? "关闭WiFi" : "打开WiFi");

        RecyclerView mRecyclerView = findViewById(R.id.rcv_ap_log);
        mAdapter = new LogAdapter(new ArrayList<>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        WifiUtils.getInstance().registerWifiBroadcast(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        WifiUtils.getInstance().unregisterWifiBroadcast(this);
    }

    private void showLog(String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                String time = format.format(new Date(System.currentTimeMillis()));
                mAdapter.addDataAndScroll(time + "\n" + data);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_use_ap:
                if ("打开热点".equals(btnAp.getText())) {
                    WifiUtils.getInstance().openAp(this, AP_SSID, AP_PWD);
                } else {
                    WifiUtils.getInstance().closeAp(this);
                }
                break;
            case R.id.btn_use_wifi:
                if (Build.VERSION.SDK_INT >= Q) {
                    ToastUtils.showShort("不支持Android 10以上系统");
                    return;
                }
                if ("打开WiFi".equals(btnWiFi.getText())) {
                    WifiUtils.getInstance().openWifi(this);
                } else {
                    WifiUtils.getInstance().closeWifi(this);
                }
                break;
            case R.id.btn_connect:
//                if (TextUtils.isEmpty(etSsid.getText()) || TextUtils.isEmpty(etSsid.getText())) {
//                    ToastUtils.showShort("WiFi名或密码不能为空");
//                    return;
//                }
//                WifiUtils.getInstance().connectWifi(this, etSsid.getText().toString().trim()
//                        , etPwd.getText().toString().trim());
                WifiUtils.getInstance().connectWifi(this, "LUKE-OFFICE"
                        , "luke2020");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WifiUtils.REQUEST_WRITE_SETTING_CODE) {
            Log.e(TAG, "onActivityResult() called with: "
                    + "requestCode = [" + requestCode + "]"
                    + ", resultCode = [" + resultCode + "], data = [" + data + "]");
        }
    }

    @Override
    public void onWifiOpen() {
        showLog("WiFi已打开");
        btnWiFi.setText("关闭WiFi");
    }

    @Override
    public void onWifiClose() {
        showLog("WiFi已关闭");
        btnWiFi.setText("打开WiFi");
    }

    @Override
    public void onHotpotOpen() {
        showLog("热点已打开");
        btnAp.setText("关闭热点");
    }

    @Override
    public void onHotpotClose() {
        showLog("热点已关闭");
        btnAp.setText("打开热点");
    }

    @Override
    public void onHotpotOpenError() {
        showLog("热点打开失败");
    }
}