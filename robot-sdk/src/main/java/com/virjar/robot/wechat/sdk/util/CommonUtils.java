package com.virjar.robot.wechat.sdk.util;

public class CommonUtils {
    public static String safeToString(Object input) {
        if (input == null) {
            return null;
        }
        return input.toString();
    }

}
