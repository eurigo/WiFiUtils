# WiFiUtils [![](https://jitpack.io/v/eurigo/WiFiUtils.svg)](https://jitpack.io/#eurigo/WiFiUtils)

### Android WiFi工具类，兼容Android 11+，

### 主要功能有打开、关闭、连接WIFI及热点。

+ [Github](https://github.com/eurigo/UDPUtils)

+ [Gitee](https://gitee.com/Eurigo/UDPUtils)

### 集成使用

+ 在项目级 `build.gradle`添加

```
allprojects {
   repositories {
      maven { url 'https://jitpack.io' }
	}
}
```
  
+ 在app模块下的`build.gradle`文件中加入
```
dependencies {
    implementation 'com.github.eurigo:WiFiUtils:1.1.0'
}
```

+ 在app模块下的AndroidManifest.xml添加权限
```
<manifest
    ...
     <uses-permission
        android:name="android.permission.WRITE_SETTINGS"
        tools:ignore="ProtectedPermissions" />
    
    <uses-permission android:name="android.permission.INTERNET" />
    
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
    
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
/>
```

### API

+ #### 打开WiFi
```
WifiUtils.getInstance().openWifi();
```

+ #### 关闭WiFi
```
WifiUtils.getInstance().closeWifi();
```

+ #### 连接WiFi
```
WifiUtils.getInstance().connectWifi(Activity activity, String ssid, String password);
```

+ #### 打开热点
```
WifiUtils.getInstance().openAp(Activity activity, String ssid, String password);
```

+ #### 关闭热点
```
WifiUtils.getInstance().closeAp(Activity activity);
```

+ #### 注册Wifi广播
```
WifiUtils.getInstance().registerWifiBroadcast(Activity activity, WifiStateListener wifiStateListener);
```

### 其他API

+ #### 获取当前WiFi名称(Android8.0以上必需定位权限)
```
WifiUtils.getInstance().getSsid();
```

+ #### 获取当前IP地址
```
WifiUtils.getInstance().getLocalIp();
```

+ #### 获取WIFI列表
```
WifiUtils.getInstance().getWifiList();
```
