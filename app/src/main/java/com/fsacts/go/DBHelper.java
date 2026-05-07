package com.fsacts.go;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "go_location.db";
    public static final int DB_VERSION = 2;

    public static final String CUSTOM_TABLE = "LOCATION";
    public static final String COL_ID = "ID";
    public static final String COL_LOCATION_TITLE = "LOCATION_TITLE";
    public static final String COL_LOCATION_DATE = "LOCATION_DATE";
    public static final String COL_LOCATION_TIME = "LOCATION_TIME";
    public static final String COL_LOCATION_LATITUDE = "LOCATION_LATITUDE";
    public static final String COL_LOCATION_LONGITUDE = "LOCATION_LONGITUDE";
    public static final String COL_LOCATION_ADDRESS = "LOCATION_ADDRESS";
    public static final String COL_LOCATION_NOTE = "LOCATION_NOTE";
    public static final String COL_CAPTURED_AT = "CAPTURED_AT";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + CUSTOM_TABLE + " ("
                        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COL_LOCATION_DATE + " TEXT NOT NULL, "
                        + COL_LOCATION_TIME + " TEXT NOT NULL, "
                        + COL_LOCATION_LATITUDE + " REAL NOT NULL, "
                        + COL_LOCATION_LONGITUDE + " REAL NOT NULL, "
                        + COL_LOCATION_ADDRESS + " TEXT NOT NULL, "
                        + COL_LOCATION_TITLE + " TEXT NOT NULL DEFAULT 'New Location', "
                        + COL_LOCATION_NOTE + " TEXT NOT NULL DEFAULT '', "
                        + COL_CAPTURED_AT + " INTEGER NOT NULL DEFAULT 0"
                        + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + CUSTOM_TABLE + " ADD COLUMN " + COL_LOCATION_TITLE + " TEXT NOT NULL DEFAULT 'New Location'");
            } catch (Exception ignored) {
            }
            try {
                db.execSQL("ALTER TABLE " + CUSTOM_TABLE + " ADD COLUMN " + COL_LOCATION_NOTE + " TEXT NOT NULL DEFAULT ''");
            } catch (Exception ignored) {
            }
            try {
                db.execSQL("ALTER TABLE " + CUSTOM_TABLE + " ADD COLUMN " + COL_CAPTURED_AT + " INTEGER NOT NULL DEFAULT 0");
            } catch (Exception ignored) {
            }
        }
    }

    public boolean addOne(LocationModel locationModel) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            if (isDuplicate(db, locationModel)) {
                return false;
            }

            ContentValues values = new ContentValues();
            values.put(COL_LOCATION_DATE, locationModel.getLocationDate());
            values.put(COL_LOCATION_TIME, locationModel.getLocationTime());
            values.put(COL_LOCATION_LATITUDE, locationModel.getLatitude());
            values.put(COL_LOCATION_LONGITUDE, locationModel.getLongitude());
            values.put(COL_LOCATION_ADDRESS, locationModel.getAddress());
            values.put(COL_LOCATION_TITLE, locationModel.getTitle());
            values.put(COL_LOCATION_NOTE, locationModel.getNote());
            values.put(COL_CAPTURED_AT, locationModel.getCapturedAtMillis());

            return db.insert(CUSTOM_TABLE, null, values) != -1;
        } finally {
            db.close();
        }
    }

    public boolean updateLocation(long locationId, String newLocationTitle, String newLocationNote) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_LOCATION_TITLE, normalizeTitle(newLocationTitle));
            values.put(COL_LOCATION_NOTE, normalizeNote(newLocationNote));
            int rows = db.update(CUSTOM_TABLE, values, COL_ID + " = ?", new String[]{String.valueOf(locationId)});
            return rows > 0;
        } finally {
            db.close();
        }
    }

    public boolean deleteOne(long itemId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            return db.delete(CUSTOM_TABLE, COL_ID + " = ?", new String[]{String.valueOf(itemId)}) > 0;
        } finally {
            db.close();
        }
    }

    public List<LocationModel> getAllLocations() {
        List<LocationModel> returnList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    CUSTOM_TABLE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    COL_CAPTURED_AT + " DESC, " + COL_ID + " DESC"
            );

            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION_TIME));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LOCATION_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LOCATION_LONGITUDE));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION_ADDRESS));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION_TITLE));
                String note = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION_NOTE));
                long capturedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CAPTURED_AT));

                returnList.add(new LocationModel(
                        id,
                        capturedAt,
                        date,
                        time,
                        latitude,
                        longitude,
                        address,
                        normalizeTitle(title),
                        normalizeNote(note)
                ));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return returnList;
    }

    private boolean isDuplicate(SQLiteDatabase db, LocationModel locationModel) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT 1 FROM " + CUSTOM_TABLE
                            + " WHERE abs(" + COL_LOCATION_LATITUDE + " - ?) < 0.00001"
                            + " AND abs(" + COL_LOCATION_LONGITUDE + " - ?) < 0.00001"
                            + " LIMIT 1",
                    new String[]{
                            String.valueOf(locationModel.getLatitude()),
                            String.valueOf(locationModel.getLongitude())
                    }
            );
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String normalizeTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "New Location";
        }
        return title.trim();
    }

    private String normalizeNote(String note) {
        return note == null ? "" : note.trim();
    }
}
