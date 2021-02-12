package ru.locarus.androidtrackerapp;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class GettingPointsString {
    static String latLon;
    private static String getData(long data){
        SimpleDateFormat sf = new SimpleDateFormat("ddMMyy;");
        sf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sf.format(new Date(data));
    }
    private static String getTime(long data){
        SimpleDateFormat sf = new SimpleDateFormat("HHmmss;");
        sf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sf.format(new Date(data));
    }
    private static String getLat (double lat){
        String latitude =  lat > 0 ? ";N;": ";S;";
        double result = Math.abs((int)lat*100 + (int)(lat%1*60) + (lat%1*60)%1);
        return String.format(Locale.US,"%09.4f",result) + latitude;
    }
    private static String getLon (double lon){
        String longitude =  lon > 0 ? ";E;": ";W;";
        double result = Math.abs((int)lon*100 + (int)(lon%1*60) + (lon%1*60)%1);

        return String.format(Locale.US,"%010.4f",result)+ longitude;
    }
    private static Integer getSpeed (float speed){
        return (int)speed;
    }
    private static Integer getAltitude (double altitude){
        return (int)altitude;
    }


    static String getString (Point point){
        StringBuilder stringBuilder = new StringBuilder();
       stringBuilder.append(getData(point.getTime()));
       stringBuilder.append(getTime(point.getTime()));
       stringBuilder.append(getLat(point.getLatitude()));
       stringBuilder.append(getLon(point.getLongitude()));
       stringBuilder.append(getSpeed(point.getSpeed()));
       stringBuilder.append(";");
       stringBuilder.append("NA;");
       stringBuilder.append(getAltitude(point.getAltitude()));
       stringBuilder.append(";NA|");
       return stringBuilder.toString();
    }


}
