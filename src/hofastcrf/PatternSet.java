/*
Copyright (C) 2014 Hiroshi Manabe
Copyright (C) 2012 Nguyen Viet Cuong, Ye Nan, Sumit Bhagwani

This file is part of HOFastCRF, which is based on HOSemiCRF.

HOFastCRF is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

HOFastCRF is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with HOFastCRF. If not, see <http://www.gnu.org/licenses/>.
*/

package hofastcrf;

import java.util.List;

/**
 * A class that represents a position in the data sequence.
 * @author Hiroshi Manabe
 */
public class PatternSet {
    
    List<Pattern> patternList;
    Pattern longestMatchPattern;
    
    int scale;
    
    /**
     * Constructor.
     * @param patternList
     * @param longestMatchPattern the pattern that matches the training label sequence with the longest length.
     */
    PatternSet(List<Pattern> patternList, Pattern longestMatchPattern) {
        this.patternList = patternList;
        this.longestMatchPattern = longestMatchPattern;
    }
    
    /**
     * Initializes the scores (both for training and for decoding).
     */
    void initializeScores() {
        for (Pattern pattern : patternList) {
            pattern.alpha = 0.0;
            pattern.beta = 0.0;
            pattern.gamma = 0.0;
            pattern.delta = 0.0;
            pattern.theta = 0.0;
            pattern.sigma = 0.0;
            pattern.bestScore = 0.0;
            pattern.bestScoreForLabel = 0.0;
            pattern.expWeight = 1.0;
        }
        this.scale = 0;
    }
    
    /**
     * Sets the corresponding pattern at the previous position.
     * @param prevPattern
     */
    void setPrevPattern(Pattern prevPattern) {
        for (Pattern pattern : patternList) {
            pattern.prevPattern = prevPattern;
        }
    }
    
    /**
     * Sets the weights for the patterns by accumulating the feature weights.
     */
    void setPatternWeights() {
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            pattern.expWeight *= pattern.longestSuffixPattern.expWeight;
            for (Feature f : pattern.featureList) {
                pattern.expWeight *= f.expWeight;
            }
        }
    }
    
    /**
     * Calculates forward difference scores.
     */
    void calcAlpha() {
        for (int i = patternList.size() - 1; i >= 1; --i) {
            Pattern pattern = patternList.get(i);
            pattern.alpha += pattern.prevPattern.gamma * pattern.expWeight;
            pattern.longestSuffixPattern.alpha -= pattern.prevPattern.gamma * pattern.expWeight;
        }
        scaleAlpha();
    }
    
    /**
     * Calculates forward sum scores.
     */
    void calcGamma() {
        for (int i = patternList.size() - 1; i >= 1; --i) {
            Pattern pattern = patternList.get(i);
            pattern.gamma += pattern.alpha;
            pattern.longestSuffixPattern.gamma += pattern.gamma;
        }
    }
    
    /**
     * Sets the delta (backward difference score) for the empty pattern at the last position.
     */
    void setLastDelta() {
        patternList.get(0).delta = 1.0;
    }
    
    /**
     * Calculates backward sum scores.
     */
    void calcBeta() {
        patternList.get(0).beta = patternList.get(0).delta;
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            pattern.beta = pattern.delta + pattern.longestSuffixPattern.beta;
        }
        patternList.get(0).beta = 0.0;
        scaleBeta();
    }
    
    /**
     * Calculates backward difference scores, pattern difference expectations, pattern sum expectations. 
     */
    void calcDeltaAndOthers() {
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            // delta (backward difference score)
            pattern.prevPattern.delta += pattern.beta * pattern.expWeight - pattern.longestSuffixPattern.beta * pattern.expWeight;
            // theta (difference expectation)
            pattern.theta = pattern.beta * pattern.alpha;
        }
        for (int i = patternList.size() - 1; i >= 1; --i) {
            Pattern pattern = patternList.get(i);
            // sigma (sum expectation)
            pattern.sigma += pattern.theta;
            pattern.longestSuffixPattern.sigma += pattern.sigma;
        }
    }
    
    /**
     * Scales the forward scores in order not to over/underflow
     */
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
        }
        this.scale += maxAlphaExponent;
    }
    
    /**
     * Scales the backward scores in order not to over/underflow (reusing the forward scale factors).
     */
    void scaleBeta() {
        double expScale = Math.pow(2.0, this.scale);
        for (Pattern pattern : patternList) {
            pattern.beta /= expScale;
        }
    }
    
    /**
     * Initialize the best scores of the patterns at the beginning with their weights. 
     */
    void setFirstBestScores() {
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            pattern.bestScore = pattern.expWeight;
        }
        patternList.get(0).bestScore = 0.0;
    }
    
    /**
     * Resets the best scores for a specific label.
     */
    void resetBestScoresForLabel() {
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            pattern.bestScoreForLabel = pattern.bestScore;
            pattern.bestPrefixPattern = pattern;
        }
        patternList.get(0).bestScoreForLabel = 0.0;
    }
    
    /**
     * Scales the best scores in order not to over/underflow.
     */
    void scaleBestScores() {
        double bestScore = 0;  
        for (Pattern pattern : patternList) {
            if (pattern.bestScore > bestScore) {
                bestScore = pattern.bestScore;
            }
        }
        int bestProbExponent = (int)((Double.doubleToLongBits(bestScore) & 0x7ff0000000000000L) >> 52) - 1023;
        double expScale = Math.pow(2.0, -bestProbExponent);
        for (Pattern pattern : patternList) {
            pattern.bestScore *= expScale;
        }
    }
    
    /**
     * Accumulates the expectations of the features associated with the patterns.
     */
    void addFeatureExpectations() {
        double Z = getZ();
        
        for (int i = 1; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            for (Feature feature : pattern.featureList) {
                feature.addExpectation(-pattern.sigma / Z);
            }
        }
    }
    
    /**
     * Returns the Z (normalization factor).
     * @return
     */
    double getZ() {
        return patternList.get(0).sigma;
    }
}
