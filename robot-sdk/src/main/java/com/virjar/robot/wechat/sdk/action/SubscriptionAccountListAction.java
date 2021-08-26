package com.virjar.robot.wechat.sdk.action;

import com.virjar.robot.wechat.sdk.service.db.DbHelper;
import com.virjar.robot.wechat.sdk.service.db.ObjectCursorMapper;
import com.virjar.sekiro.api.ActionHandler;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroResponse;

public class SubscriptionAccountListAction implements ActionHandler {
    @Override
    public String action() {
        return "listSubscriptionAccount";
    }

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
        sekiroResponse.success(
                DbHelper.queryModels(
                        "select * from rcontact  where type='3'", null,
                        ObjectCursorMapper.instance
                )
        );
    }
}
