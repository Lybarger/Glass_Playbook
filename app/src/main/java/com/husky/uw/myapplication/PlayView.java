package com.husky.uw.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by lybar_000 on 3/10/2015.
 */
public class PlayView extends View {

    //Define graphics variables
    private Paint paint_circle, paint_canvas, paint_text, paint_path;
    private Canvas canvasPlay;
    private Bitmap bitmap_play;
    private Path path_play;

    //Player definition constants
    private int PLAYER_ICON_SIZE = 40;
    private int selection_size = PLAYER_ICON_SIZE*1;
    private int text_size = 30;
    private int text_shift = text_size*6/20;

    //Define initial player positions
    private static int PLAYER_COUNT = 5;
    private static int[] INITIAL_X = new int[] {400, 100, 700, 250, 550};
    private static int[] INITIAL_Y = new int[] {200, 350, 350, 500, 500};
    public Player[] players = new Player[PLAYER_COUNT];
    private Map<String,List<String>> points;

    // Related to animation
    private Context context;
    private android.os.Handler handler;
    private final int FRAME_RATE = 30;
    private Bitmap[] playerIcons = new Bitmap[PLAYER_COUNT];
    private int pointIndex = 0;

    public boolean playExisting = false;
    private final int STAGE_LENGTH = 3;   //Time in seconds
    private int stageFrameCount = 0;
    private int currentStage = 0;
    private Play interpolatedPlay;
    private int FRAMES_PER_STAGE = FRAME_RATE*STAGE_LENGTH;

        //Instantiate view
    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Create handler for animation thread
        this.context = context;
        handler = new android.os.Handler();

        setupDrawing();

    }

    // Create runnable animation thread
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // Triggers update of canvas
            invalidate();
        }
    };

    //Set up hash map to store position information
    public void setupDataPoints(Map<String,List<String>> points){
        this.points = points;
    }

    private void setupDrawing(){
        //Define circle format
        paint_circle = new Paint();
        paint_circle.setStyle(Paint.Style.FILL);
        paint_circle.setColor(getResources().getColor(R.color.husky_purple));

        //Define text format
        paint_text = new Paint();
        paint_text.setTextSize(text_size);
        paint_text.setTextAlign(Paint.Align.CENTER);
        paint_text.setColor(Color.WHITE);
        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);
        paint_text.setTypeface(tf);

        //Define path format
        paint_path = new Paint();
        paint_path.setColor(getResources().getColor(R.color.husky_metallic_gold));
        paint_path.setAntiAlias(true);
        paint_path.setStrokeWidth(5);
        paint_path.setStyle(Paint.Style.STROKE);
        paint_path.setStrokeJoin(Paint.Join.ROUND);
        paint_path.setStrokeCap(Paint.Cap.ROUND);

        // Create canvas
        paint_canvas = new Paint(Paint.DITHER_FLAG);

        // Create path
        path_play = new Path();

        // Create player icons, consisting of circle and player name/number
        for (int i = 0; i < PLAYER_COUNT; i++) {
            // Create array of player icons as bitmap
            playerIcons[i] = Bitmap.createBitmap(PLAYER_ICON_SIZE, PLAYER_ICON_SIZE, Bitmap.Config.ARGB_8888);

            // Add canvas to each bitmap in array
            Canvas temporaryCanvas = new Canvas(playerIcons[i]);

            // Draw circle on canvas, for player icon
            temporaryCanvas.drawCircle(PLAYER_ICON_SIZE/2, PLAYER_ICON_SIZE/2, PLAYER_ICON_SIZE/2, paint_circle);

            // Add player name/number two player icon
            temporaryCanvas.drawText(Integer.toString(i + 1), PLAYER_ICON_SIZE/2, PLAYER_ICON_SIZE/2 + text_shift,paint_text);

            // Initialize players, including location, name, and selection status
            players[i]=new Player(INITIAL_X[i], INITIAL_Y[i], Integer.toString(i + 1), false);
        }
    }

    //Create and display blank canvas
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap_play = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvasPlay = new Canvas(bitmap_play);
    }

    //Initial insertion of players onto court
    public void initialPlayerInsert(){
        //Repository for single XY coordinate
        List<String> data;
        
        //Loop on players
        for (int i=0; i<PLAYER_COUNT; i++) {
            // Place players in initial positions
            players[i]=new Player(INITIAL_X[i], INITIAL_Y[i], Integer.toString(i + 1), false);

            //Push initial positions into map
            data = points.get(Integer.toString(i + 1));
            data.add(pointsToString(INITIAL_X[i],INITIAL_Y[i]));
        }
    }

    @Override
    // onDraw is called when:
    // 1) Initial draw of view
    // 2) Function invalidate() (Note that this is part of the animation thread/runnable)
    protected void onDraw(Canvas canvas) {

        // Update frame if replaying existing play
        if (playExisting) {
            updatePlay();
            pointIndex++;
        }

        // Update player icon positions
        for (int i = 0; i < PLAYER_COUNT; i++) {

            // Determine location of player icon, with offset
            float X = players[i].X-PLAYER_ICON_SIZE/2;
            float Y = players[i].Y-PLAYER_ICON_SIZE/2;

            // Draw player icons on canvas
            canvas.drawBitmap(playerIcons[i], X, Y, null);
        }

        // Need to determine if necessary
        canvas.drawBitmap(bitmap_play, 0, 0, paint_canvas);

        // Draw path
        canvas.drawPath(path_play, paint_path);

        //Call in invalidate() using animation thread
        handler.postDelayed(runnable, FRAME_RATE);
    }


    //Capture and handle touch events during play recording
    public boolean onTouchEvent(MotionEvent event) {

        // If replaying play, stop on touch
        stopPlay();

        //Capture touch location
        Location locationTouch = locationProjected(event.getX(), event.getY());

        //Update player positions
        for (int i=0; i<PLAYER_COUNT; i++) {
            players[i] = positionUpdate(event,locationTouch, players[i]);
        }

        //Clear the canvas
        clear_canvas();

        //Draw players on canvas
        for (int i=0; i<PLAYER_COUNT; i++) {
            //drawPlayer(players[i]);
        }

        //Draw path from most recent player move
        canvasPlay.drawPath(path_play, paint_path);

        //Invalidate canvas, causing it to redraw
        //invalidate();

        return true;
    }

