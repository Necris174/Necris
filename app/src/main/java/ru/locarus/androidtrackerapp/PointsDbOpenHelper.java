package ru.locarus.androidtrackerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PointsDbOpenHelper extends SQLiteOpenHelper {
    public PointsDbOpenHelper( Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_POINTS_TABLE = "CREATE TABLE " + Constants.TABLE_NAME + "("
                + Constants._ID + " INTEGER PRIMARY KEY,"
                + Constants.LATITUDE + " REAL NOT NULL,"
                + Constants.LONGITUDE + " REAL NOT NULL,"
                + Constants.SPEED + " REAL NOT NULL,"
                + Constants.TIME + " REAL NOT NULL,"
                + Constants.ALTITUDE + " REAL NOT NULL" + ")";
        db.execSQL(CREATE_POINTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.DATABASE_NAME);
        onCreate(db);
    }
    public void addPoint (Point point){
        //Получение объекта БД
        SQLiteDatabase db = this.getWritableDatabase();
        //Взаимодействие с БД и передача ключа и значения
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.LATITUDE, point.getLatitude());
        contentValues.put(Constants.LONGITUDE, point.getLongitude());
        contentValues.put(Constants.SPEED, point.getSpeed());
        contentValues.put(Constants.TIME, point.getTime());
        contentValues.put(Constants.ALTITUDE, point.getAltitude());
        //Запись в БД
        db.insert(Constants.TABLE_NAME,null,contentValues);
        db.close();
    }
    public Point getPoint(int id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Constants.TABLE_NAME, new String[] {
                Constants._ID,
                Constants.LATITUDE,
                Constants.LONGITUDE,
                Constants.SPEED,
                Constants.TIME,
                Constants.ALTITUDE },
                Constants._ID + "=?",
                new String[] {String.valueOf(id)},
                null, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        cursor.close();

        return new Point(cursor.getInt(0), cursor.getDouble(1),
                cursor.getDouble(2),
                cursor.getFloat(3),
                cursor.getDouble(4),
                cursor.getDouble(5));
    }
    public void deletePoint(Point point){
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d(Constants.TAG, "Delete point: " + point.getId());
        db.delete(Constants.TABLE_NAME,Constants._ID + "=?", new String[]{String.valueOf(point.getId())});

    }

    public List<Point> getAllPoints(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Point> pointList = new ArrayList<>();
        String selectPoints = "SELECT * FROM " + Constants.TABLE_NAME;
        Cursor cursor = db.rawQuery(selectPoints,null);
        if (cursor.moveToFirst()){
            for (int i = 0; i < 20 ; i++) {
                cursor.moveToNext();
                    Point point = new Point(cursor.getInt(0),
                            cursor.getDouble(1),
                            cursor.getDouble(2),
                            cursor.getFloat(3),
                            cursor.getDouble(4),
                            cursor.getDouble(5));
                    pointList.add(point);
            }

        }
        cursor.close();
        return pointList;
    }
}
