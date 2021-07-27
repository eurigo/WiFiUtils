package com.eurigo.wifiutils;

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

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.eurigo.udplibrary.UdpUtils;
import com.eurigo.wifilib.WifiUtils;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static android.os.Build.VERSION_CODES.Q;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final String AP_SSID = "lkmw1234";
    private static final String AP_PWD = "lkmw1234";
    private static final String WIFI_TYPE = "wifi_data";

    private LogAdapter mAdapter;

    private EditText etSsid, etPwd;
    private HashMap<String, String> map;
    private MaterialButton btnOpen, btnReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        map = new HashMap<>(2);
    }

    private void initView() {
        btnOpen = findViewById(R.id.btn_open_ap);
        btnOpen.setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Q) {
            btnOpen.setBackgroundColor(ColorUtils.getColor(R.color.panda));
            btnOpen.setEnabled(false);
        }
        findViewById(R.id.btn_connect_ap).setOnClickListener(this);
        findViewById(R.id.btn_send_data).setOnClickListener(this);
        btnReceive = findViewById(R.id.btn_start_receive);
        btnReceive.setOnClickListener(this);
        etSsid = findViewById(R.id.et_wifi_ssid);
        etPwd = findViewById(R.id.et_wifi_pwd);

        RecyclerView mRecyclerView = findViewById(R.id.rcv_ap_log);
        mAdapter = new LogAdapter(new ArrayList<>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * 开始接收数据
     */
    private void startReceived() {
        UdpUtils.getInstance().setUdpPort(9090);
        UdpUtils.getInstance().startUdpSocket();
        UdpUtils.getInstance().setReceiveListener(new UdpUtils.OnUdpReceiveListener() {
            @Override
            public void onReceived(String data) {
                UdpBean udpBean = GsonUtils.fromJson(data, UdpBean.class);
                // 如果传输的是wifi数据，则获取ssid和pwd连接wifi
                if (udpBean.getType().equals(WIFI_TYPE)) {
                    showLog("接收到WiFi数据，开始连接WiFi");
                    WifiUtils.getInstance()
                            .connectWifi(ActivityUtils.getTopActivity()
                                    , udpBean.getSsid(), udpBean.getPwd(), new WifiUtils.OpenWiFiResultListener() {
                                        @Override
                                        public void openSuccess() {
                                            showLog("WiFi 连接成功，SSID:" + udpBean.getSsid());
                                        }

                                        @Override
                                        public void openFailed() {
                                            showLog("WiFi 连接失败");
                                        }
                                    });
                }
            }
        });
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
            case R.id.btn_open_ap:
                if (WifiUtils.getInstance().openAp(this, AP_SSID, AP_PWD)) {
                    showLog("热点打开成功, ssid:" + AP_SSID + "pwd:" + AP_PWD);
                } else {
                    showLog("热点打开失败");
                }
                break;
            case R.id.btn_connect_ap:
                if (Build.VERSION.SDK_INT < Q) {
                    WifiUtils.getInstance().connectWifi(this, etSsid.getText().toString().trim()
                            , etPwd.getText().toString().trim(), new WifiUtils.OpenWiFiResultListener() {
                                @Override
                                public void openSuccess() {
                                    showLog("连接成功");
                                }

                                @Override
                                public void openFailed() {
                                    showLog("连接失败");
                                }
                            });
                } else {
                    WifiUtils.getInstance().connectWifi(this, AP_SSID, AP_PWD
                            , new WifiUtils.OpenWiFiResultListener() {
                                @Override
                                public void openSuccess() {
                                    showLog("连接成功");
                                }

                                @Override
                                public void openFailed() {
                                    showLog("连接失败");
                                }
                            });
                }
                break;
            case R.id.btn_send_data:
                map.put("type", WIFI_TYPE);
                map.put("to", "lkmw1234");
                map.put("ssid", etSsid.getText().toString().trim());
                map.put("pwd", etPwd.getText().toString().trim());
                UdpUtils.getInstance().sendBroadcastMessageInAndroidHotspot(map);
                break;
            case R.id.btn_start_receive:
                if (btnReceive.getText().equals("开始接收")) {
                    startReceived();
                    showLog("开始接收");
                    btnReceive.setText("停止接收");
                } else {
                    UdpUtils.getInstance().stopUdpSocket();
                    showLog("停止接收");
                    btnReceive.setText("开始接收");
                }
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
}