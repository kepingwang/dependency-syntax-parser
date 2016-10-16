package test.java.keping.ec;

import java.util.List;

import org.junit.Test;

import keping.common.Paragraph;
import keping.common.Sentence;
import keping.ec.EC;
import keping.ec.ECDecoder;
import keping.ec.ECTuple;

public class ECDecoderTest {
    
    @Test
    public void getTuplesTest() throws Exception {
        Paragraph p = new Paragraph("data\\test_sentence2_pre.ec", true);
        for (Sentence S : p.getSentences())
            for (ECTuple tuple : ECDecoder.getTuples(S))
                System.out.println(tuple);
    }
     
    @Test
    public void getTrainingDataTest() throws Exception {
        Paragraph p = new Paragraph("data\\test_sentence2_pre.ec", true);
        List<EC> data = ECDecoder.getTrainingData(p);
        for (EC trn : data) {
            System.out.print(trn.getTuple()+"  ");
            System.out.println(trn.getType());
        }
    }

}
