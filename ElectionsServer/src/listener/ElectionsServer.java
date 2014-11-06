/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import stagger.AnalyzeText;
import stagger.TagFile;

/**
 *
 * @author filippia
 */
public class ElectionsServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        TagFile taggedInput = null ;
        try {
            taggedInput = new TagFile();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AnalyzeText.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(AnalyzeText.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        boolean listening = true;

        /*localhost:4444 is default if there are no arguments*/
        int port = 4444;
        String host = "localhost";

        /*Read host and port from arguments*/
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("USAGE: java Server [hostname] [port] ");
                System.exit(0);
            }
            host = args[0];

            if (args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("-help")) {
                System.out.println("USAGE: java Server [hostname] [port] ");
                System.exit(1);
            }
        }

        try {

            //create an IP address and the server's socket to this address and port 2222
            InetAddress addr = InetAddress.getByName(host);
            ServerSocket serversocket = new ServerSocket(port, 1000, addr);

            while (listening) {    // the main server's loop
                System.out.println("Listening to connections on " + host + " : " + port + "...");
                Socket clientsocket = serversocket.accept();
                (new Handler(clientsocket, taggedInput)).start();
            }
            serversocket.close();
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }
}
