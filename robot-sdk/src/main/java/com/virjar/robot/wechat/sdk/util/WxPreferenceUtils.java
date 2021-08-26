package com.virjar.robot.wechat.sdk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RposedHelpers;

public class WxPreferenceUtils {

    public static String productId = "";
    public static String WX_ID;

    public static String getWxId() {
        SharedPreferences sharedPreferences = RatelToolKit.sContext.getSharedPreferences("com.tencent.mm_preferences", Context.MODE_PRIVATE);
        try {
            if (TextUtils.isEmpty(WX_ID)) {
                WX_ID = sharedPreferences.getString("login_weixin_username", "");
                if (TextUtils.isEmpty(WX_ID)) {//全局搜索 login_weixin_username，可以看到存储的地方
                    Class G = RposedHelpers.findClass("com.tencent.mm.kernel.g", RatelToolKit.hostClassLoader);
                    // mCoreStorage not initialized!
                    if (RposedHelpers.getObjectField(RposedHelpers.callStaticMethod(G, "alt"), "gDq") == null) {
                        return "";
                    }
                    Object model = RposedHelpers.callMethod(RposedHelpers.callStaticMethod(G, "als"), "alb");
                    WX_ID = String.valueOf(RposedHelpers.callMethod(model, "get", 2, (Object) null));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return WX_ID;
    }

    // 微信在高版本android中获取微信密码和android8.0以下不一样,存sp
    public static void setDatabasePassword(Context context, String dbPassword) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.tencent.mm_preferences", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("wxDbPassWord", dbPassword).apply();
    }

    public static String getDbPassword(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.tencent.mm_preferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("wxDbPassWord", "");
    }
}
