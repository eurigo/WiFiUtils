package com.eurigo.wifiutils;

import static android.os.Build.VERSION_CODES.P;
import static com.blankj.utilcode.constant.PermissionConstants.LOCATION;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.eurigo.wifilib.WifiReceiver;
import com.eurigo.wifilib.WifiUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
        , WifiReceiver.WifiStateListener, NetworkUtils.OnNetworkStatusChangedListener {

    private static final String AP_SSID = "TEST_AP";
    private static final String AP_PWD = "TEST_PWD";
    private static final String WIFI_SSID = "WIFI_SSID";
    private static final String WIFI_PWD = "WIFI_PWD";

    private static final String CLOSE_WIFI = "关闭WiFi";
    private static final String OPEN_WIFI = "打开WiFi";
    private static final String CLOSE_AP = "关闭热点";
    private static final String OPEN_AP = "打开热点";

    private LogAdapter mAdapter;

    private MaterialButton btnAp, btnWiFi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WifiUtils.getInstance().init(this);
        initView();
        NetworkUtils.registerNetworkStatusChangedListener(this);
    }

    private void initView() {
        btnAp = findViewById(R.id.btn_use_ap);
        btnWiFi = findViewById(R.id.btn_use_wifi);
        btnAp.setOnClickListener(this);
        btnWiFi.setOnClickListener(this);
        findViewById(R.id.btn_connect).setOnClickListener(this);
        EditText etSsid = findViewById(R.id.et_wifi_ssid);
        EditText etPwd = findViewById(R.id.et_wifi_pwd);
        etSsid.setText(WIFI_SSID);
        etPwd.setText(WIFI_PWD);
        btnAp.setText(WifiUtils.getInstance().isApEnable() ? CLOSE_AP : CLOSE_WIFI);
        btnWiFi.setText(WifiUtils.getInstance().isWifiEnable() ? CLOSE_WIFI : OPEN_WIFI);

        RecyclerView mRecyclerView = findViewById(R.id.rcv_ap_log);
        mAdapter = new LogAdapter(new ArrayList<>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnected(NetworkUtils.NetworkType networkType) {
        showLog("type: " + networkType.name());
        showLog("ssid: " + WifiUtils.getInstance().getSsid());
        showLog("net ssid: " + NetworkUtils.getSSID());
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnAp.setText(WifiUtils.getInstance().isApEnable() ? CLOSE_AP : OPEN_AP);
        btnWiFi.setText(WifiUtils.getInstance().isWifiEnable() ? CLOSE_WIFI : OPEN_WIFI);
        if (!WifiUtils.getInstance().isRegisterWifiBroadcast()) {
            WifiUtils.getInstance().registerWifiBroadcast(this);
        }
        getPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WifiUtils.getInstance().unregisterWifiBroadcast();
        WifiUtils.getInstance().release();
    }

    private void getPermission() {
        if (PermissionUtils.isGranted(LOCATION)) {
            return;
        }
        PermissionUtils.permission(LOCATION)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                    }

                    @Override
                    public void onDenied() {
                    }
                })
                .request();
    }

    private void showLog(String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.addDataAndScroll(data);
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_use_ap:
                if (OPEN_AP.contentEquals(btnAp.getText())) {
                    if (Build.VERSION.SDK_INT < P && WifiUtils.getInstance().isWifiEnable()) {
                        WifiUtils.getInstance().closeWifi();
                    }
                    WifiUtils.getInstance().openAp(this, AP_SSID, AP_PWD);
                } else {
                    if (WifiUtils.getInstance().isApEnable()) {
                        WifiUtils.getInstance().closeAp(this);
                    }
                }
                break;
            case R.id.btn_use_wifi:
                if (OPEN_WIFI.contentEquals(btnWiFi.getText())) {
                    if (Build.VERSION.SDK_INT < P && WifiUtils.getInstance().isApEnable()) {
                        WifiUtils.getInstance().closeAp(this);
                    }
                    WifiUtils.getInstance().openWifi();
                } else {
                    if (WifiUtils.getInstance().isWifiEnable()) {
                        WifiUtils.getInstance().closeWifi();
                    }
                }
                break;
            case R.id.btn_connect:
                WifiUtils.getInstance().connectWifi(this, WIFI_SSID, WIFI_PWD);
                break;
            default:
                break;
        }
    }

    @Override
    public void onWifiOpen() {
        showLog("WiFi已打开");
        btnWiFi.setText(CLOSE_WIFI);
    }

    @Override
    public void onWifiClose() {
        showLog("WiFi已关闭");
        btnWiFi.setText(OPEN_WIFI);
    }

    @Override
    public void onHotpotOpen() {
        showLog("热点已打开");
        btnAp.setText(CLOSE_AP);
    }

    @Override
    public void onHotpotClose() {
        showLog("热点已关闭");
        btnAp.setText(OPEN_AP);
    }

    @Override
    public void onHotpotOpenError() {
        showLog("热点打开失败");
    }
}