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

/**
 * A class that infers the labels from the observations.
 * @author Hiroshi Manabe
 *
 * @param <T> The class for the observations.
 */
public class Decoder<T> implements Schedulable {

    int curID; // Current task ID (for parallelization)
    List<RawDataSequence<T>> rawDataSequenceList; // List of testing sequences
    FeatureTemplateGenerator<T> generator;
    final List<Feature> featureList;
    final Map<FeatureTemplate, List<Feature>> featureTemplateToFeatureMap;
    final Map<String, Integer> labelMap;
    final Map<Integer, String> reversedLabelMap;
    final int maxOrder;
    String[][] labelArrayArray;

    /**
     * Constructor.
     * @param rawDataSequenceList
     * @param generator
     * @param featureList the features appeared in the training set
     * @param labelMap
     */
    public Decoder(List<RawDataSequence<T>> rawDataSequenceList, FeatureTemplateGenerator<T> generator, 
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
            if (f.pat.getLength() > maxOrder) {
                maxOrder = f.pat.getLength();
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
    
    /**
     * Returns the stored predicted labels.
     * @return
     */
    public String[][] getPredictedLabels() {
        return labelArrayArray;
    }
    
    /**
     * Infers the labels and stores it.
     */
    public Object compute(int taskID) {
        RawDataSequence<T> rawDataSequence = rawDataSequenceList.get(taskID);
        DataSequence dataSequence = rawDataSequence.generateDataSequence(generator, labelMap, maxOrder);
        PatternSetSequence patternSetSequence = dataSequence.generatePatternSetSequence(featureTemplateToFeatureMap);
        
        int[] labels = patternSetSequence.decode();
        String[] strLabels = new String[labels.length];
        for (int i = 0; i < labels.length; ++i) {
            strLabels[i] = reversedLabelMap.get(labels[i]);
        }
        labelArrayArray[taskID] = strLabels;
        return null;
    }
    
    @Override
    public int getNumTasks() {
        return rawDataSequenceList.size();
    }
    
    @Override
    public synchronized int fetchCurrTaskID() {
        if (curID < getNumTasks()) {
            curID++;
        }
        return curID;
    }
    
    @Override
    public void update(Object partialResult) {
        // Does nothing
    }
}
