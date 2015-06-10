package com.husky.uw.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lybar_000 on 5/6/2015.
 */
public class Ball {
    private int WIDTH = 40;
    private int LINEWIDTH_DEFAULT = 5;
    private int LINEWIDTH_SELECTED = 10;
    private int LINEWIDTH_PATH = 3;
    private float[] dashLength = new float[]{10.0f, 10.0f};
    private static Context context;
    public float X; //Ball X position
    public float Y; //Ball Y position
    public int playerIndex; //Player with ball (Note: value of -1 denotes hoop)
    public int playerIndexInitial;
    public boolean beingPassed; //Ball being passed
    public boolean selected; //Player selection status (selected by touch event)
    public Paint paint, paintPath;
    public Bitmap icon;
    public Path path;


    // Constructor
    public Ball(int playerIndexTemp, Context mcontext, Court court){

        // Index of player with ball
        playerIndex = playerIndexTemp;

        // Initial index of player with ball
        playerIndexInitial = playerIndex;

        // Ball XY coordinates
        X = court.getPlayerInitialPositions().get(0)[playerIndexTemp];
        Y = court.getPlayerInitialPositions().get(1)[playerIndexTemp];
        //pathMoveToPosition();

        // Ball being passed?
        beingPassed = false;

        // Ball selected?
        selected = false;


        if (context == null) {
            context = mcontext;
        }

        // Create ball icon and path
        createIcon();
        createPath();
    }

    // Default ball data
    public List<Integer> defaultData(){
        List<Integer> dataNew = new ArrayList<Integer>();
        dataNew.add(playerIndexInitial);
        return dataNew;
    }

    // Create ball icon as bitmap
    public void createIcon() {

        int size;
        float radius;
        float midpoint;

        // Define ball format if selected
        if (selected) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(LINEWIDTH_SELECTED);
            paint.setColor(context.getResources().getColor(R.color.Red));

            size = WIDTH + 2*LINEWIDTH_SELECTED;
            radius = (float)WIDTH/2 + LINEWIDTH_SELECTED/2;

        }
        // Define ball format if NOT selected
        else {

            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(LINEWIDTH_DEFAULT);
            paint.setColor(context.getResources().getColor(R.color.DarkOrange));

            size = WIDTH + 2*LINEWIDTH_DEFAULT;
            radius = (float)WIDTH/2 + LINEWIDTH_DEFAULT/2;

        }

        // Create icon as bitmap
        icon = Bitmap.createBitmap(size, size,Bitmap.Config.ARGB_8888);

        // Create canvas and draw circle, representing ball
        Canvas temporaryCanvas = new Canvas(icon);
        midpoint = (float)size/2;
        temporaryCanvas.drawCircle(midpoint, midpoint, radius, paint);

    }

    // X coordinates for insertion (accounts for width of icon)
    public float getX(){
        // Determine insertion point in view that will center icon at player position
        return X-(float)icon.getWidth() / 2;
    }

    // Y coordinate for insertion (accounts for height of icon)
    public float getY(){
        // Determine insertion point in view that will center icon at player position
        return Y-(float)icon.getHeight()/2;
    }

    // Create path and path format
    public void createPath(){
        // Define formatting for path
        paintPath = new Paint(Paint.ANTI_ALIAS_FLAG);
        DashPathEffect dashPathEffect = new DashPathEffect(dashLength, 0);
        paintPath.setPathEffect(dashPathEffect);
        paintPath.setColor(context.getResources().getColor(R.color.husky_metallic_gold));
        paintPath.setAntiAlias(true);
        paintPath.setStrokeWidth(LINEWIDTH_PATH);
        paintPath.setStyle(Paint.Style.STROKE);

        // Create path
        path = new Path();
        pathMoveToPosition();
    }

    // Update location
    public void updateLocation(float XTemp, float YTemp){
        X = XTemp;
        Y = YTemp;
    }

    // Update ball on double tap
    public void updateBall(float XTemp, float YTemp, int playerIndexTemp, boolean selectedTemp){

        playerIndex = playerIndexTemp;
        selected = selectedTemp;
        X = XTemp;
        Y = YTemp;

        path.lineTo(X + 0.1f, Y + 0.1f);

        createIcon();
    }

    // Update ball selection status
    // Called by double tap
    public Boolean updateStatus(Players players, Hoop hoop, Location touch) {
        // Determine if ball touched by double tap
        boolean distanceCheckBall = (float) icon.getWidth() / 2 >
                euclideanDistance(X, touch.X, Y, touch.Y);

        if (distanceCheckBall) {

            // Ball double tapped, so toggle selection state
            selected = !selected;

            // Update ball icon, based on new selection state
            createIcon();

            if (playerIndex >= 0) {
                path.moveTo(players.X[playerIndex], players.Y[playerIndex]);
            } else {
                path.moveTo(hoop.X, hoop.Y);
            }
        }
        return distanceCheckBall;
    }

    // Update ball position
    public void updateBallPosition(MotionEvent event, Location location, Players players, Hoop hoop){
        // Only modified ball if previously selected through double tap
        if (selected){

            float distance;

            // Evaluate touch actions
            switch (event.getAction()) {

                //Touch MOVE
                case MotionEvent.ACTION_MOVE:
                    // Update ball location based on touch
                    updateLocation(location.X, location.Y);

                    break;

                //Touch UP
                case MotionEvent.ACTION_UP:
                    for (int i = 0; i < players.PLAYER_COUNT; i++) {

                        distance = euclideanDistance(location.X, players.X[i],
                                location.Y, players.Y[i]);

                        if (distance < players.icon[0].getHeight()/2){
                            updateBall(players.X[i], players.Y[i], i, false);
                        }
                        else {
                            if (playerIndex >= 0) {
                                updateLocation(players.X[playerIndex], players.Y[playerIndex]);
                            }
                            else {
                                updateLocation(hoop.X, hoop.Y);
                            }
                        }
                    }

                    distance = euclideanDistance(location.X, hoop.X,
                            location.Y, hoop.Y);

                    if (distance<players.icon[0].getHeight()/2){
                        updateBall(hoop.X, hoop.Y, -1, false);
                    }
                    else {
                        if (playerIndex >= 0) {
                            updateLocation(players.X[playerIndex], players.Y[playerIndex]);
                        }
                        else {
                            updateLocation(hoop.X, hoop.Y);
                        }
                    }
                    break;
                default:
            }
        }
        else {
            if (playerIndex >= 0) {
                X = players.X[playerIndex];
                Y = players.Y[playerIndex];
            }
            else {
                X = hoop.X;
                Y = hoop.Y;
            }
        }

    }

    // Reinitialize ball (location and icon format)
    public void reinitialize(Players players){
        playerIndex = 0;
        X = players.X[playerIndex];
        Y = players.Y[playerIndex];
        selected = false;
        path.reset();
        pathMoveToPosition();
        createIcon();

    }

    public void updateXY(float[] XY){
        X = XY[0];
        Y = XY[1];
    }

    public void pathMoveToPosition(){
        path.moveTo(X, Y);
    }

    public void pathLineToPosition(){
        path.lineTo(X, Y);
    }

    // Calculate Euclidean distance between points
    private float euclideanDistance(float x1, float x2, float y1, float y2) {
        return (float)Math.pow(Math.pow(x2 - x1, 2) + Math.pow(y2-y1, 2), 0.5);
    }


}