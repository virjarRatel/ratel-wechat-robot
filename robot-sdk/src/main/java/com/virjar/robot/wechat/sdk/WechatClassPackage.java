package com.virjar.robot.wechat.sdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import external.com.alibaba.fastjson.JSON;
import external.com.alibaba.fastjson.JSONObject;
import external.com.alibaba.fastjson.TypeReference;
import external.org.apache.commons.io.FileUtils;

public class WechatClassPackage {
    private static WeakReference<ArrayList<String>> allClassList = new WeakReference<>(new ArrayList<String>());
    private static Map<String, String> clazzMap = new HashMap<>();


    public static void init(Context context) {
        File configFile = new File(context.getFilesDir().getAbsolutePath() + File.separator + "wx_robot_config_" + Config.weChatVersionCode + ".txt");
        if (configFile.exists()) {
            try {
                String configStr = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
                clazzMap = JSONObject.parseObject(configStr, new TypeReference<Map<String, String>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(Config.TAG, "读取缓存配置成功:" + configFile.getAbsolutePath() + ",clazzMap size=" + clazzMap.size());
        }
        if (clazzMap.isEmpty()) {
            try {
                ArrayList<String> list = new ArrayList<>();
                ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo("com.tencent.mm", 0);
                File apkFile = new File(applicationInfo.sourceDir);

                Enumeration zip = new ZipFile(apkFile).entries();
                while (zip.hasMoreElements()) {
                    ZipEntry dexInZip = (ZipEntry) zip.nextElement();
                    if (dexInZip.getName().startsWith("classes") && dexInZip.getName().endsWith(".dex")) {
                        MultiDexContainer.DexEntry<? extends DexBackedDexFile> dexEntry = DexFileFactory.loadDexEntry(apkFile, dexInZip.getName(), true, null);
                        DexBackedDexFile dexFile = dexEntry.getDexFile();
                        for (DexBackedClassDef classDef : dexFile.getClasses()) {
                            String classType = classDef.getType();
                            if (classType.contains("com/tencent/mm")) {
                                classType = classType.substring(1, classType.length() - 1).replace("/", ".");
                                list.add(classType);
                            }
                        }
                    }
                }
                Log.i(Config.TAG, "WechatClassPackage init list size = " + list.size());
                allClassList = new WeakReference<>(list);
            } catch (Exception e) {
                Log.e(Config.TAG, "WechatClassPackage init error:", e);
            }
            NetSceneQueue.getClazz();
            NetSceneSendMsg.getClazz();
            NetSceneUploadMsgImg.getClazz();
            String jsonString = JSON.toJSONString(clazzMap);
            try {
                FileUtils.writeStringToFile(configFile, jsonString, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(Config.TAG, "clazzMap = " + clazzMap.toString());
    }

    public static class SQLiteDatabase {
        private static Class clazz;
        private Object object;

        public SQLiteDatabase(Object object) {
            getClazz();
            this.object = object;
        }

        public static Class getClazz() {
            if (clazz != null) {
                return clazz;
            }
            clazz = RposedHelpers.findClass("com.tencent.wcdb.database.SQLiteDatabase", RatelToolKit.hostClassLoader);
            return clazz;
        }

        public Cursor rawQuery(String str, Object[] attrs) {
            return (Cursor) RposedHelpers.callMethod(this.object, "rawQuery", str, attrs);
        }
    }

    public static class NetSceneQueue {
        private static Class clazz;
        private static String pushMethodName;
        private static Object object;

        public static void initHook() {
            getClazz();
            RposedBridge.hookAllConstructors(clazz, new RC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    init(param.thisObject);
                }
            });
        }

        public static void init(Object object1) {
            object = object1;
        }

        public static boolean push(Object obj) {
            return (boolean) RposedHelpers.callMethod(object, pushMethodName, obj, 0);
        }

        static Class getClazz() {
            if (clazz != null) {
                return clazz;
            }
            String clazzName = clazzMap.get("NetSceneQueue");
            if (TextUtils.isEmpty(clazzName)) {
                List<String> origCheckMethods = Arrays.asList("onAppForeground", "onAppBackground", "setForeground");
                for (String itemClazzName : allClassList.get()) {
                    if (!Pattern.compile("^com\\.tencent\\.mm\\.[a-z]+\\.[a-z]+$").matcher(itemClazzName).find()) {
                        continue;
                    }
                    Class itemClazz = RposedHelpers.findClassIfExists(itemClazzName, RatelToolKit.hostClassLoader);
                    if (itemClazz == null || itemClazz.getAnnotations().length > 0 || !Modifier.isFinal(itemClazz.getModifiers()) || itemClazz.getInterfaces().length == 0 || itemClazz.getSuperclass() != Object.class) {
                        continue;
                    }
                    Method[] methods = itemClazz.getDeclaredMethods();
                    List<String> checkMethods = new LinkedList<>(origCheckMethods);
                    for (Method method : methods) {
                        checkMethods.remove(method.getName());
                    }
                    if (!checkMethods.isEmpty()) {
                        continue;
                    }
                    Log.d(Config.TAG, "itemClazzName = " + itemClazzName);
                    clazzName = itemClazzName;
                    clazz = itemClazz;
                    break;
                }
                if (TextUtils.isEmpty(clazzName)) {
                    Log.w(Config.TAG, "NetSceneQueue class is null");
                    return null;
                }
                for (Method method : clazz.getDeclaredMethods()) {
                    if (Modifier.isFinal(method.getModifiers()) && Modifier.isPublic(method.getModifiers()) && method.getReturnType() == boolean.class && method.getParameterTypes().length == 2 && method.getParameterTypes()[1] == int.class) {
                        pushMethodName = method.getName();
//                        Log.i(Config.TAG, "item method = " + method);
                    }
                }
                Log.i(Config.TAG, "NetSceneQueue class = " + clazzName);
                Log.i(Config.TAG, "pushMethod  = " + pushMethodName);
                clazzMap.put("NetSceneQueue", clazzName);
                clazzMap.put("NetSceneQueue_pushMethod", pushMethodName);
            } else {
                clazz = RposedHelpers.findClass(clazzName, RatelToolKit.hostClassLoader);
                pushMethodName = clazzMap.get("NetSceneQueue_pushMethod");
            }
            return clazz;
        }
    }

    public static class NetSceneSendMsg {
        private static Class clazz;
        private Object object;

        public NetSceneSendMsg(Object object) {
            this.object = object;
        }

        public static NetSceneSendMsg newInstance(String username, String textMsg, String atList) {
            getClazz();
            Object object = null;
            if (TextUtils.isEmpty(atList)) {
                object = RposedHelpers.newInstance(clazz, username, textMsg, 1, 0, null);
            } else {
                HashMap<String, String> map = new HashMap<>();
                map.put("atuserlist", "<![CDATA[" + atList + "]]>");
                object = RposedHelpers.newInstance(clazz, username, textMsg, 1, 1, map);
            }
            return new NetSceneSendMsg(object);
        }

        static Class getClazz() {
            if (clazz != null) {
                return clazz;
            }
            String clazzName = clazzMap.get("NetSceneSendMsg");
            if (TextUtils.isEmpty(clazzName)) {
                List<String> origCheckMethods = Arrays.asList("getType");
                for (String itemClazzName : allClassList.get()) {
                    if (!Pattern.compile("^com\\.tencent\\.mm\\.modelmulti\\.[a-z]+$").matcher(itemClazzName).find()) {
                        continue;
                    }
                    Class itemClazz = RposedHelpers.findClassIfExists(itemClazzName, RatelToolKit.hostClassLoader);
                    if (itemClazz == null || itemClazz.getAnnotations().length > 0 || !Modifier.isFinal(itemClazz.getModifiers()) || itemClazz.getInterfaces().length == 0 || itemClazz.getSuperclass() == Object.class || itemClazz.getConstructors().length != 4) {
                        continue;
                    }
                    Method[] methods = itemClazz.getDeclaredMethods();
                    List<String> checkMethods = new LinkedList<>(origCheckMethods);
                    for (Method method : methods) {
                        checkMethods.remove(method.getName());
                    }
                    if (!checkMethods.isEmpty()) {
                        continue;
                    }
                    boolean checkConstructor = false;
                    for (Constructor constructor : itemClazz.getConstructors()) {
                        Class[] parameterTypes = constructor.getParameterTypes();
                        if (parameterTypes.length == 5 && parameterTypes[0] == String.class && parameterTypes[1] == String.class && parameterTypes[2] == int.class && parameterTypes[3] == int.class && parameterTypes[4] == Object.class) {
                            checkConstructor = true;
                            break;
                        }
                    }
                    if (!checkConstructor) {
                        continue;
                    }
                    Log.d(Config.TAG, "itemClazzName = " + itemClazzName);
                    clazzName = itemClazzName;
                    clazz = itemClazz;
                    break;
                }
                if (TextUtils.isEmpty(clazzName)) {
                    Log.w(Config.TAG, "NetSceneSendMsg class is null");
                    return null;
                }
                Log.i(Config.TAG, "NetSceneSendMsg class = " + clazzName);
                clazzMap.put("NetSceneSendMsg", clazzName);
            } else {
                clazz = RposedHelpers.findClass(clazzName, RatelToolKit.hostClassLoader);
            }
            return clazz;
        }

        public Object getObject() {
            return object;
        }
    }

    public static class NetSceneUploadMsgImg {
        private static Class clazz;
        private Object object;

        public NetSceneUploadMsgImg(Object object) {
            this.object = object;
        }

        public static NetSceneUploadMsgImg newInstance(String username, String imgPath) {
            getClazz();
            Object object = RposedHelpers.newInstance(clazz, 2, username, username, imgPath, 1, null, 0, "", "", true, -1);
            return new NetSceneUploadMsgImg(object);
        }

        static Class getClazz() {
            if (clazz != null) {
                return clazz;
            }
            String clazzName = clazzMap.get("NetSceneUploadMsgImg");
            if (TextUtils.isEmpty(clazzName)) {
                List<String> origCheckMethods = Arrays.asList("getType", "doScene");
                for (String itemClazzName : allClassList.get()) {
                    if (!Pattern.compile("^com\\.tencent\\.mm\\.[a-z]+\\.[a-z]+$").matcher(itemClazzName).find()) {
                        continue;
                    }
                    Class itemClazz = RposedHelpers.findClassIfExists(itemClazzName, RatelToolKit.hostClassLoader);
                    if (itemClazz == null || itemClazz.getAnnotations().length > 0 || !Modifier.isFinal(itemClazz.getModifiers()) || itemClazz.getInterfaces().length == 0 || itemClazz.getSuperclass() == Object.class) {
                        continue;
                    }
                    Method[] methods = itemClazz.getDeclaredMethods();
                    List<String> checkMethods = new LinkedList<>(origCheckMethods);
                    for (Method method : methods) {
                        checkMethods.remove(method.getName());
                    }
                    if (!checkMethods.isEmpty()) {
                        continue;
                    }
                    boolean checkConstructor = false;
                    for (Constructor constructor : itemClazz.getConstructors()) {
                        Class[] parameterTypes = constructor.getParameterTypes();
                        if (parameterTypes.length == 11 && parameterTypes[0] == int.class && parameterTypes[1] == String.class && parameterTypes[2] == String.class && parameterTypes[3] == String.class
                                && parameterTypes[4] == int.class && parameterTypes[6] == int.class && parameterTypes[7] == String.class && parameterTypes[8] == String.class && parameterTypes[9] == boolean.class && parameterTypes[10] == int.class) {
                            checkConstructor = true;
                            break;
                        }
                    }
                    if (!checkConstructor) {
                        continue;
                    }
                    Log.d(Config.TAG, "itemClazzName = " + itemClazzName);
                    clazzName = itemClazzName;
                    clazz = itemClazz;
                    break;
                }
                if (TextUtils.isEmpty(clazzName)) {
                    Log.w(Config.TAG, "NetSceneUploadMsgImg class is null");
                    return null;
                }
                Log.i(Config.TAG, "NetSceneUploadMsgImg class = " + clazzName);
                clazzMap.put("NetSceneUploadMsgImg", clazzName);
            } else {
                clazz = RposedHelpers.findClass(clazzName, RatelToolKit.hostClassLoader);
            }
            return clazz;
        }

        public Object getObject() {
            return object;
        }
    }
}
