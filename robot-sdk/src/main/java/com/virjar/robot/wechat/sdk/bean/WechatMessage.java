package com.virjar.robot.wechat.sdk.bean;

public class WechatMessage {

    public String content;
    public String receiverWxId;
    public int msgType;
    public boolean isGroup;
    public String atWechatIds;
    /**
     * 图片下载链接
     */
    public String imageUrl;
    /**
     * 图片MD5，带MD5可以避免重复下载
     */
    public String md5;

    public WechatMessage(String content, String receiverWxId) {
        this.content = content;
        this.receiverWxId = receiverWxId;
    }

    @Override
    public String toString() {
        return "WechatMessage{" +
                "content='" + content + '\'' +
                ", receiverWxId='" + receiverWxId + '\'' +
                '}';
    }
}
