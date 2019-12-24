/*
    Prahar Patel
    Twitter Coding Challenge
*/
import org.json.simple.*;
import org.json.simple.parser.*;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

public class PollHealthStatus {

    public ArrayList<Response> list = new ArrayList<>();
    public HashMap<String,ArrayList<Float>> map = new HashMap<>(); // SuccessRate Map
    public HashMap<String,ArrayList<Long>> map1 = new HashMap<>(); // SuccessCount Map
    public HashMap<String,ArrayList<Long>> map2 = new HashMap<>(); // RequestCount Map

    private static String SERVER_FILE = "servers.txt";
    private static String OUTPUT_FILE = "JSONreport";

    public PollHealthStatus () throws IOException{
        readServerFile();
    }


    /**
     *  Reads the servers text file and stored each server in an arraylist
     *  for later processing
     *  It will scan the servers file and create a Response object which is
     *  kept in an Arraylist
     */
    public void readServerFile() throws IOException {
    
        if (list.size() !=0) {
            return; 
        }
        String line = null;
        BufferedReader br = new BufferedReader(new FileReader(SERVER_FILE));

        while ((line=br.readLine()) !=null) {
            Response r = new Response(line);
            list.add (r);
        }
        br.close();
    }

    /**
     *   First entry function to start connecting to endPoints
     *   It will go through the list of Responses object ask them to connect to servers / endpoints
     */
    public void connectToEndPoints () throws UnknownHostException, IOException, ParseException{ 
        for (Response r:list) {
            r.connect();
        }
    }

    /**
     *   This function will go through each Response objects
     *   and check if it has finished processing the endpoint json reply
     *   And if yes, it will create a hash map of the Application_Version
     *   and successrate
     */
    public void populateMap () {

        //System.out.println (">>Populating Map...");
        int count = list.size();

        // We continually check the the array list of Responses
        // till all of them are completed. Do remember that Responses
        // objects are threads, so they will finish in different order
        while (true) {
            for (int i=0 ; i<list.size(); i++) {
                Response r = list.get(i); 
                // When the response obj has finished getting the response
                // the SuccessRate is added to a HashMap 
                if (r.isComplete) {
                    String app = r.applicationName; 
                    String ver = r.version;
                    String app_ver = app + "_" + ver;

                    // Success Rate is the ratio/percentage of successCount to RequestCount
                    float sr = (float)r.successCount/r.reqCount;

                    ArrayList<Float> successRateList; 
                    ArrayList<Long> ReqCountList;
                    ArrayList<Long> SuccessCountList;

                    if (map.containsKey(app_ver)) {
                        successRateList = map.get(app_ver);
                        successRateList.add(sr);
                    }else{
                        successRateList = new ArrayList<>();
                        successRateList.add(sr);
                        map.put(app_ver,successRateList);
                    }

                    if (map1.containsKey(app_ver)) {
                        ReqCountList = map1.get(app_ver);
                        ReqCountList.add(r.reqCount);
                    }else{
                        ReqCountList = new ArrayList<>();
                        ReqCountList.add(r.reqCount);
                        map1.put(app_ver,ReqCountList);
                    }

                    if (map2.containsKey(app_ver)) {
                        SuccessCountList = map2.get(app_ver);
                        SuccessCountList.add(r.successCount);
                    }else{
                        SuccessCountList = new ArrayList<>();
                        SuccessCountList.add(r.successCount);
                        map2.put(app_ver,SuccessCountList);
                    }

                    count--;
                }
                // Once all list of Response objects are processed
                // exit loop! 
                if (count==0) 
                    return; 
            }
        }
    }

    /**
     *  Print report in human readable format 
     *     It prints the HashMap of Server_Version which the key
     *     Followed by the successRate. 
     *  Requirement did not specify if there was a need to print Average, P99 etc.
     *  so it was kept open and only prints the first success rate instead of average of all success rate throughout processes.
     */

    public void printReport () { 
        System.out.println("-----------------------------------------------------------+");
        System.out.println("|App Name_Version        |Sc Rate| Success Cnt| Request Cnt|");
        System.out.println("-----------------------------------------------------------+");
        for (String k:map.keySet()) {
            System.out.printf("|   ", "");
            System.out.printf("%-20s | ", k);
            ArrayList<Float> successRateList = map.get (k);
            ArrayList<Long> ReqCountList = map1.get (k);
            ArrayList<Long> SuccessCountList = map2.get (k);
            for (float f: successRateList) {
                System.out.printf ("%-5.2f | ", f);
                break;
            }
            
            for (long f: SuccessCountList) {
                System.out.printf ("%-10s | ", f);
                break;
            }

            for (long f: ReqCountList) {
                System.out.printf ("%-10s | ", f);
                break;
            }

            
            System.out.println();
        }
        System.out.println("-----------------------------------------------------------+");
    }

   
    @SuppressWarnings("unchecked")
    public void saveReport () {

        JSONArray  arr = new JSONArray();
        
        // Create a json file with timestamp
        Date date = new Date();
        String fileName = OUTPUT_FILE+".json";

        // for every k in Hashmap, we create a json object
        // of this format:
        // {
        //     "Version": "0.1.1",
        //     "SuccessRate": [
        //       0.92456746
        //     ],
        //     "SuccessCount": [
        //       6209771272
        //     ],
        //     "Application": "Webapp1",
        //     "RequestCount": [
        //       6716407147
        //     ]
        //   }

        for (String k: map.keySet()) {
            String []target = k.split("_");
            String app = target[0]; 
            String ver = target[1];

            JSONObject obj = new JSONObject (); 
            obj.put ("Application", app);
            obj.put ("Version", ver);

            JSONArray sucessRateArr = new JSONArray();
            JSONArray reqCountArr = new JSONArray();
            JSONArray successCountArr = new JSONArray();

            ArrayList<Float> successRateList = map.get (k);
            ArrayList<Long> reqCountList = map1.get (k);
            ArrayList<Long> successCountList = map2.get (k);
            
            for (float f: successRateList) {
                sucessRateArr.add(f);
                break;
            }
            for (long f: reqCountList) {
                reqCountArr.add(f);
                break;
            }

            for (long f: successCountList) {
                successCountArr.add(f);
                break;
            }
            obj.put ("SuccessRate", sucessRateArr);
            obj.put ("SuccessCount", successCountArr);
            obj.put ("RequestCount", reqCountArr);
            arr.add(obj);        
        }
        FileWriter file;
        try {
            file = new FileWriter(fileName, true);

            file.write(arr.toJSONString());
            System.out.println ("JSON file created successfully\t" + fileName);
            file.flush();
            file.close();
        }catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     *   Main entry that will 
     *    - read file servers.txt
     *    - connect to servers
     *    - populate hash map
     *    - print and publish report
     */

    public void launch() throws IOException, UnknownHostException,ParseException  {
        this.readServerFile();
        this.connectToEndPoints();
        this.populateMap();
        this.printReport();
        this.saveReport();
    }
    
    public static void main (String []args) throws IOException, UnknownHostException,ParseException {
        PollHealthStatus hs;      
        hs = new PollHealthStatus();       
        hs.launch();
        
    }

}