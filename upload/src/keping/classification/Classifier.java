package keping.classification;

import java.util.List;

/**
 * I for input, T for target output. A classifier based on SVM.
 * @author wkp
 *
 * @param <I>
 * @param <T>
 */
public interface Classifier<I extends Input, T extends Target> extends java.io.Serializable {

    /**
     * Train the model with Lists of Input and Target objects.
     * @param inputList
     * @param targetList
     */
    public void train(List<I> inputList, List<T> targetList);
    
    /**
     * Classify Input to Target.
     * @param input
     * @return
     */
    public T classify(I input);
    
    /**
     * Number of observations used to train the model.
     * @return
     */
    public int numObs();
    /**
     * Number of features in the model.
     * @return
     */
    public int numFeatures();
    
}
