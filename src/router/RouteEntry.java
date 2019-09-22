/**
 * Redes Integradas de Telecomunicacoes I
 * MIEEC 2019/2020
 *
 * RouteEntry.java
 *
 * Hold routing table entries
 *
 * Created on August 30, 19:00
 * @author  Luis Bernardo
 */

package router;

public class RouteEntry extends Entry {

// Fields inherited from Entry
//    public char dest;
//    public int dist;
// New fields
    /** next hop */
    public char next_hop;
    /** Link State Specific field - true=route is final; false=route is tentative */
    public boolean ok;    
    
    /**
     * Constructor - create an empty instance to a destination
     * @param dest destination address
     */
    public RouteEntry(char dest) {
        super(dest, Router.MAX_DISTANCE);
        next_hop= ' ';
        ok= false;
    }

    /**
     * Constructor - clone an existing entry
     * @param src  object that will be cloned
     */
    public RouteEntry(RouteEntry src) {
        super(src);
        next_hop= src.next_hop;
        this.ok= false;
    }

    /**
     * Constructor - create an entry from a ROUTE vector element defining next hop
     * @param src               ROUTE vector element
     * @param next_hop          next hop address
     */
    public RouteEntry(Entry src, char next_hop) {
        super(src);
        this.next_hop= next_hop;
        this.ok= false;
    }
    
    /**
     * Constructor - create an entry defining all fields
     * @param dest           destination address
     * @param next_hop       next hop address
     * @param dist           distance
     */
    public RouteEntry(char dest, char next_hop, int dist) {
        super(dest, dist);
        this.next_hop= next_hop;
        this.ok= false;
    }

    /**
     * compares with another routing entry
     * @param re    comparing object
     * @return      true if objects are equal, false otherwise
     */
    public boolean equals_to (RouteEntry re) {
        return ((Entry)this).equals_to(re) && (this.next_hop==re.next_hop);
    }
    
    /**
     * Link State Specific - Set ok flag as "final"
     */
    public void set_final() { ok= true; }
    
    /**
     * Link State Specific - check if route is final
     * @return true if is final, false if is tentative
     */
    public boolean is_final() { return ok; }
    
    /**
     * Check if next hop is defined
     * @return true if is defined, false otherwise
     */
    public boolean has_next() { return next_hop!=' '; }
    
    /**
     * returns a string with the contents of a RouteEntry object
     * @return String with entry contents
     */
    @Override
    public String toString() {
        return "(dest="+dest+",dist="+dist+", next_hop="+next_hop+")";
    }
}
