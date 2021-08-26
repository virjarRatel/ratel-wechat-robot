package com.virjar.robot.wechat.sdk.action;

import android.os.Message;
import android.util.Log;

import com.virjar.robot.wechat.sdk.Config;
import com.virjar.robot.wechat.sdk.bean.WechatMessage;
import com.virjar.robot.wechat.sdk.service.chat.MessageSendManager;
import com.virjar.robot.wechat.sdk.service.db.ContactDbHelper;
import com.virjar.sekiro.api.ActionHandler;
import com.virjar.sekiro.api.CommonRes;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroResponse;
import com.virjar.sekiro.api.databind.AutoBind;

import external.org.apache.commons.lang3.StringUtils;


public class MessageAction implements ActionHandler {

    private static final MessageSendManager SEND_MESSAGE_MANAGER = MessageSendManager.getHandler();

    @AutoBind
    private String content;
    @AutoBind
    private String receiverWxId;
    @AutoBind
    private String receiverNickname;
    @AutoBind
    private Boolean isGroup;
    @AutoBind
    private String atWechatIds;
    @AutoBind
    private String imageUrl;
    @AutoBind
    private String md5;
    @AutoBind
    private int msgType;


    @Override
    public void handleRequest(SekiroRequest invokeRequest, SekiroResponse sekiroResponse) {
        if (StringUtils.isBlank(receiverWxId)) {
            receiverWxId = ContactDbHelper.findWxIdByNickName(receiverNickname);
            if (StringUtils.isBlank(receiverWxId)) {
                Log.i(Config.TAG, "not found wxId,receiverNickname:" + receiverNickname);
                sekiroResponse.send(CommonRes.failed("not found wxId,receiverNickname:" + receiverNickname));
                return;
            }
        }
        WechatMessage wechatMessage = new WechatMessage(content, receiverWxId);
        wechatMessage.isGroup = isGroup;
        wechatMessage.atWechatIds = atWechatIds;
        wechatMessage.msgType = msgType;
        wechatMessage.imageUrl = imageUrl;
        wechatMessage.md5 = md5;
        Message message = Message.obtain();
        message.obj = wechatMessage;
        Log.i(Config.TAG, "post message:" + wechatMessage);
        SEND_MESSAGE_MANAGER.sendMessage(message);
        sekiroResponse.send(CommonRes.success("收到消息发送任务"));
    }

    @Override
    public String action() {
        return "sendMessage";
    }
}
