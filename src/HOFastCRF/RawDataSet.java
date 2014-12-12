package HOFastCRF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RawDataSet {
    List<RawDataSequence> rawDataSequenceList;
    
    public RawDataSet(List<RawDataSequence> rawDataSequenceList) {
        this.rawDataSequenceList = rawDataSequenceList;
    }
    
    public Map<String, Integer> generateLabelMap() {
        Map<String, Integer> labelMap = new HashMap<String, Integer>();
        for (RawDataSequence seq : rawDataSequenceList) {
            List<String> rawLabelList = seq.getRawLabelList();
            for (String rawLabel : rawLabelList) {
                if (!labelMap.containsKey(rawLabel)) {
                    labelMap.put(rawLabel, labelMap.size());
                }
            }
        }
        return labelMap;
    }
    
    public DataSet generateDataSet(FeatureTemplateGenerator featureTemplateGenerator, Map<String, Integer> labelMap) {
        List<DataSequence> dataSequenceList = new ArrayList<DataSequence>();
        for (RawDataSequence rawDataSequence : rawDataSequenceList) {
            dataSequenceList.add(rawDataSequence.convertToDataSequence(featureTemplateGenerator, labelMap));
        }
        return new DataSet(dataSequenceList);
    }
}
