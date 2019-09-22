/**
 * Redes Integradas de Telecomunicações I
 * MIEEC 2019/2020
 *
 * neighbourList.java
 *
 * Holds the neighbor list router internal data
 *
 * Created on August 30, 19:00
 * @author  Luis Bernardo
 */
package router;

import java.util.*;
import java.net.*;
import java.io.*;
import javax.swing.*;


public class NeighbourList {
    
    /** Maximum number of neigbour objects in the list */
    private int max_range= 0;
    /** Reference to the main window of the GUI */
    private final Router win;
    /** List of neighbour objects */
    private final HashMap<String,Neighbour> list;

    
    /**
     * Constractor - create a new instance of NeighbourList
     * @param max_range maximum number of neigbours in the list
     * @param win       main window
     */
    public NeighbourList(int max_range, Router win) {        
        this.max_range= max_range;
        this.win= win;
        list= new HashMap<>();
    }

    /**
     * 
     * @return 
     */
    public boolean is_empty() {
        return list.isEmpty();
    }
    
    /**
     * Creates an Iterator for all neigbour objects in the list
     * @return iterator for all neigbours in the list
     */
    public Iterator<Neighbour> iterator() {
        return list.values().iterator();
    }

    /**
     * Add a new neighbour to the list
     * @param name      neighbour's name
     * @param ip        ip address
     * @param port      port number
     * @param distance  distance
     * @param ds        datagram socket
     * @return true if new neighbour was created and added, false otherwise
     */
    public boolean add_neig(char name, String ip, int port, int distance, DatagramSocket ds) {
        char local_name= win.local_name();        
        boolean novo;
        System.out.println("add_neig("+name+")");
        synchronized (this) {
            if ((novo= !list.containsKey(""+name)) && (list.size()==max_range)) {
                System.out.println("List is full\n");
                return false;
            }
        }
        Neighbour pt= locate_neig(ip, port);
        if (local_name == name) {
            System.out.println("Name equals local_name");
            return false;
        }
        if ((pt != null) && (pt.Name()!= name)) {                
            System.out.println("Duplicated IP and port\n");
            return false;
        }
        if ((distance<1) || (distance>Router.MAX_DISTANCE)) {
            System.out.println("Invalid distance ("+distance+")");
            return false;
        }
        // Prepare Neighbour entry
        pt= new Neighbour(name, ip, port, distance);
        if (!pt.is_valid()) {
            System.out.println("Invalid neighbour data\n");
            return false;
        }
        synchronized (this) {
            // Adds or replaces a member of the table
            list.put(""+name, pt);
        }
        if (novo) // If not known
            pt.send_Hello(ds, win);
        return true;
    }
        
    /**
     * Update the field values of a neighbour with the ip+port
     * @param name      neighbour's name
     * @param ip        ip address
     * @param port      port number
     * @param distance  distance
     * @return true if updated the fields, false otherwise
     */
    public boolean update_neig(char name, String ip, int port, int distance) {
        System.out.println("update_neig("+name+")");
        Neighbour pt= locate_neig(ip, port);
        if (pt == null) {
            System.out.println("Inexistant Neighbour\n");
            return false;
        }
        if ((distance<1) || (distance>Router.MAX_DISTANCE)) {
            System.out.println("Invalid distance ("+distance+")");
            return false;
        }
        if (name != pt.Name ()) {
            System.out.println("Invalid name - missmatched name previously associated with IP/port");
            return false;
        }
        if (pt.Dist() == distance) {
            // Did not change distance
            return false;
        }
        // Prepare Neighbour entry
        pt.update_neigh(pt.Name(), ip, port, distance);
        return true;
    }    
    
    /**
     * Delete a neighbour from the list, selected by name
     * @param name        name of neighbour
     * @param send_msg    if true, sends a BYE message
     * @param ds          datagram socket
     * @return true if deleted successfully, false otherwise
     */
    public boolean del_neig(char name, boolean send_msg, DatagramSocket ds) {
        Neighbour neig;
        synchronized (this) {
            try {
                neig= (Neighbour)list.get(""+name);
            }
            catch (Exception e) {
                return false;
            }
        }
        if (neig == null) {
            win.Log("Neighbour "+name+" not deleted\n");
            return false;
        }
        if (send_msg)
            neig.send_Bye(ds, win);
        synchronized (this) {
            // Adds or replaces a member of the table
            list.remove(""+name);
        }
        return true;
    }    

