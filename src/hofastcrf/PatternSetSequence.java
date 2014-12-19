package hofastcrf;

import java.util.List;

public class PatternSetSequence {
    List<PatternSet> patternSetList;
    
    PatternSetSequence(List<PatternSet> patternSetList) {
        this.patternSetList = patternSetList;
    }
    
    void updateFeatureExpectation() {
        for (PatternSet patternSet : patternSetList) {
            patternSet.initialize();
            patternSet.setPatternWeights();
            patternSet.calcAlpha();
            patternSet.calcGamma();
        }
        for (int i = patternSetList.size() - 1; i >= 0; --i) {
            PatternSet patternSet = patternSetList.get(i);
            patternSet.calcGamma();
            patternSet.calcDeltaAndOthers();
        }
    }
    
    void addFeatureExpectations() {
        for (PatternSet patternSet : patternSetList) {
            patternSet.addFeatureExpectations();
        }
    }
    
    double calcLogLikelihood() {
        double ret = 0.0;
        for (PatternSet patternSet : patternSetList) {
            ret += Math.log(patternSet.longestMatchPattern.weight);
        }
        return ret;
    }
}
