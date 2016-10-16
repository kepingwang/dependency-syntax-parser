package keping.parsing;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import keping.common.DepTree;
import keping.common.Paragraph;
import keping.common.Sentence;

/**
 * Feature vector. To add new features, you should modify both the Features class
 * and the getFeatures() method in State class.
 * @author wkp
 */
public class StateFeatures implements java.io.Serializable {
	private static final long serialVersionUID = 5941739891890826155L;
    private int rareCutoff = 9;
    private Map<String, Integer> wrdID; // word ID.
    private Map<String, Integer> posID; // pos tag ID.
    private Map<String, Integer> labID; // label ID.
    private String[] wrdAR; // word Array.
    private String[] posAR; // word Array.
    private String[] labAR; // label Array.
    List<String> featNames;
    Map<String, String> attribute;
    // store data temporarily (one State at a time)
    Map<String, boolean[]> map;
    transient Sentence S; // The original sentence.
    DepTree T; // current dependency arcs
    Deque<Integer> stack; // stack
    Deque<Integer> buffer; // buffer
    
    public StateFeatures() {
        featNames = new ArrayList<String>();
        attribute = new HashMap<String, String>();
        attribute.put("STK(0)", "wrd");
        attribute.put("STK(1)", "wrd");
        attribute.put("BUF(0)", "wrd");
        attribute.put("BUF(1)", "wrd");
        attribute.put("BUF(2)", "wrd");
        attribute.put("POS_STK(0)", "pos");
        attribute.put("POS_STK(1)", "pos");
        attribute.put("POS_STK(2)", "pos");
        attribute.put("POS_BUF(0)", "pos");
        attribute.put("POS_BUF(1)", "pos");
        attribute.put("POS_BUF(2)", "pos");
        attribute.put("POS_BUF(3)", "pos");
        attribute.put("LDEP_STK(0)", "lab");
        attribute.put("RDEP_STK(0)", "lab");
        attribute.put("LDEP_BUF(0)", "lab");
        attribute.put("RDEP_BUF(0)", "lab");
        attribute.put("POS_LDEP_STK(0)", "pos");
        attribute.put("POS_RDEP_STK(0)", "pos");
        attribute.put("POS_LDEP_BUF(0)", "pos");
        attribute.put("POS_RDEP_BUF(0)", "pos");
        attribute.put("POS_COMBO(0,0)", "posCombo");
        featNames.addAll(attribute.keySet());
    }
    
    public void initialize(Paragraph p) {
        initWrd(p);
        initPos(p);
        initLab(p);
    }
    
    public boolean[] getVector(State state) {
    	map = new HashMap<String, boolean[]>();
    	addFeatures(state);
        int featCount = 0;
        for (int i = 0; i < featNames.size(); i++)
            featCount += map.get(featNames.get(i)).length;
        boolean[] vector = new boolean[featCount];
        int i = 0;
        for (String feat : featNames)
            for (boolean subFeat : map.get(feat)) {
                vector[i++] = subFeat;
            }
        map = null;
        return vector;
    }
    
