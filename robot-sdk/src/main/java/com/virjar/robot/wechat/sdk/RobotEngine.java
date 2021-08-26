package com.virjar.robot.wechat.sdk;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.api.rposed.callbacks.RC_LoadPackage;
import com.virjar.robot.wechat.sdk.service.RpcService;
import com.virjar.robot.wechat.sdk.service.chat.MessageReceiveManager;
import com.virjar.robot.wechat.sdk.service.db.DbHelper;
import com.virjar.sekiro.log.SekiroLogger;

public class RobotEngine {

    private static boolean enableAutoOpenLuckMoney = false;

    public static void init(RC_LoadPackage.LoadPackageParam llparm) {
        if (!llparm.packageName.equals(llparm.processName)) {
            //只工作在主进程
            return;
        }

        RposedHelpers.findAndHookMethod("com.tencent.tinker.loader.app.TinkerApplication", llparm.classLoader, "onCreate", new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Application tinkerApplication = (Application) param.thisObject;
                RatelToolKit.sContext = tinkerApplication;
                RatelToolKit.hostClassLoader = tinkerApplication.getClassLoader();
                WechatClassPackage.init(RatelToolKit.sContext);

                try {
                    onWeChatProcessStartup();
                } catch (Throwable e) {
                    Log.e(Config.TAG, "onWeChatProcessStartup failed:", e);
                }
            }
        });
    }

    public static void enableAutoOpenLuckyMoney() {
        enableAutoOpenLuckMoney = true;
    }

    private static void envInit() {
        PackageInfo packageInfo;
        try {
            packageInfo = RatelToolKit.sContext.getPackageManager()
                    .getPackageInfo(RatelToolKit.packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException(e);
        }

        Config.weChatVersionName = packageInfo.versionName;
        Config.weChatVersionCode = packageInfo.versionCode;
        SekiroLogger.tag = Config.TAG;
    }

    private static void onWeChatProcessStartup() {

        envInit();

        // hook 数据库层
        DbHelper.init();

        // 监控微信消息
        MessageReceiveManager.init();

        // rpc服务提供方
        RpcService.startRpcService();

        // not work
        if (enableAutoOpenLuckMoney) {
//            LuckyMoneyApplet.enable();
        }
    }
}