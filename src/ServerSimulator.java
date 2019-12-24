/*
    Prahar Patel
    Twitter Coding Challenge
*/

/**
 *   The class will simulate a  server.
 *   It will run a server on socket
 *   and return a response based on a file
 */
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

/**
 *  This is a server socket 
 *     It reads from the server file and creates localhost server sockets
 *     When a client connects, it randomly picks up response from the respone files
 * 
 */
public class ServerSimulator {
    
    private int numberOfServers = 0;
    private static final String SERVERFILE = "servers.txt";
    private static final int portOffset = 20000; 
    private ArrayList<Server> serverList = new ArrayList<>();
    private ArrayList<String> responses = new ArrayList<>();

    // Mock Server objects by reading Server list and populating the responses randomly
    public  void initializeServers () {
        try {
            String line  = null; 
            BufferedReader br = new BufferedReader(new FileReader(SERVERFILE));
            while ((line=br.readLine())!=null) {
                numberOfServers++;
                String []target = line.split("-");
                serverList.add (new Server(line, Integer.parseInt(target[1]) +portOffset, getResponse()));                
            }
            br.close();
        }catch (IOException io) {
            io.printStackTrace();
        }
    }

    // Read responses from resposnes file as JSON format just to mock responses. 
    public void initializeResponses () {
        try {
            String resp = null;
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader ("./responses.txt");
            Object obj = jsonParser.parse(reader);
            JSONArray responseList = (JSONArray)obj;

            for (Object o: responseList) {
                String tmp= o.toString();
                responses.add(tmp);                    
            }
        
        }catch (IOException io) {
            io.printStackTrace();
        } catch (ParseException pe) {
            pe.printStackTrace();
        }

    }

    // Get response randomly
    public String getResponse() {
        if (responses.size() == 0) 
            initializeResponses();
        
        int size = responses.size(); 
        int index = new Random().nextInt(size);

        return  responses.get(index);
    }

    public void bootUpServers () {
        if (serverList.size() == 0) 
            initializeServers();

        for (int i=0; i<numberOfServers; i++) {
            Server s = serverList.get(i);
            s.start();
        }
    }

    public static void main (String []args) {
        ServerSimulator sim = new ServerSimulator();
        sim.bootUpServers();
    }


}

class Server extends Thread {
    
    // Server deetails
    ServerSocket serverSocket;
    Socket clientSocket; 
    PrintWriter out; 
    BufferedReader in; 
    String serverName; 
    int port;
    String response;  
    
    // Thread details
    Thread t; 

    //Overloaded constructors
    Server (String serverName, int port) {
        this.serverName=serverName;
        this.port=port;
    }
    Server (String serverName, int port, String response) {
        this.serverName=serverName;
        this.port=port;
        this.response=response; 

    }

    public void run () {
        runServer();
    }

    public void start () {
        System.out.println (serverName + " starting on port: " + port);
        if (t == null) {
            t = new Thread(this,serverName);
            t.start();
        }
    }

    private void runServer () { 
        try {
            serverSocket = new ServerSocket(port);

            while (true) {
                clientSocket = serverSocket.accept();
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out.println (response);
                clientSocket.close();
            }

        }catch (IOException io1) {
            io1.printStackTrace();
        }
    }
}