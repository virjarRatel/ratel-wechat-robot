package com.virjar.robot.wechat.sdk.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IMMessage {
    private String content;
    private String talker;
    private String msgId;
    private String createTime;
    private String talkerNickName;
}

