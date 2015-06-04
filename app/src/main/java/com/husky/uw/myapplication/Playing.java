package com.husky.uw.myapplication;

import android.graphics.Canvas;

/**
 * Created by lybar_000 on 6/4/2015.
 */
public class Playing {

       /**
     * Created by lybar_000 on 6/4/2015.
     */

    public int stage = 0;
    public int frame = 0;


    public Playing(){

    }


    public PlayElements startPlay(PlayInterpolated playInterpolated, PlayElements playElements){
        // Update player and ball initial positions
        for (int playerIndex : playInterpolated.dataPlayers.keySet()){
            playElements.players.updateXY(playerIndex, playInterpolated.getXYcoordinate(playerIndex, 0, 0));
        }
        playElements.ball.updateXY(playInterpolated.dataBall.get(0).get(0));

        // Reset paths and move to initial positions
        playElements.players.pathsReset();
        playElements.players.pathsMoveToPlayerPositions();
        playElements.ball.path.reset();
        playElements.ball.path.moveTo(playElements.ball.X, playElements.ball.Y);

        return playElements;
    }
    public PlayElements updatePlay(PlayInterpolated playInterpolated, PlayElements playElements){

        playElements.playEndReached = false;

        // Determine if end of stage reached
        if (frame >= playInterpolated.FRAMES_PER_STAGE-1){

            // Increment stage
            stage++;
            // Reset frame counter within stage
            frame = 0;

            // Determine if end of play reached
            if (stage >= playInterpolated.getStageCount()){

                // Reset stage
                stage = 0;
                playElements.playEndReached = true;

                // Reset paths
                playElements.players.pathsReset();
                playElements.ball.path.reset();

                // Update player and ball location to initial starting positions
                for (int i : playInterpolated.dataPlayers.keySet()) {
                    playElements.players.updateData(i, playInterpolated.getData(i,stage,frame));
                }
                playElements.ball.updateXY(playInterpolated.dataBall.get(stage).get(frame));

                // Move paths to initial starting locations
                playElements.players.pathsMoveToPlayerPositions();
                playElements.ball.pathMoveToPosition();
            }

        }
        else{
            // Increment frame count within stage
            frame++;
        }

        playElements.ball.updateXY(playInterpolated.dataBall.get(stage).get(frame));
        playElements.ball.beingPassed = true;

        // Loop on players
        for (int playerIndex : playInterpolated.dataPlayers.keySet()){
            // Store previous XY coordinates
            float Xprevious = playElements.players.X[playerIndex];
            float Yprevious = playElements.players.Y[playerIndex];

            // Update player positions
            playElements.players.updateData(playerIndex, playInterpolated.getData(playerIndex, stage, frame));

            // Determine average XY coordinates for quadratic interpolation
            float Xavg = (Xprevious + playElements.players.X[playerIndex])/2;
            float Yavg = (Yprevious + playElements.players.Y[playerIndex])/2;

            // Update player paths using quadratic interpolation
            playElements.players.path[playerIndex].quadTo(Xprevious, Yprevious, Xavg, Yavg);

            // Determine if player has bought all or if ball is being passed
            boolean playerHasBall = ((playElements.ball.X == playElements.players.X[playerIndex])
                    &&(playElements.ball.Y == playElements.players.Y[playerIndex]));
            playElements.ball.beingPassed = playElements.ball.beingPassed && !playerHasBall;
        }

        // If ball is being passed, then draw path
        if (playElements.ball.beingPassed){playElements.ball.path.lineTo(playElements.ball.X,playElements.ball.Y); }
        // If player has ball, then do not draw path
        else {playElements.ball.path.moveTo(playElements.ball.X,playElements.ball.Y);}

        return playElements;
    }


    public void drawPlay(Canvas canvas){


    }










}