    /**
     * Delete a neighbour from the list, selected by object
     * @param neig      neighbour to be deleted
     * @param send_msg    if true, sends a BYE message
     * @param ds          datagram socket
     * @return true if deleted successfully, false otherwise
     */
    public boolean del_neig(Neighbour neig, boolean send_msg, DatagramSocket ds) {
        synchronized (this) {
            if (!list.containsValue(neig))
                return false;
        }
        if (send_msg)
            neig.send_Bye(ds, win);        
        synchronized (this) {
            // Removes a member from the list
            list.remove(""+neig.Name());
        }
        return true;
    }
    
     /**
     * Clear the neighbour list and send BYE to all members
     * @param ds            datagram socket
     */
    public void clear_BYE(DatagramSocket ds) {
        synchronized (this) {
            for (Neighbour pt : list.values()) {
                pt.send_Bye(ds, win);
            }
        }
        clear();
    }
    
    /**
     * Clear the neighbour list
     */
    public void clear() {
        synchronized (this) {
            list.clear();
        }
    }
    
    /**
     * Locate a neighbour by name in the list
     * @param name  name to look for
     * @return the neighbour object, or null if not found
     */
    public Neighbour locate_neig(char name) {
        return (Neighbour)list.get(""+name);
    }

    /**
     * Locate a neighbour by ip+port in the list
     * @param ip    IP address
     * @param port  port number
     * @return the neighbour object, or null if not found
     */
    public Neighbour locate_neig(String ip, int port) {
        synchronized (this) {
            for (Neighbour pt : list.values()) {
                if ((ip.compareTo(pt.Ip()) == 0) && (port == pt.Port()))
                    return pt;
            }
        }
        return null;
    }

    /**
     * Send a packet to all neighbours in the list except 'exc'
     * @param ds    datagram socket
     * @param dp    datagram packet to be sent
     * @param exc   neighbour to exclude, or null
     * @throws IOException 
     */
    public void send_packet(DatagramSocket ds, DatagramPacket dp, 
                            Neighbour exc) throws IOException {
        synchronized (this) {
            for (Neighbour pt : list.values()) {
                if (pt != exc)
                    pt.send_packet(ds, dp);
            }
        }        
    }

    /**
     * Print the neighbour list in the table at the GUI
     * @param table  reference to the graphical table
     * @return true if successful, false otherwise
     */
    public boolean refresh_table(JTable table) {
        synchronized (this) {
            if (table.getColumnCount() < 4)
                // Invalid number of columns
                return false;
            if (table.getRowCount() < max_range)
                // Invalid number of rows
                return false;
            
            // Update table
            Iterator it= list.values().iterator();        
            for (int i= 0; i<max_range; i++) { // For every row
                Neighbour pt;
                if (it.hasNext())
                    pt= (Neighbour)it.next();
                else
                    pt= null;
                if (pt == null) {
                    for (int j= 0; j<4; j++)
                        table.setValueAt("", i,  j);
                } else {
                    table.setValueAt(""+pt.Name(), i,  0);
                    table.setValueAt(pt.Ip(), i,  1);
                    table.setValueAt(""+pt.Port(), i,  2);
                    table.setValueAt(""+pt.Dist(), i,  3);
                }
            }
        }
        return true;
    }   
    
    
    /* ********************************************************************* */
    /* Functions for link state support                                      */
    /* ********************************************************************* */
    
    /**
     * For link state protocols - returns the vector with the neighbors that 
     * belong to a given area
     * @param add_local if true, the list includes the node name
     * @return          a vector with the neighbour nodes
     */
    public Entry[] local_vec(boolean add_local) {
        ArrayList<Entry> aux= new ArrayList<>();
        
        if (add_local) {
            // Adds the local name
            aux.add(new Entry(win.local_name(), 0));
        }
            
        synchronized (this) {            
            Iterator<Neighbour> it= list.values().iterator();
            while (it.hasNext()) {
                Neighbour pt= it.next();
                if (pt.is_valid()) {
                    aux.add(new Entry(pt.Name(), pt.Dist()));
                }
            }
                
            // Creates an array with all elements
            Entry[] vec= new Entry[aux.size()];
            aux.toArray(vec);
            aux.clear();
            return vec;
        }        
    }
    
    /* ********************************************************************* */
    /* End of functions for link state support                               */
    /* ********************************************************************* */

}