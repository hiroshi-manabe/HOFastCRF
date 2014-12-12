package HOFastCRF;

import java.util.List;

public class PatternSetSequence {
    List<PatternSet> patternSetList;
    
    public PatternSetSequence(List<PatternSet> patternSetList) {
        this.patternSetList = patternSetList;
    }
    
    public void updateFeatureExpectation() {
        for (PatternSet patternSet : patternSetList) {
            patternSet.setPatternWeights();
            patternSet.calcAlpha();
            patternSet.calcGamma();
        }
        for (int i = patternSetList.size(); i >= 0; --i) {
            PatternSet patternSet = patternSetList.get(i);
            patternSet.calcGamma();
            patternSet.calcDeltaAndOthers();
        }
    }
    
    public void addFeatureExpectations() {
        for (PatternSet patternSet : patternSetList) {
            patternSet.addFeatureExpectations();
        }
    }
}
