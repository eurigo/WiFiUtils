# WiFiUtils [![](https://jitpack.io/v/eurigo/WifiUtils.svg)](https://jitpack.io/#eurigo/WifiUtils)

### Android WiFi工具类，兼容至Android 10.0，部分Api不支持9.0+
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
    implementation 'com.github.eurigo:WiFiUtils:1.0.1'
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
/>
```

### API
+ #### 打开WiFi，不支持Android 10及以上设备
```
WifiUtils.getInstance().openWifi(Context context);
```
+ #### 关闭WiFi，不支持Android 10及以上设备
```
WifiUtils.getInstance().closeWifi(Context context);
```
+ #### 连接WiFi，7.0以上不同OS界面可能会不同
```
WifiUtils.getInstance().connectWifi(Context context, String ssid, String password);
```

+ #### 打开热点，Android 7.1版本可能存在问题
```
WifiUtils.getInstance().openAp(Context Context context, String ssid, String password);
```
+ #### 关闭热点，Android 7.1版本可能存在问题
```
WifiUtils.getInstance().closeAp(Context context);
```
