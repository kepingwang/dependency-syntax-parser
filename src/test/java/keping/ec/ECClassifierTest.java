package test.java.keping.ec;

import org.junit.Test;

import keping.ec.ECClassifier;
import libsvm.svm_node;

public class ECClassifierTest {

    @Test
    public void vectorToNode() {
        double[] V = new double[] { 0, 0, 2, -2, 0 };
        svm_node[] nodes = ECClassifier.vectorToNode(V);
        for (svm_node node : nodes) {
            System.out.print("index="+node.index);
            System.out.println(" value="+node.value);
        }
    }
    
    @Test
    public void testString() {
        String[] a = new String[3];
        for (String b : a)
            System.out.println(b);
    }
    
}
