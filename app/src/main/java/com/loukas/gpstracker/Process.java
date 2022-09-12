package com.loukas.gpstracker;


import android.location.Location;

public class Process {

    private double kar_x ,kar_y;
    private double x ,y;
    private double distance;

    public Process(double kar_x, double kar_y, double x, double y) {
        this.kar_x = kar_x;
        this.kar_y = kar_y;
        this.x = x;
        this.y = y;
        this.distance=0;
    }

    public Process() {
    }

    public double CalculateDistance(){
        float[] results = new float[1];
        Location.distanceBetween(this.kar_x, this.kar_y,this.x, this.y,results);
        this.setDistance(results[0]);
        return this.distance;
    }

    public String toString(){
        String str = new String("kar_x:"+ kar_x + " kar_y:"+ kar_y +" x:"+ x + " y:" + y + " distance:" + distance);
        return str;
    }

    public double getKar_x() {
        return kar_x;
    }

    public void setKar_x(double kar_x) {
        this.kar_x = kar_x;
    }

    public double getKar_y() {
        return kar_y;
    }

    public void setKar_y(double kar_y) {
        this.kar_y = kar_y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
