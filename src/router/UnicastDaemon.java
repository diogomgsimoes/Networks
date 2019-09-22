/**
 * Redes Integradas de Telecomunicações I
 * MIEEC 2019/2020
 *
 * UnicastDaemon.java
 *
 * Class that supports unicast communication
 *
 * Created on August 30, 19:00
 * @author  Luis Bernardo
 */
package router;

import java.io.*;
import java.net.*;

/** 
 * Thread that handles socket events 
 */
public class UnicastDaemon extends Thread {
        volatile boolean keepRunning= true;
        Router win;
        DatagramSocket ds;
        
        // Constructor
        UnicastDaemon(Router win, DatagramSocket ds) {
            this.win= win;
            this.ds= ds;
        }
        
        // Thread main function
        @Override
        public void run() {
            byte [] buf= new byte[8096];
            DatagramPacket dp= new DatagramPacket(buf, buf.length);
            try {
                while (keepRunning) {
                    try {
                        ds.receive(dp);
                        ByteArrayInputStream BAis= 
                            new ByteArrayInputStream(buf, 0, dp.getLength());
                        DataInputStream dis= new DataInputStream(BAis);
                        System.out.println("Received packet ("+dp.getLength()+
                            ") from " + dp.getAddress().getHostAddress() +
                            ":" +dp.getPort());
                        
                        synchronized (this) {
                            win.process_packet(dp, dis);
                        }
                    }
                    catch (SocketException se) {
                        if (keepRunning)
                            win.Log("recv UDP SocketException : " + se + "\n");
                    }
                }
            }
            catch(IOException e) {
                if (keepRunning)
                    win.Log("IO exception receiving data from socket : " + e);
            }
        }
        
        // Stops thread
        public void stopRunning() {
            keepRunning= false;
        }
    
}
