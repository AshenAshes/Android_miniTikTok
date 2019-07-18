package com.bytedance.androidcamp.network.dou.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LikeDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME="minitictok.db";
    private static final int DB_VERSION = 2;
    public LikeDbHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + LikeContract.LikeNode.TABLE_NAME
                + "(" + LikeContract.LikeNode._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LikeContract.LikeNode.COLUMN_COUNT + " INTEGER, "
                + LikeContract.LikeNode.COLUMN_STATE + " INTEGER, "
                + LikeContract.LikeNode.COLUMN_URL + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
