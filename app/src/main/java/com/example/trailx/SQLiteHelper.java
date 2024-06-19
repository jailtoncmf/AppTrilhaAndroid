package com.example.trailx;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "trilha.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_TRAILS = "percurso";
    private static final String TABLE_SUMMARY = "resumo";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_TRAILS = "CREATE TABLE " + TABLE_TRAILS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "latitude REAL,"
                + "longitude REAL,"
                + "distance REAL,"
                + "timestamp INTEGER" + ")";
        db.execSQL(CREATE_TABLE_TRAILS);

        String CREATE_TABLE_SUMMARY = "CREATE TABLE " + TABLE_SUMMARY + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "origin TEXT,"
                + "destination TEXT,"
                + "timestamp INTEGER,"
                + "distance REAL,"
                + "duration INTEGER,"
                + "avg_speed REAL" + ")";
        db.execSQL(CREATE_TABLE_SUMMARY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_SUMMARY + " ADD COLUMN origin TEXT");
            db.execSQL("ALTER TABLE " + TABLE_SUMMARY + " ADD COLUMN destination TEXT");
        }
    }

    public Cursor getAllTrails() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SUMMARY, null);
    }

    public Cursor getTrailSummary(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SUMMARY + " WHERE id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteTrail(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRAILS, "id = ?", new String[]{String.valueOf(id)});
        db.delete(TABLE_SUMMARY, "id = ?", new String[]{String.valueOf(id)});
    }

    public long registerTrail(long startTime, long endTime, float totalDistance, double endLatitude, double endLongitude) {
        SQLiteDatabase db = this.getWritableDatabase();

        long duration = endTime - startTime;

        float avgSpeed = totalDistance / (duration / 1000f);

        ContentValues values = new ContentValues();
        values.put("timestamp", endTime);
        values.put("distance", totalDistance);
        values.put("duration", duration);
        values.put("avg_speed", avgSpeed);
        long trailId = db.insert(TABLE_SUMMARY, null, values);

        String insertSQL = "INSERT INTO " + TABLE_TRAILS + "(latitude, longitude, distance, timestamp) VALUES (?, ?, ?, ?)";
        db.execSQL(insertSQL, new Object[]{endLatitude, endLongitude, totalDistance, endTime});

        return trailId;
    }

    public void updateTrail(int id, String origin, String destination) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("origin", origin);
        values.put("destination", destination);
        db.update(TABLE_SUMMARY, values, "id = ?", new String[]{String.valueOf(id)});
    }
}

