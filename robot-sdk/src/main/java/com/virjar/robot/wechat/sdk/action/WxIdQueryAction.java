package com.virjar.robot.wechat.sdk.action;

import android.util.Log;

import com.virjar.robot.wechat.sdk.Config;
import com.virjar.robot.wechat.sdk.service.db.ContactDbHelper;
import com.virjar.sekiro.api.ActionHandler;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroResponse;
import com.virjar.sekiro.api.databind.AutoBind;

import external.org.apache.commons.lang3.StringUtils;

public class WxIdQueryAction implements ActionHandler {

    @AutoBind(require = true)
    private String nickname;

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
        Log.i(Config.TAG, "query contact info for: " + nickname);
        String wxId = ContactDbHelper.findWxIdByNickName(nickname);
        if (StringUtils.isBlank(wxId)) {
            sekiroResponse.failed("not found");
        } else {
            sekiroResponse.success(wxId);
        }
    }

    @Override
    public String action() {
        return "wxIdQuery";
    }
}
