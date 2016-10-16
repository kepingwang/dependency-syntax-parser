package keping.main;

import keping.ec.ECMain;
import keping.parsing.ParsingMain;

public class PipelineMain {

    public static void main(String[] arcs) throws Exception {
        
        for (double C : new double[] {20})
            for (double gamma : new double[] {0.01})
                ParsingMain.parse("new", "trn_2000.nec", "dev_200.nec", "dev_parse1.nec", "dev_200.nec", "model_parse_nec.ser", C, gamma);
        for (double C : new double[] {8})
            for (double gamma : new double[] {0.6})
                for (double subsetRatio : new double[] {0.02})
                    ECMain.parse("new", "trn_2000.ec", "dev_200.nec", "dev_parse1.ec", "dev_200.ec", "model_detect_ec.ser", subsetRatio, C, gamma);
        for (double C : new double[] {20})
            for (double gamma : new double[] {0.03})
                ParsingMain.parse("new", "trn_2000.ec", "dev_200.ec", "dev_parse2.ec", "dev_200.ec", "model_parse_ec.ser", C, gamma);
        
        ParsingMain.parse("load", "trn_400.nec", "tst.nec", "tst_parse1.nec", "", "model_parse_nec.ser", 20, 0.01);
        ECMain.parse("load", "trn_400.ec", "tst_parse1.nec", "tst_parse1.ec", "", "model_detect_ec.ser", 0.02, 8, 0.6);
        ParsingMain.parse("load", "trn_400.ec", "tst_parse1.ec", "tst_parse2.ec", "", "model_parse_ec.ser", 20, 0.03);

    }
    
}
