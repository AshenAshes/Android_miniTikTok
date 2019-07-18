package com.bytedance.androidcamp.network.dou.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LikeDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME="minitictok.db";
    private static final int DB_VERSION = 6;
    public LikeDbHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + LikeContract.LikeNode.TABLE_NAME
                + "(" + LikeContract.LikeNode._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LikeContract.LikeNode.COLUMN_COUNT + " INTEGER, "
                + LikeContract.LikeNode.COLUMN_STATE + " INTEGER, "
                + LikeContract.LikeNode.COLUMN_URL + " TEXT, "
                + LikeContract.LikeNode.COLUMN_DATE+" INTEGER, "
                + LikeContract.LikeNode.COLUMN_NAME+" TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        for(int j=i;j<=i1;j++){
            switch(i){
                case 5:
                    try{
                        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ LikeContract.LikeNode.TABLE_NAME);
                        sqLiteDatabase.execSQL("CREATE TABLE " + LikeContract.LikeNode.TABLE_NAME
                                + "(" + LikeContract.LikeNode._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + LikeContract.LikeNode.COLUMN_COUNT + " INTEGER, "
                                + LikeContract.LikeNode.COLUMN_STATE + " INTEGER, "
                                + LikeContract.LikeNode.COLUMN_URL + " TEXT, "
                                + LikeContract.LikeNode.COLUMN_DATE+" INTEGER, "
                                + LikeContract.LikeNode.COLUMN_NAME+" TEXT)");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                default:break;
            }
        }
    }
}
