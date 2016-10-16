package keping.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import keping.ec.EC;

/**
 * Sentence object.
 * @author wkp
 */
public class Sentence implements java.io.Serializable {
    private static final long serialVersionUID = 2580565324870541611L;
    private String[] word; // words
    private String[] pos; // part-of-speech tags
    private transient DepTree tree; // dependency tree.
    private int N; // length of the sentence
    
    /**
     * Constructor for test/dev data. (Don't allow empty nodes.)
     * @param s
     * @param pos
     */
    public Sentence(List<String> word, List<String> pos) {
        this.word   = word.toArray(new String[0]);
        this.pos = pos.toArray(new String[0]);
        N = this.word.length;
    }
    public Sentence(String[] word, String[] pos) {
        this.word = word;
        this.pos = pos;
        N = this.word.length;
    }
    /**
     * Constructor for training data. (Allow empty nodes.)
     * @param s
     * @param pos
     * @param head
     * @param label
     */
    public Sentence(List<String> word, List<String> pos,
            List<Integer> head, List<String> lab) {
        this.word   = word.toArray(new String[0]);
        this.pos = pos.toArray(new String[0]);
        N = this.word.length;
        this.tree = new DepTree(
                head.toArray(new Integer[0]), lab.toArray(new String[0]));
    }

    /**
     * Number of empty nodes in the sentence.
     * -1 if empty nodes are not allowed.
     * @return
     */
    public int emptyLen() {
        int emptyCount = 0;
        for (int i = 0; i < N; i++) 
            if (word[i].equals("*PRO*")) { emptyCount++; }
        return emptyCount;
    }
    public int len() { return N; }
    public String wrd(int i) {
        if (i == -1) return "_ROOT_";
        else         return word[i]; 
    }
    public String pos(int i) {
        if (i == -1) return "_ROOT_"; 
        else         return pos[i]; 
    }
    public int head(int i) { return tree.head(i); }
    public String lab(int i) { return tree.lab(i); }
    
    public void setTree(DepTree newTree) {
        this.tree = newTree;
    }
    public DepTree getTree() {
        return tree;
    }
    
    /**
     * Return a new Sentence with a List of ECTuples added.
     * Only true EC are passed to this method.
     */
    public Sentence addEC(List<EC> ecList) {
        Collections.sort(ecList, new EC.TComparator());
        int ecNum = 0;
        int[] ecCount = new int[N];
        for (EC ec : ecList) {
            ecCount[ec.getTuple().t()]++;
            ecNum++;
        }
        String[] word = new String[N+ecNum];
        String[] pos = new String[N+ecNum];
        int c = 0;
        for (int i = 0; i < N; i++) {
            c += ecCount[i];
            word[c] = this.word[i];
            pos[c]  = this.pos[i];
            c += 1;
        }
        
        int id = 0;
        for (EC ec : ecList) {
            while (word[id] != null) {
                id++;
            }
            word[id] = "*PRO*";
            pos[id]  = "EMCAT";
            if (ec.getType().isTrue()) {}
        }
        return new Sentence(word, pos);
    }
    /**
     * Gets a new Sentence without Empty Categories.
     * @return new Sentence without EC
     */
    public Sentence removeEC() {
        int[] ecCount = new int[N];
        int count = 0;
        for (int i = 0; i < N; i++) {
            ecCount[i] = count;
            if (word[i].equals("*PRO*")) count++;
        }
        List<String> wrdNEC = new ArrayList<String>();
        List<String> posNEC  = new ArrayList<String>();
        List<Integer>  headNEC = new ArrayList<Integer>();
        List<String> labNEC  = new ArrayList<String>();
        for (int i = 0; i < N; i++) {
            if (!word[i].equals("*PRO*")) {
                wrdNEC.add(wrd(i));
                posNEC.add(pos(i));
                int headNum = head(i);
                if (headNum >= 0) headNum -= ecCount[headNum];
                headNEC.add(headNum);
                labNEC.add(lab(i));
            }
        }
        Sentence newS = new Sentence(wrdNEC, posNEC, headNEC, labNEC);
        return newS;
    }
    
    public Sentence addEND() {
        List<String>  word = new ArrayList<String>();
        List<String>  pos  = new ArrayList<String>();
        for (int i = 0; i < N; i++) {
            word.add(wrd(i));
            pos.add(pos(i));
        }
        word.add("_END");
        pos.add("_END");
        if (tree == null) {
            return new Sentence(word, pos);
        } else {
            List<Integer> head = new ArrayList<Integer>();
            List<String>  lab  = new ArrayList<String>();
            for (int i = 0; i < N; i++) {
                head.add(head(i));
                lab.add(lab(i));
            }
            head.add(-9);
            lab.add("_END");
            return new Sentence(word, pos, head, lab);
        }
    }
    public Sentence removeEND() {
        List<String>  word = new ArrayList<String>();
        List<String>  pos  = new ArrayList<String>();
        for (int i = 0; i < N-1; i++) {
            word.add(wrd(i));
            pos.add(pos(i));
        }
        if (tree == null) {
            return new Sentence(word, pos);
        } else {
            List<Integer> head = new ArrayList<Integer>();
            List<String>  lab  = new ArrayList<String>();
            for (int i = 0; i < N-1; i++) {
                head.add(head(i));
                lab.add(lab(i));
            }
            return new Sentence(word, pos, head, lab);
        }
    }
    
    public String toStringNo_ROOT() {
        String s = "";
        for (int i = 1; i < N; i++) {
            s += word[i];
            s += "\t" + pos[i];
            if (tree != null) {
                s += "\t"+(tree.head(i)-1);
                s += "\t"+tree.lab(i);
            }
            s += "\n";
        }
        return s;
    }
    public String toString() {
        String s = "";
        for (int i = 0; i < N; i++) {
            s += word[i];
            s += "\t" + pos[i];
            if (tree != null) {
                s += "\t"+(tree.head(i));
                s += "\t"+tree.lab(i);
            }
            s += "\n";
        }
        return s;
    }

    public boolean equals(Sentence that) {
        if (this.tree.len() != N || that.tree.len() != that.N) {
            try { throw new Exception("Tree not ready."); } catch (Exception e) {}
        }
        if (this.N != that.N) return false;
        for (int i = 0; i < N; i++) {
            if (!word[i].equals(that.word[i]))     return false;
            if ( !pos[i].equals(that.pos[i]) )     return false;
            if (tree.head(i) != that.tree.head(i)) return false;
            if (!tree.lab(i).equals(that.lab(i)))  return false;
        }
        return true;
    }
}
