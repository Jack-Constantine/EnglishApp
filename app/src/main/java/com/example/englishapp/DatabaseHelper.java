package com.example.englishapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "english_data.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "words";
    private static final String COLUMN_ID = "id";
    public static final String COLUMN_WORD = "word";
    private static final String COLUMN_FREQUENCY = "frequency";
    private static final String COLUMN_LOCATION = "location";

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建数据表
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_WORD + " TEXT," +
                COLUMN_FREQUENCY + " TEXT," +
                COLUMN_LOCATION + " TEXT" +
                ")";
        db.execSQL(createTableQuery);

        // 从 CSV 文件导入数据到数据库
        importDataFromCSV(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    private void importDataFromCSV(SQLiteDatabase db) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.data);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_WORD, data[0]);
                    values.put(COLUMN_FREQUENCY, data[1]);
                    values.put(COLUMN_LOCATION, data[2]);
                    db.insert(TABLE_NAME, null, values);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor searchData(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_WORD, COLUMN_FREQUENCY, COLUMN_LOCATION};
        String selection = COLUMN_WORD + " LIKE ?";
        String[] selectionArgs = {"%" + query + "%"};
        return db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
    }
}
