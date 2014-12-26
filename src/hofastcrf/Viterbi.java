package hofastcrf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Parallel.Schedulable;

public class Viterbi<T> implements Schedulable {

    int curID; // Current task ID (for parallelization)
    List<RawDataSequence<T>> rawDataSequenceList; // List of testing sequences
    FeatureTemplateGenerator<T> generator;
    final List<Feature> featureList;
    final Map<FeatureTemplate, List<Feature>> featureTemplateToFeatureMap;
    final Map<String, Integer> labelMap;
    final Map<Integer, String> reversedLabelMap;
    final int maxOrder;
    String[][] labelArrayArray;

    public Viterbi(List<RawDataSequence<T>> rawDataSequenceList, FeatureTemplateGenerator<T> generator, 
            List<Feature> featureList, Map<String, Integer> labelMap) {
        curID = -1;
        this.rawDataSequenceList = rawDataSequenceList;
        this.generator = generator;
        this.featureList = featureList;
        this.labelMap = labelMap;
        this.labelArrayArray = new String[rawDataSequenceList.size()][];
        
        featureTemplateToFeatureMap = new HashMap<FeatureTemplate, List<Feature>>();
        int maxOrder = 0;
        for (Feature f : featureList) {
            if (f.pat.getOrder() > maxOrder) {
                maxOrder = f.pat.getOrder();
            }
            FeatureTemplate ft = f.createFeatureTemplate();
            if (!featureTemplateToFeatureMap.containsKey(ft)) {
                featureTemplateToFeatureMap.put(ft, new ArrayList<Feature>());
            }
            featureTemplateToFeatureMap.get(ft).add(f);
        }
        this.maxOrder = maxOrder;
        
        reversedLabelMap = new HashMap<Integer, String>();
        for (Map.Entry<String, Integer> entry : labelMap.entrySet()) {
            reversedLabelMap.put(entry.getValue(), entry.getKey());
        }
    }
    
    public String[][] getPredictedLabels() {
        return labelArrayArray;
    }
    
    public Object compute(int taskID) {
        RawDataSequence<T> rawDataSequence = rawDataSequenceList.get(taskID);
        DataSequence dataSequence = rawDataSequence.convertToDataSequence(generator, labelMap, maxOrder);
        PatternSetSequence patternSetSequence = dataSequence.generatePatternSetSequence(featureTemplateToFeatureMap);
        int[] labels = patternSetSequence.executeViterbi();
        String[] strLabels = new String[labels.length];
        for (int i = 0; i < labels.length; ++i) {
            strLabels[i] = reversedLabelMap.get(labels[i]);
        }
        labelArrayArray[taskID] = strLabels;
        return null;
    }
    
    public int getNumTasks() {
        return rawDataSequenceList.size();
    }

    public synchronized int fetchCurrTaskID() {
        if (curID < getNumTasks()) {
            curID++;
        }
        return curID;
    }

    public void update(Object partialResult) {
        // Do nothing
    }
}
