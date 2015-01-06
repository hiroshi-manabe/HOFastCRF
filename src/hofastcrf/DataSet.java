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
 * @author Hiroshi Manabe
 */
public class DataSet {

    List<DataSequence> dataSequenceList;

    public DataSet(List<DataSequence> dataSequenceList) {
        this.dataSequenceList = dataSequenceList;
    }
    
    public Map<Feature, Integer> generateFeatureCountMap() {
        Map<Feature, Integer> featureMap = new HashMap<Feature, Integer>();
        for (DataSequence dataSequence : dataSequenceList) {
            dataSequence.accumulateFeatureCountsToMap(featureMap);
        }
        return featureMap;
    }
    
    public List<PatternSetSequence> generatePatternSetSequenceList(Map<FeatureTemplate, List<Feature>> featureTemplateToFeatureMap) {
        List<PatternSetSequence> list = new ArrayList<PatternSetSequence>();
        for (DataSequence sequence : dataSequenceList) {
            list.add(sequence.generatePatternSetSequence(featureTemplateToFeatureMap));
        }
        return list;
    }
}
