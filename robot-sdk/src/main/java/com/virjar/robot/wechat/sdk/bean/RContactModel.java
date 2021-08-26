package com.virjar.robot.wechat.sdk.bean;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;


@Data
public class RContactModel {

    //数据表：rcontact    (字段 - 不全）
    public String username = ""; //微信 id
    public String alias = ""; //微信号
    private String conRemark = ""; //微信备注
    private String domainList = "";
    public String nickname = "";//微信昵称
    private String pyInitial = "";
    private String quanPin = "";
    private int showHead = 0;
    private int type = 0;
    private String weiboFlag = "";
    private String weiboNickname = "";
    private String conRemarkPYFull = "";
    private String conRemarkPYShort = "";
    private String encryptUsername = "";//加密的wxid
    private int chatroomFlag = 0;
    private int verifyFlag = 0;
    private String contactLabelIds = "";
    private byte[] lvbuff;

    //附加属性
    private int sex;// 0未知 1男 2女
    public String avatar = "";
    private List<String> phone = new ArrayList<>();// 列表中第一条是从联系人列表中获取的电话号码,可能为空;剩余的是备注添加的电话号码
    private String ticketId = "";
    private int contactScene = -1;
    private ArrayList tagList = new ArrayList();
    private int addWay;

}
