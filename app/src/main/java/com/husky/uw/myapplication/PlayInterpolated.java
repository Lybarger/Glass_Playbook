package com.husky.uw.myapplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lybar_000 on 5/16/2015.
 */
public class PlayInterpolated {

    public Map<Integer,List<List<float[]>>> dataPlayers;
    public List<List<float[]>> dataBall;
    public int FRAMES_PER_STAGE = 50;

    // Constructor
    public PlayInterpolated(PlayData playData, Court court){
        dataPlayers = interpolatePlayers(playData, court);
        dataBall = interpolateBall(playData, court);
    }

    public boolean isValid(){
        return (dataPlayers != null) && (dataBall != null);

    }
    // Interpolate player positions
    private Map<Integer,List<List<float[]>>> interpolatePlayers(PlayData playData, Court court){

        // Original (not interpolated) data
        Map<Integer,List<List<float[]>>> dataOriginal = playData.dataPlayers;

        // Repository for new (interpolated) data
        Map<Integer,List<List<float[]>>> dataNew = new HashMap<Integer,List<List<float[]>>>();

        // Number of stages in play
        int stageCount = dataOriginal.get(dataOriginal.keySet().iterator().next()).size();

        // Loop on players
        for (int playerIndex : dataOriginal.keySet()){

            // Data structure for all of the information for a single player
            List<List<float[]>> stageList = new ArrayList<List<float[]>>();

            // Loop on stages
            for (int stageIndex = 0; stageIndex < stageCount; stageIndex++) {

                // Get XY coordinates for player
                List<float[]> originalXY = dataOriginal.get(playerIndex).get(stageIndex);

                // Add coordinate list to stage
                stageList.add(interpolateXY(originalXY, court));


            }// End loop on stage

            // Add stages to player
            dataNew.put(playerIndex, stageList);

        }// End loop on player

        return dataNew;
    }

    public int getStageCount(){
        return dataBall.size();
    }

    // Interpolate ball position
    private List<List<float[]>> interpolateBall(PlayData playData, Court court){

        // Original (not interpolated) data
        List<Integer> dataBallOriginal = playData.dataBall;
        Map<Integer,List<List<float[]>>> dataPlayerOriginal = playData.dataPlayers;

        // Hoop position
        //float[] hoopPosition = new float[] {court.getHoopPosition()[0], court.getHoopPosition()[1]};

        // Repository for (interpolated) data
        List<List<float[]>> dataNew = new ArrayList<List<float[]>>();

        // Number of stages in play
        int stageCount = dataBallOriginal.size();

        // Determine if ball is passed
        List<Boolean> ballPassed = new ArrayList<Boolean>();
        ballPassed.add(false);
        if (stageCount > 1) {
            for (int stageIndex = 0; stageIndex < stageCount - 1; stageIndex++) {
                int playerIndexCurrent = dataBallOriginal.get(stageIndex);
                int playerIndexNext = dataBallOriginal.get(stageIndex + 1);
                ballPassed.add(playerIndexCurrent != playerIndexNext);
            }
        }

        int i;
        int size;
        // Loop on stages
        for (int stageIndex = 0; stageIndex < stageCount; stageIndex++) {

            // Repository for XY coordinates
            List<float[]> originalXY = new ArrayList<float[]>();

            // Ball passed, so interpolate between passing and receiving player
            if (ballPassed.get(stageIndex)){

                // Index of player passing ball
                i = dataBallOriginal.get(stageIndex-1);

                if (i  >= 0) {
                    // Last position of player passing ball
                    size = dataPlayerOriginal.get(i).get(stageIndex).size();
                    originalXY.add(dataPlayerOriginal.get(i).get(stageIndex).get(size - 1));
                }
                else {
                    originalXY.add(court.getHoopPositionNorm());
                }

                // Index of player receiving ball
                i = dataBallOriginal.get(stageIndex);

                if (i  >= 0) {
                    // First position of player receiving ball
                    size = dataPlayerOriginal.get(i).get(stageIndex).size();
                    originalXY.add(dataPlayerOriginal.get(i).get(stageIndex).get(0));
                }
                else {

                    originalXY.add(court.getHoopPositionNorm());
                }
            }
            // Ball not passed, so get coordinates of player holding ball
            else {
                // Index of player with ball
                i = dataBallOriginal.get(stageIndex);

                if (i  >= 0) {
                    originalXY = new ArrayList<>(dataPlayerOriginal.get(i).get(stageIndex));
                }
                else {
                    originalXY.add(court.getHoopPositionNorm());
                }
            }
            dataNew.add(interpolateXY(originalXY, court));
        }
        return dataNew;
    }

