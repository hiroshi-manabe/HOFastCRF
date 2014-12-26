package hofastcrf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Parallel.Scheduler;
import edu.stanford.nlp.optimization.QNMinimizer;

/**
 * High-order Fast CRF class
 * @author Hiroshi Manabe
 */
public class HighOrderFastCRF<T> {

    private HighOrderCRFData modelData;
    
    public void train(RawDataSet<T> rawDataSet, FeatureTemplateGenerator<T> featureTemplateGenerator,
            int maxOrder, int maxIters, int concurrency,
            double inverseSigmaSquared, double epsilonForConvergence) {
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
        Function df = new Function(patternSetSequenceList, featureList, featureCountArray, concurrency, inverseSigmaSquared);
        double[] lambda = new double[featureList.size()];
        lambda = qn.minimize(df, epsilonForConvergence, lambda, maxIters);
        for (int i = 0; i < lambda.length; ++i) {
            featureList.get(i).reset(lambda[i]);
        }
        modelData = new HighOrderCRFData(featureList, labelMap);
    }

    public String[][] decode(List<RawDataSequence<T>> rawData, FeatureTemplateGenerator<T> featureTemplateGenerator) throws InterruptedException {
        Viterbi<T> viterbi = new Viterbi<T>(rawData, featureTemplateGenerator, modelData.getFeatureList(),
                modelData.getLabelMap());
        Scheduler sch = new Scheduler(viterbi, 1, Scheduler.DYNAMIC_NEXT_AVAILABLE);
        sch.run();
        return viterbi.getPredictedLabels();
    }
    
    public String[][] extractLabels(List<RawDataSequence<T>> rawDataSequenceList) {
        String[][] ret = new String[rawDataSequenceList.size()][];
        for (int i = 0; i < rawDataSequenceList.size(); ++i) {
            List<String> labelList = rawDataSequenceList.get(i).getRawLabelList();
            ret[i] = labelList.toArray(new String[labelList.size()]);
        }
        return ret;
    }

    public void write(String filename) throws IOException {
        ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
        output.writeObject(modelData);
        output.close();
    }
    
    public void read(String filename) throws IOException, ClassNotFoundException {
        ObjectInput input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)));
        modelData = (HighOrderCRFData)input.readObject();
        input.close();
    }
}
