package com.virjar.robot.wechat.sdk.service.db;

import android.database.Cursor;

import java.util.HashMap;
import java.util.Map;

public class ObjectCursorMapper implements DbHelper.CursorMapper<Map<String, Object>> {
    public static ObjectCursorMapper instance = new ObjectCursorMapper();

    private ObjectCursorMapper() {
    }

    @Override
    public Map<String, Object> parse(Cursor cursor) {
        Map<String, Object> ret = new HashMap<>();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            ret.put(cursor.getColumnName(i), getOriginData(cursor, i));
        }
        return ret;
    }

    private static Object getOriginData(Cursor cursor, int index) {
        int type = cursor.getType(index);
        switch (type) {
            case Cursor.FIELD_TYPE_STRING:
                return cursor.getString(index);
            case Cursor.FIELD_TYPE_INTEGER:
                return cursor.getLong(index);
            case Cursor.FIELD_TYPE_FLOAT:
                return cursor.getDouble(index);
            case Cursor.FIELD_TYPE_NULL:
                return null;
            case Cursor.FIELD_TYPE_BLOB:
                return cursor.getBlob(index);
            default:
                //这是由于，sqlite是一个基于字符串的嵌入式数据库，数据类型底层使用字符串描述，对上游支持的各种数据类型其实都是字符串转换过来的，所以可以使用字符串兜底
                return cursor.getString(index);

        }
    }
}
