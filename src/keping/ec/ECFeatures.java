package keping.ec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import keping.common.Paragraph;
import keping.common.Sentence;

public class ECFeatures implements java.io.Serializable {
    private static final long serialVersionUID = 7214530836485061762L;
    private int rareCutoff = 9;
    private Map<String, Integer> wrdID; // word ID.
    private Map<String, Integer> posID; // pos tag ID.
    private Map<String, Integer> labID; // label ID.
    private String[] wrdAR; // word Array.
    private String[] posAR; // word Array.
    private String[] labAR; // label Array.
    private List<String> featNames;
    private Map<String, String> attribute;
    // The following fields to store data temporarily (for one Input).
    private Map<String, double[]> map; 
    private Sentence S;
    private int h; // head loc of EC
    private int t; // word loc after EC
    private int p; // word loc before EC
    
    public ECFeatures() {
        featNames = new ArrayList<String>();
        attribute = new HashMap<String, String>();
        // TODO: add feature names
        attribute.put("WRD(h)", "wrd");
        attribute.put("WRD(t)", "wrd");
        attribute.put("WRD(p)", "wrd");
        attribute.put("POS(h)", "pos");
        attribute.put("POS(t)", "pos");
        attribute.put("POS(p)", "pos");
        attribute.put("POS_COMBO(h,t)", "posCombo");
        attribute.put("POS_COMBO(t,p)", "posCombo");
        attribute.put("VERB_DIST(h,t)", "dist");
        attribute.put("PUNC_DIST(h,t)", "dist");
        attribute.put("LOC_DIST(h,t)", "locDist");
        attribute.put("LOC_DIST(LDEP(h),t)", "locDist");
        attribute.put("LOC_DIST(RDEP(h),t)", "locDist");
        attribute.put("LAB_LDEP(h)", "lab");
        attribute.put("LAB_RDEP(h)", "lab");
        attribute.put("POS(LDEP(h))", "pos");
        attribute.put("POS(RDEP(h))", "pos");
        // POS combination
        featNames.addAll(attribute.keySet());
    }
    
    /**
     * Initialize the Features, scan through the Paragraph
     * and fill wrdID, wrdAR, posID, ...
     * @param p
     */
    public void initialize(Paragraph p) {
        initWrd(p);
        initPos(p);
        initLab(p);
    }
    
    /**
     * Return factorized feature vector of a given tuple.
     * @param tuple
     * @return
     */
    public double[] getFeatureVector(ECTuple tuple) {
        map = new HashMap<String, double[]>();
        addFeatures(tuple);
        // Retrieve vector from map.
        int featCount = 0;
        for (int i = 0; i < featNames.size(); i++)
            featCount += map.get(featNames.get(i)).length;
        double[] vector = new double[featCount];
        int i = 0;
        for (String featName : featNames)
            for (double featVal : map.get(featName))
                vector[i++] = featVal;
        map = null; // Free memory.
        return vector;
    }
    
    // Below are all private methods for initializing and feature extracting
    // Initialize wrdID, wrdAR...
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

    // Extract features
    /**
     * Add features to the map.
     * @param tuple
     */
    private void addFeatures(ECTuple tuple) {
        S = tuple.S();
        h = tuple.h();
        t = tuple.t();
        p = t-1;
        // TODO: add features
        // 1) Horizontal features
        add("WRD(h)", "wrd", WRD(h));
        add("WRD(t)", "wrd", WRD(t));
        add("WRD(p)", "wrd", WRD(p));
        add("POS(h)", "pos", POS(h));
        add("POS(t)", "pos", POS(t));
        add("POS(p)", "pos", POS(p));
        addPosCombo("POS_COMBO(h,t)", POS(h), POS(t));
        addPosCombo("POS_COMBO(t,p)", POS(t), POS(p));
        addDist("VERB_DIST(h,t)", "V");
        addDist("PUNC_DIST(h,t)", "PU");
        addLocDist("LOC_DIST(h,t)", h, t);
        addLocDist("LOC_DIST(LDEP(h),t)", LDEP(h), t);
        addLocDist("LOC_DIST(RDEP(h),t)", RDEP(h), t);
        add("LAB_LDEP(h)", "lab", LAB_LDEP(h));
        add("LAB_RDEP(h)", "lab", LAB_RDEP(h));
        add("POS(LDEP(h))", "pos", POS(LDEP(h)));
        add("POS(RDEP(h))", "pos", POS(RDEP(h)));
    }
    
