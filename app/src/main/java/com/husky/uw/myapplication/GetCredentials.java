package com.husky.uw.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Environment;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.System.out;

public class GetCredentials extends AsyncTask<Void,Void,Void> {

    private static final String BUCKET = "huskyglass";
    private Resources resources;
    private Context context;
    private TCPClient tcpClient;
    private CopyOnWriteArrayList<String> responses;
    private static final String serverIp = "10.0.1.2";
    private static final int serverPort = 49152;

    public GetCredentials(Resources res, Context context,TCPClient tcpClient, CopyOnWriteArrayList<String> responses){
        this.resources = res;
        this.context = context;
        this.tcpClient = tcpClient;
        this.responses = responses;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        System.out.println("Connecting to the tcp");
/*        FileInputStream fis = null;
        try {
            fis = context.openFileInput("IP_SERVER.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String all = sb.toString();
        String[] splits = all.split(" ");
        System.out.println("IP " + splits[0]+ " HOST " + splits[1]);*/

        //                            (serverIp, serverPort,responses)
        this.tcpClient = new TCPClient(serverIp,serverPort,this.responses);
        //this.tcpClient = new TCPClient(splits[0],Integer.parseInt(splits[1]),this.responses);
        this.tcpClient.start();
    }


    @Override
    protected Void doInBackground(Void...voids) {
/*        String access = "";
        String secret = "";
        try {

            InputStream inputStream = resources.openRawResource(R.raw.rootkey);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                access = bufferedReader.readLine();
                secret = bufferedReader.readLine();
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println("access" + access);
        out.println("secret" + secret);
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(access, secret);
        AmazonS3 s3Client = new AmazonS3Client(awsCreds);
        getCredentials(s3Client);*/
        return null;
    }

    /*public void getCredentials(AmazonS3 s3Client){
        try {
            out.println("Uploading a new object to S3 from a file\n");
            String path = context.getFilesDir() + "/" + "IP_SERVER.txt";
            File file = new File(path);

            S3Object object = s3Client.getObject(new GetObjectRequest(BUCKET, "IP_SERVER.txt"));
            InputStream objectData = object.getObjectContent();
            writeTextInputStream(objectData);

        } catch (AmazonServiceException ase) {
            out.println("Caught an AmazonServiceException, which " +
                    "means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            out.println("Error Message:    " + ase.getMessage());
            out.println("HTTP Status Code: " + ase.getStatusCode());
            out.println("AWS Error Code:   " + ase.getErrorCode());
            out.println("Error Type:       " + ase.getErrorType());
            out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            out.println("Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            out.println("Error Message: " + ace.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println("Finish getting the credentials");
    }*/


/*    private void writeTextInputStream(InputStream input)
            throws IOException {
        // Read one text line at a time and display.
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("IP_SERVER.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(line);
            outputStreamWriter.close();
            System.out.println("    " + line);
        }
        System.out.println();
    }*/

    public void getImage(AmazonS3 s3Client){
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(BUCKET)
                .withPrefix("images");
        ObjectListing objectListing;
        do {
            objectListing = s3Client.listObjects(listObjectsRequest);
            for (S3ObjectSummary objectSummary :
                    objectListing.getObjectSummaries()) {
                String filename = objectSummary.getKey();
                filename = filename.replace("images/","");
                out.println("Filename : " + filename);
                File imagesDir = context.getDir("images", Context.MODE_PRIVATE); //Creating an internal dir;
                File file = new File(imagesDir, filename);
                if(!file.exists()) {
                    out.println("File didn't exists : Getting the data from the cloud");
                    S3Object object = s3Client.getObject(new GetObjectRequest(BUCKET, objectSummary.getKey()));
                    InputStream objectData = object.getObjectContent();
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    int read = 0;
                    byte[] bytes = new byte[1024];

                    try {
                        while ((read = objectData.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, read);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    out.println("Done!");
                }
            }
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}