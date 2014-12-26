package hofastcrf;

import java.util.List;

public class PatternSet {
    
    List<Pattern> patternList;
    Pattern longestMatchPattern;
    
    int scale;
    
    PatternSet(List<Pattern> patternList, Pattern longestMatchPattern) {
        this.patternList = patternList;
        this.longestMatchPattern = longestMatchPattern;
    }
    
    void initializeScores() {
        for (Pattern pattern : patternList) {
            pattern.alpha = 0.0;
            pattern.beta = 0.0;
            pattern.gamma = 0.0;
            pattern.delta = 0.0;
            pattern.theta = 0.0;
            pattern.sigma = 0.0;
            pattern.bestScore = 0.0;
            pattern.weight = 1.0;
            pattern.bestNextPattern = Pattern.DUMMY_PATTERN;
        }
    }
    
    void setPrevPattern(Pattern prevPattern) {
        for (Pattern pattern : patternList) {
            pattern.prevPattern = prevPattern;
        }
    }
    
    void setPatternWeights() {
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            pattern.weight *= pattern.longestSuffixPattern.weight;
            for (Feature f : pattern.featureList) {
                pattern.weight *= f.expWeight;
            }
        }
    }
    
    void calcAlpha() {
        for (int i = patternList.size() - 1; i >= 1; --i) {
            Pattern pattern = patternList.get(i);
            pattern.alpha += pattern.prevPattern.gamma * pattern.weight;
            pattern.longestSuffixPattern.alpha -= pattern.prevPattern.gamma * pattern.weight;
        }
        scaleAlpha();
    }
    
    void calcGamma() {
        for (int i = patternList.size() - 1; i >= 1; --i) {
            Pattern pattern = patternList.get(i);
            pattern.gamma += pattern.alpha;
            pattern.longestSuffixPattern.gamma += pattern.gamma;
        }
    }
    
    void setLastDelta() {
        patternList.get(0).delta = 1.0;
    }
    
    void setLastBestScore() {
        patternList.get(0).bestScore = 1.0;
    }
    
    void updateBestScore() {
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            if (pattern.bestNextPattern == Pattern.DUMMY_PATTERN) {
                pattern.bestNextPattern = pattern.longestSuffixPattern.bestNextPattern;
                pattern.bestScore = pattern.longestSuffixPattern.bestScore;
            }
        }
        patternList.get(0).bestScore = 0.0;
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            pattern.bestScore *= pattern.weight;
        }
        scaleBestScore();
    }
    
    void executeViterbi() {
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            if (pattern.bestScore > pattern.prevPattern.bestScore) {
                pattern.prevPattern.bestScore = pattern.bestScore;
                pattern.prevPattern.bestNextPattern = pattern;
            }
        }
    }
    
    void scaleBestScore() {
        double bestProb = 0;  
        for (Pattern pattern : patternList) {
            if (pattern.bestScore > bestProb) {
                bestProb = pattern.bestScore;
            }
        }
        int bestProbExponent = (int)((Double.doubleToLongBits(bestProb) & 0x7ff0000000000000L) >> 52) - 1023;
        double expScale = Math.pow(2.0, -bestProbExponent);
        for (Pattern pattern : patternList) {
            pattern.bestScore *= expScale;
        }
    }
    
    void calcBeta() {
        patternList.get(0).beta = patternList.get(0).delta;
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            pattern.beta = pattern.delta + pattern.longestSuffixPattern.beta;
        }
        patternList.get(0).beta = 0.0;
        scaleBeta();
    }
    
    void calcDeltaAndOthers() {
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            // delta
            pattern.prevPattern.delta += pattern.beta * pattern.weight - pattern.longestSuffixPattern.beta * pattern.weight;
            // theta
            pattern.theta = pattern.beta * pattern.alpha;
        }
        for (int i = patternList.size() - 1; i >= 1; --i) {
            Pattern pattern = patternList.get(i);
            // sigma
            pattern.sigma += pattern.theta;
            pattern.longestSuffixPattern.sigma += pattern.sigma;
        }
    }
    
    void scaleAlpha() {
        double maxAlpha = 0;  
        for (Pattern pattern : patternList) {
            if (pattern.alpha > maxAlpha) {
                maxAlpha = pattern.alpha;
            }
        }
        int maxAlphaExponent = (int)((Double.doubleToLongBits(maxAlpha) & 0x7ff0000000000000L) >> 52) - 1023;
        double expScale = Math.pow(2.0, -maxAlphaExponent);
        for (Pattern pattern : patternList) {
            pattern.alpha *= expScale;
            this.scale = maxAlphaExponent;
        }
    }
    
    void scaleBeta() {
        double expScale = Math.pow(2.0, this.scale);
        for (Pattern pattern : patternList) {
            pattern.beta /= expScale;
        }
    }
    
    void addFeatureExpectations() {
        double Z = getZ();
        
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            for (Feature feature : pattern.featureList) {
                feature.addExpectation(-pattern.sigma / Z);
            }
        }
    }
    
    double getZ() {
        return patternList.get(0).sigma;
    }
}
