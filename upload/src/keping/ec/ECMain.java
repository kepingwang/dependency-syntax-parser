package keping.ec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

import keping.common.Paragraph;

public class ECMain {
    
    /**
     * Trains a Empty Category Detector and makes prediction.
     * <p>Note: to change the features used, you should modify both the 
     * Features class and the getFeatures() method in State class.</p>
     * @param option "new" to train a new model, "load" to load an existing model.
     * @param trnFilename name of the training data file.
     * @param devFilename name of the development (test) data file.
     * @param parseFilename name of the parsed development (test) data file.
     * @param compareFilename name of the file to compare for precision and recall.
     * @param modelName name of the serialized model.
     * @param C costs of constraints violation.
     * @param gamma parameter for (RBF) kernel. 
     * Large gamma means a point is not influenced by others.
     * @throws Exception
     */
    public static void parse( String option,
            String trnFilename, String devFilename, String parseFilename, String compareFilename,
            String modelName, double subsetRatio, double C, double gamma) throws Exception {
        
        ECClassifier model = null;
        double trainSec = -1; // training time, to be set later
        
        // Load serialized model.
        if (option.equals("new")) {
            model = new ECClassifier();
            model.setSVMParam(C, gamma); // SVM model parameters
            ECDecoder.setSubsetRatio(subsetRatio);
            // Train the model.
            final double sec = 1e9;
            long startTime = System.nanoTime(); // in nanoseconds
            model.train(new Paragraph("data\\"+trnFilename, true));
            long endTime   = System.nanoTime(); 
            trainSec = (endTime - startTime) / sec;
            // Serialized the trained model and save to disk.
            System.out.println("Saving model...");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
                    "data\\"+modelName));
            out.writeObject(model);
            out.close();
            System.out.println("Serialized model is saved.");
        } else if (option.equals("load")){
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(
                    "data\\"+modelName));
            model = (ECClassifier) in.readObject();
            in.close();
            System.out.println("Serialized model is loaded.");
        } else { System.out.println("Wrong option!"); }
        
        // Make prediction using the model.
        Paragraph p = new Paragraph("data\\"+devFilename, true);
        Paragraph pNEC;
        if (devFilename.substring(devFilename.length()-3).equals(".ec")) {
            pNEC = p.removeEC();
        } else {
            pNEC = p;
        }
        System.out.println("Making prediction...");
        Paragraph pECpre = model.predict(pNEC);
        pECpre.write("data\\"+parseFilename);
        System.out.println("Prediction written.");
        double precision = -1;
        double recall = -1;
        if (!compareFilename.equals("")) {
            Paragraph pComp = new Paragraph("data\\"+compareFilename, false);
            precision = pECpre.ecPrecision(pComp);
            recall    = pECpre.ecRecall(pComp);
            System.out.println("EC prediction precision is: "+precision);
            System.out.println("EC prediction recall is: "+recall);
        }
        
        // Write training log if model is newly trained.
        BufferedWriter bw = null;
        if (option.equals("new")) {
            try {
                bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(new File("data\\"+modelName+".log"), true), "UTF-8"));
                bw.write("Training="+trnFilename+"\t");
                bw.write(" Prediction="+devFilename+"\t");
                bw.write(" Comparison="+compareFilename+"\t");
                bw.write(" "+model.numObs()+" obs, "+model.numFeatures()+" features"+"\t");
                bw.write(" Trained in "+(int) trainSec+"s");
                bw.newLine();
                bw.write("Precision="+precision+"\t");
                bw.write(" Recall="+recall+"\t");
                bw.write(" C="+C+"\t");
                bw.write(" gamma="+gamma);
                bw.newLine();
                bw.flush();
            } finally { if (bw != null) bw.close(); }
        }
    }
    
    public static void main(String[] args) throws Exception {
        String option = "new";
        String trnFilename = "trn_400.ec";
        String devFilename = "dev.ec";
        String parseFilename = "devEC.ec";
        String compareFilename = "dev.ec";
        String modelName = "EC_trn_400.ser";
        double C = 1;
        double gamma = 1;
        
        ECMain.parse(option, trnFilename, devFilename, parseFilename, compareFilename, modelName, 0.01, C, gamma);
        
    }

}
