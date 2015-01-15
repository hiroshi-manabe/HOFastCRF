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
 * The class for a data set.
 * A data set is simply a list of data sequences.
 * 
 * @author Hiroshi Manabe
 */
public class DataSet {

    List<DataSequence> dataSequenceList;

    /**
     * Constructor. 
     * @param dataSequenceList
     */
    public DataSet(List<DataSequence> dataSequenceList) {
        this.dataSequenceList = dataSequenceList;
    }
    
    /**
     * Generates a map having the features as keys and the frequency in the data set as values.  
     * @return the resulting map
     */
    public Map<Feature, Integer> generateFeatureCountMap() {
        Map<Feature, Integer> featureMap = new HashMap<Feature, Integer>();
        for (DataSequence dataSequence : dataSequenceList) {
            dataSequence.accumulateFeatureCountsToMap(featureMap);
        }
        return featureMap;
    }
    
    /**
     * Generates a list of PatternSetSequence for this data set.
     * @param featureTemplateToFeatureMap
     * @return
     */
    public List<PatternSetSequence> generatePatternSetSequenceList(Map<FeatureTemplate, List<Feature>> featureTemplateToFeatureMap) {
        List<PatternSetSequence> list = new ArrayList<PatternSetSequence>();
        int count = 0;
        for (DataSequence sequence : dataSequenceList) {
            list.add(sequence.generatePatternSetSequence(featureTemplateToFeatureMap));
            System.out.println("Generated a PatternSetSequence" + count++);
        }
        return list;
    }
}
