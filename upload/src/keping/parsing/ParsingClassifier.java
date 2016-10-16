package keping.parsing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import keping.classification.Classifier;
import keping.common.DepTree;
import keping.common.Paragraph;
import keping.common.Sentence;
import libsvm.*;

/**
 * An SVM classifier implemented using libsvm. Too slow for large data set.
 * @author wkp
 *
 */
public class ParsingClassifier implements Classifier<State, Transition> {
    private static final long serialVersionUID = -3283137692000705847L;
    private StateFeatures f;
    private svm_parameter param;
    private svm_model svm_model;
    private int numObs;
    private int numFeatures;
    private boolean trained;
    
    public ParsingClassifier() {
        f = new StateFeatures();
        param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC; // C_SVC is faster than NU_SVC
        param.kernel_type = svm_parameter.RBF;
        param.gamma = 1; // kernel param, if 0, will be set to default 1/num_features
        param.cache_size = 500; // size of the kernel cache, in MB
        param.C = 1; // the cost of constraints violation (for C_SVC), regularization param
        param.eps = 1e-3; // stopping criteria 
    }
    
    public void setSVMParam(double C, double gamma) {
        param.gamma = gamma;
        param.C = C;
    }
    
    public void train(Paragraph p) {
    	f.initialize(p);
        List<State> stateList = new ArrayList<State>();
        List<Transition> transList = new ArrayList<Transition>();
        for (Sentence S : p.getSentences()) {
            TrainingData trn = Decoder.getTrainingData(S);
            stateList.addAll(trn.getStateList());
            transList.addAll(trn.getTransList());
        }
        train(stateList, transList);
    }
    /**
     * Train the Classifier with a List of TrainingData.
     * @param trnList
     */
    public void train(List<State> stateList, List<Transition> transList) {
        /* *NOTE* Because svm_model contains pointers to svm_problem, you can
        not free the memory used by svm_problem if you are still using the
        svm_model produced by svm_train().*/
        svm_problem prob = new svm_problem();
        List<svm_node[]> SVs  = new ArrayList<svm_node[]>();
        List<Integer> targets = new ArrayList<Integer>();
        int featNum = f.getVector(stateList.get(0)).length;
        for (int i = 0; i < stateList.size(); i++) {
            SVs.add(stateToNode(stateList.get(i)));
            targets.add(transToNum(transList.get(i)));
        }
        prob.l = SVs.size();
        
        prob.y = new double[prob.l];
        prob.x = new svm_node[prob.l][];
        for (int i = 0; i < prob.l; i++) {
            prob.y[i] = targets.get(i);
            prob.x[i] = SVs.get(i);
        }
        numObs = prob.l;
        System.out.println("Number of vectors: "+numObs);
        numFeatures = featNum;
        System.out.println("Number of boolean features: "+numFeatures);
        System.out.println("Model training...");
        if (svm.svm_check_parameter(prob, param) != null) {
            System.out.println("Parameter problem!");
        }
        svm_model = svm.svm_train(prob, param);
        trained = true;
        System.out.println("Model trained.");
    };
    
    /**
     * Classify a given State to a Transition.
     * @param state
     * @return the predicted Transition.
     */
    public Transition classify(State state) {
        if (!trained) {
        	try {throw new Exception("Not trained"); } catch(Exception e) {}
        }
        double pred = svm.svm_predict(svm_model, stateToNode(state));
        Transition trans = numToTrans(pred);
        // Some hard rules of classification.
        /* The transition predicted could be limited with MORE hard-rules.
         * Or use the probability model? Who knows...
         */
        if (state.emptyStack()) {
            return new Transition("shift");
        } else if (state.STK0().equals("_ROOT")) {
            if    (state.lenBUF() != 1)  return (new Transition("shift"));
            else /*state.lenBUF() == 1*/ return (new Transition("rightArc", "ROOT"));
        } else if (state.lenBUF() == 1 && trans.type().equals("shift")) { 
            // cannot shift, just return some arbitrary leftArc Transition.
            return new Transition("leftArc", "NMOD"); // TODO: more than this?
        }
        return trans;
    }
    public DepTree predict(Sentence S) {
    	State state = new State(S);
        while (!state.isTerminal()) {
            Transition trans = classify(state);
            // Make state transition (including updating dependency arcs).
            state.transit(trans); 
        }
        DepTree T = new DepTree(state.getTree());
        T.sort();
        return T;
    }
    public Paragraph predict(Paragraph p) {
        List<Sentence> pPre = new ArrayList<Sentence>();
        for (Sentence S : p.getSentences()) {
            S.setTree(predict(S));
            pPre.add(S);
        }
        return new Paragraph(pPre);
    }
    public void writePrediction(Paragraph p, String filename) throws Exception {
        System.out.println("Writing prediction for given paragraph...");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(filename)), "UTF-8"));
        for (Sentence S : p.getSentences()) {
            S.setTree(predict(S));
            writer.write(S.toString());
            writer.newLine();
        }
        writer.close();
        System.out.println("Prediction written.");
    }
    
    public int numObs() {
        return numObs;
    }
    public int numFeatures() {
        return numFeatures;
    }
    
    // excluding the _NULL on the last of lab array
    private int transToNum(Transition trans) { 
        if (trans.type().equals("leftArc")) // 0 ... labN-2
            return f.getLabID(trans.lab());
        else if (trans.type().equals("rightArc")) // labN-1 ... 2labN-3
            return f.getLabID(trans.lab()) + f.labN()-1;
        else // shift 
            return 2*f.labN()-2;
    }
    private Transition numToTrans(double y) {
        int labN = f.labN();
        if ((y != Math.floor(y)) || Double.isInfinite(y)) {
            try { throw new Exception("input not integer!"); } 
            catch (Exception e) { e.printStackTrace(); }
        }
        if (y >= 0 && y <= labN-2)
            return new Transition("leftArc", f.getLab((int) y));
        else if (y <= 2*labN-3)
            return new Transition("rightArc", f.getLab( (int) y-(labN-1) ));
        else if (y == 2*labN-2)
            return new Transition("shift");
        else {
            try { throw new Exception("Input out of label range"); } 
            catch (Exception e) { e.printStackTrace(); }
            return null;
        }
    }
    
    private svm_node[] stateToNode(State state) {
        boolean[] vector = f.getVector(state);
        return arrayToNode(vector);
    }
    private svm_node[] arrayToNode(boolean[] a) {
        int trueCount = 0;
        for (boolean val : a) 
            if (val) trueCount++;
        svm_node[] sv = new svm_node[trueCount];
        int j = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i]) {
                sv[j] = new svm_node();
                sv[j].index = i+1;
                sv[j].value = 1;
                j++;
            }
        }
        return sv;
    }
    
}
