package com.virjar.robot.wechat.sdk.service.db;

import android.database.Cursor;
import android.util.Log;

import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.robot.wechat.sdk.Config;
import com.virjar.robot.wechat.sdk.WechatClassPackage;

import java.util.ArrayList;
import java.util.Arrays;

import static android.database.Cursor.FIELD_TYPE_BLOB;
import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.database.Cursor.FIELD_TYPE_STRING;


public class DbHelper {

    private static final DbHelper dbHelper = new DbHelper();
    private volatile WechatClassPackage.SQLiteDatabase mDBConnection;

    public static DbHelper getInstance() {
        return dbHelper;
    }

    public static void init() {
        RposedBridge.hookAllConstructors(WechatClassPackage.SQLiteDatabase.getClazz(), new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String path = (String) RposedHelpers.callMethod(param.thisObject, "getPath");
                if (path.endsWith("EnMicroMsg.db")) {
                    DbHelper.getInstance().setConnection(new WechatClassPackage.SQLiteDatabase(param.thisObject));
                }
            }
        });
    }

    public void setConnection(WechatClassPackage.SQLiteDatabase connection) {
        mDBConnection = connection;
    }

    public WechatClassPackage.SQLiteDatabase getConnection() {
        return mDBConnection;
    }

    public synchronized Cursor rawQuery(String sql, String[] strArr) {
        return mDBConnection.rawQuery(sql, strArr);
    }

    public void printCursor(Cursor cursor) {
        int position = cursor.getPosition();
        if (!cursor.moveToFirst()) {
            Log.d(Config.TAG, "Cursor 数据为空\n");
            return;
        }
        Log.d(Config.TAG, "************** DB Cursor start  **************\n");
        do {
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                switch (cursor.getType(i)) {
                    case FIELD_TYPE_INTEGER:
                        Log.d(Config.TAG, cursor.getColumnName(i) + ": " + cursor.getLong(i));
                        break;
                    case FIELD_TYPE_FLOAT:
                        Log.d(Config.TAG, cursor.getColumnName(i) + ": " + cursor.getDouble(i));
                        break;
                    case FIELD_TYPE_STRING:
                        Log.d(Config.TAG, cursor.getColumnName(i) + ": " + cursor.getString(i));
                        break;
                    case FIELD_TYPE_BLOB:
                        Log.d(Config.TAG, cursor.getColumnName(i) + ": " + Arrays.toString(cursor.getBlob(i)));
                        break;
                    case FIELD_TYPE_NULL:
                        Log.d(Config.TAG, cursor.getColumnName(i) + ": NULL");
                        break;
                    default:
                        Log.d(Config.TAG, cursor.getColumnName(i) + ": " + "请手动解析");
                        break;
                }
            }
            if (!cursor.isLast())
                Log.d(Config.TAG, "---------------------------------------------\n");
        } while (cursor.moveToNext());
        Log.d(Config.TAG, "************** DB Cursor end  **************\n");
        cursor.moveToPosition(position);
    }

    public interface CursorMapper<T> {
        T parse(Cursor cursor);
    }

    public static <T> ArrayList<T> queryModels(String sql, String[] strArr, CursorMapper<T> parser) {
        Cursor cursor = getInstance().rawQuery(sql, strArr);
        if (cursor == null) {
            return null;
        }
        ArrayList<T> ret = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                ret.add(parser.parse(cursor));
            }
        } finally {
            cursor.close();
        }
        return ret;
    }

    public static <T> T queryModel(String sql, String[] strArr, CursorMapper<T> parser) {
        Cursor cursor = getInstance().rawQuery(sql, strArr);
        if (cursor == null) {
            return null;
        }
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        try {
            return parser.parse(cursor);
        } finally {
            cursor.close();
        }
    }
}
