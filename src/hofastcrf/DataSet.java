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
