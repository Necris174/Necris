package ru.locarus.androidtrackerapp;

import java.io.Serializable;

public class Point implements Serializable {

    private int id;
    private double latitude;
    private double longitude;
    private float speed;
    private double time;
    private double altitude;

    public Point(double latitude, double longitude, float speed, double time, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.time = time;
        this.altitude = altitude;
    }

    public Point(int id, double latitude, double longitude, float speed, double time, double altitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.time = time;
        this.altitude = altitude;
    }

    public Point() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getSpeed() {
        return speed;
    }

    public double getTime() {
        return time;
    }

    public double getAltitude() {
        return altitude;
    }
}
