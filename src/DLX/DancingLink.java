package DLX;

import java.util.*;
import java.lang.System;

/**
 * @author Elias Bendjaballah 3700088
 * 
 *         The following implementation of Algorithm X using Dancing Links
 *         follows the article written by Donald Knuth in 2000. I used Mark
 *         Engelberg's famous tarantella github repo to make the following
 *         implementation of the dancing links. Many attributes share the same
 *         names because I decided to use the names chosen by Knuth in his
 *         paper. I tried to write my own version of the algorithm as much as
 *         possible and used the tarantella code to debug the problems I
 *         encoutered during the coding of the doubly linked lists. Using Java
 *         was a necessity for me because I do not have the sufficient clojure
 *         knowledge to code them efficiently and as discussed in Mark
 *         Engelberg's clojure talk (available in the README.md file) the
 *         following java implementation is much more efficient.
 * 
 *         I did read Knuth's paper entirely and did not proceed to a simple
 *         copy/paste. (I added the article in this repo).
 * 
 */

public class DancingLink {
    public DancingLink left; // for the left link
    public DancingLink right; // for the right link
    public DancingLink up; // for the upper link
    public DancingLink down; // for the down link
    public DancingLink column; // used to link with the column header
    public Object n; // Object that will be used to solve the problem
    public int size; // The size corresponds to the number of ones in the matrix
    public boolean optional; // Optional columns have one more choice?

    public DancingLink(Object n) {
        this.n = n;
    }

    public DancingLink(Object n, int size, boolean optional) {
        if (optional) {
            this.size = size++;
        } else {
            this.size = size;
            this.n = n;
            this.optional = optional;
        }
    }

    // The cover method updates the size and removes the column that called it 
    // from the header list and removes all rows in the column's own list
    // from the other columns lists where they appear.
    // We use this function to remove objects in the horizontal and vertical
    // directions.
    void cover() {
        this.right.left = this.left;
        this.left.right = this.right;
        for (DancingLink i = this.down; i != this; i = i.down) {
            for (DancingLink j = i.right; j != i; j = j.right) {
                j.down.up = j.up;
                j.up.down = j.down;
                j.column.size--;
            }
        }
    }

    // The uncovering operation proceeds in the reverse order of the cover operation
    // and updates the size.
    void uncover() {
        for (DancingLink i = this.up; i != this; i = i.up){
            for (DancingLink j = this.left; j != this; j = j.left) {
                j.column.size++ ;
                j.down.up = j;
                j.up.down = j ;
            }
        }
        this.right.left = this;
        this.left.right = this;
    }

    // The following function links elements in the horizontal direction
    // given an ArrayList<DancingLink> of DancingLinks in order to 
    // create a doubly linked list.
    static void linkHorizontally(ArrayList<DancingLink> links) {
        
        if (links.size()==0) return;

        DancingLink first = links.get(0);
        DancingLink last = links.get(links.size()-1);
        // Linking of the first and last elements
        last.right = first;
        first.left = last;
        // Double linking of the DancingLink elements  
        for (int i = 0; i < links.size() - 1; i++) {
            links.get(i).right = links.get(i+1);
            links.get(i+1).left = links.get(i);
        }
    }

    // The following function links elements in the vertical direction
    // given an ArrayList<DancingLink> of DancingLinks in order to 
    // create a doubly linked list while .
    static void linkVertically(ArrayList<DancingLink> links) {
        DancingLink first = links.get(0);
        DancingLink last = links.get(links.size()-1);
        // Linking of the first and last elements
        first.up = last;
        last.down = first;
        // The column field of each object points to the column object at the
        // head of the corresponding column. The headers form a list and each
        // element is linked to the other columns by the left and right attributes.
        // According to the 'Dancing Links article', The circular doubly linked list
        // includes a root which is a special column object where the size,up,down and 
        // column attributes are not used.
        for (DancingLink e : links) e.column = first;
        for (int i = 0; i < links.size(); i++) {
            links.get(i).down = links.get(i+1);
            links.get(i+1).up = links.get(i);
        }
    }

    

}