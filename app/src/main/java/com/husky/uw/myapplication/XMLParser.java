package com.husky.uw.myapplication;

//Code from:
//http://www.androidhive.info/2011/11/android-xml-parsing-tutorial/

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class XMLParser {


    // constructor
    public XMLParser() {

    }

    /**
     * Getting XML from URL making HTTP request
     * @param url string
     * */
    public String getXmlFromUrl(String url) {
        String xml = null;

        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            xml = EntityUtils.toString(httpEntity);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // return XML
        return xml;
    }

    /**
     * Getting XML DOM element
     //* @param XML string
     * */
    public Document getDomElement(String xml){
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is);

        } catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (SAXException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }

        return doc;
    }

    /** Getting node value
     * @param elem element
     */
    public final String getElementValue( Node elem ) {
        Node child;
        if( elem != null){
            if (elem.hasChildNodes()){
                for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                    if( child.getNodeType() == Node.TEXT_NODE  ){
                        return child.getNodeValue();
                    }
                }
            }
        }
        return "";
    }


    // Read play from XML file
    public Play getPlay(String XML, int playerCount){
// public Map<String,List<String>> getPlay(String XML, int playerCount){
// public Map<String,String> getStage(String XML, int stageNumber){
        // Data format
        // Key: player number (1-5)
        // Outer list: stage
        // Inner list: X and Y coordinates of stage, stored as array
        // Numeric array, float[]: [X, Y]

        // XML node names
        final String KEY_STAGE = "stage";
        final String KEY_PLAYER = "player";
        final String KEY_STAGE_NUMBER= "stage_number";
        final String KEY_XY="xy";
        final String KEY_DATA="data";
        final String KEY_ID="id";
        final String KEY_BALL="ball";
        final String KEY_SCREEN = "screen";
        System.out.println("XML" + XML );
        // Create hashmap repository
        Map<Integer,List<List<float[]>>> dataPlayers = new HashMap<Integer,List<List<float[]>>>();
        List<Integer> dataBall = new ArrayList<Integer>();


        // Map<String, List<String>> dataPlayers = new HashMap<String,List<String>>();


        // XML nodes
        Node child;
        Node grandchild;

        // Get XML as document
        Document document = getDomElement(XML);

        //Get list of all STAGES
        NodeList stageList = document.getElementsByTagName(KEY_STAGE);
        int stageCount =stageList.getLength();

        for (int playerIndex=0; playerIndex < playerCount; playerIndex++) {

            // Create new outer list, stage level information per player
            List<List<float[]>> outerList = new ArrayList<List<float[]>>();

            // String name = Integer.toString(i+1);
            dataPlayers.put(playerIndex, outerList);
        }

        //Loop through each STAGE
        for (int i = 0; i < stageList.getLength(); i++) {

            //Current STAGE node
            Node currentItem = stageList.item(i);

            //Current STAGE element
            Element currentElement = (Element) currentItem;

            //Make sure current STAGE element is not empty
            if (currentElement != null) {

//                System.out.println(getValue(currentElement, KEY_BALL));
                dataBall.add(Integer.valueOf(getValue(currentElement, KEY_BALL)));

                //Determine if current STAGE element has children
                if (currentElement.hasChildNodes()) {

                    //Loop through children within STAGE
                    int j = 0;
                    for (child = currentElement.getFirstChild(); child != null; child = child.getNextSibling()) {
                        // Get child as element
                        Element childElement = (Element) child;

                        // Get name of child element
                        String childName = childElement.getNodeName();
                        //System.out.println( "child name:" + childName+":"+KEY_BALL + ":");
                        //System.out.println(childName.equals(KEY_BALL));
                        if (!(childName.equals(KEY_BALL) || childName.equals(KEY_SCREEN))){
                            // Get ID of child (Player number)
                            //System.out.println( "child is not a ball" );
                            String idValueAsString = getValue(childElement, KEY_ID);
                            //System.out.println("ID value as string:" +idValueAsString);
                            int idValue = Integer.valueOf(idValueAsString);
                            //System.out.println(idValue);

                            // Get XY coordinates for ID (player)
                            String coordinatesAsString = getValue(childElement, KEY_DATA);
                            //System.out.println(coordinatesAsString);

                            List<float[]> innerList = new ArrayList<float[]>();
                            innerList = parseCoordinates(coordinatesAsString);
                            for (int x = 0; x < innerList.size(); x++) {
                                //    System.out.println("X " + Float.toString(innerList.get(x)[0]) + ", Y " + Float.toString(innerList.get(x)[1]));
                            }
                            //List<List<float[]>> outerList = dataPlayers.get(idValue);
                            //outerList.add(innerList);
                            Set<Integer> keys = dataPlayers.keySet();

                            dataPlayers.get(idValue).add(innerList);
                        }
                    }
                }
            }

        }
        return new Play(dataPlayers,dataBall);
       /* else{
            return null;
        }*/

    }

    // Parse strings containing coordinates into a list of float array
    private List<float[]> parseCoordinates(String coordinatesAsString){
        //System.out.println("Coordinate string:" +coordinatesAsString);

        List<float[]> output = new ArrayList<float[]>();

        // Determine if coordinates are present
        boolean coordinatesPresent = !coordinatesAsString.equals("");

        if (coordinatesPresent){
            String[] coordinatesAsStringArray = coordinatesAsString.split(";");


            int coordinateCount = coordinatesAsStringArray.length;

            // Loop on pairs of coordinates
            for (int i = 0; i < coordinateCount; i++) {
                // Set of coordinates in format (x,y)
                String[] currentCoordinate = coordinatesAsStringArray[i].split(",");

                // Add coordinates to output
                float[] coordinatesAsNumber = new float[] { Float.parseFloat(currentCoordinate[0]),
                        Float.parseFloat(currentCoordinate[1]),
                        Float.parseFloat(currentCoordinate[2]),
                        Float.parseFloat(currentCoordinate[3])};

                output.add(coordinatesAsNumber);

            }
        }

        return output;
    }

    public void printPlay(Map<Integer,List<List<float[]>>> play) {


        for (int playerIndex : play.keySet()){

            List<List<float[]>> outerList = play.get(playerIndex);
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

                    System.out.println(string1 + string2 + string3 + string4);

                }

            }

        }
    }
    /**
     * Getting node value
     * @param //Element node
     * @param//key//string
     * */
    public String getValue(Element item, String str) {
        NodeList n = item.getElementsByTagName(str);
        return this.getElementValue(n.item(0));
    }




}
