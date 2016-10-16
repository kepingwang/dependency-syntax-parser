package keping.parsing;

import keping.common.Sentence;

/**
 * A (static) transition-based syntax decoder containing static methods.
 * @author wkp
 */
public final class Decoder { 
    private Decoder() {}
    
    /**
     * Return the TrainingData object of a training Sentence.
     * @param S Sentence
     * @return TrainingData
     */
    public static TrainingData getTrainingData(Sentence S) {
        TrainingData trn = new TrainingData();
        State state = new State(S);
        Transition trans;
        while (!state.isTerminal()) {
            trans = state.getGoldTrans();
            trn.addData(new State(state), trans);
            state.transit(trans);
        }
        return trn;
    }

}
