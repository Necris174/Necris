package ru.locarus.androidtrackerapp;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class GettingPointsString {

    private static String getData(long data){
        return new SimpleDateFormat("ddMMyy;").format(data);
    }
    private static String getTime(long data){
        return new SimpleDateFormat("HHmmss;").format(data);
    }
    private static String getLat (double lat){

        String latitude = Double.toString(lat);

        String grad = latitude.substring(0,latitude.indexOf("."));
        Double minutes = Double.parseDouble(latitude.substring(latitude.indexOf(".")));
        minutes =minutes*60;
        String min = Double.toString(minutes).substring(0,7);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(grad);
        stringBuilder.append(min);
        stringBuilder.append(";N;");
        return stringBuilder.toString();
    }
    private static String getLon (double lon){
        StringBuilder stringBuilder = new StringBuilder();
        String latitude = Double.toString(lon);
        String grad = latitude.substring(0,latitude.indexOf("."));
        if (grad.length()<2){
            stringBuilder.append("00");
        } else if (grad.length()<3) {
            stringBuilder.append("0");
        }
            Double minutes = Double.parseDouble(latitude.substring(latitude.indexOf(".")));
            minutes = minutes * 60;
            String min = Double.toString(minutes).substring(0, 7);
            stringBuilder.append(grad);
            stringBuilder.append(min);
            stringBuilder.append(";E;");


        return stringBuilder.toString();
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
