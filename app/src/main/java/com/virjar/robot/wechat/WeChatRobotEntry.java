package com.virjar.robot.wechat;

import com.virjar.ratel.api.rposed.IRposedHookLoadPackage;
import com.virjar.ratel.api.rposed.callbacks.RC_LoadPackage;
import com.virjar.robot.wechat.sdk.Config;
import com.virjar.robot.wechat.sdk.RobotEngine;

public class WeChatRobotEntry implements IRposedHookLoadPackage {
    @Override
    public void handleLoadPackage(RC_LoadPackage.LoadPackageParam lpparam) {
        Config.SEKIRO_GROUP = "wx_robot";
        RobotEngine.init(lpparam);
        RobotEngine.enableAutoOpenLuckyMoney();
    }
}
