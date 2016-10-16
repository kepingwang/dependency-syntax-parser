package keping.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A dependency tree object containing arrays/Lists of head, deps, and labs.
 * Not linked to a particular Sentence.
 * @author wkp
 */
public class DepTree implements java.io.Serializable {
    private static final long serialVersionUID = 1699348477544331403L;
    private transient int[] head; // heads of dependency arcs
    private transient String[] lab; // labels of dependency arcs
    private transient int N; // length of the sentence
    private List<Integer> heads; // indices should start from 0.
    private List<Integer> deps;
    private List<String> labs;

    /**
     * Initialize DepTree using empty lists of heads, deps, labs.
     */
    public DepTree() {
        heads = new ArrayList<Integer>();
        deps  = new ArrayList<Integer>();
        labs  = new ArrayList<String>();
    }
    
    public DepTree(DepTree Tree) {
        heads = new ArrayList<Integer>();
        deps  = new ArrayList<Integer>();
        labs  = new ArrayList<String>();
        heads.addAll(Tree.heads);
        deps.addAll(Tree.deps);
        labs.addAll(Tree.labs);
    }
    
    /**
     * Construct DepTree using existing head and lab array in order.
     * @param head
     * @param lab
     */
    public DepTree(Integer[] head, String[] lab) {
        this.head = new int[head.length];
        for (int i = 0; i < head.length; i++)
            this.head[i] = head[i];
        initLab(lab);
        initList();
    }    
    public DepTree(List<Integer> head, List<String> lab) {
        this.head = new int[head.size()];
        this.lab  = new String[lab.size()];
        for (int i = 0; i < this.head.length; i++) {
            this.head[i] = head.get(i);
            this.lab[i]  = lab.get(i);
        }
        this.N = this.head.length;
        initList();
    }  
    /**
     * Construct DepTree using existing head and lab array in order.
     * @param head
     * @param lab
     */
    public DepTree(int[] head, String[] lab) {
        this.head = new int[head.length];
        for (int i = 0; i < head.length; i++)
            this.head[i] = head[i];
        initLab(lab);
        initList();
    }
    private void initLab(String[] lab) {
        this.lab  = new String[lab.length];
        for (int i = 0; i < lab.length; i++)
            this.lab[i]  = lab[i];
        this.N = lab.length;
    }
    private void initList() {
        heads = new ArrayList<Integer>();
        deps  = new ArrayList<Integer>();
        labs  = new ArrayList<String>();
        int endLoc = N-1;
        if (lab[endLoc].equals("_END")) endLoc--;
        int startLoc = 0;
        if (lab[startLoc].equals("_START")) startLoc++;
        for (int i = startLoc; i <= endLoc; i++) {
            deps.add(i);
            heads.add(head[i]);
            labs.add(lab[i]);
        }
    }
    
    public int head(int i)   { return head[i]; }
    public String lab(int i) { return lab[i]; }
    /**
     * Length of the head, lab array (in ascending order).
     * @return
     */
    public int len() { return N;}
    
    /**
     * Append a dependency arc (head, dep, lab) to the list. 
     * @param head
     * @param dep
     * @param lab
     */
    public void addDep(int head, int dep, String lab) {
        heads.add(head);
        deps.add(dep);
        labs.add(lab);
    }

    /**
     * Size of the unordered heads, labs, deps Lists.
     * @return
     */
    public int listSize()       { return heads.size(); }
    public int getHead(int i)   { return heads.get(i); }
    public int getDep(int i)    { return deps.get(i);  }
    public String getLab(int i) { return labs.get(i);  }
    public boolean containsArc(int i, int j) {
        for (int s = 0; s < heads.size(); s++)
            if (heads.get(s) == i && deps.get(s) == j) return true;
        return false;
    }
    public String[] getArcsFrom(int h) {
        Map<Integer, String> children = new HashMap<Integer, String>();
        for (int i = 0; i < listSize(); i++) {
            if (heads.get(i) == h) {
                children.put(deps.get(i), labs.get(i));
            }
        }
        if (children.size() == 0) return new String[0];
        String[] arcs = new String[children.size()];
        List<Integer> depList = new ArrayList<Integer>();
        for (Integer dep : children.keySet())
            depList.add(dep);
        Collections.sort(depList);
        for (int i = 0; i < arcs.length; i++)
            arcs[i] = children.get(depList.get(i));
        return arcs;
    }
    public int[] getChildrenFrom(int h) {
        Map<Integer, String> children = new HashMap<Integer, String>();
        for (int i = 0; i < listSize(); i++) {
            if (heads.get(i) == h) {
                children.put(deps.get(i), labs.get(i));
            }
        }
        if (children.size() == 0) return new int[0];
        List<Integer> depList = new ArrayList<Integer>();
        for (Integer dep : children.keySet())
            depList.add(dep);
        Collections.sort(depList);
        int[] depAR = new int[depList.size()];
        for (int i = 0; i < depAR.length; i++)
            depAR[i] = depList.get(i);
        return depAR;
    }
    
    /**
     * Sort the heads, deps, labs list in sentence (deps) order, and
     * construct the array. Add the head of _ROOT to dependency tree.
     */
    public void sort() {
        N = heads.size()+1;
        if (head != null || lab != null) {
            try { throw new Exception("DepTree already set!"); } 
            catch (Exception e) { e.printStackTrace(); }
        }
        head = new int[N];
        lab  = new String[N];
        head[0] = -1;
        lab[0]  = "_ROOT";
        for (int i = 0; i < heads.size(); i++) {
            head[deps.get(i)] = heads.get(i);
            lab[deps.get(i)]  = labs.get(i);
        }
        
    }
    
    /**
     * If arcs are not sorted, return the arcs. If they are sorted, skip the 
     * first _ROOT row, and return the sorted --head, lab.
     */
    public String toString() {
        String s = "";
        if (head != null) {
            for (int i = 1; i < N; i++) {
                s += head[i]-1;
                if (lab != null) s += "\t" + lab[i];
                s += "\n";
            }
        } else {
            for (int i = 0; i < heads.size(); i++) {
                s += heads.get(i)+"\t";
                s += labs.get(i) +"\t";
                s += deps.get(i) +"\n";
            }
        }
        return s;
    }
    
}
