package hofastcrf;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class HighOrderCRFData implements Serializable {
    final List<Feature> featureList;
    final Map<String, Integer> labelMap;
    
    public HighOrderCRFData(List<Feature> featureList, Map<String, Integer> labelMap) {
        this.featureList = featureList;
        this.labelMap = labelMap;
    }

    public List<Feature> getFeatureList() {
        return featureList;
    }

    public Map<String, Integer> getLabelMap() {
        return labelMap;
    }
}
