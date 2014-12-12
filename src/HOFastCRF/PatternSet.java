package HOFastCRF;

import java.util.List;

public class PatternSet {
    List<Pattern> patternList;
    Pattern longestMatchPattern;
    
    int scale;
    
    public PatternSet(List<Pattern> patternList, Pattern longestMatchPattern) {
        this.patternList = patternList;
        this.longestMatchPattern = longestMatchPattern;
    }
    
    public void setPatternWeights() {
        for (Pattern pattern : patternList) {
            double weight = 1.0;
            weight *= pattern.longestSuffixPattern.weight;
            for (Feature f : pattern.featureList) {
                weight *= f.expWeight;
            }
            pattern.weight = weight;
        }
    }
    
    public void calcAlpha() {
        for (int i = patternList.size() - 1; i >= 1; --i) {
            Pattern pattern = patternList.get(i);
            pattern.forwardScore += pattern.prevPattern.forwardScore;
            pattern.forwardScore *= pattern.weight;
            pattern.longestSuffixPattern.forwardScore -= pattern.prevPattern.forwardScore;
        }
    }
    
    public void calcGamma() {
        for (int i = patternList.size() - 1; i >= 1; --i) {
            Pattern pattern = patternList.get(i);
            pattern.longestSuffixPattern.forwardScore += pattern.forwardScore;
        }
        scaleForwardScores();
    }
    
    public void setLastDelta() {
        for (Pattern pattern : patternList) {
            pattern.backwardScore = 1.0;
        }
        patternList.get(0).backwardScore = 0;
    }
    
    public void calcBeta() {
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            pattern.backwardScore += pattern.longestSuffixPattern.backwardScore;
        }
        scaleBackwardScores();
    }
    
    public void calcDeltaAndOthers() {
        for (Pattern pattern : patternList) {
            // beta * W
            pattern.backwardScore *= pattern.weight;
            // delta
            pattern.prevPattern.backwardScore += pattern.backwardScore;
            // theta
            pattern.backwardScore *= pattern.forwardScore;
        }
        for (int i = patternList.size() - 1; i >= 1; --i) {
            Pattern pattern = patternList.get(i);
            // sigma
            pattern.longestSuffixPattern.backwardScore += pattern.backwardScore;
        }
    }
    
    public void scaleForwardScores() {
        double maxForwardScore = 0;  
        for (Pattern pattern : patternList) {
            if (pattern.forwardScore > maxForwardScore) {
                maxForwardScore = pattern.forwardScore;
            }
        }
        int maxForwardScoreExponent = (int)((Double.doubleToLongBits(maxForwardScore) & 0x7ff0000000000000L) >> 52) - 1023;
        double expScale = Math.pow(2.0, -maxForwardScoreExponent);
        for (Pattern pattern : patternList) {
            pattern.forwardScore *= expScale;
            this.scale = maxForwardScoreExponent;
        }
    }
    public void scaleBackwardScores() {
        double expScale = Math.pow(2.0, this.scale);
        for (Pattern pattern : patternList) {
            pattern.backwardScore *= expScale;
        }
    }
    
    public void addFeatureExpectations() {
        double Z = patternList.get(0).backwardScore;
        
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            for (Feature feature : pattern.featureList) {
                feature.addExpectation(pattern.backwardScore / Z);
            }
        }
    }
    
    public double getZ() {
        return patternList.get(0).backwardScore;
    }
}
