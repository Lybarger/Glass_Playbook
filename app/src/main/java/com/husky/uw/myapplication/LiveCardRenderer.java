package com.husky.uw.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.View;

import com.google.android.glass.timeline.DirectRenderingCallback;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/*
*  ISSUES:
*  Short-Term:
*  1. PARSER
*
*  Long-Term:
*  1. Slow, clicking stop
*  2. Players pixelated
*  3. Cropped image could create issues with the percentages and location
*
* */

public class LiveCardRenderer implements DirectRenderingCallback {

    /**
     * The duration, in millisconds, of one frame.
     */
    private static final long FRAME_TIME_MILLIS = 40;

    /**
     * "Hello world" text size.
     */
    private static final float TEXT_SIZE = 70f;

    /**
     * Alpha variation per frame.
     */
    private static final int ALPHA_INCREMENT = 5;

    /**
     * Max alpha value.
     */
    private static final int MAX_ALPHA = 256;

    private static final int PLAYER_COUNT = 5;
    private static int SCREEN_WIDTH = 640;
    private static int SCREEN_HEIGHT = 360;
    private Paint ballPaint;
    private Paint paint_circle;     // draws player circle
    private Paint paint_text;       // draws number on player
    private Paint textPaint;        // text displays current stage


    private int mCenterX;
    private int mCenterY;

    private SurfaceHolder mHolder;
    private boolean mRenderingPaused;
    private RenderThread mRenderThread;

    // defines pixels of height and width
    private float screenWidthPixels;
    private float screenHeighthPixels;

    private Bitmap background;

    private final int positionFont = 20;

    private final int FRAME_RATE = 30;
    private final int STAGE_LENGTH = 3;   //Time in seconds
    private int FRAMES_PER_STAGE = FRAME_RATE*STAGE_LENGTH;
    private int frame = 0;
    private int stage = 0;

    private Bitmap[] playerIcons = new Bitmap[PLAYER_COUNT];
    //Player definition constants
    private int PLAYER_ICON_SIZE = 40;
    private int selection_size = PLAYER_ICON_SIZE*1;
    private int text_size = 30;
    private int text_shift = text_size*6/20;

    //public Player[] players = new Player[PLAYER_COUNT];
    private Map<String,List<String>> points;
    private PlayInterpolated interpolatedPlay;

    private Resources resources;
    private CopyOnWriteArrayList<String> responses;
    // For debugging purposes
    private static int[] INITIAL_X = new int[] {320, 140, 500, 220, 420};
    private static int[] INITIAL_Y = new int[] {110, 180, 180, 320, 320};

    private static boolean connect = false;

    private Players players;
    private Ball ball;
    private Hoop hoop;

    public LiveCardRenderer(Resources resources,Context context,CopyOnWriteArrayList<String> responses) {

        this.resources = resources;
        this.responses = responses;
        //Define circle format for bitmap
        paint_circle = new Paint();
        paint_circle.setStyle(Paint.Style.FILL);
        paint_circle.setColor(context.getResources().getColor(R.color.husky_purple));

        //Define text format
        paint_text = new Paint();
        paint_text.setTextSize(text_size);
        paint_text.setTextAlign(Paint.Align.CENTER);
        paint_text.setColor(Color.WHITE);
        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);
        paint_text.setTypeface(tf);

        // Text that updates stage
        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setStyle(Paint.Style.FILL);

        screenWidthPixels = context.getResources().getInteger(R.integer.screenWidthPixels);
        screenHeighthPixels = context.getResources().getInteger(R.integer.screenHeightPixels);


        background = BitmapFactory.decodeResource(context.getResources(), R.drawable.half_court_cropped_glass_ratio_size);
        background = background.createScaledBitmap(background,SCREEN_WIDTH,SCREEN_HEIGHT,false);




        players = new Players(context);
        ball = new Ball(INITIAL_X[0], INITIAL_Y[0], 0, context);

        // Create hoop
        hoop = new Hoop(context);

