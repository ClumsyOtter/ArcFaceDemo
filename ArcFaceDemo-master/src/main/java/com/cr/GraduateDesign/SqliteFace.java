package com.cr.GraduateDesign;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cr.GraduateDesign.ArcFaceHelper.SqliteTools;


//存储界面
class SqliteFace {
    private final String table = "facedb";
    //名字内容
    private SqliteTools sqliteTools;

    SqliteFace(Context context) {
        sqliteTools = new SqliteTools(context, "faceDB", null, 1);
    }

    void insert(String name, String face) {
        SQLiteDatabase writableDatabase = sqliteTools.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("face", face);
        long insert = writableDatabase.insert(table, null, contentValues);
        Log.i("添加成功条目：", name + "数量" + insert);
    }

    void delete(String name) {
        SQLiteDatabase writableDatabase = sqliteTools.getWritableDatabase();
        int delete = writableDatabase.delete(table, "name=?", new String[]{name});
        Log.i("删除成功", "删除条目：" + name + "  数量：" + delete);
    }

    Cursor findAllFace() {
        SQLiteDatabase readableDatabase = sqliteTools.getReadableDatabase();
        String[] columns = {"name", "face"};
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;

        return readableDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }
}
