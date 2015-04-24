package com.husky.uw.myapplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lybar_000 on 4/14/2015.
 */
public class Play {

    public Map<Integer,List<List<float[]>>> pointMap;
    public int currentStage;
    public int currentPoint;
    public float stagePercentComplete;
    public boolean playComplete;



    public Play(Map<Integer,List<List<float[]>>> pointMapTemp, int currentStageTemp,  float stagePercentCompleteTemp, int currentPointTemp,
                boolean playCompleteTemp){

//        public Play(Map<Integer,List<List<float[]>>> pointsTemp, int currentStageTemp,  float stagePercentCompleteTemp, int currentPointTemp,
 //       boolean playCompleteTemp){

        pointMap = pointMapTemp;
        currentStage = currentStageTemp;
        stagePercentComplete = stagePercentCompleteTemp;
        currentPoint = currentPointTemp;
        playComplete = playCompleteTemp;

    }

    public String toString(){

        String summary="";
        for (int playerIndex : pointMap.keySet()){

            List<List<float[]>> outerList = pointMap.get(playerIndex);
            int outerListLength = outerList.size();

            for (int stageIndex = 0; stageIndex < outerListLength; stageIndex++) {

                List<float[]> innerList = outerList.get(stageIndex);
                int innerListLength = innerList.size();

                for (int pointIndex = 0; pointIndex < innerListLength; pointIndex++) {
                    float[] coordinate = innerList.get(pointIndex);

                    String string1 = "Player: " + Integer.toString(playerIndex);
                    String string2 = ", Stage: " + Integer.toString(stageIndex + 1);
                    String string3 = ", X: " + Float.toString(coordinate[0]);
                    String string4 = ", Y: " + Float.toString(coordinate[1]);

                   summary = string1 + string2 + string3 + string4 + '\n';

                }

            }

        }
        return summary;

    }

    public float[] getXYcoordinate(int playerRequest, int stageRequest, int pointRequest){

        // Determine if requested player exists
        boolean playerPresent = false;
        for (Integer player : pointMap.keySet()) {
            if (playerRequest == player){
                playerPresent = true;
                break;
            }
        }

        // Initialize output
        float[] XY = new float[2];

        // If player present, check other requested parameters
        if (playerPresent) {
            List<List<float[]>> outerList = pointMap.get(playerRequest);

            // Determine if requested stages present
            boolean stagePresent = (outerList.size() >= stageRequest);

            if (stagePresent) {
                // Determine if requested pointed present
                boolean pointPresent = (outerList.get(stageRequest).size() >= pointRequest);

                if (pointPresent){
                    XY = outerList.get(stageRequest).get(pointRequest);
                }
                else {
                    System.out.println("Play.getXY: Requested point is not exist");}
            }
            else {
                System.out.println("Play.getXY: Requested stage does not exist");}
        }
        else {
            System.out.println("Play.getXY: Requested player does not exist");}

        return XY;

    }

    public List<float[]> getXYlist(int playerRequest, int stageRequest){

        // Determine if requested player exists
        boolean playerPresent = false;
        for (Integer player : pointMap.keySet()) {
            if (playerRequest == player){
                playerPresent = true;
                break;
            }
        }

        // Initialize output
        List<float[]> XY = new ArrayList<float[]>();

        // If player present, check other requested parameters
        if (playerPresent) {
            List<List<float[]>> outerList = pointMap.get(playerRequest);

            // Determine if requested stages present
            boolean stagePresent = (outerList.size() >= stageRequest);

            if (stagePresent) {
                return outerList.get(stageRequest);
            }
            else {
                System.out.println("Play.getXY: Requested stage does not exist");}
        }
        else {
            System.out.println("Play.getXY: Requested player does not exist");}

        return XY;

    }

    public int getStageCount(){
        Object key = pointMap.keySet().toArray()[0];
        return pointMap.get(key).size();
    }

}
