package hofastcrf;

import java.util.List;

public class PatternSetSequence {
    
    List<PatternSet> patternSetList;
    Pattern dummyPattern;
    
    PatternSetSequence(List<PatternSet> patternSetList) {
        this.patternSetList = patternSetList;
        this.dummyPattern = new Pattern();
        dummyPattern.gamma = 1.0;
        patternSetList.get(0).setPrevPattern(dummyPattern);
    }
    
    void updateFeatureExpectation() {
        for (PatternSet patternSet : patternSetList) {
            patternSet.initializeScores();
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
    
    int[] executeViterbi() {
        for (PatternSet patternSet : patternSetList) {
            patternSet.initializeScores();
            patternSet.setPatternWeights();
        }
        patternSetList.get(patternSetList.size() - 1).setLastBestScore();
        for (int i = patternSetList.size() - 1; i >= 0; --i) {
            PatternSet patternSet = patternSetList.get(i);
            patternSet.updateBestScore();
            patternSet.executeViterbi();
        }
        int[] ret = new int[patternSetList.size()];
        Pattern pattern = dummyPattern;
        for (int i = 0; i < patternSetList.size(); ++i) {
            pattern = pattern.bestNextPattern;
            ret[i] = pattern.featureList.get(0).pat.labels[0];
        }
        return ret;
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
        return ret;
    }
}
