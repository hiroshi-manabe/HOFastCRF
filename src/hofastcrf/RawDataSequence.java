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
import java.util.List;
import java.util.Map;

/**
 * Class for a raw data sequence
 * @author Hiroshi Manabe
 */
public class RawDataSequence<T> {
    
    List<T> rawObservationList;
    List<String> rawLabelList;
    boolean hasValidLabels;
    
    /**
     * Construct a data sequence from a label map, labels and observations with default segmentation.
     * @param labels Label array
     * @param inps Observation array
     * @param labelm Label map
     */
    public RawDataSequence(List<T> rawObservationList, List<String> rawLabelList, boolean hasValidLabels) {
        this.rawObservationList = rawObservationList;
        this.rawLabelList = rawLabelList;
        this.hasValidLabels = hasValidLabels;
    }
    
    public List<T> getRawObservationList() {
        return rawObservationList;
    }
    
    public List<String> getRawLabelList() {
        return rawLabelList;
    }
    
    public DataSequence convertToDataSequence(FeatureTemplateGenerator<T> featureTemplateGenerator, Map<String, Integer> labelMap, int maxOrder) {
        List<List<FeatureTemplate>> featureTemplateListList = new ArrayList<List<FeatureTemplate>>();
        int[] labels = new int[rawObservationList.size()];
        for (int pos = 0; pos < rawObservationList.size(); ++pos) {
            featureTemplateListList.add(featureTemplateGenerator.generateFeatureTemplatesAt(rawObservationList, pos));
            labels[pos] = hasValidLabels ? labelMap.get(rawLabelList.get(pos)) : 0;
        }
        return new DataSequence(featureTemplateListList, labels, maxOrder, hasValidLabels);
    }
}
