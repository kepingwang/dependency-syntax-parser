package keping.ec;

import java.util.ArrayList;
import java.util.List;

import keping.classification.Classifier;
import keping.common.Paragraph;
import keping.common.Sentence;
import libsvm.*;

public class ECClassifier implements Classifier<ECTuple, ECType> {
    private static final long serialVersionUID = -609689173653328329L;
    private ECFeatures f;
    private svm_parameter param;
    private svm_model svm_model;
    private int numObs;
    private int numFeatures;
    private boolean trained;
    private transient static int truePredictCount = 0;
    
    public ECClassifier() {
        f = new ECFeatures();
        param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC; // C_SVC is faster than NU_SVC
        param.kernel_type = svm_parameter.RBF;
        param.gamma = 0.1; // kernel param, if 0, will be set to default 1/num_features
        param.cache_size = 500; // size of the kernel cache, in MB
        param.C = 1; // the cost of constraints violation (for C_SVC), regularization param
        param.eps = 1e-3; // stopping criteria 
    }
    
    /**
     * Set parameters for SVM.
     * @param C cost of constraints violation
     * @param gamma parameter for (RBF) kernel. 
     * Large gamma means a point is not influenced by others.
     */
    public void setSVMParam(double C, double gamma) {
        param.C = C;
        param.gamma = gamma;
    }

    /**
     * Train the model with a Paragraph.
     * @param p
     */
    public void train(Paragraph p) {
    	p = p.addEND();
        System.out.println("Number of sentences for training: "+p.len());
        f.initialize(p);
        List<EC> data = ECDecoder.getTrainingData(p);
        List<ECTuple> tupleList = new ArrayList<ECTuple>();
        List<ECType>  typeList = new ArrayList<ECType>();
        for (int i = 0; i < data.size(); i++) {
            tupleList.add(data.get(i).getTuple());
            typeList.add(data.get(i).getType());
        }
        train(tupleList, typeList);
    }
    public void train(List<ECTuple> tupleList, List<ECType> typeList) {
        svm_problem prob = new svm_problem();
        List<svm_node[]> SVs  = new ArrayList<svm_node[]>();
        List<Integer> targets = new ArrayList<Integer>();
        numObs = tupleList.size();
        numFeatures = f.getFeatureVector(tupleList.get(0)).length;
        for (int i = 0; i < numObs; i++) {
            svm_node[] SV = vectorToNode(f.getFeatureVector(tupleList.get(i)));
            Integer target = typeList.get(i).isTrue() ? 1 : 0;
            SVs.add(SV);
            targets.add(target);
        }
        int numTrue = 0;
        for (int val : targets)
            if (val == 1) numTrue++;
        System.out.println(numTrue+" empty categories in total.");
        
        prob.l = numObs;
        prob.x = new svm_node[prob.l][];
        prob.y = new double[prob.l];
        for (int i = 0; i < prob.l; i++) {
            prob.x[i] = SVs.get(i);
            prob.y[i] = (double) targets.get(i);
        }
        System.out.println("Number of vectors: "+numObs);
        System.out.println("Number of boolean features: "+numFeatures);
        if (svm.svm_check_parameter(prob, param) != null) {
            System.out.println("Parameter problem!");
        }
        svm_model = svm.svm_train(prob, param);
        trained = true;
    }

    public ECType classify(ECTuple input) {
        if (!trained) {
            try { throw new Exception("Model not trained!"); } catch (Exception e) {}
            return new ECType(false);
        }
        double[] V = f.getFeatureVector(input);
        double pred = svm.svm_predict(svm_model, vectorToNode(V));
        if (pred != 0) {
            System.out.print(".");
            truePredictCount++;
            if (truePredictCount % 50 == 0) System.out.println();
        }
        ECType type = new ECType( (int) pred == 1 );
        return type;
    }
    public Sentence predict(Sentence S) {
        S = S.addEND();
        List<EC> ecList = new ArrayList<EC>();
        for (ECTuple tuple : ECDecoder.getTuples(S)) {
            ECType type = classify(tuple);
            if (type.isTrue()) ecList.add(new EC(tuple, type));
        }
        return S.addEC(ecList).removeEND();
    }
    public Paragraph predict(Paragraph p) {
        truePredictCount = 0;
        List<Sentence> pPre = new ArrayList<Sentence>();
        for (Sentence S : p.getSentences())
            pPre.add(predict(S));
        Paragraph pEC = new Paragraph(pPre);
        return pEC;
    }
    
    public int numObs() {
        return numObs;
    }
    public int numFeatures() {
        return numFeatures;
    }
    
    public static svm_node[] vectorToNode(double[] V) {
        List<svm_node> nodeList = new ArrayList<svm_node>();
        for (int i = 0; i < V.length; i++) 
            if (V[i] != 0) {
                svm_node node = new svm_node();
                node.index = i+1;
                node.value = V[i];
                nodeList.add(node);
            }
        return nodeList.toArray(new svm_node[0]);
    }
    
}
