package com.example.electricbillcalculator_syariful;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "electric_bill.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "bills";

    public static final String COL_ID = "id";
    public static final String COL_MONTH = "month";
    public static final String COL_UNIT = "unit";
    public static final String COL_TOTAL_CHARGES = "total_charges";
    public static final String COL_REBATE = "rebate";
    public static final String COL_FINAL_COST = "final_cost";

    public DataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MONTH + " TEXT NOT NULL, " +
                COL_UNIT + " INTEGER NOT NULL, " +
                COL_TOTAL_CHARGES + " REAL NOT NULL, " +
                COL_REBATE + " REAL NOT NULL, " +
                COL_FINAL_COST + " REAL NOT NULL" +
                ")";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
