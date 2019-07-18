package com.bytedance.androidcamp.network.dou.db;

import android.provider.BaseColumns;

public class LikeContract {
    private LikeContract(){

    }
    public static class LikeNode implements BaseColumns{
        public static final String TABLE_NAME="likecount";
        public static final String COLUMN_COUNT="count";
        public static final String COLUMN_STATE="state";
        public static final String COLUMN_URL="url";
        public static final String COLUMN_DATE="date";
        public static final String COLUMN_NAME="name";
    }

}