    public int labN() { return labAR.length; }
    public String getLab(int i) {
        return labAR[i];
    }
    public int getLabID(String lab) {
        return labID.get(lab);
    }

    
    private void addFeatures(State state) {
    	S = state.S;
    	T = state.T;
    	stack = state.stack;
    	buffer = state.buffer;
        add("STK(0)", STK(0)); // the 0th word in stack (last word)
        add("STK(1)", STK(1)); 
        add("BUF(0)", BUF(0)); // the 0th word in buffer 
        add("BUF(1)", BUF(1)); // the 1th word in buffer
        add("BUF(2)", BUF(2));
        add("POS_STK(0)", POS_STK(0)); // Pos tag of the word
        add("POS_STK(1)", POS_STK(1));
        add("POS_STK(2)", POS_STK(2));
        add("POS_BUF(0)", POS_BUF(0));
        add("POS_BUF(1)", POS_BUF(1));
        add("POS_BUF(2)", POS_BUF(2));
        add("POS_BUF(3)", POS_BUF(3));
        /* LDEP(w) is the farthest child of w to the Left. 
         * LDEP_w represents the arc label. */
        add("LDEP_STK(0)", LDEP_STK(0));
        add("RDEP_STK(0)", RDEP_STK(0));
        add("LDEP_BUF(0)", LDEP_BUF(0));
        add("RDEP_BUF(0)", RDEP_BUF(0));
        add("POS_LDEP_STK(0)", POS_LDEP_STK(0)); // Pos tag of the child
        add("POS_RDEP_STK(0)", POS_RDEP_STK(0));
        add("POS_LDEP_BUF(0)", POS_LDEP_BUF(0));
        add("POS_RDEP_BUF(0)", POS_RDEP_BUF(0));
        addPosCombo("POS_COMBO(0,0)", POS_STK(0), POS_BUF(0));
        // TODO: We always need some new features.
    }
    
    private void add(String featName, String featVal) {
        add(featName, attribute.get(featName), featVal);
    }
    private void add(String featName, String attr, String featVal) {
        boolean[] feat = new boolean[0];
        if      (attr.equals("wrd")) feat = featWrdVector(featVal);
        else if (attr.equals("pos")) feat = featPosVector(featVal);
        else if (attr.equals("lab")) feat = featLabVector(featVal);
        map.put(featName, feat);
    }
    private void addPosCombo(String featName, String pos1, String pos2) {
    	map.put(featName, featPosCombo(pos1, pos2));
    }
    
    private boolean[] featWrdVector(String wrd) {
        if (!wrdID.containsKey(wrd)) wrd = "_RARE";
        boolean[] feat = new boolean[wrdAR.length];
        feat[wrdID.get(wrd)] = true;
        return feat;
    }
    private boolean[] featPosVector(String pos) {
        if (!posID.containsKey(pos)) pos = "_NULL";
        boolean[] feat = new boolean[posAR.length];
        feat[posID.get(pos)] = true;
        return feat;
    }
    private boolean[] featLabVector(String lab) {
        if (!labID.containsKey(lab)) lab = "_NULL";
        boolean[] feat = new boolean[labAR.length];
        feat[labID.get(lab)] = true;
        return feat;
    }
    private boolean[] featPosCombo(String pos1, String pos2) {
    	if (!posID.containsKey(pos1)) pos1 = "_NULL";
    	if (!posID.containsKey(pos2)) pos2 = "_NULL";
    	boolean[] V = new boolean[posAR.length*posAR.length];
    	V[posID.get(pos1)*posAR.length + posID.get(pos2)] = true;
    	return V;
    }
    
    private void initWrd(Paragraph p) { // including _ROOT
        Map<String, Integer> wrdC = new HashMap<String, Integer>(); // word count
        for (Sentence S : p.getSentences())
            for (int i = 0; i < S.len(); i++) {
                String wrd = S.wrd(i);
                if (!wrdC.containsKey(wrd)) wrdC.put(wrd, 1);
                else                        wrdC.put(wrd, wrdC.get(wrd)+1);
            }
        wrdID = new HashMap<String, Integer>();
        List<String> wrdList = new ArrayList<String>();
        for (String wrd : wrdC.keySet())
            if (wrdC.get(wrd) > rareCutoff) {
                wrdID.put(wrd, wrdList.size());
                wrdList.add(wrd);
            }
        wrdID.put("_NULL", wrdList.size());
        wrdList.add("_NULL");
        wrdID.put("_RARE", wrdList.size());
        wrdList.add("_RARE");
        wrdAR = wrdList.toArray(new String[0]);
    }
    private void initPos(Paragraph p) { // including pos _ROOT
        posID = new HashMap<String, Integer>();
        List<String> posList = new ArrayList<String>();
        for (Sentence S : p.getSentences())
            for (int i = 0; i < S.len(); i++)
                if (!posID.containsKey(S.pos(i))) {
                    posID.put(S.pos(i), posList.size());
                    posList.add(S.lab(i));
                }
        posID.put("_NULL", posList.size());
        posList.add("_NULL");
        posAR = posList.toArray(new String[0]);
    }
    private void initLab(Paragraph p) { // excluding the arc (_ROOT) to ROOT.
        labID = new HashMap<String, Integer>();
        List<String> labList = new ArrayList<String>();
        for (Sentence S : p.getSentences())
            for (int i = 1; i < S.len(); i++)
                if (!labID.containsKey(S.lab(i))) {
                    labID.put(S.lab(i), labList.size());
                    labList.add(S.lab(i));
                }
        labID.put("_NULL", labList.size());
        labList.add("_NULL");
        labAR = labList.toArray(new String[0]);
    }
    
