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
