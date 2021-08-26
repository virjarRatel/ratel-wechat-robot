package com.virjar.robot.wechat.sdk.applets.luckymoney;

import com.virjar.robot.wechat.sdk.bean.IMMessage;
import com.virjar.robot.wechat.sdk.interfaze.IMListener;
import com.virjar.robot.wechat.sdk.service.chat.ChatConstant;

public class LuckyMoneyMsgListener implements IMListener {

    private static final LuckyMoneyMsgListener INSTANCE = new LuckyMoneyMsgListener();

    public static LuckyMoneyMsgListener getInstance(){
        return INSTANCE;
    }

    @Override
    public boolean supportType(int type) {
        return type == ChatConstant.MESSAGE_TYPE_HONGBAO;
    }

    @Override
    public void onReceiveIMMessage(IMMessage imMessage) {
        // 过滤非红包消息
        LuckyMoneyApplet.receiveLuckyMoneyMessage(imMessage.getTalker(),imMessage.getContent());
    }
}
