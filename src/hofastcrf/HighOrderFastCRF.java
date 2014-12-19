package hofastcrf;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.optimization.QNMinimizer;

/**
 * High-order Fast CRF class
 * @author Hiroshi Manabe
 */
public class HighOrderFastCRF<T> {

    FeatureTemplateGenerator<T> featureTemplateGenerator; // Feature template generator
    List<RawDataSequence<T>> dataSequenceList;  // List of the processed data sequence
    double[] lambda; // Feature weight vector
    
    /**
     * Construct and initialize a high-order CRF from feature generator.
     * @param featureTemplateGenerator Feature generator
     */
    public HighOrderFastCRF(FeatureTemplateGenerator<T> featureTemplateGenerator) {
        this.featureTemplateGenerator = featureTemplateGenerator;
    }
    
    /**
     * Train a high-order CRF from data.
     * @param data Training data
     */
    public List<Feature> train(RawDataSet<T> rawDataSet, int maxOrder, int maxIters, int concurrency,
            double invSigmaSquare, double epsForConvergence) {
        Map<String, Integer> labelMap = rawDataSet.generateLabelMap();
        DataSet dataSet = rawDataSet.generateDataSet(featureTemplateGenerator, labelMap, maxOrder);
        Map<Feature, Integer> featureCountMap = dataSet.generateFeatureCountMap();
        Map<FeatureTemplate, List<Feature>> featureTemplateToFeatureMap = new HashMap<FeatureTemplate, List<Feature>>();
        
        List<Feature> featureList = new ArrayList<Feature>();
        int[] featureCountArray = new int[featureCountMap.size()];
        
        int count = 0;
        for (Map.Entry<Feature, Integer> entry : featureCountMap.entrySet()) {
            Feature f = entry.getKey();
            featureList.add(f);
            featureCountArray[count] = entry.getValue();
            
            FeatureTemplate ft = f.createFeatureTemplate();
            if (!featureTemplateToFeatureMap.containsKey(ft)) {
                featureTemplateToFeatureMap.put(ft, new ArrayList<Feature>());
            }
            featureTemplateToFeatureMap.get(ft).add(f);
            
            ++count;
        }
        
        List<PatternSetSequence> patternSetSequenceList = dataSet.generatePatternSetSequenceList(featureTemplateToFeatureMap);
        
        QNMinimizer qn = new QNMinimizer();
        Function df = new Function(patternSetSequenceList, featureList, featureCountArray, concurrency, invSigmaSquare);
        lambda = new double[featureList.size()];
        lambda = qn.minimize(df, epsForConvergence, lambda, maxIters);
        return featureList;
    }

    /**
     * Run Viterbi algorithm on testing data.
     * @param data Testing data
     */
    public void runViterbi(List<RawDataSequence<T>> data) throws Exception {
    }
    
    /**
     * Write the high-order CRF to a file.
     * @param filename Name of the output file
     */
    public void write(String filename) throws Exception {
        PrintWriter out = new PrintWriter(new FileOutputStream(filename));
        out.println(lambda.length);
        for (int i = 0; i < lambda.length; i++) {
            out.println(lambda[i]);
        }
        out.close();
    }

    /**
     * Read the high-order CRF from a file.
     * @param filename Name of the input file
     */
    public void read(String filename) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        int featureNum = Integer.parseInt(in.readLine());
        lambda = new double[featureNum];
        for (int i = 0; i < featureNum; i++) {
            String line = in.readLine();
            lambda[i] = Double.parseDouble(line);
        }
        in.close();
    }
}