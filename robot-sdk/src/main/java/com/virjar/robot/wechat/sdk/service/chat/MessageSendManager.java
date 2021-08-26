package com.virjar.robot.wechat.sdk.service.chat;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.robot.wechat.sdk.Config;
import com.virjar.robot.wechat.sdk.WechatClassPackage;
import com.virjar.robot.wechat.sdk.bean.RContactModel;
import com.virjar.robot.wechat.sdk.bean.WechatMessage;
import com.virjar.robot.wechat.sdk.service.db.ContactDbHelper;

import external.org.apache.commons.lang3.StringUtils;

public class MessageSendManager extends Handler {

    private static final int SEND_INTERVAL_MILLION_SECONDS = 1000;

    private static MessageSendManager INSTANCE;

    private String lastReceiver;

    public MessageSendManager(Looper looper) {
        super(looper);
    }

    public static MessageSendManager getHandler() {
        if (INSTANCE == null) {
            HandlerThread handlerThread = new HandlerThread("sendMessageHandlerThread");
            handlerThread.start();
            INSTANCE = new MessageSendManager(handlerThread.getLooper());
        }
        return INSTANCE;
    }

    @Override
    public void handleMessage(Message msg) {
        WechatMessage wechatMessage = (WechatMessage) msg.obj;
        if (wechatMessage == null) {
            return;
        }
        Log.i(Config.TAG, "SendMessageHandler receive send message:" + wechatMessage);
        sendMessage(wechatMessage);
        try {
            if (!StringUtils.equals(wechatMessage.receiverWxId, lastReceiver)) {
                Thread.sleep(SEND_INTERVAL_MILLION_SECONDS);
                lastReceiver = wechatMessage.receiverWxId;
            }
        } catch (InterruptedException e) {
            Log.e(Config.TAG, "SendMessage sleep interrupted:", e);
        }
    }

    public static void sendMessage(WechatMessage wechatMessage) {
        switch (wechatMessage.msgType) {
            case ChatConstant.SEND_TYPE_TEXT:
                if (StringUtils.isEmpty(wechatMessage.content)) {
                    Log.i(Config.TAG, "文本消息不能为空！");
                    return;
                }
                if (wechatMessage.isGroup) {
                    String atIds = wechatMessage.atWechatIds;
                    StringBuilder content = new StringBuilder(wechatMessage.content);
                    if (TextUtils.isEmpty(atIds) || atIds.equals("null")) {
                        sendRawText(wechatMessage.receiverWxId, content.toString(), null);
                    } else {
                        //如果是群聊，并且是at
                        String[] wxIds = atIds.split(",");
                        for (String wxId : wxIds) {
                            content.append(getSingleAtHeader(wxId));
                        }
                        sendRawText(wechatMessage.receiverWxId, content.toString(), atIds);
                    }
                } else {
                    sendRawText(wechatMessage.receiverWxId, wechatMessage.content, null);
                }
                break;
            case ChatConstant.SEND_TYPE_IMAGE:
                try {
                    if (TextUtils.isEmpty(wechatMessage.imageUrl)) {
                        Log.e(Config.TAG, "发送图片下载链接不能为空:" + wechatMessage);
                        return;
                    }
                    ChatHelper.downloadAndSendImageFile(wechatMessage.imageUrl, wechatMessage.receiverWxId, wechatMessage.md5);
                } catch (Throwable e) {
                    Log.e(Config.TAG, "downloadAndSendImageFile error:" + wechatMessage, e);
                }
                break;
        }
    }

    private static void sendRawText(String talker, String content, String atList) {
        WechatClassPackage.NetSceneSendMsg msg = WechatClassPackage.NetSceneSendMsg.newInstance(talker, content, atList);
        WechatClassPackage.NetSceneQueue.push(msg.getObject());
    }

    //获取单个人@文本
    private static String getSingleAtHeader(String weixinId) {
        if (weixinId.equals("notify@all")) {
            return "@所有人 ";
        }
        char a = 8197;
        //获取weixinId的用户名
        RContactModel contactModel = ContactDbHelper.findUserInfo(weixinId);
        if (contactModel == null) {
            return "";
        }
        String username = contactModel.nickname;
        return "@" + username + ' ';
    }
}