        // Replay as XML
        //XML String is what given from TCP connection
        //checkResponse();
        //String playAsXml = XMLString();
        String playAsXml = "<?xml version='1.0' encoding='UTF-8'?> <play><stage><ball>0</ball><player><id>0</id><data>400.0,200.0,0.0,0.0</data></player><player><id>1</id><data>55.0,542.0,0.0,104.03625</data></player><player><id>2</id><data>747.0,514.0,0.0,66.80141</data></player><player><id>3</id><data>311.0,332.0,0.0,277.125;311.0,332.0,0.0,277.125;315.0,345.0,0.0,72.89727;315.0,346.0,0.0,90.0;315.0,347.0,0.0,90.0;315.0,348.0,0.0,90.0;315.0,349.0,0.0,90.0;315.0,350.0,0.0,90.0;315.0,351.0,0.0,90.0;315.0,352.0,0.0,90.0;315.0,353.0,0.0,90.0;315.0,355.0,0.0,90.0;315.0,356.0,0.0,90.0;315.0,357.0,0.0,90.0;315.0,358.0,0.0,90.0;315.0,360.0,0.0,90.0;315.0,361.0,0.0,90.0;315.0,362.0,0.0,90.0;315.0,363.0,0.0,90.0;315.0,364.0,0.0,90.0;315.0,365.0,0.0,90.0;315.0,366.0,0.0,90.0;315.0,367.0,0.0,90.0;315.0,368.0,0.0,90.0;315.0,369.0,0.0,90.0;315.0,370.0,0.0,90.0;315.0,371.0,0.0,90.0;315.0,371.0,0.0,90.0</data></player><player><id>4</id><data>483.0,322.0,0.0,270.0</data></player></stage><stage><ball>0</ball><player><id>0</id><data>400.0,200.0,0.0,0.0</data></player><player><id>1</id><data>55.0,542.0,0.0,104.03625</data></player><player><id>2</id><data>747.0,514.0,0.0,66.80141</data></player><player><id>3</id><data>315.0,371.0,0.0,90.0</data></player><player><id>4</id><data>483.0,322.0,0.0,270.0;483.0,322.0,0.0,270.0;483.0,322.0,0.0,270.0;483.0,322.0,1.0,270.0;477.0,339.0,1.0,109.44003;477.0,340.0,1.0,90.0;477.0,341.0,1.0,90.0;477.0,342.0,1.0,90.0;477.0,343.0,1.0,90.0;477.0,346.0,1.0,90.0;477.0,347.0,1.0,90.0;477.0,348.0,1.0,90.0;477.0,349.0,1.0,90.0;477.0,350.0,1.0,90.0;477.0,351.0,1.0,90.0;477.0,352.0,1.0,90.0;477.0,354.0,1.0,90.0;477.0,355.0,1.0,90.0;477.0,359.0,1.0,90.0;477.0,360.0,1.0,90.0;477.0,361.0,1.0,90.0;477.0,362.0,1.0,90.0;477.0,363.0,1.0,90.0;477.0,364.0,1.0,90.0;477.0,365.0,1.0,90.0;477.0,366.0,1.0,90.0;477.0,367.0,1.0,90.0;477.0,368.0,1.0,90.0;477.0,369.0,1.0,90.0;477.0,370.0,1.0,90.0;477.0,371.0,1.0,90.0;477.0,372.0,1.0,90.0;477.0,373.0,1.0,90.0;477.0,374.0,1.0,90.0;477.0,375.0,1.0,90.0;477.0,376.0,1.0,90.0;477.0,376.0,1.0,90.0</data></player></stage><stage><ball>0</ball><player><id>0</id><data>400.0,200.0,0.0,0.0;400.0,200.0,0.0,0.0;400.0,200.0,0.0,0.0</data></player><player><id>1</id><data>55.0,542.0,0.0,104.03625</data></player><player><id>2</id><data>747.0,514.0,0.0,66.80141</data></player><player><id>3</id><data>315.0,371.0,0.0,90.0</data></player><player><id>4</id><data>477.0,376.0,1.0,90.0</data></player></stage><stage><ball>3</ball><player><id>0</id><data>400.0,200.0,0.0,0.0</data></player><player><id>1</id><data>55.0,542.0,0.0,104.03625</data></player><player><id>2</id><data>747.0,514.0,0.0,66.80141</data></player><player><id>3</id><data>315.0,371.0,0.0,90.0;315.0,371.0,0.0,90.0;315.0,371.0,0.0,90.0</data></player><player><id>4</id><data>477.0,376.0,1.0,90.0</data></player></stage><stage><ball>-1</ball><player><id>0</id><data>400.0,200.0,0.0,0.0</data></player><player><id>1</id><data>55.0,542.0,0.0,104.03625</data></player><player><id>2</id><data>747.0,514.0,0.0,66.80141</data></player><player><id>3</id><data>315.0,371.0,0.0,90.0</data></player><player><id>4</id><data>477.0,376.0,1.0,90.0</data></player></stage></play>";