    private List<float[]> interpolateXY(List<float[]> XYorig, Court court){
        // XYorig = XY coordinates as list of array
        float courtWidth = court.courtWidthPixels;
        float courtHeight = court.courtHeightPixels;

        // Initialize list for interpolated coordinates
        List<float[]> XYscaled = new ArrayList<float[]>();
        List<float[]> XYnew = new ArrayList<float[]>();

        float[] xy;
        float[] screen;
        float[] data;
        // Number of points for selected stage and player
        int pointCount = XYorig.size();

        for (int pointIndex = 0; pointIndex < pointCount; pointIndex++) {

            /*XYscaled.get(pointIndex)[0] = XYorig.get(pointIndex)[0]*courtWidth;
            XYorig.get(pointIndex)[1] = XYorig.get(pointIndex)[1]*courtHeight;*/

            if (XYorig.get(pointIndex).length > 2) {

                XYscaled.add(new float[]{XYorig.get(pointIndex)[0] * courtWidth,
                        XYorig.get(pointIndex)[1] * courtHeight,
                        XYorig.get(pointIndex)[2],
                        XYorig.get(pointIndex)[3]});
            }
            else {
                XYscaled.add(new float[]{XYorig.get(pointIndex)[0] * courtWidth,
                        XYorig.get(pointIndex)[1] * courtHeight});
            }
        }

        // Only 1 coordinate in current stage, so repeat value
        if (pointCount == 1) {
            xy = new float[2];
            screen = new float[2];


            xy[0] = XYscaled.get(0)[0];
            xy[1] = XYscaled.get(0)[1];
            if (XYscaled.get(0).length > 2) {
                screen[0] = XYscaled.get(0)[2];
                screen[1] = XYscaled.get(0)[3];
                data = new float[]{xy[0], xy[1], screen[0], screen[1]};
            }
            else {
                data = new float[]{xy[0], xy[1]};
            }
            //XYnew = new ArrayList<>(Collections.nCopies(FRAMES_PER_STAGE, xy));
            XYnew = new ArrayList<>(Collections.nCopies(FRAMES_PER_STAGE, data));
        }
        // pointCount > 1
        else {

            // Determine Euclidean distance traveled by points
            float distance = 0;

            // Array of distances traveled
            // Same length as points (i.e. first value is 0, and last value is total length)
            float[] totalDistance = new float[pointCount];
            totalDistance[0] = 0;

            for (int pointIndex = 0; pointIndex < pointCount-1; pointIndex++) {

                // Calculate distance between i and i+1 points
                distance = euclideanDistance(
                        XYscaled.get(pointIndex    )[0],
                        XYscaled.get(pointIndex + 1)[0],
                        XYscaled.get(pointIndex    )[1],
                        XYscaled.get(pointIndex + 1)[1]);

                // Create running total of distance traveled
                totalDistance[pointIndex + 1] = totalDistance[pointIndex] + distance;
//                         System.out.println(Integer.toString(pointIndex) + " " + Float.toString(totalDistance[pointIndex]) );
            }

            // Loop on points for interpolated coordinates
            //int pointIndex2 = 0;
            for (int pointIndex = 0; pointIndex < FRAMES_PER_STAGE; pointIndex++) {

                xy = new float[2];

                // Interpolate points, given that there are at least 2 points

                // Percent of way through interpolated stage
                float fractionalIndex = (pointCount - 1) * pointIndex/(float)FRAMES_PER_STAGE;

                // Distance traveled in stage, based on progression in stage
                float distanceTarget = totalDistance[pointCount-1]*pointIndex/((float)(FRAMES_PER_STAGE-1));
                if (distanceTarget > totalDistance[pointCount-1]){
                    distanceTarget = totalDistance[pointCount-1];
                }

                // Find index preceding target value
                int previousIndex = 0;
                int pointIndex2 = 0;
                while (totalDistance[pointIndex2] < distanceTarget){
                    previousIndex = pointIndex2;
                    pointIndex2++;
                }

                // Find index following target value
                int nextIndex = 0;
                pointIndex2 = pointCount-1;
                while ((totalDistance[pointIndex2] >= distanceTarget)&&(pointIndex2 > 0)){
                    nextIndex = pointIndex2;
                    pointIndex2--;
                }

                //System.out.println("PtInd2 " + Integer.toString(pointIndex2) + ", Targ "
                //        + Float.toString(distanceTarget) + ", Dist0 " + Float.toString(totalDistance[pointIndex2])
                //        + ", Dist1 " + Float.toString(totalDistance[pointIndex2 + 1]));
                float z1 = (distanceTarget - totalDistance[previousIndex]);
                float z2 = (totalDistance[nextIndex] - totalDistance[previousIndex]);
                float interpolationWeight;
                if (z2 == 0){
                    interpolationWeight = 0;
                }
                else {
                    interpolationWeight = z1 / z2;
                }

                // XY coordinates preceding and following target
                float[] xy1 = XYscaled.get(previousIndex);
                float[] xy2 = XYscaled.get(nextIndex);

                // Interpolated coordinate
                xy[0] = xy1[0] + (xy2[0] - xy1[0]) * interpolationWeight;
                xy[1] = xy1[1] + (xy2[1] - xy1[1]) * interpolationWeight;

                if (xy2.length > 2) {
                    float screenAngle = (float)Math.toDegrees(Math.atan2(xy2[1] - xy1[1], xy2[0] - xy1[0]));
                    if (screenAngle < 0){screenAngle = screenAngle + 360;}
                    data = new float[]{xy[0],  xy[1], xy2[2], screenAngle};

                    //System.out.println("Planter plated" +Float.toString(data[0]) + " " + Float.toString(data[1]) + " " + Float.toString(data[2]) + " " + Float.toString(data[3]));
                }
                else {
                    data = new float[]{xy[0],  xy[1]};
                }

                // Add interpolated XY points to Coordinate list
                XYnew.add(data);
            }
        }
        return XYnew;
    }

