package keping.ec;

import keping.classification.Input;
import keping.common.Sentence;

public class ECTuple extends Input {
    private static final long serialVersionUID = 423300473135099755L;
    private Sentence S;
    private int h;
    private int t;
    public ECTuple(int h, int t, Sentence S) {
        this.h = h;
        this.t = t;
        this.S = S;
    }
    
    /**
     * Head of the EC.
     */
    public int h() { return h; }
    /**
     * The (original) position of the word following EC.
     */
    public int t() { return t; }
    /**
     * Get the sentence corresponding to the tuple.
     * @return
     */
    public Sentence S() { return S; }
    public String toString() {
        return "("+h+", "+t+")";
    }

}