        loadPlay(playAsXml);

    }

    private void loadPlay(String playAsXml){

        // Create instance of XML parser
        XMLParser parser = new XMLParser();

        // Parse XML play into map
        Play originalPlay = parser.getPlay(playAsXml, PLAYER_COUNT);

        // Interpolate play
        interpolatedPlay = new PlayInterpolated(originalPlay, hoop);

        // Update player and ball initial positions
        for (int playerIndex : interpolatedPlay.dataPlayers.keySet()){
            players.updateXY(playerIndex, interpolatedPlay.getXYcoordinate(playerIndex, 0, 0));
        }
        ball.updateXY(interpolatedPlay.dataBall.get(0).get(0));

        // Reset paths and move to initial positions
        players.pathsReset();
        players.pathsMoveToPlayerPositions();
        ball.path.reset();
        ball.path.moveTo(ball.X, ball.Y);

    }
    public void checkResponse(){
        System.out.println("Check responses");



            if (!this.responses.isEmpty()) {
                System.out.println("Responses is not empty");
                String playAsXml = this.responses.remove(0);

                loadPlay(playAsXml);

                //Play originalPlay = new Play(playOriginal, 0, 0, 0, false);
                //interpolatedPlay = interpolatePlay(originalPlay); //interpolates play

                // sets players in initial position of interpolatedPlay
                // initializePlayers();
                //players.reinitialize();

        }

    }

