package keping.parsing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A TrainingData object, converted from a Sentence in the training set. 
 * It has one <code>Sentence</code>, a List of <code>State</code>,
 * and a List of corresponding <code>Transition</code>. The Sentence field within
 * the State is transient.
 * @author wkp
 */
public class TrainingData implements Serializable {
    private static final long serialVersionUID = -7214086302183505385L;
    private List<State> stateList;
    private List<Transition> transList;
    public TrainingData() {
        stateList = new ArrayList<State>();
        transList = new ArrayList<Transition>();
    }
    
    /**
     * Add a State-Transition pair to the TrainingData object.
     * @param state
     * @param trans
     */
    public void addData(State state, Transition trans) {
        stateList.add(state);
        transList.add(trans);
    }
    
    /**
     * Get State List.
     * @return
     */
    public List<State> getStateList() {
        return stateList;
    }
    
    /**
     * Get Transition List.
     * @return
     */
    public List<Transition> getTransList() {
        return transList;
    }
    
}
