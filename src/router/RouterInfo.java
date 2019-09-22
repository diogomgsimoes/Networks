/**
 * Redes Integradas de Telecomunicacoes I
 * MIEEC 2019/2020
 *
 * RouterInfo.java
 *
 * Holds the information regarding the routers sending ROUTE packets
 * Note that in link state the routers flood the ROUTE packets - they come from 
 * all nodes in an area
 *
 * Created on August 30, 19:00
 * @author  Luis Bernardo
 */

package router;

import java.util.Date;
import java.util.HashMap;

/**
 * Auxiliary class to hold routing information received from each router
 */
public class RouterInfo {

    /**
     * class with specific area data
     */
    /** address name */
    public char name;
    /** Entry vector with neighbour list received */
    public Entry[] vec;
    /** Last sequence number */
    public int seq;
    /** Time To Live (s) */
    public int TTL;
    /** Time when the vector was received */
    public Date date;
    /** Reference to the main window of the GUI */
    private Router win;

    /**
     * Creates a new instance of RouterInfo
     */
    /**
     * Constructor - creates a new instance of RouterInfo
     * @param win   Reference to the main window of the GUI
     * @param name  address name
     * @param seq   ROUTE sequence number
     * @param TTL   Time To Live (s)
     * @param vec   Entry vector with neighbour list
     */
    public RouterInfo(Router win, char name, int seq, int TTL, Entry[] vec) {
        this.name = name;
        this.vec = vec;
        this.seq = seq;
        this.TTL = TTL;
        this.date = new Date();
        this.win = win;
    }

    /**
     * Constructor - clones the content of another object
     * @param src  object to be cloned
     */
    public RouterInfo(RouterInfo src) {
        this.name = src.name;
        this.vec = src.vec;
        this.seq = src.seq;
        this.TTL = src.TTL;
        this.date = src.date;
    }

    /**
     * Update the vector received
     * @param vec   Entry vector received
     * @param seq   sequence number
     * @param TTL   Time to live
     */
    public void update_vec(Entry[] vec, int seq, int TTL) {
        this.date = new Date(); // Get current time
        this.vec = vec;
        this.seq = seq;
        this.TTL = TTL;
    }

    /**
     * Test if the vector is still valid (is defined and TTL has not elapsed
     * @return true if is valid, false otherwise
     */
    public boolean vec_valid() {
        long now = System.currentTimeMillis();
        return (vec != null) && (date != null) && ((now - date.getTime()) <= TTL * 1000);
    }

    /**
     * Test if the vector in _vec is valid
     * @param _vec  vector to be tested
     * @return true if valid, false otherwise
     */
    private boolean test_vec_contents(Entry[] _vec) {
        if (_vec == null) {
            return false;
        }
        HashMap<String, String> h = new HashMap<>();
        for (Entry entry : _vec) {
            if (h.containsKey("" + entry.dest)) {
                win.Log("Invalid vector - duplicated destination '" + entry.dest + "'\n");
                return false;
            }
            h.put("" + entry.dest, "");
        }
        return true;
    }

    /**
     * Test if the objects' vector is equal to _vec
     * @param _vec  the vector to be compared
     * @return true if different, false otherwise
     */
    public boolean test_diff_vec(Entry[] _vec) {
        if (!test_vec_contents(_vec)) {
            return false;
        }
        return !Entry.equal_Entry_vec(vec, _vec);
    }
}
