package com.eurigo.wifiutils;

import com.google.gson.annotations.SerializedName;

/**
 * @author Eurigo
 * Created on 2021/7/26 14:08
 * desc   : udp发送的数据bean
 */
public class UdpBean {

    /**
     * to
     */
    @SerializedName("to")
    private String to;
    /**
     * type
     */
    @SerializedName("type")
    private String type;
    /**
     * ssid
     */
    @SerializedName("ssid")
    private String ssid;
    /**
     * pwd
     */
    @SerializedName("pwd")
    private String pwd;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return "UdpBean{" +
                "to='" + to + '\'' +
                ", type='" + type + '\'' +
                ", ssid='" + ssid + '\'' +
                ", pwd='" + pwd + '\'' +
                '}';
    }
}
