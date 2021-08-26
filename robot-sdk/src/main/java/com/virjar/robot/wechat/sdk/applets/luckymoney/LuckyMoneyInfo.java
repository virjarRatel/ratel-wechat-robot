package com.virjar.robot.wechat.sdk.applets.luckymoney;

import java.util.Map;

public class LuckyMoneyInfo {


    public static final String PARAM_SCENEID = "sceneid";
    public static final String PARAM_SENDID = "sendid";
    public static final String PARAM_CHANNELID = "channelid";

    private String fromUserName;
    private String nativeUrl;
    private String toWeixinId;
    private Map<String, String> params;

    public LuckyMoneyInfo(String toWeixinId, String fromUserName, String nativeUrl, Map<String, String> params) {
        this.fromUserName = fromUserName;
        this.nativeUrl = nativeUrl;
        this.params = params;
        this.toWeixinId = toWeixinId;
    }

    public void addInfo(String key, String value) {
        this.params.put(key, value);
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public String getString(String key) {
        return this.params.get(key);
    }

    public Integer getInt(String key) {
        return Integer.parseInt(this.params.get(key));
    }

    public String getNativeUrl() {
        return this.nativeUrl;
    }

    public String getToWeixinId() {
        return toWeixinId;
    }

    @Override
    public String toString() {
        return "LuckMoneyInfo{" +
                "fromUserName='" + fromUserName + '\'' +
                ", nativeUrl='" + nativeUrl + '\'' +
                ", params=" + params +
                '}';
    }
}
