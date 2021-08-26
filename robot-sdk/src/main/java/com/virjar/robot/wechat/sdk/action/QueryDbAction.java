package com.virjar.robot.wechat.sdk.action;

import com.virjar.robot.wechat.sdk.service.db.DbHelper;
import com.virjar.robot.wechat.sdk.service.db.ObjectCursorMapper;
import com.virjar.sekiro.api.ActionHandler;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroResponse;
import com.virjar.sekiro.api.databind.AutoBind;

public class QueryDbAction implements ActionHandler {
    @Override
    public String action() {
        return "queryWithSql";
    }

    @AutoBind
    private String sql;

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {

        sekiroResponse.success(
                DbHelper.queryModels(sql, null, ObjectCursorMapper.instance)
        );
    }
}
