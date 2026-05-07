package com.fsacts.go;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final String CUSTOM_TABLE = "LOCATION";
    public static final String COL_ID = "ID";

    public static final int DB_VERSION = 1;

    public static final String COL_LOCATION_TITLE = "LOCATION_TITLE";
    public static final String COL_LOCATION_DATE = "LOCATION_DATE";
    public static final String COL_LOCATION_TIME = "LOCATION_TIME";
    public static final String COL_LOCATION_LATITUDE = "LOCATION_LATITUDE";
    public static final String COL_LOCATION_LONGITUDE = "LOCATION_LONGITUDE";
    public static final String COL_LOCATION_ADDRESS = "LOCATION_ADDRESS";
    public static final String COL_LOCATION_NOTE = "LOCATION_NOTE";


    public DBHelper(Context context) {
        super(context, "go_location.db", null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        onUpgrade(sqLiteDatabase, 0, DB_VERSION);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        if(oldVersion <1){
            String createTableStatement = "CREATE TABLE " + CUSTOM_TABLE + " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_LOCATION_DATE + " TEXT, " + COL_LOCATION_TIME + " TEXT, " + COL_LOCATION_LATITUDE + " TEXT, " + COL_LOCATION_LONGITUDE + " TEXT, " + COL_LOCATION_ADDRESS + " TEXT)";
            sqLiteDatabase.execSQL(createTableStatement);
        }

        if(oldVersion <2){
            String alterTableFirstStatement = "ALTER TABLE " + CUSTOM_TABLE + " ADD COLUMN " + COL_LOCATION_TITLE + " TEXT" + " DEFAULT 'New Location'";
            String alterTableSecondStatement = "ALTER TABLE " + CUSTOM_TABLE + " ADD COLUMN " + COL_LOCATION_NOTE + " TEXT" + " DEFAULT '... ... ...'";

            sqLiteDatabase.execSQL(alterTableFirstStatement);
            sqLiteDatabase.execSQL(alterTableSecondStatement);
        }
    }

    //Add a record
    public boolean addOne(LocationModel locationModel){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LOCATION_DATE, locationModel.getLocation_date());
        cv.put(COL_LOCATION_TIME, locationModel.getLocation_time());
        cv.put(COL_LOCATION_LATITUDE, locationModel.getLocation_latitude());
        cv.put(COL_LOCATION_LONGITUDE, locationModel.getLocation_longitude());
        cv.put(COL_LOCATION_ADDRESS, locationModel.getLocation_address());

        //Check if the getLocation_address is already present
        SQLiteDatabase db_check = this.getReadableDatabase();

        String queryStr = "SELECT * FROM "+CUSTOM_TABLE+ " WHERE EXISTS ( SELECT " +COL_LOCATION_ADDRESS+" FROM "+CUSTOM_TABLE+ " WHERE " +COL_LOCATION_ADDRESS+ " like  '%" + locationModel.getLocation_address() + "%' )";
        Cursor cursor = db_check.rawQuery(queryStr, null);

        if(String.valueOf(cursor.moveToFirst()) == "true"){
            return false;
        }else{
            long insert = db.insert(CUSTOM_TABLE, null, cv);

            if(insert == -1){
                Log.i("DB_Error", "DB Error: Cannot insert current location");
                return false;
            }else{
                return true;
            }
        }
    }

    //Update a record
    public boolean updateLocation(int location_id, String new_location_title, String new_location_note){

        if(new_location_title.isEmpty()){
            new_location_title = "New Location";
        }

        if(new_location_note.isEmpty()){
            new_location_note = "... ... ...";
        }

        String queryStr = "UPDATE " + CUSTOM_TABLE + " SET " + COL_LOCATION_TITLE + " = '" + new_location_title.replace("'", "''")+  "', " + COL_LOCATION_NOTE + " = '" + new_location_note.replace("'", "''") + "' WHERE " + COL_ID + " = " + location_id;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(queryStr, null);

        if(cursor.moveToLast()){
            return false;
        }else{
            return true;
        }
    }

    //Delete a record
    public boolean deleteOne(int itemId){
        //Find locationModel in the database, if it found, delete it, and return true. If it is not found, return false
        String queryStr = "DELETE FROM "+CUSTOM_TABLE + " WHERE " + COL_ID + " = " + itemId;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(queryStr, null);

        if(cursor.moveToFirst()){
            return true;
        }else{
            return false;
        }
    }

    //Get all records
    public List<LocationModel> getAllLocations(){
        List<LocationModel> returnList = new ArrayList<>();

        //get data from database
        String queryStr = "SELECT * FROM "+CUSTOM_TABLE+ " ORDER BY " + COL_LOCATION_DATE + " DESC" +", SUBSTR("+ COL_LOCATION_TIME + ",0,7) DESC" ;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryStr, null);

        if(cursor.moveToFirst()){
            //loop through the cursor (result set) and crate new location objects. Put them init the result list
            do{
                int COL_ID = cursor.getInt(0);
                String COL_LOCATION_DATE = cursor.getString(1);
                String COL_LOCATION_TIME = cursor.getString(2);
                String COL_LOCATION_LATITUDE = cursor.getString(3);
                String COL_LOCATION_LONGITUDE = cursor.getString(4);
                String COL_LOCATION_ADDRESS = cursor.getString(5);
                String COL_LOCATION_TITLE = cursor.getString(6);
                String COL_LOCATION_NOTE = cursor.getString(7);

                LocationModel locationModel = new LocationModel(COL_ID, COL_LOCATION_DATE, COL_LOCATION_TIME, COL_LOCATION_LATITUDE, COL_LOCATION_LONGITUDE, COL_LOCATION_ADDRESS, COL_LOCATION_TITLE, COL_LOCATION_NOTE);
                returnList.add(locationModel);
            }while (cursor.moveToNext());
        }else{
            //Failure. Do not add anything to the list
        }

        //Close both the cursor and db when done
        cursor.close();
        db.close();

        return returnList;
    }
}
