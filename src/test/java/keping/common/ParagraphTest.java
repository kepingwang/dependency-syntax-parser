package test.java.keping.common;

import static org.junit.Assert.*;

import org.junit.Test;

import keping.common.Paragraph;

public class ParagraphTest {
    
    @Test
    public void ecPrecisionRecallTest() throws Exception {
        Paragraph p = new Paragraph("data\\test_sentence2.ec", true);
        
        Paragraph pPre = new Paragraph("data\\test_sentence2_pre.ec", true);
        double precision = pPre.ecPrecision(p);
        assertTrue(precision > 0.66 && precision < 0.67);
        double recall = pPre.ecRecall(p);
        assertTrue(recall > 0.99 && recall < 1.01);
        
        Paragraph pPre1 = new Paragraph("data\\test_sentence2_pre1.ec", true);
        double precision1 = pPre1.ecPrecision(p);
        assertTrue(precision1 > 0.33 && precision1 < 0.34);
        double recall1 = pPre1.ecRecall(p);
        assertTrue(recall1 > 0.49 && recall1 < 0.51);
        
        Paragraph pPre2 = new Paragraph("data\\test_sentence2_pre2.ec", true);
        System.out.println(pPre2.ecPrecision(p));
        System.out.println(pPre2.ecRecall(p));
        
        
        
    }

}
