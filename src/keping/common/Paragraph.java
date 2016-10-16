package keping.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Paragraph {
    private List<Sentence> p;
    private boolean training;
    
    public Paragraph() {}
    public Paragraph(List<Sentence> pThat) {
        this.p = pThat;
    }
    
    /**
     * Read from file with name <code>filename</code>. Add the first _root
     * node to each sentence, following head indices++.
     * @param filename
     * @param training true if it is training(dev) data
     * @throws IOException
     */
    public Paragraph(String filename, boolean training) throws IOException {
        this.training = training;
        BufferedReader reader = null;
        p = new ArrayList<Sentence>();
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(filename)), "UTF-8"));
            List<String> word, pos, lab;
            List<Integer> head;
            word = new ArrayList<String>();
            pos  = new ArrayList<String>();
            head = new ArrayList<Integer>();
            lab  = new ArrayList<String>();
            word.add("_ROOT");
            pos.add("_ROOT");
            if (training) {
                head.add(-1);
                lab.add("_ROOT");
            }
            while (reader.ready()) {
                String sLine = reader.readLine();
                if (sLine.trim().length() > 0) { // not empty line
                    String[] sArray = sLine.trim().split("\\s+");
                    word.add(sArray[0]);
                    pos.add(sArray[1]);
                    if (training) {
                        head.add(Integer.parseInt(sArray[2])+1);
                        lab.add(sArray[3]);
                    }
                } else if (word.size() != 1) { // empty line, end of a sentence.
                    if (training) p.add(new Sentence(word, pos, head, lab));
                    else          p.add(new Sentence(word, pos));
                    word = new ArrayList<String>();
                    pos  = new ArrayList<String>();
                    if (training) {
                        head = new ArrayList<Integer>();
                        lab  = new ArrayList<String>();
                    }
                    word.add("_ROOT");
                    pos.add("_ROOT");
                    if (training) {
                        head.add(-1);
                        lab.add("_ROOT");
                    }
                } else { // just empty line...
                    
                }
            }
            // Add the last sentence.
            if (word.size() != 1) { p.add(new Sentence(word, pos, head, lab)); }
        } finally {
            if (reader != null) { reader.close(); }
        } 
    }
    
    /**
     * Compare the paragraph with the predicted dependency tree and print out 
     * the accuracy.
     * @param filename
     * @return accuracy
     * @throws IOException 
     */
    public double compareTree(Paragraph that) throws Exception {
        // Compare two trees.
        int totalCount = 0;
        int sameCount  = 0;
        for (int i = 0; i < p.size(); i++) {
            DepTree T1 = p.get(i).getTree();
            DepTree T2 = that.p.get(i).getTree();
            for (int j = 1; j < T1.len(); j++) {
                totalCount++;
                if (T1.head(j) == T2.head(j) && T1.lab(j).equals(T2.lab(j)))
                    sameCount++;
            }
        }
        System.out.println("The accuracy is: "+(double) sameCount / totalCount);
        return (double) sameCount / totalCount;
    }
    
    /**
     * What percentage of EC in <code>this</code> Paragraph is present in 
     * <code>that</code> Paragraph, and head right.
     * @param that the gold standard
     * @return precision (the fraction of retrieved instances that are relevant)
     * @throws Exception
     */
    public double ecPrecision(Paragraph that) throws Exception {
        if (this.p.size() != that.p.size())
            throw new Exception("Two paragraphs are of different sizes");
        
        // Compare two trees.
        int totalCount = 0;
        int sameCount  = 0;
        for (int i = 0; i < p.size(); i++) {
            Sentence S1 = this.p.get(i);
            Sentence S2 = that.p.get(i);
            int count;
            int[] ecS1 = new int[S1.len()]; // how many ec exist before each word
            count = 0;
            for (int j = 0; j < S1.len(); j++) {
                ecS1[j] = count;
                if (S1.wrd(j).equals("*PRO*")) count++;
            }
            int[] ecS2 = new int[S2.len()];
            count = 0;
            for (int j = 0; j < S2.len(); j++) {
                ecS2[j] = count;
                if (S2.wrd(j).equals("*PRO*")) count++;
            }
            for (int j = 1; j < S1.len(); j++) {
                if (S1.wrd(j).equals("*PRO*")) {
                    totalCount++;
                    for (int k = 1; k < S2.len(); k++) {
                        if (k-ecS2[k] == j-ecS1[j]) {
                            if (S2.wrd(k).equals("*PRO*")) {
//                                int S1HeadLoc = S1.head(j) != 0 ? S1.head(j)-ecS1[S1.head(j)] : -1;
//                                int S2HeadLoc = S2.head(k) != 0 ? S2.head(k)-ecS2[S2.head(k)] : -1;
//                                if (S1HeadLoc == S2HeadLoc) sameCount++;
                                sameCount++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return (double) sameCount / totalCount;
    }
    
    /**
     * What percentage of EC in <code>that</code> Paragraph is present in 
     * <code>this</code> Paragraph, and head right.
     * @param that the gold standard
     * @return recall (the fraction of relevant instances that are retrieved)
     * @throws Exception
     */
    public double ecRecall(Paragraph that) throws Exception {
        if (this.p.size() != that.p.size())
            throw new Exception("Two paragraphs are of different sizes");
        
        // Compare two trees.
        int totalCount = 0;
        int sameCount  = 0;
        for (int i = 0; i < p.size(); i++) {
            Sentence S2 = this.p.get(i); // Precision to Recall: switch S2 and S1.
            Sentence S1 = that.p.get(i);
            int count;
            int[] ecS1 = new int[S1.len()]; // how many ec exist before each word
            count = 0;
            for (int j = 0; j < S1.len(); j++) {
                ecS1[j] = count;
                if (S1.wrd(j).equals("*PRO*")) count++;
            }
            int[] ecS2 = new int[S2.len()];
            count = 0;
            for (int j = 0; j < S2.len(); j++) {
                ecS2[j] = count;
                if (S2.wrd(j).equals("*PRO*")) count++;
            }
            for (int j = 1; j < S1.len(); j++) {
                if (S1.wrd(j).equals("*PRO*")) {
                    totalCount++;
                    for (int k = 1; k < S2.len(); k++) {
                        if (k-ecS2[k] == j-ecS1[j]) {
                            if (S2.wrd(k).equals("*PRO*")) {
//                                int S1HeadLoc = S1.head(j) != 0 ? S1.head(j)-ecS1[S1.head(j)] : -1;
//                                int S2HeadLoc = S2.head(k) != 0 ? S2.head(k)-ecS2[S2.head(k)] : -1;
//                                if (S1HeadLoc == S2HeadLoc) sameCount++;
                                sameCount++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return (double) sameCount / totalCount;
    }
    
    /**
     * Return the list of the sentences.
     * @return
     */
    public List<Sentence> getSentences() {
        return p;
    }
    
    /**
     * Returns a Paragraph with EC removed.
     * @return
     */
    public Paragraph removeEC() {
        Paragraph pNEC = new Paragraph();
        pNEC.p = new ArrayList<Sentence>();
        for (Sentence S : p) {
            pNEC.p.add(S.removeEC());
        }
        return pNEC;
    }
    
    /**
     * Return a new Paragraph with _END added to the end of each Sentence.
     * @return
     */
    public Paragraph addEND() {
    	List<Sentence> pEND = new ArrayList<Sentence>();
    	for (Sentence S : p)
    		pEND.add(S.addEND());
    	return new Paragraph(pEND);
    }
    
    /**
     * Return number of sentences in the paragraph.
     * @return
     */
    public int len() { return p.size(); }
    
    public boolean isTraining() {
        return training;
    }

    /**
     * Print sentences on screen.
     */
    public void printHead(int num) {
        int count = 1;
        for (Sentence S : p) {
            System.out.println(S);
            System.out.println(count++);
            System.out.println();
            if (count == num) break;
        }
    }
    
    /**
     * Write sentences to file.
     * @param filename
     * @throws FileNotFoundException 
     * @throws UnsupportedEncodingException 
     */
    public void write(String filename) throws Exception {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(filename)), "UTF-8"));
        for (Sentence S : p) {
            writer.write(S.toStringNo_ROOT());
            writer.newLine();
        }
        writer.close();
    }
    
    /**
     * Write to file <code>n</code> random sentences.
     * @param filename
     * @param n
     * @throws Exception
     */
    public void writeRand(String filename, int n) throws Exception {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(filename)), "UTF-8"));
        List<Sentence> newP = new ArrayList<Sentence>();
        newP.addAll(p);
        Collections.shuffle(newP);
        if (n > newP.size()) {
            try { throw new Exception("Not that many sentences over there."); }
            finally {writer.close();}
        }
        for (int i = 0; i < n; i++) {
            writer.write(newP.get(i).toStringNo_ROOT());
            writer.newLine();
        }
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        // This snippet can be used to randomly subset a paragraph.
        Paragraph p = new Paragraph("data\\trn.ec", true);
        System.out.println("trn.ec "+p.len()+" sentences");
//        p.writeRand("data\\trn_100.ec", 100);
//        p.writeRand("data\\trn_200.ec", 200);
//        p.writeRand("data\\trn_400.ec", 400);
//        p.writeRand("data\\trn_800.ec", 800);
//        p.writeRand("data\\trn_1600.ec", 1600);
//        p.writeRand("data\\trn_2500.ec", 2500);
//        p.writeRand("data\\trn_2000.ec", 2000);
//        p.writeRand("data\\trn_3200.ec", 3200);
        
        // Remove EC
//        Paragraph pEC = new Paragraph("data\\trn_2000.ec", true);
//        pEC.removeEC().write("data\\trn_2000.nec");
        
        
//        Paragraph pDev = new Paragraph("data\\dev.ec", true);
//        System.out.println("dev.ec "+pDev.len()+" sentences");
//        pDev.writeRand("data\\dev_200.ec", 200);
//        Paragraph pDev200 = new Paragraph("data\\dev_200.ec", true);
//        pDev200.removeEC().write("data\\dev_200.nec");
    }
    
}