/*    private void manualRead(){




            XMLParser parser = new XMLParser();

            // Parse XML play into map
            System.out.println("Parse a new playmap");
            Map<Integer, List<List<float[]>>> playMap = parser.getPlay(playAsXml, PLAYER_COUNT);

            Play originalPlay = new Play(playMap, 0, 0, 0, false);
            //interpolatedPlay = interpolatePlay(originalPlay); //interpolates play

            // sets players in initial position of interpolatedPlay
            //initializePlayers();
            //players.reinitialize();
    }*/

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCenterX = width / 2;
        mCenterY = height / 2;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        mRenderingPaused = false;
        updateRenderingState();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
        updateRenderingState();
    }

    @Override
    public void renderingPaused(SurfaceHolder holder, boolean paused) {
        mRenderingPaused = paused;
        updateRenderingState();
    }

    private void updateRenderingState() {
        boolean shouldRender = (mHolder != null) && !mRenderingPaused;
        boolean isRendering = (mRenderThread != null);

        if (shouldRender != isRendering) {
            if (shouldRender) {
                mRenderThread = new RenderThread();
                mRenderThread.start();
            } else {
                mRenderThread.quit();
                mRenderThread = null;
            }
        }
    }

    // converts given coordinate percentage to pixel coordinate
    // boolean x = true if x
    private float toCoordinates (float percentage, boolean x) {
        float coord;
        if (x) { // if x coordinate
            coord = (percentage / 800) * screenWidthPixels;
        }else { // if y coordinate
            coord = (percentage / 600) * screenHeighthPixels;
        }
        return coord;
    }



    // updates play to display new stage on screen
    public void updatePlay(){
        // Determine if end of stage reached
        //System.out.println("update play");
        if (frame >= FRAMES_PER_STAGE-1){

            // Increment stage
            stage++;

            // Determine if end of play reached
            if (stage >= interpolatedPlay.getStageCount()){
                // Reset stage
                if (connect) {
                    if (!this.responses.isEmpty()) {
                        checkResponse();
                    }
                }
                else {
                    mRenderingPaused = true;
                    //manualRead();
                    mRenderingPaused = false;
                }
                stage = 0;
            }
            // Reset frame counter within stage
            frame = 0;
        }
        else{
            // Increment frame count within stage
            frame++;
        }

        // Loop on players
        for (int i : interpolatedPlay.dataPlayers.keySet()) {
            //players.updateXY(i, interpolatedPlay.getXYcoordinate(i, stage, frame));
            //float[] xx = ;
            players.updateData(i, interpolatedPlay.getData(i,stage,frame));
//System.out.println( "player" + players.X[1]);
            //System.out.println("Playview " +Float.toString(xx[0]) + " " + Float.toString(xx[1]) + " " + Float.toString(xx[2]) + " " + Float.toString(xx[3]));
            //System.out.println(players.screenPresent[i]);
            //System.out.println(players.screenAngle[i]);
            //players.screenPresent[i]=true;
        }
    }


    /**
     * Draws the view in the SurfaceHolder's canvas.
     */
    private void draw() {
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            return;
        }
        if (canvas != null) {

            // Clear the canvas.
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            // Sets background to image of court
            //background = resizeImage(background, 640, 360);
            canvas.drawBitmap(background, 0, 0, null);
            System.out.println("Height: " + Integer.toString(background.getHeight()) + ", Width:" + Integer.toString(background.getWidth()));

            // for debugging
            //initialPlayerInsert(canvas);

            // Update player icon positions
            for (int i = 0; i < PLAYER_COUNT; i++) {

                // Determine location of player icon, with offset
                float X = players.getX(i);
                float Y = players.getY(i);

                // Draw player icons on canvas
                canvas.drawBitmap(players.icon[i], X, Y, null);
            }
            canvas.drawCircle(0, 0, 10, new Paint());
            canvas.drawCircle(100,100, 10, new Paint());
            canvas.drawCircle(200,200, 10, new Paint());
            canvas.drawCircle(300,300, 10, new Paint());
            canvas.drawCircle(360,360, 10, new Paint());
            canvas.drawCircle(400,400, 10, new Paint());
            canvas.drawCircle(640,360, 10, new Paint());

            // updates player locations
            //updatePlay();


            mHolder.unlockCanvasAndPost(canvas);
        }
    }

    // USED FOR DEBUGGING ONLY
    /*public void initialPlayerInsert(Canvas canvas){

        // Update player icon positions
        for (int i = 0; i < PLAYER_COUNT; i++) {

            // Determine location of player icon, with offset
            float X = players[i].X-PLAYER_ICON_SIZE/2;
            float Y = players[i].Y-PLAYER_ICON_SIZE/2;

            // Draw player icons on canvas
            canvas.drawBitmap(playerIcons[i], X, Y, null);
        }
    }*/

    //Create string from points
    private String pointsToString(float x, float y){
        return "(" + Float.toString(x) + "," + Float.toString(y) + ")";
    }


    private void initializePlayers(){
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
            // Initializes players to first location in play
            int playerNum = i + 1;
            float[]XY = interpolatedPlay.getXYcoordinate(playerNum, 0, 0);
/*            float x = toCoordinates(XY[0], true);
            float y = toCoordinates(XY[1], false);*/
            //players[i]=new Player(x, y, Integer.toString(i + 1), false);

            // REPLACE WITH ABOVE CODE, debugging purposes
            // players[i]=new Player(INITIAL_X[i], INITIAL_Y[i], Integer.toString(i + 1), false);

        }
    }

    // Returns Play of Interpolated points
    /*public Play interpolatePlay(Play originalPlay){
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
        Play interpolatePlay = originalPlay;

        // Update point map based on interpolated values
        interpolatePlay.pointMap = interpolatedMap;

        return interpolatePlay;
    }
*/
    // resizes bitmap given max width and height
    private Bitmap resizeImage(Bitmap image, int maxWidth, int maxHeight)
    {
        Bitmap resizedImage = null;
        try {
            int imageHeight = image.getHeight();


            if (imageHeight > maxHeight)
                imageHeight = maxHeight;
            int imageWidth = (imageHeight * image.getWidth())
                    / image.getHeight();

            if (imageWidth > maxWidth)
                imageWidth = maxWidth;
            imageHeight = (imageWidth * image.getHeight())
                    / image.getWidth();


            if (imageHeight > maxHeight)
                imageHeight = maxHeight;
            if (imageWidth > maxWidth)
                imageWidth = maxWidth;


            resizedImage = Bitmap.createScaledBitmap(image, imageWidth,
                    imageHeight, true);
        } catch (OutOfMemoryError e) {

            e.printStackTrace();
        }catch(NullPointerException e)
        {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return resizedImage;
    }


    /*// returns example string for debugging purposes, doesnt work
    private String XMLString(){
        return //"<?xml version='1.0' encoding='UTF-8'?>\n" +
                "         <play><stage>\n" +
                        "         <player>\n" +
                        "         <id>1</id>\n" +
                        "         <xy>'[(400.0,200.0)]'</xy>\n" +
                        "         </player>\n" +
                        "         <player>\n" +
                        "         <id>2</id>\n" +
                        "         <xy>'[(35.0,504.0)]'</xy>\n" +
                        "         </player>\n" +
                        "         <player>\n" +
                        "         <id>3</id>\n" +
                        "         <xy>'[(748.0,532.0)]'</xy>\n" +
                        "         </player>\n" +
                        "         <player>\n" +
                        "         <id>4</id>\n" +
                        "         <xy>'[(250.0,500.0)]'</xy>\n" +
                        "         </player>\n" +
                        "         <player>\n" +
                        "         <id>5</id>\n" +
                        "         <xy>'[(550.0,500.0)]'</xy>\n" +
                        "         </player>\n" +
                        "         </stage>\n" +
                        "         </play>";
    }
*/

    /**
     * Redraws the {@link View} in the background.
     */
    private class RenderThread extends Thread {
        private boolean mShouldRun;

        /**
         * Initializes the background rendering thread.
         */
        public RenderThread() {
            mShouldRun = true;
        }

        /**
         * Returns true if the rendering thread should continue to run.
         *
         * @return true if the rendering thread should continue to run
         */
        private synchronized boolean shouldRun() {
            return mShouldRun;
        }

        /**
         * Requests that the rendering thread exit at the next opportunity.
         */
        public synchronized void quit() {
            mShouldRun = false;
        }

        @Override
        public void run() {
            while (shouldRun()) {
                long frameStart = SystemClock.elapsedRealtime();
                updatePlay();
                draw();
                long frameLength = SystemClock.elapsedRealtime() - frameStart;

                long sleepTime = FRAME_TIME_MILLIS - frameLength;
                if (sleepTime > 0) {
                    SystemClock.sleep(sleepTime);
                }
            }
        }


    }
}
