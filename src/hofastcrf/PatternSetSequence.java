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
        patternSetList.get(patternSetList.size() - 1).setLastDelta();
        for (int i = patternSetList.size() - 1; i >= 0; --i) {
            PatternSet patternSet = patternSetList.get(i);
            patternSet.calcBeta();
            patternSet.calcDeltaAndOthers();
        }
        addFeatureExpectations();
    }
    
    void addFeatureExpectations() {
        for (PatternSet patternSet : patternSetList) {
            patternSet.addFeatureExpectations();
        }
    }
    
    double getZ() {
        return patternSetList.get(0).getZ();
    }
    
    double calcLogLikelihood() {
        double ret = 0.0;
        // log probability of the train set labels
        for (PatternSet patternSet : patternSetList) {
            ret += Math.log(patternSet.longestMatchPattern.weight);
        }
        // log probability of the train set
        ret -= Math.log(getZ());
        for (PatternSet patternSet : patternSetList) {
            ret -= Math.log(2.0)  * patternSet.scale;
        }
        if (Double.isNaN(ret)) {
            ret += 1.0;
        }
        return ret;
    }
}
