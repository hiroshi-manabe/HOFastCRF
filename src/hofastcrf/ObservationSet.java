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

/**
 * A class that represents a observation set (list of observation sequences).
 * @author Hiroshi Manabe
 * @param <T> The class of the observations
 */
public class ObservationSet<T> {
    
    List<ObservationSequence<T>> observationSequenceList;
    
    /**
     * Constructor.
     * @param observationSequenceList
     */
    public ObservationSet(List<ObservationSequence<T>> observationSequenceList) {
        this.observationSequenceList = observationSequenceList;
    }
    
    /**
     * Generates the map that associates the string keys with the integer representations.  
     * @return
     */
    public Map<String, Integer> generateLabelMap() {
        Map<String, Integer> labelMap = new HashMap<String, Integer>();
        for (ObservationSequence<T> seq : observationSequenceList) {
            List<String> labelList = seq.getLabelList();
            for (String label : labelList) {
                if (!labelMap.containsKey(label)) {
                    labelMap.put(label, labelMap.size());
                }
            }
        }
        return labelMap;
    }
    
    /**
     * Generates the processed data set using the feature template generator.
     * @param featureTemplateGenerator
     * @param labelMap
     * @param maxOrder
     * @return
     */
    public DataSet generateDataSet(FeatureTemplateGenerator<T> featureTemplateGenerator, Map<String, Integer> labelMap, int maxOrder) {
        List<DataSequence> dataSequenceList = new ArrayList<DataSequence>();
        for (ObservationSequence<T> observationSequence : observationSequenceList) {
            dataSequenceList.add(observationSequence.generateDataSequence(featureTemplateGenerator, labelMap, maxOrder));
        }
        return new DataSet(dataSequenceList);
    }
}
