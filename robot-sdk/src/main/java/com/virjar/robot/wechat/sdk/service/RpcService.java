package com.virjar.robot.wechat.sdk.service;

import com.virjar.robot.wechat.sdk.Config;
import com.virjar.robot.wechat.sdk.action.ArticleListAction;
import com.virjar.robot.wechat.sdk.action.ContactListAction;
import com.virjar.robot.wechat.sdk.action.MessageAction;
import com.virjar.robot.wechat.sdk.action.QueryDbAction;
import com.virjar.robot.wechat.sdk.action.SubscriptionAccountListAction;
import com.virjar.robot.wechat.sdk.action.WxIdQueryAction;
import com.virjar.robot.wechat.sdk.util.ClientIdentifier;
import com.virjar.sekiro.api.SekiroClient;

public class RpcService {
    public static void startRpcService() {
        SekiroClient sekiroClient = SekiroClient.start(
                Config.SEKIRO_HOST,
                Config.SEKIRO_PORT,
                ClientIdentifier.id(),
                Config.SEKIRO_GROUP
        );

        // 发送消息
        sekiroClient.registerHandler(new MessageAction());
        // 根据微信昵称查询微信id
        sekiroClient.registerHandler(new WxIdQueryAction());
        // 查询库里存在的联系人
        sekiroClient.registerHandler(new ContactListAction());
        // 列出文章列表
        sekiroClient.registerHandler(new ArticleListAction());
        // 直接执行sql
        sekiroClient.registerHandler(new QueryDbAction());

        // 列出订阅号
        sekiroClient.registerHandler(new SubscriptionAccountListAction());
    }
}
