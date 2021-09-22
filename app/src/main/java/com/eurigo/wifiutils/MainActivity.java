package com.eurigo.wifiutils;

import static com.blankj.utilcode.constant.PermissionConstants.LOCATION;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.eurigo.udplibrary.UdpUtils;
import com.eurigo.wifilib.WifiReceiver;
import com.eurigo.wifilib.WifiUtils;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
        , WifiReceiver.WifiStateListener, NetworkUtils.OnNetworkStatusChangedListener {

    private static final String TAG = "MainActivity";
    private static final String AP_SSID = "TEST_AP";
    private static final String AP_PWD = "TEST_PWD";
    private static final String WIFI_SSID = "lkmw16310929400007";
    private static final String WIFI_PWD = "lkmw16310929400007";

    private LogAdapter mAdapter;

    private MaterialButton btnAp, btnWiFi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        btnAp.setText(WifiUtils.getInstance().isApEnable() ? "关闭热点" : "打开热点");
        btnWiFi.setText(WifiUtils.getInstance().isWifiEnable() ? "关闭WiFi" : "打开WiFi");

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
        LogUtils.e(networkType.name(), WifiUtils.getInstance().getSsid()
                , NetworkUtils.getSSID());
        if (networkType.equals(NetworkUtils.NetworkType.NETWORK_WIFI)
                && NetworkUtils.getSSID().equals(WIFI_SSID)) {
            UdpUtils.getInstance().sendBroadcastMessageInAndroidHotspot("good!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (WifiUtils.getInstance().isRegisterWifiBroadcast()) {
            WifiUtils.getInstance().registerWifiBroadcast(this, this);
        }
        getPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WifiUtils.getInstance().unregisterWifiBroadcast(this);
        WifiUtils.getInstance().release();
    }

    private void getPermission() {
        PermissionUtils.permission(PermissionConstants.getPermissions(LOCATION))
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        ToastUtils.showShort("获取WIFI名称需要位置权限");
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
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                String time = format.format(new Date(System.currentTimeMillis()));
                mAdapter.addDataAndScroll(time + "\n" + data);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_use_ap:
                if ("打开热点".contentEquals(btnAp.getText())) {
                    WifiUtils.getInstance().openAp(this, AP_SSID, AP_PWD);
                    btnAp.setText("关闭热点");
                } else {
                    WifiUtils.getInstance().closeAp(this);
                    btnAp.setText("打开热点");
                }
                break;
            case R.id.btn_use_wifi:
                if ("打开WiFi".contentEquals(btnWiFi.getText())) {
                    WifiUtils.getInstance().openWifi();
                    btnWiFi.setText("关闭WiFi");
                } else {
                    WifiUtils.getInstance().closeWifi();
                    btnWiFi.setText("打开WiFi");
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