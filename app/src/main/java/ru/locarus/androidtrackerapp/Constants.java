package ru.locarus.androidtrackerapp;

import android.provider.BaseColumns;

public class Constants implements BaseColumns {
    static final int LOCATION_SERVICE_ID = 175;
    static final String ACTION_START_LOCATION_SERVICE = "startLocationService";
    static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationService";
    static final String TAG = "RRRR";



    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "points_base";

    static final String TABLE_NAME = "points";

    static final String _ID = BaseColumns._ID;
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String SPEED = "speed";
    static final String TIME = "time";
    static final String ALTITUDE = "altitude";

}
