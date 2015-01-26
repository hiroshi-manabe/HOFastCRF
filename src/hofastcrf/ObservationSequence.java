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
 * Class for a observation data sequence
 * @author Hiroshi Manabe
 * @param <T> The class of the observations
 */
public class ObservationSequence<T> {
    
    List<T> observationList;
    List<String> labelList;
    boolean hasValidLabels;
    
    /**
     * Construct a data sequence from a label map, labels and observations with default segmentation.
     * @param labels Label array
     * @param inps Observation array
     * @param labelm Label map
     */
    public ObservationSequence(List<T> observationList, List<String> labelList, boolean hasValidLabels) {
        this.observationList = observationList;
        this.labelList = labelList;
        this.hasValidLabels = hasValidLabels;
    }
    
    public List<T> getObservationList() {
        return observationList;
    }
    
    public List<String> getLabelList() {
        return labelList;
    }
    
    /**
     * Generates the processed data sequence (which is a list of lists of feature templates) from the feature template generator.
     * @param featureTemplateGenerator
     * @param labelMap
     * @param maxLabelLength the maximum label length of the features.
     * @return
     */
    public DataSequence generateDataSequence(FeatureTemplateGenerator<T> featureTemplateGenerator, Map<String, Integer> labelMap, int maxLabelLength) {
        List<List<FeatureTemplate>> featureTemplateListList = new ArrayList<List<FeatureTemplate>>();
        int[] labels = new int[observationList.size()];
        for (int pos = 0; pos < observationList.size(); ++pos) {
            featureTemplateListList.add(featureTemplateGenerator.generateFeatureTemplatesAt(observationList, pos));
            labels[pos] = hasValidLabels ? labelMap.get(labelList.get(pos)) : 0;
        }
        return new DataSequence(featureTemplateListList, labels, maxLabelLength, hasValidLabels);
    }
}
