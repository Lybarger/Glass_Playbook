package com.husky.uw.myapplication;

/**
 * Created by lybar_000 on 6/4/2015.
 */
public class PlayElements {

    public Players players;
    public Ball ball;
    public boolean playEndReached;


    public PlayElements(Players playersTemp, Ball ballTemp){
        players = playersTemp;
        ball = ballTemp;
        playEndReached = false;

    }
}
