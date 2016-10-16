package keping.ec;

import keping.classification.Target;

public class ECType extends Target {
    private static final long serialVersionUID = 752012750170189965L;
    private boolean val;
    public ECType(boolean val) { this.val = val; }
    
    /**
     * Returns whether it is empty category.
     * @return
     */
    public boolean isTrue() {
        return val;
    }
    public String toString() {
        return ""+val;
    }
}
