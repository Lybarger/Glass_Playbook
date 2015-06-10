package com.husky.uw.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lybar_000 on 4/2/2015.
 */
public class Players {


    public static int PLAYER_COUNT = 5;
    private static String[] NAMES = new String[] {"1", "2", "3", "4", "5"};

    private int WIDTH_SCREEN = 100;
    private int HEIGHT_SCREEN = 100;

    public float[] X = new float[PLAYER_COUNT]; //Player X positions
    private float[] Xi = new float[PLAYER_COUNT];
    public float[] Y = new float[PLAYER_COUNT]; //Player Y positions
    private float[] Yi = new float[PLAYER_COUNT];
    public String[] name = new String[PLAYER_COUNT]; //Player name (ID)
    public boolean[] screenPresent = new boolean[PLAYER_COUNT];
    public float[] screenAngle = new float[PLAYER_COUNT];

    public Boolean[] selectionStatus = new Boolean[PLAYER_COUNT]; //Player selection status (selected by touch event)
    public Bitmap[] icon = new Bitmap[PLAYER_COUNT];
    public Bitmap[] iconScreenOriginal = new Bitmap[PLAYER_COUNT];
    public Bitmap[] iconScreen = new Bitmap[PLAYER_COUNT];
    private Matrix matrix = new Matrix();
    public Path[] path = new Path[PLAYER_COUNT];

    private static Context context;
    public Paint paintCircle, paintText, paintPath, paintScreen;
    private static int LINEWIDTH = 3;
    private static int ICON_SIZE = 40;

    private static int TEXT_SIZE = 30;
    private static int TEXT_SHIFT = TEXT_SIZE*6/20;

    private float courtWidthPixels;
    private float courtHeightPixels;

    public Players(Context mcontext, Court court){
        if (context == null) {
            context = mcontext;
        }
        //Define path format
        paintPath = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPath.setColor(context.getResources().getColor(R.color.husky_metallic_gold));
        paintPath.setAntiAlias(true);
        paintPath.setStrokeWidth(LINEWIDTH);
        paintPath.setStyle(Paint.Style.STROKE);
        paintPath.setStrokeJoin(Paint.Join.ROUND);
        paintPath.setStrokeCap(Paint.Cap.ROUND);

        Xi = court.getPlayerInitialPositions().get(0);
        Yi = court.getPlayerInitialPositions().get(1);


        for (int i = 0; i < PLAYER_COUNT; i++){
            X[i] = Xi[i];
            Y[i] = Yi[i];
            name[i] = NAMES[i];
            path[i] = new Path();
            path[i].moveTo(X[i], Y[i]);
            screenPresent[i] = false;
            screenAngle[i] = 0;
            selectionStatus[i] = false;
        }

        courtWidthPixels = context.getResources().getInteger(R.integer.court_width);
        courtHeightPixels = context.getResources().getInteger(R.integer.court_height);

        createIcons();
        createScreenIcons();

    }

    public Map<Integer,List<List<float[]>>> defaultData (){
        Map<Integer,List<List<float[]>>> dataNew = new HashMap<Integer,List<List<float[]>>>();
        for (int i = 0; i < PLAYER_COUNT; i++) {
            List<float[]> innerList = new ArrayList<float[]>();
            innerList.add(new float[]{Xi[i]/courtWidthPixels,Yi[i]/courtHeightPixels,0,0});
            List<List<float[]>> outerList = new ArrayList<List<float[]>>();
            outerList.add(innerList);
            dataNew.put(i,outerList);
        }
        return dataNew;
    }


    public void reinitialize(){

        for (int i = 0; i < PLAYER_COUNT; i++){
            X[i] = Xi[i];
            Y[i] = Yi[i];
            selectionStatus[i] = false;
            screenPresent[i] = false;
            screenAngle[i] = 0;
        }
    }

    public void createIcons() {
        paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setColor(context.getResources().getColor(R.color.husky_purple));


        //Define text format
        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setTextSize(TEXT_SIZE);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setColor(Color.WHITE);
        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);
        paintText.setTypeface(tf);

        for (int i = 0; i < PLAYER_COUNT; i++) {
            icon[i] = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888);

            // Add canvas to each bitmap in array
            Canvas temporaryCanvas = new Canvas(icon[i]);

            // Draw circle on canvas, for player icon
            temporaryCanvas.drawCircle(ICON_SIZE / 2, ICON_SIZE / 2, ICON_SIZE / 2, paintCircle);

