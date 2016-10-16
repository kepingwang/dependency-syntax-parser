package test.java.keping.common;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import keping.common.Paragraph;
import keping.common.Sentence;
import keping.ec.ECType;
import keping.ec.EC;
import keping.ec.ECTuple;

public class SentenceTest {
    private Sentence S;

    @Before
    public void setSentence() throws Exception {
        Paragraph p = new Paragraph("data\\test_sentence2.ec", true);
        S = p.getSentences().get(0);
    }
    
    @After
    public void clear() {
        S   = null;
    }
    
    @Test
    public void equalsTest() throws Exception {
        Sentence S1 = new Paragraph("data\\test_sentence2.ec", true).getSentences().get(0);
        assertTrue(S.equals(S1));
        Sentence S2 = new Paragraph("data\\test_sentence2_diff.ec", true).getSentences().get(0);
        assertFalse(S.equals(S2));
    }
    
    @Test
    public void removeECTest() throws Exception {
        Sentence S_nec = new Paragraph("data\\test_sentence2.nec", true).getSentences().get(0);
//        System.out.println(S);
//        System.out.println(S.removeEC());
//        System.out.println(S_nec);
        assertTrue(S.removeEC().equals(S_nec));
    }

    @Test
    public void addECTest() throws Exception {
        Sentence Snec = S.removeEC();
        List<EC> ecList = new ArrayList<EC>();
        ecList.add(new EC(new ECTuple(2, 1, S), new ECType(true)));
        ecList.add(new EC(new ECTuple(2, 3, S), new ECType(true)));
        ecList.add(new EC(new ECTuple(2, 3, S), new ECType(true)));
        ecList.add(new EC(new ECTuple(2, 4, S), new ECType(true)));
        ecList.add(new EC(new ECTuple(4, 8, S), new ECType(true)));
        System.out.println();
        System.out.println(Snec);
        System.out.println();
        System.out.println(Snec.addEC(ecList));
    }
}
