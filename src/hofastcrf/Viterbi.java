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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parallel.Schedulable;

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
