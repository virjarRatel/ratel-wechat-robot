package com.virjar.robot.wechat.sdk.action;

import android.database.Cursor;

import com.virjar.robot.wechat.sdk.service.db.DbHelper;
import com.virjar.sekiro.api.ActionHandler;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroResponse;

import java.util.ArrayList;

import external.com.alibaba.fastjson.JSONObject;

public class ContactListAction implements ActionHandler {
    @Override
    public String action() {
        return "ContactList";
    }

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
        ArrayList<JSONObject> jsonObjects = DbHelper.queryModels("select * from rcontact", null, new DbHelper.CursorMapper<JSONObject>() {
            @Override
            public JSONObject parse(Cursor cursor) {
                String username = cursor.getString(cursor.getColumnIndex("username"));
                String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("nickname", nickname);
                return jsonObject;
            }
        });
        sekiroResponse.success(jsonObjects);
    }
}
