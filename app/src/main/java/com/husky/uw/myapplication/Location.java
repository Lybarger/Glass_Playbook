package com.husky.uw.myapplication;

/**
 * Created by lybar_000 on 4/2/2015.
 */
public class Location {

    public float X; //Player X position
    public float Y; //Player Y position

    public Location(float X_temp, float Y_temp){
        X = X_temp;
        Y = Y_temp;

    }

    public String toString(){
        return "X: " + X + ", Y: " + Y;

    }
}