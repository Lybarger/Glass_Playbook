package com.husky.uw.myapplication;

/**
 * Created by lybar_000 on 4/2/2015.
 */
public class Player {

    public float X; //Player X position
    public float Y; //Player Y position
    public String name; //Player name (ID)
    public Boolean selection_status; //Player selection status (selected by touch event)

    public Player(float X_temp, float Y_temp , String name_temp, Boolean selection_status_temp){
        X = X_temp;
        Y = Y_temp;
        name = name_temp;
        selection_status = selection_status_temp;
    }

    public String toString(){
        return "ID: " + name + ", X: " + X + ", Y: " + Y + ", Selected: " + selection_status;

    }
}
