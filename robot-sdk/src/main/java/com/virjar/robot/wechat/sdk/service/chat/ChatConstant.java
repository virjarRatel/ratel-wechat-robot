package com.virjar.robot.wechat.sdk.service.chat;

import android.text.TextUtils;

/**
 * 定义聊天的常量和类型
 * Created by wangbin on 2017/3/15.
 */

public class ChatConstant {

    static final int MESSAGE_TYPE_TEXT = 1;
    static final int MESSAGE_TYPE_IMAGE = 3;
    static final int MESSAGE_TYPE_AUDIO = 34;
    static final int MESSAGE_TYPE_GIF = 47;
    static final int MESSAGE_TYPE_GIF2 = 1048625;
    static final int MESSAGE_TYPE_URL_ARTICLE = 49;
    static final int MESSAGE_TYPE_VIDEO = 43;
    static final int MESSAGE_TYPE_SYSTEM = 10000;
    static final int MESSAGE_TYPE_CARD = 42;
    public static final int MESSAGE_TYPE_HONGBAO = 436207665;
    static final int MESSAGE_TYPE_GROUP_NOTIFY = 570425393;//接受微信群通知

    private static final String SUFFIX_NO_FRIEND = "开启了朋友验证，你还不是他（她）朋友。请先发送朋友验证请求，对方验证通过后，才能聊天。<a href=\"weixin://findfriend/verifycontact\">发送朋友验证</a>";
    private static final String SUFFIX_GROUP_NO_FRIEND = "对方通过验证后，才能加入群聊。";
    private static final String PREFIX_GROUP_NO_FRIEND = "你无法邀请未添加你为好友的用户进去群聊，请先向";
    private static final String PREFIX_GROUP_CREATE = "你邀请";
    private static final String SUFFIX_GROUP_CREATE = "加入了群聊";
    public static final String PREFIX_ADD_GROUP_INVITE = "[CDATA[邀请你加入群聊]]";

    /**
     * 发送消息的类型
     */
    static final int SEND_TYPE_XML = -10;
    static final int SEND_TYPE_OTHER = -1;
    public static final int SEND_TYPE_TEXT = 0;
    public static final int SEND_TYPE_IMAGE = 1;
    static final int SEND_TYPE_AUDIO = 2;
    static final int SEND_TYPE_VIDEO = 3;
    static final int SEND_TYPE_CARD = 4;
    static final int SEND_TYPE_ARTICLE = 5;
    static final int SEND_TYPE_GIF = 6;
    static final int SEND_TYPE_VIDEO_IMAGE = 7;
    static final int SEND_TYPE_FILE = 8;
    static final int SEND_TYPE_BIG_IMAGE = 9;
    public static final int SEND_TYPE_SYSTEM = 10;
    static final int SEND_TYPE_HONGBAO = 100;
    static final int SEND_TYPE_MINI_PROGRAM = 11;


    /**
     * 上传文件的重复次数
     */
    static int TRY_TIMES = 0;
    /**
     * 大图的消息id，防止大图重复下载
     */
    static long BIG_IMAGE_ID = 0;

    /**
     * 把微信的消息type转化成我们自己的消息type
     *
     * @param weixinMessageType 微信内部消息类型
     * @return 鲲鹏系统消息类型
     */
    public static int getMessageType(int weixinMessageType) {
        int type;
        switch (weixinMessageType) {
            case MESSAGE_TYPE_TEXT:
                type = SEND_TYPE_TEXT;
                break;
            case MESSAGE_TYPE_IMAGE:
                type = SEND_TYPE_IMAGE;
                break;
            case MESSAGE_TYPE_AUDIO:
                type = SEND_TYPE_AUDIO;
                break;
            case MESSAGE_TYPE_VIDEO:
                type = SEND_TYPE_VIDEO;
                break;
            case MESSAGE_TYPE_CARD:
                type = SEND_TYPE_CARD;
                break;
            case MESSAGE_TYPE_URL_ARTICLE:
                type = SEND_TYPE_ARTICLE;
                break;
            case MESSAGE_TYPE_GIF:
                type = SEND_TYPE_GIF;
                break;
            case MESSAGE_TYPE_SYSTEM:
                type = SEND_TYPE_SYSTEM;
                break;
            default:
                type = weixinMessageType;
        }
        return type;
    }

    static boolean shouldNotifySystemMsg(String realContent) {
        return !TextUtils.isEmpty(realContent) && (
                realContent.endsWith(SUFFIX_NO_FRIEND) ||  //不是好友
                        (realContent.startsWith(PREFIX_GROUP_NO_FRIEND) && realContent.endsWith(SUFFIX_GROUP_NO_FRIEND))  //拉取群 群提醒不是好友
                        || (realContent.startsWith(PREFIX_GROUP_CREATE) && realContent.endsWith(SUFFIX_GROUP_CREATE))
        );
    }
}