/**
 * Redes Integradas de Telecomunicações I
 * MIEEC 2019/2020
 *
 * MulticastDaemon.java
 *
 * Class that supports multicast communication
 *
 * Created on August 30, 19:00
 *
 * @author Luis Bernardo
 */
package router;

import java.io.*;
import java.net.*;

/**
 *
 * @author user
 */
public class MulticastDaemon extends Thread {

    volatile boolean keepRunning = true;
    private DatagramSocket ds;
    private MulticastSocket ms;
    private String multicast_addr;
    private InetAddress group;
    private int mport;
    private Router win;
    private Routing route;

    /**
     * Constructor
     */
    MulticastDaemon(DatagramSocket ds, String multicast_addr, int mport,
            Router win, Routing route) {
        this.ds = ds;
        this.multicast_addr = multicast_addr;
        this.mport = mport;
        this.win = win;
        this.route = route;
        try {
            // Starts the multicast socket
            ms = new MulticastSocket(mport);
            group = InetAddress.getByName(multicast_addr);
            // SecurityManager.checkMulticast(InetAddress);
            ms.joinGroup(group);
        } catch (Exception e) {
            win.Log("Multicast daemon failure: " + e + "\n");
            if (ms != null) {
                ms.close();
                ms = null;
            }
            mport = -1;
            group = null;
        }

    }

    /**
     * Test object
     *
     * @return true if multicast socket is valid
     */
    public boolean valid() {
        return ms != null;
    }

    /**
     * Sends packet to group
     *
     * @param dp Datagram packet with data to be sent
     * @throws java.io.IOException due to communication errors
     */
    public void send_packet(DatagramPacket dp) throws IOException {
        if (!valid()) {
            win.Log("Invalid call to send_packet multicast\n");
            return;
        }
        try {
            dp.setAddress(group);
            dp.setPort(mport);
            ds.send(dp);
        } catch (IOException e) {
            throw e;
        }
        // win.Log("mpacket sent to " + mport + "\n");
    }

    /**
     * Thread main function
     */
    @Override
    public void run() {
        byte[] buf = new byte[8096];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);
        try {
            while (keepRunning) {
                try {
                    ms.receive(dp);
                    ByteArrayInputStream BAis
                            = new ByteArrayInputStream(buf, 0, dp.getLength());
                    DataInputStream dis = new DataInputStream(BAis);
                    System.out.println("Received mpacket (" + dp.getLength()
                            + ") from " + dp.getAddress().getHostAddress()
                            + ":" + dp.getPort());
                    byte code;
                    char sender;
                    try {
                        code = dis.readByte();     // read code
                        sender = dis.readChar();   // read sender id
                        String ip = dp.getAddress().getHostAddress();  // Get sender address
                        switch (code) {
                            case Router.PKT_ROUTE:
                                route.process_multicast_ROUTE(sender,
                                        dp, ip, dis);
                                break;
                            default:
                                win.Log("Invalid mpacket type: " + code + "\n");
                        }
                    } catch (IOException e) {
                        win.Log("Multicast Packet too short\n");
                    }
                } catch (SocketException se) {
                    if (keepRunning) {
                        win.Log("recv UDP SocketException : " + se + "\n");
                    }
                }
            }
        } catch (IOException e) {
            if (keepRunning) {
                win.Log("IO exception receiving data from socket : " + e);
            }
        }
    }

    /**
     * Stop the thread
     */
    public void stopRunning() {
        keepRunning = false;
        try {
            InetAddress _group = InetAddress.getByName(multicast_addr);
            ms.leaveGroup(_group);
        } catch (UnknownHostException e) {
            win.Log("Invalid address in stop running '" + multicast_addr + "': " + e + "\n");
        } catch (IOException e) {
            win.Log("Failed leave group: " + e + "\n");
        }
        if (this.isAlive()) {
            this.interrupt();
        }
    }

}