            // Add player name/number two player icon
            temporaryCanvas.drawText(name[i], ICON_SIZE / 2, ICON_SIZE / 2 + TEXT_SHIFT, paintText);
        }
    }

    // Create icons representing screens (blocks)
    public void createScreenIcons() {
        // Define paint
        paintScreen = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintScreen.setStyle(Paint.Style.STROKE);
        paintScreen.setTextAlign(Paint.Align.CENTER);
        paintScreen.setStrokeWidth(LINEWIDTH);
        paintScreen.setColor(context.getResources().getColor(R.color.husky_dark_gray));

        // Loop on players
        for (int i = 0; i < PLAYER_COUNT; i++) {
            // Create temporary icon and canvas
            iconScreenOriginal[i] = Bitmap.createBitmap(WIDTH_SCREEN + LINEWIDTH, HEIGHT_SCREEN + LINEWIDTH, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(iconScreenOriginal[i]);

            // Create T shaped drawing
            canvas.drawLine((float) HEIGHT_SCREEN * 3 / 4, (float) WIDTH_SCREEN / 2, HEIGHT_SCREEN, (float) WIDTH_SCREEN / 2, paintScreen);
            canvas.drawLine(HEIGHT_SCREEN, WIDTH_SCREEN / 3, HEIGHT_SCREEN, WIDTH_SCREEN * 2 / 3, paintScreen);

            // Create rotation matrix
            matrix.setRotate(screenAngle[i]);

            // Create rotated icon, based on rotation matrix
            iconScreen[i] = Bitmap.createBitmap(iconScreenOriginal[i], 0, 0, iconScreenOriginal[i].getWidth(), iconScreenOriginal[i].getHeight(), matrix, true);
        }
    }

    // Update screen icons
    public void updateScreenIcons(){
        for (int i = 0; i < PLAYER_COUNT; i++) {
            if (screenPresent[i]) {
                matrix.setRotate(screenAngle[i]);

                // Create rotated icon, based on rotation matrix
                iconScreen[i] = Bitmap.createBitmap(iconScreenOriginal[i], 0, 0, iconScreenOriginal[i].getWidth(), iconScreenOriginal[i].getHeight(), matrix, true);
            }
        }
    }

    // Update screen icon (just one icon)
    public void updateScreenIcon(int i){

        if (screenPresent[i]) {
            matrix.setRotate(screenAngle[i]);

            // Create rotated icon, based on rotation matrix
            iconScreen[i] = Bitmap.createBitmap(iconScreenOriginal[i], 0, 0, iconScreenOriginal[i].getWidth(), iconScreenOriginal[i].getHeight(), matrix, true);
        }

    }
    // Update player locations and build map of points
    public int updatePositions(MotionEvent event, Location touch) {

        int output = -1;

        for (int j = 0; j < PLAYER_COUNT; j++) {

            // Minimum distance between jth player and other players
            float minDistanceToOtherPlayers = Float.POSITIVE_INFINITY;

            // Too close to other players?
            boolean obstacleEncountered = false;

            // Loop on players
            for (int i = 0; i < PLAYER_COUNT; i++) {

                if (i != j) {

                    // Distance to other players
                    float distance = euclideanDistance(touch.X, X[i], touch.Y, Y[i]);

                    // Minimum distance to other players
                    minDistanceToOtherPlayers = Math.min(distance, minDistanceToOtherPlayers);

                    // Other player encountered?
                    obstacleEncountered = obstacleEncountered || (minDistanceToOtherPlayers < (float) ICON_SIZE);

                }
            }

            // Check screen bounds
            obstacleEncountered = obstacleEncountered || (touch.X < 0 + ICON_SIZE/2) || (touch.X > courtWidthPixels - ICON_SIZE/2)
                    || (touch.Y < 0 + ICON_SIZE/2) || (touch.Y > courtHeightPixels - ICON_SIZE/2);

            // Distance between touch event and jth player
            float distanceToTouch = euclideanDistance(touch.X, X[j],
                    touch.Y, Y[j]);

            //Address each touch event
            switch (event.getAction()) {

                //Touch DOWN
                case MotionEvent.ACTION_DOWN:
                    // Determine if touch event within bounds of jth player icon
                    selectionStatus[j] = (distanceToTouch < ((float) ICON_SIZE) / 2);

                    if (selectionStatus[j]) {
                        // Move path to location of current touch event
                        path[j].moveTo(X[j], Y[j]);

                        //output = j;
                    }
                    break;

                //Touch MOVE
                case MotionEvent.ACTION_MOVE:

                    // If jth player selected and no other players encountered
                    // update jth player position
                    if (selectionStatus[j] && !obstacleEncountered) {

                        // Determine midpoints between patch location and jth
                        // player location
                        float avgX = (touch.X + X[j]) / 2;
                        float avgY = (touch.Y + Y[j]) / 2;

                        // Build path using quadratic
                        path[j].quadTo(X[j], Y[j], avgX, avgY);

                        // Set jth player location
                        screenAngle[j] = (float)Math.toDegrees(Math.atan2(touch.Y - Y[j], touch.X - X[j]));
                        if (screenAngle[j] < 0){screenAngle[j] = screenAngle[j] + 360;}

                        if(screenPresent[j]){
                            // Redraw icon
                            updateScreenIcon(j);
                        }

                        X[j] = touch.X;
                        Y[j] = touch.Y;
                        output = j;
                    }
                    break;

                //Touch UP
                case MotionEvent.ACTION_UP:
                    if (selectionStatus[j]) {

                        // Complete path outline
                        path[j].lineTo(X[j], Y[j]);

                        // Toggle selection status (selection event is complete)
                        selectionStatus[j] = false;

                        //output = j;
                    }
                    break;
                default:
            }
        }
        return output;
    }

    // Calculate Euclidean distance between points
    private float euclideanDistance(float x1, float x2, float y1, float y2) {
        return (float)Math.pow(Math.pow(x2 - x1, 2) + Math.pow(y2-y1, 2), 0.5);
    }

    //Create string from points
    private String pointsToString(float x, float y){
        return "(" + Float.toString(x) + "," + Float.toString(y) + ")";
    }


    // X coordinates for insertion (accounts for width of icon)
    public float getX(int playerIndex){
        // Determine insertion point in view that will center icon at player position
        return X[playerIndex]-(float)ICON_SIZE/2;
    }

    // Y coordinate for insertion (accounts for height of icon)
    public float getY(int playerIndex){
        // Determine insertion point in view that will center icon at player position
        return Y[playerIndex]-(float)ICON_SIZE/2;
    }

    // Move player pPath to current player position
    public void pathMoveToPlayerPosition(int i){
        path[i].moveTo(X[i], Y[i]);
    }

    // Move all player paths to current player positions
    public void pathsMoveToPlayerPositions(){
        for (int i = 0; i < PLAYER_COUNT; i++) {
            pathMoveToPlayerPosition(i);
        }
    }

    public void pathLineToPlayerPosition(int i){
        path[i].lineTo(X[i], Y[i]);
    }

    public void pathsLineToPlayerPositions(){
        for (int i = 0; i < PLAYER_COUNT; i++) {
            pathLineToPlayerPosition(i);
        }
    }

    public void pathQuadToPlayerPosition(int i, float x, float y){
        path[i].quadTo(X[i], Y[i], (X[i] + x) / 2, (Y[i] + y) / 2);
    }

    public void pathsQuadToPlayerPositions(float[] x, float[] y){
        for (int i = 0; i < PLAYER_COUNT; i++) {
            pathQuadToPlayerPosition(i, x[i], y[i]);
        }
    }

    public void updateXY(int i, float[] XY){
        X[i] = XY[0];
        Y[i] = XY[1];
    }

    public void updateData(int i, float[] data){
        X[i] = data[0];
        Y[i] = data[1];
        screenPresent[i] = data[2]==1;
        screenAngle[i] = data[3];
        if (screenPresent[i]){
            updateScreenIcon(i);
        }
    }

    public void pathsReset(){
        for (int i = 0; i < PLAYER_COUNT; i++) {
            path[i].reset();
        }
    }

    // X coordinates for insertion (accounts for width of icon)
    public float getScreenX(int i){
        // Determine insertion point in view that will center icon at player position
        return X[i] -(float)iconScreen[i].getWidth() / 2;
    }

    // Y coordinate for insertion (accounts for height of icon)
    public float getScreenY(int i){
        // Determine insertion point in view that will center icon at player position
        return Y[i] -(float)iconScreen[i].getHeight() / 2;
    }


    public void updateCanvas(Canvas canvas){
        // Update player icon positions
        for (int i = 0; i < PLAYER_COUNT; i++) {

            // Draw player icons on canvas
            canvas.drawPath(path[i], paintPath);
            canvas.drawBitmap(icon[i], getX(i), getY(i), null);

            if (screenPresent[i]){
                canvas.drawBitmap(iconScreen[i],getScreenX(i),getScreenY(i),null);
            }
        }
    }

    // Determine whether or not screen is displayed based on touch event and ball
    public void updateScreenState(MotionEvent event, Ball ball){


        for (int i = 0; i < PLAYER_COUNT; i++) {
            boolean distanceCheckScreen = (float) icon[i].getWidth()/2
                    > euclideanDistance(X[i], event.getX(), Y[i], event.getY());
            boolean ballCheck = ball.playerIndex != i;

            if (!ballCheck){
                screenPresent[i] = false;
            }
            else if(distanceCheckScreen && ballCheck) {
                screenPresent[i] = !screenPresent[i];
            }
        }
    }

    // Determine whether or not screen is displayed based on touch event and ball
    public void updateScreenState2(Ball ball){


        for (int i = 0; i < PLAYER_COUNT; i++) {

            boolean ballCheck = ball.playerIndex != i;

            if (!ballCheck){
                screenPresent[i] = false;
            }
        }
    }

    public float[] getCurrentData(int i){
        return new float[] {X[i], Y[i], screenPresent[i]?1.0f:0.0f, screenAngle[i]};

    }

}
