package com.virjar.robot.wechat.sdk.service.chat;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.virjar.robot.wechat.sdk.Config;
import com.virjar.robot.wechat.sdk.WechatClassPackage;
import com.virjar.robot.wechat.sdk.util.MD5Util;
import com.virjar.robot.wechat.sdk.util.ThreadPoolManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import external.org.apache.commons.io.IOUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatHelper {

    public static LruCache<String, String> imageCache = new LruCache<>(30);
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .build();

    /**
     * 下载图片文件
     *
     * @param imageUrl 图片的url
     * @param talker   聊天的对象
     */
    public static void downloadAndSendImageFile(final String imageUrl, final String talker, String md5) {
        if (md5 == null) {
            md5 = "";
        }
        final File downloadFile = getDownloadFile(imageUrl, "", FolderSettings.IMAGE_DIR, md5);
        if (downloadFile == null) {// 读写SD卡出错,回调发送文件失败,携带requestId
            return;
        }
        // 如果之前下载过，那么就不再重复下载这个文件了。根据md5判断文件的一致性
        Log.d(Config.TAG, "down image:" + md5 + ",save path=" + downloadFile.getAbsolutePath());
        if (downloadFile.exists() && (md5.equalsIgnoreCase(MD5Util.getFileMD5(downloadFile)) || md5.equals(""))) {
            sendImage(imageUrl, downloadFile.getAbsolutePath(), talker);
        } else {
            downImage(downloadFile, imageUrl, talker, md5);
        }
    }

    private static File getDownloadFile(String url, String fileName, String folder, String md5) {
        //检查SD卡状态是否mounted
        if (!Environment.getExternalStorageDirectory().canRead() || !Environment.getExternalStorageDirectory().canWrite()) {
            Log.e(Config.TAG, "读写SD卡出错，请检查SD卡");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        File dir;
        if (TextUtils.isEmpty(md5)) {
            dir = new File(folder);
        } else {
            dir = new File(folder, sb.append(md5.substring(0, 2)).append(File.separator).append(md5.substring(2, 5)).toString());
        }

        if (TextUtils.isEmpty(fileName)) {
            String[] urlSplit = url.split("/");
            fileName = urlSplit[urlSplit.length - 1];
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return new File(dir, fileName);
    }

    private static void sendImage(String imageUrl, final String path, final String weixinId) {
        if (!TextUtils.isEmpty(imageUrl)) {
            String key = path + weixinId;
            imageCache.put(key, imageUrl);
        }
        ThreadPoolManager.getInstace().execute(new Runnable() {
            @Override
            public void run() {
                if (!new File(path).exists()) {
                    return;
                }
                WechatClassPackage.NetSceneUploadMsgImg msg = WechatClassPackage.NetSceneUploadMsgImg.newInstance(weixinId, path);
                WechatClassPackage.NetSceneQueue.push(msg.getObject());
            }
        });
    }

    private static void downImage(final File downloadFile, final String imageUrl, final String talker, final String md5) {
        Log.d(Config.TAG, "downImage " + imageUrl);
        final Request request = new Request.Builder().url(imageUrl).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.w(Config.TAG, "downImage " + imageUrl + " fail!!");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream in = response.body().byteStream();
                    OutputStream fos = new FileOutputStream(downloadFile);
                    IOUtils.copy(in, fos);
                    fos.close();
                    in.close();
                    sendImage(imageUrl, downloadFile.getAbsolutePath(), talker);
                } else {
                    Log.w(Config.TAG, "downImage " + imageUrl + " fail2!!");
                }
            }
        });
    }
}