    public List<float[]> getXYlist(int playerRequest, int stageRequest){

        // Determine if requested player exists
        boolean playerPresent = false;
        for (Integer player : dataPlayers.keySet()) {
            if (playerRequest == player){
                playerPresent = true;
                break;
            }
        }

        // Initialize output
        List<float[]> XY = new ArrayList<float[]>();

        // If player present, check other requested parameters
        if (playerPresent) {
            List<List<float[]>> outerList = dataPlayers.get(playerRequest);

            // Determine if requested stages present
            boolean stagePresent = (outerList.size() >= stageRequest);

            if (stagePresent) {
                return outerList.get(stageRequest);
            }
            else {System.out.println("Play.getXY: Requested stage does not exist");}
        }
        else {System.out.println("Play.getXY: Requested player does not exist");}

        return XY;

    }

    public float[] getXYcoordinate(int playerRequest, int stageRequest, int pointRequest){

        // Determine if requested player exists
        boolean playerPresent = false;
        for (Integer player : dataPlayers.keySet()) {
            if (playerRequest == player){
                playerPresent = true;
                break;
            }
        }

        // Initialize output
        float[] XY = new float[2];

        // If player present, check other requested parameters
        if (playerPresent) {
            List<List<float[]>> outerList = dataPlayers.get(playerRequest);

            // Determine if requested stages present
            boolean stagePresent = (outerList.size() >= stageRequest);

            if (stagePresent) {
                // Determine if requested pointed present
                boolean pointPresent = (outerList.get(stageRequest).size() >= pointRequest);

                if (pointPresent){

                    XY = outerList.get(stageRequest).get(pointRequest);
                }
                else {System.out.println("Play.getXY: Requested point is not exist");}
            }
            else {System.out.println("Play.getXY: Requested stage does not exist");}
        }
        else {System.out.println("Play.getXY: Requested player does not exist");}

        return XY;

    }

    public float[] getData(int playerRequest, int stageRequest, int pointRequest){

        // Determine if requested player exists
        boolean playerPresent = false;
        for (Integer player : dataPlayers.keySet()) {
            if (playerRequest == player){
                playerPresent = true;
                break;
            }
        }

        // Initialize output
        float[] XY = new float[4];

        // If player present, check other requested parameters
        if (playerPresent) {
            List<List<float[]>> outerList = dataPlayers.get(playerRequest);

            // Determine if requested stages present
            boolean stagePresent = (outerList.size() >= stageRequest);

            if (stagePresent) {
                // Determine if requested pointed present
                boolean pointPresent = (outerList.get(stageRequest).size() >= pointRequest);

                if (pointPresent){

                    XY = outerList.get(stageRequest).get(pointRequest);
                }
                else {System.out.println("Play.getXY: Requested point is not exist");}
            }
            else {System.out.println("Play.getXY: Requested stage does not exist");}
        }
        else {System.out.println("Play.getXY: Requested player does not exist");}

        return XY;

    }

    public float euclideanDistance(float x1, float x2, float y1, float y2) {
        return (float) Math.pow(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2), 0.5);
    }
}

