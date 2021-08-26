package com.virjar.robot.wechat.sdk.interfaze;

import com.virjar.robot.wechat.sdk.bean.IMMessage;

public interface IMListener {
    boolean supportType(int type);

    void onReceiveIMMessage(IMMessage imMessage);
}
