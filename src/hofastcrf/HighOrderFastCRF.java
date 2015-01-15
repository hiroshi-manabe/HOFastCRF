/*
Copyright (C) 2014 Hiroshi Manabe
Copyright (C) 2012 Nguyen Viet Cuong, Ye Nan, Sumit Bhagwani

This file is part of HOFastCRF, which is based on HOSemiCRF.

HOFastCRF is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

HOFastCRF is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with HOFastCRF. If not, see <http://www.gnu.org/licenses/>.
*/

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

import parallel.Scheduler;
import edu.stanford.nlp.optimization.QNMinimizer;

/**
 * The main class for the high-order Fast CRF.
 * @author Hiroshi Manabe
 */
public class HighOrderFastCRF<T> {

    private HighOrderCRFData modelData;
    
    /**
     * Executes the training.
     * @param rawDataSequenceList the list of the raw observations
     * @param featureTemplateGenerator the feature template generator that generate feature templates from the raw observations
     * @param maxLabelLength the maximum length of the feature labels
     * @param maxIters the maximum iteration counts
     * @param concurrency
     * @param useL1Regularization true if use the L1 regularization, false if use the L2 regularization
     * @param regularizationCoefficient regularization coefficient (either for L1 or L2)
     * @param epsilonForConvergence
     */
    public void train(List<RawDataSequence<T>> rawDataSequenceList, FeatureTemplateGenerator<T> featureTemplateGenerator,
            int maxLabelLength, int maxIters, int concurrency,
            boolean useL1Regularization, double regularizationCoefficient, double epsilonForConvergence) {
        RawDataSet<T> rawDataSet = new RawDataSet<T>(rawDataSequenceList);
        Map<String, Integer> labelMap = rawDataSet.generateLabelMap();
        DataSet dataSet = rawDataSet.generateDataSet(featureTemplateGenerator, labelMap, maxLabelLength);
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
        if (useL1Regularization) {
            qn.useOWLQN(true, regularizationCoefficient);
            regularizationCoefficient = 0.0;
        }
        
        Function df = new Function(patternSetSequenceList, featureList, featureCountArray, concurrency, regularizationCoefficient);
        double[] lambda = new double[featureList.size()];
        lambda = qn.minimize(df, epsilonForConvergence, lambda, maxIters);
        
        List<Feature> featureListToSave = new ArrayList<Feature>();
        for (int i = 0; i < lambda.length; ++i) {
            Feature feature = featureList.get(i);
            if (lambda[i] != 0.0 || (feature.obs.isEmpty() && feature.pat.getLength() == 1)) {
                feature.reset(lambda[i]);
                featureListToSave.add(feature);
            } else {
                feature.reset(lambda[i]);
            }
        }
        modelData = new HighOrderCRFData(featureListToSave, labelMap);
    }

    public String[][] decode(List<RawDataSequence<T>> rawData, FeatureTemplateGenerator<T> featureTemplateGenerator,
            int concurrency) throws InterruptedException {
        Decoder<T> viterbi = new Decoder<T>(rawData, featureTemplateGenerator, modelData.getFeatureList(),
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