/*    //Draw individual player
    private void drawPlayer(Player player){
        canvasPlay.drawCircle(player.X, player.Y, PLAYER_ICON_SIZE, paint_circle);
        canvasPlay.drawText(player.name, player.X, player.Y + text_shift,paint_text);
    }*/

    //Clear canvas (Used to remove path)
    public void clear_canvas(){
        canvasPlay.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }
    
    //Project location
    private Location locationProjected(float x, float y){
        float x_projected = x;
        float y_projected = y;

        //Currently this method does nothing.
        //It will be used if there needs to be a coordinate transformation.
        return new Location(x_projected, y_projected);

    }

    //Update player position and selection status based on touch events
    private Player positionUpdate(MotionEvent event, Location locationTouch, Player player) {
        double delta_X = locationTouch.X - player.X;
        double delta_Y = locationTouch.Y - player.Y;
        double distance = Math.pow(Math.pow(delta_X, 2) + Math.pow(delta_Y, 2), 0.5);

        //Create repository points traversed by player
        List<String> data = points.get(player.name);

        //Address each touch event
        switch (event.getAction()) {

            //Touch DOWN
            case MotionEvent.ACTION_DOWN:
                player.selection_status = (distance < selection_size);
                if(player.selection_status) {
                    path_play.moveTo(player.X, player.Y);
                    data.add(pointsToString(player.X, player.Y));
                }
                break;

            //Touch MOVE
            case MotionEvent.ACTION_MOVE:
                if(player.selection_status) {
                    path_play.lineTo(locationTouch.X, locationTouch.Y);
                    player.X=locationTouch.X;
                    player.Y=locationTouch.Y;
                    data.add(pointsToString(player.X, player.Y));
                }
                break;

            //Touch UP
            case MotionEvent.ACTION_UP:
                if(player.selection_status) {
                    player.X=locationTouch.X;
                    player.Y=locationTouch.Y;
                    player.selection_status = false;
                    path_play.reset();
                    data.add(pointsToString(player.X, player.Y));
                }
                break;
            default:
                //return false;
        }
        return player;
    }

    //Create string from points
    private String pointsToString(float x, float y){
        return "(" + Float.toString(x) + "," + Float.toString(y) + ")";
    }

    // Start play
    public void startPlay(Map<Integer,List<List<float[]>>> playMap){

        // Allow play update method to be called from onDraw
        playExisting = true;

        // Create play as Play, based on points imported from XML file
        Play originalPlay = new Play(playMap, 0, 0, 0, false);

/*        // Initialize player objects and position
        for (int i=0; i<PLAYER_COUNT; i++) {
            //Define player initial positions
//            players[i]=new Player(INITIAL_X[i], INITIAL_Y[i], Integer.toString(i+1), false);

        }*/
        // Set current (initial) stage index to 0
        currentStage = 0;

        // Interpolate play, such that each stage of each player has the same number of points
        interpolatedPlay = interpolatePlay(originalPlay);
    }

    // Stop play replay
    public void stopPlay(){
        playExisting = false;
    }

    // Interpolate play, such that each stage of each player has the same number of points
    public Play interpolatePlay(Play originalPlay){
        Map<Integer,List<List<float[]>>> interpolatedMap = new HashMap<Integer,List<List<float[]>>>();

        List<List<float[]>> stageList;
        List<float[]> originalCoordinates;
        float[] XY;
        List<float[]> previousCoordinates;
        List<float[]> interpolatedCoordinates;

        // Number of stages in play
        int stageCount = originalPlay.getStageCount();

   // Loop on players
        for (int playerIndex : originalPlay.pointMap.keySet()){

            // Data structure for all of the information for a single player
            stageList = new ArrayList<List<float[]>>();

            // Loop on stages
            for (int stageIndex = 0; stageIndex < stageCount; stageIndex++) {

                // Get XY coordinates for player
                originalCoordinates = new ArrayList<float[]>();
                originalCoordinates = originalPlay.getXYlist(playerIndex, stageIndex);

                // Number of points for selected stage and player
                int pointCount = originalCoordinates.size();

                // Initialize list for interpolated coordinates
                interpolatedCoordinates = new ArrayList<float[]>();

                // Loop on points for interpolated coordinates
                for (int pointIndex = 0; pointIndex < FRAMES_PER_STAGE; pointIndex++) {
                    XY = new float[2];

                    // If no points present in current stage, use last point from last stage
                    if (pointCount < 1) {

                        previousCoordinates = stageList.get(stageIndex-1);
                        XY[0] = previousCoordinates.get(previousCoordinates.size()-1)[0];
                        XY[1] = previousCoordinates.get(previousCoordinates.size()-1)[1];
                    }

                    else if (pointCount == 1) {
                        XY[0] = originalCoordinates.get(0)[0];
                        XY[1] = originalCoordinates.get(0)[1];
                    }

                    else {
                        // Percent of way through interpolated stage
                        float fractionalIndex = (pointCount - 1) * pointIndex/(float)FRAMES_PER_STAGE;

                        // Get previous index
                        int previousIndex = (int) Math.floor(fractionalIndex);

                        // Get next index
                        int nextIndex = (int) Math.ceil(fractionalIndex);

                        if (previousIndex == nextIndex){
                            XY[0] = originalCoordinates.get(previousIndex)[0];
                            XY[1] = originalCoordinates.get(previousIndex)[1];
                        }
                        else {
                            // Weight factor for interpolation
                            float interpolationWeight = (fractionalIndex - previousIndex) / (nextIndex - previousIndex);

                            float[] previousXY = originalCoordinates.get(previousIndex);
                            float[] nextXY = originalCoordinates.get(nextIndex);
                            XY[0] = previousXY[0] + (nextXY[0] - previousXY[0]) * interpolationWeight;
                            XY[1] = previousXY[1] + (nextXY[1] - previousXY[1]) * interpolationWeight;
                        }

                    }
                    // Add interpolated XY points to Coordinate list
                    interpolatedCoordinates.add(XY);
                }
                // Add coordinate list to stage
                stageList.add(interpolatedCoordinates);

            }// End loop on stage
            // Add stages to player
            interpolatedMap.put(playerIndex, stageList);
        }// End loop on player

        // Create interpolated play based on original play
        Play interpolatedPlay = originalPlay;

        // Update point map based on interpolated values
        interpolatedPlay.pointMap = interpolatedMap;

        return interpolatedPlay;
    }

    // During play replay, update player positions
    public void updatePlay(){

        //List<float[]> coordinatesAsList;

        // Determine if end of stage reached
        if (stageFrameCount >= FRAMES_PER_STAGE-1){

            // Increment stage
            currentStage++;

            // Determine if end of play reached
            if (currentStage >= interpolatedPlay.getStageCount()){
                // Reset stage
                currentStage = 0;
            }
            // Reset frame counter within stage
            stageFrameCount = 0;
        }
        else{
            // Increment frame count within stage
            stageFrameCount++;
        }

        // Loop on players
        for (int playerIndex : interpolatedPlay.pointMap.keySet()){
            // Extract relevant XY coordinates
            float[] XY = interpolatedPlay.getXYcoordinate(playerIndex, currentStage, stageFrameCount);

            // Update player positions
            players[playerIndex-1].X = XY[0];
            players[playerIndex-1].Y = XY[1];
        }
    }
}
