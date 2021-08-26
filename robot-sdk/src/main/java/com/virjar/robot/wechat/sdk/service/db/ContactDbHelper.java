package com.virjar.robot.wechat.sdk.service.db;

import android.database.Cursor;

import com.virjar.robot.wechat.sdk.bean.RContactModel;

import java.util.ArrayList;

/**
 * <p>
 * 用户关系表，存储用户的好友关系，删除的好友也可能存在这个表里面。
 * 区分好友是否已经删除：encryptUsername ==null  好友关系存在;如果不为空,好友已经删除;
 * <p>
 * 1. rcontact.type==0     添加对方为好友，对方未通过；
 * 2. rcontact.type==1    好友；
 */
//rcontact 用户表
public class ContactDbHelper {

    public static RContactModel findUserInfo(String wx_id) {
        String sql = "select *,rowid from rcontact  where username = ? or encryptUsername = ?";
        return findUserInfo(sql, new String[]{wx_id, wx_id});
    }

    public static String findWxIdByNickName(String nickname) {
        return DbHelper.queryModel("select username from rcontact  where nickname = ? ", new String[]{nickname},
                new DbHelper.CursorMapper<String>() {
                    @Override
                    public String parse(Cursor cursor) {
                        return cursor.getString(0);
                    }
                });
    }

    /**
     * 通讯录好友的个数
     * 默认包含. “文件传输助手” --> username: filehelper,nickname: 文件传输助手
     */
    public static long findContactSize() {
        //群聊的 id 可能存在 @chatroom 和 @im.chatroom
        String sql = "select count(*) from rcontact  where (type & 1 != 0 and type & 8 == 0 and type & 32 == 0 and verifyFlag & 8 == 0 and username not like '%@%chatroom' and username not like '%@stranger')";
        Integer size = DbHelper.queryModel(sql, null, new DbHelper.CursorMapper<Integer>() {
            @Override
            public Integer parse(Cursor cursor) {
                return cursor.getInt(0);
            }
        });
        if (size == null) {
            size = 0;
        }
        return size;

    }

    public static String findUserNickname(String wxid) {
        return DbHelper.queryModel("select nickname from rcontact  where username = ? ",
                new String[]{wxid}, new DbHelper.CursorMapper<String>() {
                    @Override
                    public String parse(Cursor cursor) {
                        return cursor.getString(0);
                    }
                });

    }


    public static boolean isFriendByAlias(String alias) {
        String sql = "select * from rcontact  where alias = ?";
        RContactModel model = findUserInfo(sql, new String[]{alias});
        return model != null && (model.getType() == 1 || model.getType() == 3 || model.getType() == 5 || model.getType() == 65);
    }

    public static boolean isFriend(RContactModel model) {
        return model != null
                && (model.getType() & 1) != 0
                && (model.getType() & 8) == 0
                && (model.getType() & 32) == 0
                && (model.getVerifyFlag() & 8) == 0;
    }


    public static ArrayList<RContactModel> getRemarkFriends() {
        String sql = "select * from rcontact where conRemark <>''";
        return DbHelper.queryModels(sql, null, new DbHelper.CursorMapper<RContactModel>() {
            @Override
            public RContactModel parse(Cursor cursor) {
                RContactModel model = new RContactModel();
                model.setUsername(cursor.getString(cursor.getColumnIndex("username")));//username
                model.setConRemark(cursor.getString(cursor.getColumnIndex("conRemark")));
                model.setType(Integer.parseInt(cursor.getString(cursor.getColumnIndex("type"))));//type
                return model;
            }
        });
    }

    private static ArrayList<String> getPhone(byte[] bytes, String wx_id) {
        return new ArrayList<>();
    }

    private static String getUpload2Phone(String wx_id) {
        String sql = "select * from addr_upload2 where username = ?";
        return DbHelper.queryModel(sql, new String[]{wx_id}, new DbHelper.CursorMapper<String>() {
            @Override
            public String parse(Cursor cursor) {
                return cursor.getString(cursor.getColumnIndex("moblie"));
            }
        });

    }


    private static int getSex(byte[] bytes) {
        return 0;
    }

    public static RContactModel findUserInfo(String sql, String[] strArr) {
        return DbHelper.queryModel(sql, strArr, new DbHelper.CursorMapper<RContactModel>() {
            @Override
            public RContactModel parse(Cursor cursor) {
                RContactModel model = new RContactModel();
                String wxid = cursor.getString(cursor.getColumnIndex("username"));
                model.setUsername(wxid);//username
                model.setAlias(cursor.getString(cursor.getColumnIndex("alias")));//alias
                model.setConRemark(cursor.getString(cursor.getColumnIndex("conRemark")));
                model.setDomainList(cursor.getString(cursor.getColumnIndex("domainList")));
                model.setNickname(cursor.getString(cursor.getColumnIndex("nickname")));//nikename
                model.setQuanPin(cursor.getString(cursor.getColumnIndex("quanPin")));
                model.setConRemarkPYFull(cursor.getString(cursor.getColumnIndex("conRemarkPYFull")));
                model.setShowHead(Integer.parseInt(cursor.getString(cursor.getColumnIndex("showHead"))));
                model.setType(Integer.parseInt(cursor.getString(cursor.getColumnIndex("type"))));
                model.setEncryptUsername(cursor.getString(cursor.getColumnIndex("encryptUsername")));//strangerID
                model.setChatroomFlag(Integer.parseInt(cursor.getString(cursor.getColumnIndex("chatroomFlag"))));
                model.setVerifyFlag(Integer.parseInt(cursor.getString(cursor.getColumnIndex("verifyFlag"))));
                model.setContactLabelIds(cursor.getString(cursor.getColumnIndex("contactLabelIds")));
                model.setLvbuff(cursor.getBlob(cursor.getColumnIndex("lvbuff")));// 这个是微信加密的数据
                // todo 暂时用不上这些字段
                model.setPhone(getPhone(model.getLvbuff(), wxid));
                model.setAvatar("");
                model.setAddWay(0);
                model.setSex(getSex(model.getLvbuff()));
                return model;
            }
        });
    }
}
