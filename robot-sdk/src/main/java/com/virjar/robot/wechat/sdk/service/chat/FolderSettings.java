package com.virjar.robot.wechat.sdk.service.chat;

import android.os.Environment;

public class FolderSettings {
    public static final String DORA_DIR = "/weixin_homework";
    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + DORA_DIR;
    public static final String CACHE_DIR = ROOT_DIR + "/cache";
    public static final String IMAGE_DIR = ROOT_DIR + "/image";
    public static final String VIDEO_DIR = ROOT_DIR + "/video";
    public static final String AUDIO_DIR = ROOT_DIR + "/audio";
    public static final String APK_DIR = ROOT_DIR + "/apk";
    public static final String FILE_DIR = ROOT_DIR + "/file";
    public static final String CRASH_DIR = ROOT_DIR + "/crash";
    public static final String LOG_DIR = ROOT_DIR + "/logfile";
    public static final String STRESS_DIR = ROOT_DIR + "/stress";
    public static final String DEBUG = ROOT_DIR + "/debug";

}