package com.virjar.robot.wechat.sdk.applets.luckymoney;


import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.robot.wechat.sdk.Config;
import com.virjar.robot.wechat.sdk.service.chat.MessageReceiveManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.virjar.robot.wechat.sdk.applets.luckymoney.LuckyMoneyInfo.PARAM_CHANNELID;
import static com.virjar.robot.wechat.sdk.applets.luckymoney.LuckyMoneyInfo.PARAM_SCENEID;
import static com.virjar.robot.wechat.sdk.applets.luckymoney.LuckyMoneyInfo.PARAM_SENDID;


public class LuckyMoneyApplet {

    private static boolean status = false;

    private static final Map<String, LuckyMoneyInfo> luckyMoneyMap = new HashMap<>();
    private static final Pattern nativeUrlPattern = Pattern.compile("<nativeurl><!\\[CDATA\\[(.*?)\\]\\]>");
    private static final Pattern fromUserNamePattern = Pattern.compile("<fromusername><!\\[CDATA\\[(.*?)\\]\\]>");
    private static final Pattern sceneIdPattern = Pattern.compile("<sceneid><!\\[CDATA\\[(.*?)\\]\\]>");

    public static void enable() {
//        if (!Config.weChatVersionName.equals(Version_7_0_21.instance.version())) {
//            throw new IllegalStateException("抢红包功能目前只支持7.0.21");
//        }
        status = true;
        MessageReceiveManager.addIMListener(LuckyMoneyMsgListener.getInstance());
        initHook(RatelToolKit.hostClassLoader);
    }

    public static void disable() {
        status = false;
        MessageReceiveManager.removeIMListener(LuckyMoneyMsgListener.getInstance());
    }

    private static void initHook(ClassLoader classLoader) {
        hookTimingIdentifierReceive(classLoader);
        hookGetLuckyMoneyReceive(classLoader);
        Log.i(Config.TAG,"[LuckyMoney] 红包功能初始化完成");
    }

    /**
     * 收到红包消息时，解析其中内容，提取抢红包信息
     *
     * @param messageXml 红包消息体 XML格式
     * @param toWeixinId 目标会话talkerId
     * @return the object represent luck money info
     */
    public static LuckyMoneyInfo receiveLuckyMoneyMessage(String toWeixinId, String messageXml) {
        try {
            // nativeurl
            Matcher nativeUrlMatcher = nativeUrlPattern.matcher(messageXml);
            if (!nativeUrlMatcher.find()) {
                Log.w(Config.TAG, "[LuckyMoney] 未解析出nativeurl:" + messageXml);
                return null;
            }
            String nativeUrl = nativeUrlMatcher.group(1);
            Map<String, String> paramMap = new HashMap<>();
            String[] params = nativeUrl.split("\\?")[1].split("&");
            for (String param : params) {
                String[] split = param.split("=");
                paramMap.put(split[0], split[1]);
            }
            String sendId = paramMap.get(PARAM_SENDID);
            if (TextUtils.isEmpty(sendId)) {
                Log.w(Config.TAG, "[LuckyMoney] 未解析出sendid:" + messageXml);
                return null;
            }
            // sendusername
            Matcher fromUserNameMatcher = fromUserNamePattern.matcher(messageXml);
            if (!fromUserNameMatcher.find()) {
                Log.w(Config.TAG, "[LuckyMoney] 未解析出fromusername:" + messageXml);
                return null;
            }
            String fromUserName = fromUserNameMatcher.group(1);
            // sceneid
            Matcher sceneIdMatcher = sceneIdPattern.matcher(messageXml);
            if (!sceneIdMatcher.find()) {
                Log.w(Config.TAG, "[LuckyMoney] 未解析出sceneid:" + messageXml);
                return null;
            }
            paramMap.put(PARAM_SCENEID, sceneIdMatcher.group(1));
            LuckyMoneyInfo luckyMoneyInfo = new LuckyMoneyInfo(toWeixinId, fromUserName, nativeUrl, paramMap);
            luckyMoneyMap.put(sendId, luckyMoneyInfo);
            Log.i(Config.TAG, "接收到红包消息:" + luckyMoneyInfo);
            requestTimingIdentifier(sendId);
            return luckyMoneyInfo;
        } catch (Throwable e) {
            Log.e(Config.TAG, "解析红包消息异常:", e);
            return null;
        }
    }

