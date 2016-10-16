package test.java.keping.parsing;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.*;

import keping.common.DepTree;
import keping.common.Paragraph;
import keping.common.Sentence;
import keping.parsing.Decoder;
import keping.parsing.State;
import keping.parsing.TrainingData;
import keping.parsing.Transition;

public class DecoderTest {
    private Sentence S;
    private TrainingData trn;

    @Before
    public void setSentence() throws Exception {
        Paragraph p = new Paragraph("data\\test_sentence.ec", true);
        S = p.getSentences().get(0);
        trn = Decoder.getTrainingData(S);
    }
    
    @After
    public void clear() {
        S   = null;
        trn = null;
    }
    
    @Test
    public void showSentence() {
        System.out.println(S);
        assertTrue(true);
    }
    
    @Test
    public void trainingDataDecodedTransition() {
        String[] transTypes = new String[] {
                "shift", "leftArc", "shift", "shift", "rightArc",
                "shift", "leftArc", "leftArc", "rightArc", "shift"
        };
        List<Transition> trans = trn.getTransList();
        assertTrue("Trans not right size.", trans.size() == transTypes.length);
        for (int i = 0; i < transTypes.length; i++)
            assertTrue("TransTypes not right.", transTypes[i].equals(trans.get(i).type()));
    }
    
    @Test
    public void trainingDataDecodedStateSTK_BUF() {
        List<State> states = trn.getStateList();
        testState(states.get(0), new int[] {0}, new int[] {1,2,3,4,5});
        testState(states.get(1), new int[] {0,1}, new int[] {2,3,4,5});
        testState(states.get(2), new int[] {0}, new int[] {2,3,4,5});
        testState(states.get(3), new int[] {0,2}, new int[] {3,4,5});
        testState(states.get(4), new int[] {0,2,3}, new int[] {4,5});
        testState(states.get(5), new int[] {0,2}, new int[] {3,5});
        testState(states.get(6), new int[] {0,2,3}, new int[] {5});
        testState(states.get(7), new int[] {0,2}, new int[] {5});
        testState(states.get(8), new int[] {0}, new int[] {5});
        testState(states.get(9), new int[] {}, new int[] {0});
    }
    private void testState(State state, int[] STKGold, int[] BUFGold) {
        Integer[] STK = state.getSTK().toArray(new Integer[0]);
        assertTrue("Wrong STK deque size", STK.length == STKGold.length);
        assertTrue("STK not gold", arrayEquals(STK, STKGold));
        Integer[] BUF = state.getBUF().toArray(new Integer[0]);
        assertTrue("Wrong BUF deque size", BUF.length == BUFGold.length);
        assertTrue("BUF not gold", arrayEquals(BUF, BUFGold));
    }
    private boolean arrayEquals(Integer[] a, int[] b) {
        for (int i = 0; i < a.length; i++)
            if (a[i] != b[i]) return false;
        return true;
    }

    @Test
    public void trainingDataDecodedStateDepTree() {
        List<State> states = trn.getStateList();
        assertTrue("Wrong DepTree in states.get(0)", DepTreeListEquals(states.get(0).getTree(),
                new int[] {}, new String[] {}, new int[] {}));
        assertTrue("Wrong DepTree in states.get(3)", DepTreeListEquals(states.get(3).getTree(),
                new int[] {2}, new String[] {"NMOD"}, new int[] {1}));
        assertTrue("Wrong DepTree in states.get(4)", DepTreeListEquals(states.get(4).getTree(),
                new int[] {2}, new String[] {"NMOD"}, new int[] {1}));
        assertTrue("Wrong DepTree in states.get(5)", DepTreeListEquals(states.get(5).getTree(),
                new int[] {2,3}, new String[] {"NMOD","OBJ"}, new int[] {1,4}));
        assertTrue("Wrong DepTree in states.get(6)", DepTreeListEquals(states.get(6).getTree(),
                new int[] {2,3}, new String[] {"NMOD","OBJ"}, new int[] {1,4}));
        assertTrue("Wrong DepTree in states.get(9)", DepTreeListEquals(states.get(9).getTree(),
                new int[] {2,3,5,5,0}, new String[] {"NMOD","OBJ","LOC","SBJ","ROOT"}, new int[] {1,4,3,2,5}));
    }
    private boolean DepTreeListEquals(DepTree T, int[] heads, String[] labs, int[] deps) {
        int N = T.listSize();
        if (N != heads.length || N != labs.length || N != deps.length)
            return false;
        for (int i = 0; i < N; i++) {
            if (T.getHead(i) != heads[i] || !T.getLab(i).equals(labs[i]) ||
                    T.getDep(i) != deps[i])
                return false;
        }
        return true;
    }
}
