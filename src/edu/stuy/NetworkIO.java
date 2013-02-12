package edu.stuy;

import java.io.*;
import java.net.*;
public class NetworkIO {
    private static final int PORT = 6940;
    private double mostRecentOut;
    ServerSocket providerSocket;
    Socket connection = null;
    DataOutputStream out;
    DataInputStream in;
    String message = "";
    NetworkIO(){
    }
    
    void run () {
        try {
            if (connection == null || connection.isClosed()) {
                //1. creating a server socket
                providerSocket = new ServerSocket(PORT);
                //2. Wait for connection
                System.out.println("Waiting for connection");
                connection = providerSocket.accept();
                System.out.println("Connection received from " +
                        connection.getInetAddress().getHostName());
                out = new DataOutputStream(connection.getOutputStream());
            }
            //3. get Input and Output streams
            //out.flush();
            //in = new ObjectInputStream(connection.getInputStream());
            System.out.println("sending: " + getMostRecent());
            sendMessage("" + getMostRecent());
            //4. The two parts communicate via the input and output streams
            //if (message.equals("bye"))
                //sendMessage("bye");
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        finally {
            //4: Closing connection
            try {
                //in.close();
                out.close();
                providerSocket.close();
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    void sendMessage (String msg) {
        try {
            out.writeDouble(Double.parseDouble(msg));
            out.flush();
            System.out.println("server>" + msg);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    public void setMostRecent (double d) {
        mostRecentOut = d;
    }
    
    public double getMostRecent () {
        return mostRecentOut;
    }
}