    private void add(String featName, String attr, String featVal) {
        double[] feat = new double[0];
        if      (attr.equals("wrd")) feat = wrdV(featVal);
        else if (attr.equals("pos")) feat = posV(featVal);
        else if (attr.equals("lab")) feat = labV(featVal);
        map.put(featName, feat);
    }
    private void addPosCombo(String featName, String val1, String val2) {
    	map.put(featName, posComboV(val1, val2));
    }
    private void addLocDist(String featName, int i, int j) {
    	map.put(featName, locDist(i,j));
    }
    private void addDist(String featName, String type) {
    	int start, end;
    	if (h <= t) { start = h; end = t; }
    	else 		{ start = t; end = h; }
    	int typeCount = 0;
    	if (type.equals("V")) {
    		for (int i = start+1; i < end; i++)
    			if (S.pos(i).equals("VV") || S.pos(i).equals("VC"))
    				typeCount++;
    	} else if (type.equals("PU")) {
    		for (int i = start+1; i < end; i++)
    			if (S.pos(i).equals("PU"))
    				typeCount++;
    	}
    	map.put(featName, dist(typeCount));
    }
    
    private String WRD(int i) {
        if (i < 0) return "_NULL";
        else       return S.wrd(i);
    }
    private String POS(int i) {
        if (i < 0) return "_NULL";
        else       return S.pos(i);
    }
    
    // Get boolean[] feature from categorical feature.
    private double[] wrdV(String wrd) {
        if (!wrdID.containsKey(wrd)) wrd = "_RARE";
        double[] V = new double[wrdAR.length];
        V[wrdID.get(wrd)] = 1.0;
        return V;
    }
    private double[] posV(String pos) {
        if (!posID.containsKey(pos)) pos = "_NULL";
        double[] V = new double[posAR.length];
        V[posID.get(pos)] = 1.0;
        return V;
    }
    private double[] labV(String lab) {
        if (!labID.containsKey(lab)) lab = "_NULL";
        double[] V = new double[labAR.length];
        V[labID.get(lab)] = 1.0;
        return V;
    }
    private double[] posComboV(String pos1, String pos2) {
    	if (!posID.containsKey(pos1)) pos1 = "_NULL";
    	if (!posID.containsKey(pos2)) pos2 = "_NULL";
    	double[] V = new double[posAR.length*posAR.length];
    	V[posID.get(pos1)*posAR.length + posID.get(pos2)] = 1.0;
    	return V;
    }
    private double[] locDist(int i, int j) {
        double[] V = new double[6];
        if (i < 0 || j < 0) {
            V[5] = 1;
            return V;
        }
    	if      (i - j >  3) V[0] = 1;
    	else if (i - j >  0) V[1] = 1;
    	else if (i - j == 0) V[2] = 1;
    	else if (i - j > -3) V[3] = 1;
    	else			     V[4] = 1;
    	return V;
    }
    private double[] dist(int count) {
    	double[] V = new double[2];
    	if   (count == 0) V[0] = 1;
    	else V[1] = (double) count / 5.0; // normalization
    	return V;
    }
    private int LDEP(int i) {
        int[] a = S.getTree().getChildrenFrom(i);
        if (a.length == 0) return -2;
        else               return a[0];
    }
    private int RDEP(int i) {
        int[] a = S.getTree().getChildrenFrom(i);
        if (a.length == 0) return -2;
        else               return a[a.length-1];
    }
    private String LAB_LDEP(int i) {
        String[] a = S.getTree().getArcsFrom(i);
        if (a.length == 0) return "_NULL";
        else               return a[0];
    }
    private String LAB_RDEP(int i) {
        String[] a = S.getTree().getArcsFrom(i);
        if (a.length == 0) return "_NULL";
        else               return a[a.length-1];
    }
    
}
