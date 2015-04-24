package com.husky.uw.myapplication;

/**
 * Created by Metta on 3/2/2015.
 */

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class TCPClient extends Thread {

    private String serverMessage;
    public static String serverIp; //your computer IP address
    public static int serverPort;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private CopyOnWriteArrayList<String> responses;

    PrintWriter out;
    BufferedReader in;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(String serverIp, int serverPort, CopyOnWriteArrayList<String> responses) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.responses = responses;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(this.serverIp);


            Log.e("TCP Client", "C: Connecting..." + this.serverIp + " " + this.serverPort);

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, this.serverPort);

            try {

                //send the message to the server
                Log.e("TCP Client", "C: Sent.");

                Log.e("TCP Client", "C: Done.");

                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    serverMessage = in.readLine();

                        //call the method messageReceived from MyActivity class
                    if(responses != null){
                        this.responses.add(serverMessage);
                        System.out.println("Get the responses : "+ serverMessage);
                        System.out.println("Responses length : " + this.responses.size());
                        serverMessage = null;
                    }
                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}