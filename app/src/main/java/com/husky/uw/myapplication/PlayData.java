package com.husky.uw.myapplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lybar_000 on 4/14/2015.
 */
public class PlayData {

    public Map<Integer,List<List<float[]>>> dataPlayers;
    public List<Integer> dataBall = new ArrayList<Integer>();

    public int currentStage;
    public int currentPoint;
    public float stagePercentComplete;
    public boolean playComplete;

    public PlayData(Map<Integer, List<List<float[]>>> dataPlayersTemp, List<Integer> dataBallTemp){


        dataPlayers = dataPlayersTemp;
        dataBall = dataBallTemp;

        currentStage = 0;
        stagePercentComplete = 0;
        currentPoint = 0;
        playComplete = false;

    }

    public String toString(){

        String summary="";
        for (int playerIndex : dataPlayers.keySet()){

            List<List<float[]>> outerList = dataPlayers.get(playerIndex);
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

    public int getStageCount(){
        Object key = dataPlayers.keySet().toArray()[0];
        return dataPlayers.get(key).size();
    }

}
