package com.virjar.robot.wechat.sdk.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5Util {

    private static final ThreadLocal<MessageDigest> DIGESTER_CONTEXT = new ThreadLocal<MessageDigest>() {
        protected synchronized MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException var2) {
                throw new RuntimeException(var2);
            }
        }
    };

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest;
        FileInputStream in = null;
        byte[] buffer = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytesToHexString(digest.digest());
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static byte[] md5(byte[] data) {
        return md5(data, 0, data.length);
    }

    public static byte[] md5(byte[] data, int start, int len) {
        MessageDigest digester = (MessageDigest) DIGESTER_CONTEXT.get();
        digester.update(data, start, len);
        return digester.digest();
    }


    public static long halfDigest(String s) throws UnsupportedEncodingException {
        byte[] digest = md5(s.getBytes("UTF-8"));
        return halfValue(digest);
    }

    private static long halfValue(byte[] digest) {
        long value = 0L;

        for (int i = 0; i < 8; ++i) {
            value |= ((long) digest[i] & 255L) << 8 * (7 - i);
        }

        return value;
    }

    public static String toMd5(byte[] var0, boolean var1) {
        try {
            MessageDigest var2 = MessageDigest.getInstance("MD5");
            var2.reset();
            var2.update(var0);
            return toHexString(var2.digest(), "", var1);
        } catch (NoSuchAlgorithmException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static String toHexString(byte[] var0, String var1, boolean var2) {
        StringBuilder var3 = new StringBuilder();
        byte[] var4 = var0;
        int var5 = var0.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            byte var7 = var4[var6];
            String var8 = Integer.toHexString(255 & var7);
            if (var2) {
                var8 = var8.toUpperCase();
            }

            if (var8.length() == 1) {
                var3.append("0");
            }

            var3.append(var8).append(var1);
        }

        return var3.toString();
    }
}