    /**
     * 拆红包接口，返回拆红包的结果
     */
    private static void hookGetLuckyMoneyReceive(ClassLoader classLoader) {
        RposedHelpers.findAndHookMethod("com.tencent.mm.plugin.luckymoney.model.av", classLoader, "onGYNetEnd", int.class, String.class, JSONObject.class, new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!status) {
                    Log.i(Config.TAG, "[LuckyMoney] 抢红包功能已关闭");
                    return;
                }
                Log.i(Config.TAG, "[LuckyMoney] 抢红包结果：" + param.args[2]);
                JSONObject result = (JSONObject) param.args[2];
                if (result == null) {
                    return;
                }
                String sendId = result.optString("sendId");
                if (TextUtils.isEmpty(sendId)) {
                    return;
                }
                luckyMoneyMap.remove(sendId);
            }
        });
    }

    /**
     * 获取时间身份验证标示接口返回结果
     */
    private static void hookTimingIdentifierReceive(ClassLoader classLoader) {
        RposedHelpers.findAndHookMethod("com.tencent.mm.plugin.luckymoney.model.ba", classLoader, "onGYNetEnd", int.class, String.class, JSONObject.class, new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!status) {
                    Log.i(Config.TAG, "[LuckyMoney] 抢红包功能已关闭");
                    return;
                }
                Log.i(Config.TAG, "[LuckyMoney] 时间身份验证接口返回:" + param.args[2]);
                JSONObject jsonObject = (JSONObject) param.args[2];
                String timingIdentifier = jsonObject.optString("timingIdentifier");
                String sendId = jsonObject.optString("sendId");
                LuckyMoneyInfo luckyMoneyInfo = luckyMoneyMap.get(sendId);
                if (luckyMoneyInfo == null) {
                    Log.e(Config.TAG, "[LuckyMoney] 未获取到红包信息：" + sendId);
                    return;
                }
                // 请求拆红包接口
                requestLuckyMoney(luckyMoneyInfo, timingIdentifier);
            }
        });
    }


    /**
     * 拆红包前，需要请求获取时间身份验证标示
     */
    public static void requestTimingIdentifier(String sendId) {
        LuckyMoneyInfo luckyMoneyInfo = luckyMoneyMap.get(sendId);
        if (luckyMoneyInfo == null) {
            return;
        }
        // 构建请求参数
        String requestParamClass = luckyMoneyInfo.getInt(PARAM_SCENEID) == 1005 ? "com.tencent.mm.plugin.luckymoney.model.bb" : "com.tencent.mm.plugin.luckymoney.model.ba";
        // key_way 默认为0
        Object requestParams = RposedHelpers.newInstance(RposedHelpers.findClass(requestParamClass, RatelToolKit.hostClassLoader), luckyMoneyInfo.getInt(PARAM_CHANNELID), sendId, luckyMoneyInfo.getNativeUrl(), 0, "v1.0");
        Log.i(Config.TAG, "[LuckyMoney] 开始请求时间标示:" + luckyMoneyInfo);
        asyncRequest(requestParams);
    }

    /**
     * 拆红包接口
     *
     * @param luckyMoneyInfo   红包信息
     * @param timingIdentifier 时间身份标示
     */
    public static void requestLuckyMoney(LuckyMoneyInfo luckyMoneyInfo, String timingIdentifier) {
        String str1 = (String) RposedHelpers.callStaticMethod(RposedHelpers.findClass("com.tencent.mm.plugin.luckymoney.model.ac", RatelToolKit.hostClassLoader), "dBF");
        String str2 = (String) RposedHelpers.callStaticMethod(RposedHelpers.findClass("com.tencent.mm.model.x", RatelToolKit.hostClassLoader), "aDA");
        Object requestParams;
        if (luckyMoneyInfo.getInt(PARAM_SCENEID) == 1005) {
            requestParams = RposedHelpers.newInstance(RposedHelpers.findClass("com.tencent.mm.plugin.luckymoney.model.aw", RatelToolKit.hostClassLoader),
                    luckyMoneyInfo.getString("msgtype"),
                    luckyMoneyInfo.getInt(PARAM_CHANNELID), luckyMoneyInfo.getString(PARAM_SENDID), luckyMoneyInfo.getNativeUrl(),
                    str1,
                    str2,
                    luckyMoneyInfo.getFromUserName(), "v1.0", timingIdentifier);
        } else {
            requestParams = RposedHelpers.newInstance(RposedHelpers.findClass("com.tencent.mm.plugin.luckymoney.model.av", RatelToolKit.hostClassLoader),
                    luckyMoneyInfo.getInt("msgtype"),
                    luckyMoneyInfo.getInt(PARAM_CHANNELID), luckyMoneyInfo.getString(PARAM_SENDID), luckyMoneyInfo.getNativeUrl(),
                    str1,
                    str2,
                    luckyMoneyInfo.getToWeixinId(), "v1.0", timingIdentifier, "");

        }
        Log.i(Config.TAG, "[LuckyMoney] 开始抢红包：" + luckyMoneyInfo);
        asyncRequest(requestParams);
    }

    private static void asyncRequest(Object requestParams) {
        // 获取网络请求类
        Class<?> g = RposedHelpers.findClass("com.tencent.mm.kernel.g", RatelToolKit.hostClassLoader);
        Object gCp = RposedHelpers.getObjectField(RposedHelpers.callStaticMethod(g, "alr"), "gCp");
        RposedHelpers.callMethod(gCp, "a", requestParams, 0);
    }
}
