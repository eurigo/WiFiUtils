package com.eurigo.wifilib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.PatternMatcher;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;

/**
 * @author Eurigo
 * Created on 2021/7/14 16:46
 * desc   :
 */
public class WifiUtils {

    private static final String TAG = "WifiUtils";

    public static final int REQUEST_WRITE_SETTING_CODE = 1;

    private OpenWiFiResultListener openWiFiResultListener;

    private WifiManager wifiManager;

    public WifiUtils() {
    }

    public static WifiUtils getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private static class SingletonHelper {
        private final static WifiUtils INSTANCE = new WifiUtils();
    }

    /**
     * 便携热点是否开启
     *
     * @return 是否开启
     */
    public boolean isApOn(Context context) {
        if (wifiManager == null) {
            wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        }
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * wifi是否打开
     *
     * @return 是否打开
     */
    public boolean isWifiEnable(Context context) {
        if (wifiManager == null) {
            wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        }
        return wifiManager.isWifiEnabled();
    }

    /**
     * 关闭Wi-Fi, 不适用于Android Q+设备
     */
    public void closeWifi(Context context) throws IllegalStateException {
        if (Build.VERSION.SDK_INT >= Q) {
            throw new IllegalStateException("not support Android Q+ version!");
        }
        if (wifiManager == null) {
            wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        }
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 关闭便携热点
     */
    public void closeAp(Context context) {
        if (wifiManager == null) {
            wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        }
        try {
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifiManager, null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启便携热点, 不适用于Android Q+设备
     *
     * @param context  上下文
     * @param ssid     便携热点SSID
     * @param password 便携热点密码
     * @return
     */
    public boolean openAp(Context context, String ssid, String password) {
        if (wifiManager == null) {
            wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        }
        if (isWifiEnable(context)) {
            wifiManager.setWifiEnabled(false);
        }

        // 6.0+系统需要获取修改设置权限
        if (Build.VERSION.SDK_INT >= M) {
            if (!isGrantedWriteSettings(context)) {
                requestWriteSettings(context);
            }
        }

        try {
            // 热点的配置类
            WifiConfiguration config = new WifiConfiguration();
            // 配置热点的名称(可以在名字后面加点随机数什么的)
            config.SSID = ssid;
            config.preSharedKey = password;
            //是否隐藏网络
            config.hiddenSSID = false;
            //开放系统认证
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;

            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            // 返回热点打开状态
            return (Boolean) method.invoke(wifiManager, config, true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 连接WIFI,密码为空默认不使用密码连接
     * Android 10+ 必须配置callback
     *
     * @param ssid     wifi名称
     * @param password wifi密码
     */
    public void connectWifi(Context context, String ssid, String password
            , OpenWiFiResultListener listener) {
        openWiFiResultListener = listener;
        if (Build.VERSION.SDK_INT < Q) {
            if (isApOn(context)) {
                closeAp(context);
            }
            if (!isWifiEnable(context)) {
                wifiManager.setWifiEnabled(true);
            }
            Thread thread = new Thread(new ConnectRunnable(ssid, password));
            thread.start();
        } else {
            if (!isWifiEnable(context)) {
                Toast.makeText(context, "请先打开WiFi开关", Toast.LENGTH_SHORT).show();
                return;
            }
            connect(context, ssid, password);
        }

    }

    /**
     * 获取开启便携热点后自身热点IP地址
     *
     * @return ip地址
     */
    public String getLocalIp(Context context) {
        if (wifiManager == null) {
            wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        }
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo != null) {
            int address = dhcpInfo.serverAddress;
            return ((address & 0xFF)
                    + "." + ((address >> 8) & 0xFF)
                    + "." + ((address >> 16) & 0xFF)
                    + "." + ((address >> 24) & 0xFF));
        }
        return null;
    }

    /**
     * 查看授权情况, 开启热点需要申请系统设置修改权限，如有必要，可提前申请
     *
     * @param context activity
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestWriteSettings(Context context) {
        if (isGrantedWriteSettings(context)) {
            Log.e(TAG, "已授权修改系统设置权限");
            return;
        }
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        ((Activity) context).startActivityForResult(intent, REQUEST_WRITE_SETTING_CODE);
    }

    /**
     * 返回应用程序是否可以修改系统设置
     *
     * @return {@code true}: yes
     * {@code false}: no
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isGrantedWriteSettings(Context context) {
        return Settings.System.canWrite(context);
    }

    /**
     * 获取WiFi列表
     *
     * @return WIFI列表
     */
    public List<ScanResult> getWifiList(Context context) {
        if (wifiManager == null) {
            wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        }
        List<ScanResult> resultList = new ArrayList<>();
        if (wifiManager != null && isWifiEnable(context)) {
            resultList.addAll(wifiManager.getScanResults());
        }
        return resultList;
    }

    /**
     * wifi设置
     *
     * @param ssid     WIFI名称
     * @param pws      WIFI密码
     * @param isHasPws 是否有密码
     */
    private WifiConfiguration getWifiConfig(String ssid, String pws, boolean isHasPws) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        WifiConfiguration tempConfig = isExist(ssid);
        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }
        if (isHasPws) {
            config.preSharedKey = "\"" + pws + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        return config;
    }

    /**
     * 得到配置好的网络连接
     *
     * @param ssid
     * @return
     */
    @SuppressLint("MissingPermission")
    private WifiConfiguration isExist(String ssid) {
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        if (configs == null) {
            Log.e(TAG, "isExist: null");
            return null;
        }
        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\"" + ssid + "\"")) {
                return config;
            }
        }
        return null;
    }

    /**
     * 这个方法连接上的WIFI ，只能在当前应用中使用，当应用被kill之后，连接的这个wifi会断开
     *
     * @param context  上下文
     * @param ssid     wifi名称
     * @param password wifi密码
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void connect(Context context, String ssid, String password) {
        if (isGrantedWriteSettings(context)) {
            requestWriteSettings(context);
        }
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                .setSsidPattern(new PatternMatcher(ssid, PatternMatcher.PATTERN_PREFIX))
                .setWpa2Passphrase(password)
                .build();
        // 创建一个请求
        NetworkRequest request = new NetworkRequest.Builder()
                // 创建的是WIFI网络。
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                // 网络不受限
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                // 信任网络，增加这个参数让设备连接wifi之后还联网。
                .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                .setNetworkSpecifier(specifier)
                .build();
        connectivityManager.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                openWiFiResultListener.openSuccess();
            }

            @Override
            public void onUnavailable() {
                openWiFiResultListener.openFailed();
            }
        });
    }

    public interface OpenWiFiResultListener {

        /**
         * 打开成功
         */
        void openSuccess();

        /**
         * 打开失败
         */
        void openFailed();

    }

    class ConnectRunnable implements Runnable {

        private final String ssid;
        private final String pwd;

        public ConnectRunnable(String ssid, String pwd) {
            this.ssid = ssid;
            this.pwd = pwd;
        }

        @Override
        public void run() {
            while (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            wifiManager.disableNetwork(wifiManager.getConnectionInfo().getNetworkId());
            int netId = wifiManager.addNetwork(getWifiConfig(ssid, pwd, !TextUtils.isEmpty(pwd)));
            if (wifiManager.enableNetwork(netId, true)) {
                openWiFiResultListener.openSuccess();
            } else {
                openWiFiResultListener.openFailed();
            }
        }
    }
}
