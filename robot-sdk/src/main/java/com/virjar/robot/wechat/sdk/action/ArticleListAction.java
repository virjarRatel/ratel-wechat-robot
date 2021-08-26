package com.virjar.robot.wechat.sdk.action;

import com.virjar.ratel.api.inspect.ClassLoadMonitor;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.robot.wechat.sdk.service.db.ContactDbHelper;
import com.virjar.robot.wechat.sdk.service.db.DbHelper;
import com.virjar.robot.wechat.sdk.service.db.ObjectCursorMapper;
import com.virjar.robot.wechat.sdk.util.CommonUtils;
import com.virjar.sekiro.api.ActionHandler;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroResponse;
import com.virjar.sekiro.api.databind.AutoBind;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import external.org.apache.commons.lang3.StringUtils;

public class ArticleListAction implements ActionHandler {
    @Override
    public String action() {
        return "listArticle";
    }

    @AutoBind
    private String nickName;

    @AutoBind(defaultIntValue = 30)
    private Integer limit;

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
        String openId = ContactDbHelper.findWxIdByNickName(nickName);
        String sql;
        String[] params = null;
        if (StringUtils.isBlank(openId)) {
            sql = "select * from message  order by msgId desc limit " + limit;
        } else {
            sql = "select * from message where talker=? order by msgId desc limit " + limit;
            params = new String[]{openId};
        }
        ArrayList<Map<String, Object>> messageResult = DbHelper.queryModels(sql, params, ObjectCursorMapper.instance);
        Set<Object> ret = new LinkedHashSet<>();
        for (Map<String, Object> messageItem : messageResult) {
            Map contentMap = parseContent(messageItem);
            if (contentMap == null) {
                continue;
            }
            for (int i = 0; i < 10; i++) {
                String key = ".msg.appmsg.mmreader.category.item" + i + ".url";
                Object url = contentMap.get(key);
                if (url == null) {
                    break;
                }
                ret.add(CommonUtils.safeToString(url));
            }
            Object url = contentMap.get(".msg.appmsg.url");
            if (url != null) {
                ret.add(CommonUtils.safeToString(url));
            }
        }
        sekiroResponse.success(ret);
    }

    private Map parseContent(Map<String, Object> messageItem) {
        String content = CommonUtils.safeToString(messageItem.get("content"));
        if (StringUtils.isBlank(content)) {
            return null;
        }
        if (!content.startsWith("~SEMI_XML~")) {
            //不是图文消息，不处理
            return null;
        }
        Class decoderClass = ClassLoadMonitor.tryLoadClass("com.tencent.mm.sdk.platformtools.ba");
        //数据解码为map
        try {
            return (Map) RposedHelpers.callStaticMethod(decoderClass, "Zx", content);
        } catch (Exception e) {
            return null;
        }
    }
}
