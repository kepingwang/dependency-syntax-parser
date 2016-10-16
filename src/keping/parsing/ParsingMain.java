package keping.parsing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

import keping.common.Paragraph;

public class ParsingMain {
    
    /**
     * Trains a dependency syntax parser and makes prediction.
     * <p>Note: to change the features used, you should modify both the 
     * Features class and the getFeatures() method in State class.</p>
     * <p>The (approximately) best parameters I found: C=0.3 for LINEAR kernel;
     * C=8, gamma=0.03 for "RBF" kernel.</p>
     * @param option "new" to train a new model, "load" to load an existing model.
     * @param trnFilename name of the training data file.
     * @param devFilename name of the development (test) data file.
     * @param devParseName name of the parsed development (test) data file.
     * @param compareFilename name of the file to compare for accuracy.
     * @param modelName name of the serialized model.
     * @param C costs of constraints violation.
     * @param gamma parameter for (RBF) kernel. 
     * Large gamma means a point is not influenced by others.
     * @throws Exception
     */
    public static void parse( String option,
            String trnFilename, String devFilename, String devParseName, String compareFilename,
            String modelName, double C, double gamma ) throws Exception {
        
        ParsingClassifier model = new ParsingClassifier();
        double trainSec = -1; // training time, to be set later
        
        // Load serialized model.
        if (option.equals("new")) {
            
            // Load training Paragraph
            Paragraph p = new Paragraph("data\\"+trnFilename, true); // true for training (with dep)
            System.out.println(p.len()+" sentences");
            
            // SVM model parameters
            model.setSVMParam(C, gamma);
            // Train the model.
            final double sec = 1e9;
            long startTime = System.nanoTime(); // in nanoseconds
            model.train(p);
            long endTime   = System.nanoTime(); 
            trainSec = (endTime - startTime) / sec;
            /* The svm models will be trained for each classification label.*/
            
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
            model = (ParsingClassifier) in.readObject();
            in.close();
            System.out.println("Serialized model is loaded.");
            
        } else {
            System.out.println("Wrong option!");
        }
        
        // Make prediction using the model.
        Paragraph p = new Paragraph("data\\"+devFilename, false);
        System.out.println("Making prediction...");
        Paragraph pPre = model.predict(p);
        pPre.write("data\\"+devParseName);
        System.out.println("Prediction written.");
        double accuracy = -1;
        if (!compareFilename.equals("")) {
            Paragraph pComp = new Paragraph("data\\"+compareFilename, true);
            accuracy = pComp.compareTree(pPre);
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
                bw.write("Accuracy="+accuracy+"\t");
                bw.write(" C="+C+"\t");
                bw.write(" gamma="+gamma);
                bw.newLine();
                bw.flush();
            } finally { if (bw != null) bw.close(); }
        }
    }

    public static void main(String[] args) throws Exception {
        String option = "new"; 
        // "new" to train a new model. "load" to load a trained model.
        String trnFilename = "trn_1600.ec";
        String devFilename = "dev.ec";
        String devParseName= "dev_parse.ec";
        String compareFilename = "dev.ec";
        String modelName   = "parsing_trn_1600.ser";
        
        // Best parameters:
        // For RBF: C=8, gamma=0.03
        double C = 8;
        double gamma = 0.03; // Large gamma means a point is not influenced by others.
        ParsingMain.parse(option, trnFilename, devFilename, devParseName, 
                modelName, compareFilename, C, gamma);
        
    }

}
