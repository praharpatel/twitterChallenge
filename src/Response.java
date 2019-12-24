/*
    Prahar Patel
    Twitter Coding Challenge
*/
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

import org.json.simple.*;
import org.json.simple.parser.*;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;


/**
 *   Response object is used to encapsulate a response when an endpoint is hit
 *   Response object is a thread that will talk to a endpoint, get teh json object
 *   and store it within its object
 */
public class Response extends Thread{ 

    String serverName;
    String applicationName; 
    String version; 
    Long upTime;
    Long reqCount;
    Long errCount; 
    Long successCount;
    JSONObject rawJSON; 
    boolean isComplete = false; // true when response is obtained from server
    Thread t; 

    public Response (String serverName, String applicationName, String version, Long upTime, Long reqCount, Long errCount, Long successCount) {

        this.serverName = serverName; 
        this.applicationName = applicationName;
        this.version = version;
        this.upTime = upTime;
        this.reqCount = reqCount;
        this.errCount = errCount;
        this.successCount = successCount; 
    }

    public Response (String serverName, JSONObject rawJSON) {
        this.serverName =serverName; 
        this.rawJSON = rawJSON;
        parse();
    }

    public Response (String serverName) {
        this.serverName=serverName; 
    }

    /**
     *  Threads execution starts when it starts to run
     *  Thread running involved calling a connect to endpoint
     */
    public void run () {
        try {
            connect();
        }catch (UnknownHostException ue) {
            ue.printStackTrace();
        }catch (ParseException pe) {
            pe.printStackTrace();
        }
        catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     *  Basic start of a thread which will will call run
     */
    public void start () {
        t = new Thread (this,serverName);
        t.start();
    }
    
    /**
     *  Helper function to parse the json response from the server
     *  and populate each of the fields of the Response object
     */
    public Response parse () {
        if (rawJSON == null) 
            throw new RuntimeException("json object is null");
        applicationName = (String)rawJSON.get("Application");
        version = (String) rawJSON.get("Version");
        upTime = (Long) rawJSON.get("Uptime");
        reqCount = (Long) rawJSON.get("Request_Count");
        errCount = (Long) rawJSON.get("Error_Count");
        successCount = (Long) rawJSON.get("Success_Count");

        return this;
    }

    /**
     *   It will start a HTTP connection to the REST server
     *     Under simulator mode, it will connect to socket server simulatoir
     *     Under non-simulator mode, it will make a REST API call to endpoint
     */
    public void connect() throws UnknownHostException,IOException,ParseException {
        String data= "";   
            data = _connectSimulator();
        JSONParser parser = new JSONParser();
        rawJSON = (JSONObject) parser.parse (data);
        parse();
    }

    Socket clientSocket;
    PrintWriter out;
    BufferedReader in;
    /**
     *  Connects to Simulator to get JSON object
     *  Simple Server / client logic
     */
    public String _connectSimulator () throws UnknownHostException, IOException {
        // The Simulator creates a series of Server listing on localhost at ports
        // read from the server name e.g server-0012, Server will list at port (20000+12)
        // So over here we connect to those ports

        String []target = serverName.split("-");
        int offset = 20000;
        
        String ip = "127.0.0.1";
        int port=Integer.parseInt(target[1]) + offset; //serverName-xxxx
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String resp = sendMessage();
        this.isComplete = true;
        clientSocket.close(); out.close(); in.close(); 

        //Return the json object 
        return resp;

    }

    /**
     *  Helper function for the simulator code
     */
     private String sendMessage()throws IOException {
        String resp = in.readLine();
        return resp;
    }

}