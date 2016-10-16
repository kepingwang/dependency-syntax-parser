package keping.parsing;


/**
 * Transition function of the transition-based parsing, intermediate between
 * Classifier and Decoder. Immutable after initialization.
 * @author wkp
 *
 */
public class Transition extends keping.classification.Target {
    private static final long serialVersionUID = -4025552063217505680L;
    private String type;
    private String lab;
    public Transition(String type) {
        if (!type.equals("shift")) { try {throw new Exception("Wrong type!");} catch (Exception e) {} }
        this.type = type;
    }
    public Transition(String type, String lab) {
        if (!type.equals("shift") && !type.equals("leftArc") && !type.equals("rightArc") ) 
            try {throw new Exception("Wrong type!");} catch (Exception e) {}
        this.type = type;
        if (lab != null) this.lab = lab;
    }
    
    /**
     * The type of the transition to be performed.
     * @return
     */
    public String type() {
        return type;
    }
    /**
     * The label of the arc transition, null if it's shift transition.
     * @return
     */
    public String lab() {
        if (lab != null) return lab;
        else             return null;
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lab == null) ? 0 : lab.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Transition other = (Transition) obj;
        if (lab == null) {
            if (other.lab != null)
                return false;
        } else if (!lab.equals(other.lab))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return type+"\t"+lab;
    }
    
}
