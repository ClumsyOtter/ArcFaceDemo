package com.arcsoft.GraduateDesign.ArcFaceHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SqliteTools extends SQLiteOpenHelper {

    //创建一个数据库
    String sql = "create table facedb( id integer primary key autoincrement not null, " +
            " name varchar(20) not null,face text not null)";

    public SqliteTools(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version) {
        super(context, name, cursorFactory, version);
    }

    //数据库创建
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(sql);
    }

    //数据库升级
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
