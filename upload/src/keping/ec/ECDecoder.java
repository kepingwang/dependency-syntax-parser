package keping.ec;

import java.util.ArrayList;
import java.util.List;

import keping.common.Paragraph;
import keping.common.Sentence;

public final class ECDecoder {
    private static double subsetRatio = 0.01;
    private ECDecoder() {}
    
    public static List<ECTuple> getTuples(Sentence S) {
        List<ECTuple> tuples = new ArrayList<ECTuple>();
        for (int h = 1; h < S.len(); h++)
            for (int t = 1; t < S.len(); t++)
                tuples.add(new ECTuple(h, t, S));
        return tuples;
    }
    
    public static List<EC> getTrainingData(Paragraph p) {
        List<EC> data = new ArrayList<EC>();
        for (Sentence S : p.getSentences())
            data.addAll(subset(getTrainingData(S)));
        return data;
    }
    
    public static List<EC> getTrainingData(Sentence S) {
        Sentence Snec = S.removeEC();
        int[][] types = new int[Snec.len()][Snec.len()];
        
        int[] ecBeforeS = new int[S.len()]; // Number of all ECs before word i
        int ecCount = 0;
        for (int i = 1; i < S.len(); i++) {
            ecBeforeS[i] = ecCount;
            if (S.wrd(i).equals("*PRO*"))  {
                ecCount++;
            }
        }
        
        // There should be a mapping from i in Snec to S,
        // that is, we need ecBefore[Snec.len()].
        int[] ecBefore  = new int[Snec.len()];
        int k = 1;
        for (int i = 1; i < S.len(); i++)
            if (!S.wrd(i).equals("*PRO*"))
                ecBefore[k++] = ecBeforeS[i];
        
        // hList, tList
        List<Integer> hList = new ArrayList<Integer>();
        List<Integer> tList = new ArrayList<Integer>();
        for (int i = 1; i < S.len(); i++)
            if (S.wrd(i).equals("*PRO*")) {
                hList.add(S.head(i));
                int m = i;
                while (S.wrd(++m).equals("*PRO*")) {}
                tList.add(m);
                
            }
                
        
        // Fill types[][]
        for (int h = 1; h < Snec.len(); h++) {
            int hS = h + ecBefore[h]; 
            for (int t = 1; t < Snec.len(); t++) {
                int tS = t + ecBefore[t];
                // Slow code but doesn't matter a lot
                if (S.wrd(tS-1).equals("*PRO*"))
                    for (int m = 0; m < tList.size(); m++)
                        if (tList.get(m) == tS && hList.get(m) == hS)
                            types[h][t] = 1;
            }
        }
        
        List<EC> data = new ArrayList<EC>();
        for (int h = 1; h < Snec.len(); h++)
            for (int t = 1; t < Snec.len(); t++) {
                if (types[h][t] == 1) data.add(new EC(new ECTuple(h, t, Snec), new ECType(true)));
                else                  data.add(new EC(new ECTuple(h, t, Snec), new ECType(false)));
            }
            
        return data;
    }
    
    
    /**
     * Get the a subset of the EC List where all true EC
     * and a random subset of false EC are preserved.
     * @param ecList
     * @return
     */
    private static List<EC> subset(List<EC> ecList) {
        List<EC> subList = new ArrayList<EC>();
        for (EC ec : ecList) {
            if (ec.getType().isTrue()) subList.add(ec);
            else if (Math.random() < subsetRatio) subList.add(ec);
        }
        return subList;
    }
    
    public static void setSubsetRatio(double ratio) {
        subsetRatio = ratio;
    }
    
}
