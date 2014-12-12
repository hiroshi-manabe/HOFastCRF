package HOFastCRF;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class for a raw data sequence
 * @author Hiroshi Manabe
 */
public class RawDataSequence {
    
    List<Object> rawObservationList;
    List<String> rawLabelList;
    boolean hasValidLabels;
    
    /**
     * Construct a data sequence from a label map, labels and observations with default segmentation.
     * @param labels Label array
     * @param inps Observation array
     * @param labelm Label map
     */
    public RawDataSequence(List<Object> rawObservationList, List<String> rawLabelList, boolean hasValidLabels) {
        this.rawObservationList = rawObservationList;
        this.rawLabelList = rawLabelList;
        this.hasValidLabels = hasValidLabels;
    }
    
    public List<Object> getRawObservationList() {
        return rawObservationList;
    }
    
    public List<String> getRawLabelList() {
        return rawLabelList;
    }
    
    public DataSequence convertToDataSequence(FeatureTemplateGenerator featureTemplateGenerator, Map<String, Integer> labelMap) {
        List<List<FeatureTemplate>> featureTemplateListList = new ArrayList<List<FeatureTemplate>>();
        int[] labels = new int[rawObservationList.size()];
        for (int pos = 0; pos < rawObservationList.size(); ++pos) {
            featureTemplateListList.add(featureTemplateGenerator.generateFeatureTemplatesAt(rawObservationList, pos));
            labels[pos] = hasValidLabels ? labelMap.get(rawLabelList.get(pos)) : 0;
        }
        return new DataSequence(featureTemplateListList, labels, hasValidLabels);
    }
}
