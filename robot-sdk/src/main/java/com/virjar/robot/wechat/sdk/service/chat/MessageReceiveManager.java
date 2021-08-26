package com.virjar.robot.wechat.sdk.service.chat;

import android.content.ContentValues;
import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.robot.wechat.sdk.Config;
import com.virjar.robot.wechat.sdk.WechatClassPackage;
import com.virjar.robot.wechat.sdk.bean.IMMessage;
import com.virjar.robot.wechat.sdk.interfaze.IMListener;
import com.virjar.robot.wechat.sdk.service.db.ContactDbHelper;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class MessageReceiveManager {
    private static final Set<IMListener> imListeners = new CopyOnWriteArraySet<>();

    public static void addIMListener(IMListener imListener) {
        imListeners.add(imListener);
    }

    public static void removeIMListener(IMListener imListener) {
        imListeners.remove(imListener);
    }

    public static void init() {
        /**
         * 消息入库
         */
        RposedHelpers.findAndHookMethod(RposedHelpers.findClass("com.tencent.wcdb.database.SQLiteDatabase", RatelToolKit.hostClassLoader),
                "insertWithOnConflict",
                String.class, String.class, ContentValues.class, int.class, new RC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        // message为消息表
                        if (!"message".equals(param.args[0])) {
                            return;
                        }
                        ContentValues contentValues = (ContentValues) param.args[2];
                        if (contentValues == null) {
                            Log.i(Config.TAG, "message insert with conflict content is null");
                            return;
                        }
                        onReceiveMsg(contentValues);
                    }
                });
        WechatClassPackage.NetSceneQueue.initHook();
    }

    private static void onReceiveMsg(ContentValues contentValues) {
        if (contentValues.getAsInteger("isSend") != 0) {
            // 是否发送
            return;
        }
        String content = contentValues.getAsString("content");
        String talker = contentValues.getAsString("talker");
        String msgId = contentValues.getAsString("msgId");
        String createTime = contentValues.getAsString("createTime");
        Log.i(Config.TAG, "receive  message from" + talker + " content: " + content);

        for (IMListener i : imListeners) {
            try {
                if (i.supportType(contentValues.getAsInteger("type"))) {
                    callIMListener(i, content, talker, msgId, createTime);
                }
            } catch (Exception e) {
                Log.e(Config.TAG, "call imListener failed", e);
            }

        }

    }

    private static void callIMListener(IMListener imListener, String content, String talker, String msgId,
                                       String createTime) {
        String talkerNickname = ContactDbHelper.findUserNickname(talker);
        IMMessage imMessage = new IMMessage(content, talker, msgId, createTime, talkerNickname);
        imListener.onReceiveIMMessage(imMessage);
    }
}
