package keping.parsing;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import keping.common.DepTree;
import keping.common.Sentence;

/**
 * State object of the transition-based parsing.
 * @author wkp
 */
public class State extends keping.classification.Input {
    private static final long serialVersionUID = 8774109568748602023L;
    transient Sentence S; // The original sentence.
    DepTree T; // current dependency arcs
    Deque<Integer> stack; // stack
    Deque<Integer> buffer; // buffer

    public State() {}
    
    /**
     * Return a clone of the State object.
     */
    public State(State state) {
        S = state.S;
        T = new DepTree(state.T);
        stack  = new LinkedList<Integer>();
        buffer = new LinkedList<Integer>();
        stack.addAll(state.stack);
        buffer.addAll(state.buffer);
    }
    
    /**
     * Construct the initial state of the given sentence.
     * @param S
     */
    public State(Sentence S) {
        this.S = S; 
        T = new DepTree();
        stack  = new LinkedList<Integer>();
        buffer = new LinkedList<Integer>();
        stack.addLast(0);
        for (int i = 1; i < S.len(); i++)
            buffer.addLast(i);
    }
    
    public void setSentence(Sentence S) {
        this.S = S;
    }
    
    /**
     * @return <b>true</b> if the state is a terminal state.
     */
    public boolean isTerminal() { return buffer.isEmpty(); }
    public boolean emptyStack() { return stack.isEmpty();  }
    public int lenSTK()         { return stack.size();     }
    public int lenBUF()         { return buffer.size();    }
    public Deque<Integer> getSTK() { return stack; }
    public Deque<Integer> getBUF() { return buffer; }
    
    /**
     * Make a transition to the state according to <code>trans</code>.
     * @param trans
     */
    public void transit(Transition trans) {
        String type = trans.type();
        String lab  = trans.lab();
        if      (type.equals("leftArc"))  { leftArc(lab);  }
        else if (type.equals("rightArc")) { rightArc(lab); }
        else if (type.equals("shift"))    { shift();       }
        else { 
            try { throw new Exception("Something Wrong during Transition!"); } 
            catch (Exception e) { e.printStackTrace();}
        }
    }
    
    /**
     * Get the gold transition for this state given training Sentence.
     * @return <code>Transition</code>
     */
    public Transition getGoldTrans() {
        if (stack.isEmpty() || buffer.isEmpty()) return new Transition("shift");
        int i =  stack.peekLast();
        int j = buffer.peekFirst();
        if (S.head(i) == j) { return new Transition("leftArc", S.lab(i)); }
        else if (S.head(j) == i) {
            boolean moreArc = false;
            for (int q = 0; q < S.len(); q++)
                if (S.head(q) == j && !T.containsArc(j, q)) {
                    moreArc = true;
                    break;
                }
            if (!moreArc) return new Transition("rightArc", S.lab(j));
            else          return new Transition("shift");
        } else { return new Transition("shift"); }
    }
    
    /**
     * Get the dependency tree from a terminal state. Note that if the state
     * is not terminal, then the tree's fields of sorted heads, labs array are
     * null.
     * @return
     */
    public DepTree getTree() {
        return T;
    }

    /**
     * Get the first word in the stack (last in queue).
     * @return
     */
    public String STK0() {
        return STK(0);
    }
    
    /**
     * Return STK(i) word id (position of the word in the sentence.)
     * @param i
     * @return
     */
    private int STKID(int i) {
        if (i > stack.size()-1) { return -2; }
        int id = -3; 
        int count = 0;
        Iterator<Integer> li = stack.descendingIterator();
        while (li.hasNext()) {
            id = li.next();
            if (i == count++) break;
        }
        return id; // position of the word in the sentence
    }
    private String STK(int i) {
        int id = STKID(i);
        if (id == -2) return "_NULL";
        else          return S.wrd(id);
    }
    
    // private methods for transition operation
    private void leftArc(String lab) {
        int i =  stack.pollLast();
        int j = buffer.peekFirst();
        T.addDep(j, i, lab);
    }
    private void rightArc(String lab) {
        int i =  stack.pollLast();
        int j = buffer.pollFirst();
        T.addDep(i, j, lab);
        buffer.addFirst(i);
    }
    private void shift() {
        int i = buffer.pollFirst();
        stack.addLast(i);
    }
    

}
