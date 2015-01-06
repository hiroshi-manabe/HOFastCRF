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

public class RawDataSet<T> {
    
    List<RawDataSequence<T>> rawDataSequenceList;
    
    public RawDataSet(List<RawDataSequence<T>> rawDataSequenceList) {
        this.rawDataSequenceList = rawDataSequenceList;
    }
    
    public Map<String, Integer> generateLabelMap() {
        Map<String, Integer> labelMap = new HashMap<String, Integer>();
        for (RawDataSequence<T> seq : rawDataSequenceList) {
            List<String> rawLabelList = seq.getRawLabelList();
            for (String rawLabel : rawLabelList) {
                if (!labelMap.containsKey(rawLabel)) {
                    labelMap.put(rawLabel, labelMap.size());
                }
            }
        }
        return labelMap;
    }
    
    public DataSet generateDataSet(FeatureTemplateGenerator<T> featureTemplateGenerator, Map<String, Integer> labelMap, int maxOrder) {
        List<DataSequence> dataSequenceList = new ArrayList<DataSequence>();
        for (RawDataSequence<T> rawDataSequence : rawDataSequenceList) {
            dataSequenceList.add(rawDataSequence.convertToDataSequence(featureTemplateGenerator, labelMap, maxOrder));
        }
        return new DataSet(dataSequenceList);
    }
}