    // private methods for retrieving features
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
    private int BUFID(int i) {
        if (i > buffer.size()-1) { return -2; }
        int id = -3;
        int count = 0;
        Iterator<Integer> li = buffer.iterator();
        while (li.hasNext()) {
            id = li.next();
            if (i == count++) break;
        }
        return id;
    }
    private String STK(int i) {
        int id = STKID(i);
        if (id == -2) return "_NULL";
        else          return S.wrd(id);
    }
    private String BUF(int i) {
        int id = BUFID(i);
        if (id == -2) return "_NULL";
        else          return S.wrd(id);
    }
    private String POS_STK(int i) {
        int id = STKID(i);
        if (id == -2) return "_NULL";
        else          return S.pos(id);
    }
    private String POS_BUF(int i) {
        int id = BUFID(i);
        if (id == -2) return "_NULL";
        else          return S.pos(id);
    }
    private String LDEP_STK(int i) {
        int h = STKID(i);
        if (h == -2) return "_NULL";
        String[] arcs = T.getArcsFrom(h);
        if (arcs.length == 0) return "_NULL";
        else                  return arcs[0];
    }
    private String RDEP_STK(int i) {
        int h = STKID(i);
        if (h == -2) return "_NULL";
        String[] arcs = T.getArcsFrom(h);
        if (arcs.length == 0) return "_NULL";
        else                  return arcs[arcs.length-1];
    }
    private String LDEP_BUF(int i) {
        int h = BUFID(i);
        if (h == -2) return "_NULL";
        String[] arcs = T.getArcsFrom(h);
        if (arcs.length == 0) return "_NULL";
        else                  return arcs[0];
    }
    private String RDEP_BUF(int i) {
        int h = STKID(i);
        if (h == -2) return "_NULL";
        String[] arcs = T.getArcsFrom(h);
        if (arcs.length == 0) return "_NULL";
        else                  return arcs[arcs.length-1];
    }
    private String POS_LDEP_STK(int i) {
        int h = STKID(i);
        if (h == -2) return "_NULL";
        int[] children = T.getChildrenFrom(h);
        if (children.length == 0) return "_NULL";
        else                      return S.pos(children[0]);
    }
    private String POS_RDEP_STK(int i) {
        int h = STKID(i);
        if (h == -2) return "_NULL";
        int[] children = T.getChildrenFrom(h);
        if (children.length == 0) return "_NULL";
        else                      return S.pos(children[children.length-1]);
    }
    private String POS_LDEP_BUF(int i) {
        int h = BUFID(i);
        if (h == -2) return "_NULL";
        int[] children = T.getChildrenFrom(h);
        if (children.length == 0) return "_NULL";
        else                      return S.pos(children[0]);
    }
    private String POS_RDEP_BUF(int i) {
        int h = BUFID(i);
        if (h == -2) return "_NULL";
        int[] children = T.getChildrenFrom(h);
        if (children.length == 0) return "_NULL";
        else                      return S.pos(children[children.length-1]);
    }

}
