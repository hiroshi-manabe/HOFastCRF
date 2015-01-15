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
 * A class that represents the sequence of the pattern sets converted from a data sequence.
 * @author Hiroshi Manabe
  */
public class PatternSetSequence {
    
    List<PatternSet> patternSetList;
    Pattern dummyPattern;
    
    /**
     * Constructor.
     * @param patternSetList
     */
    PatternSetSequence(List<PatternSet> patternSetList) {
        this.patternSetList = patternSetList;
        this.dummyPattern = new Pattern(LabelSequence.createEmptyLabelSequence());
        dummyPattern.gamma = 1.0;
        patternSetList.get(0).setPrevPattern(dummyPattern);
    }
    
    /**
     * Executes the forward backward algorithm and accumulates the feature expectations.
     */
    void accumulateFeatureExpectation() {
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
        for (PatternSet patternSet : patternSetList) {
            patternSet.addFeatureExpectations();
        }
    }
    
    /**
     * Returns the Z (normalization factor).
     * @return
     */
    double getZ() {
        return patternSetList.get(0).getZ();
    }
    
    /**
     * Calculates the log likelihood of the train set labels.
     * @return log likelihood
     */
    double calcLogLikelihood() {
        double ret = 0.0;
        // log probability of the train set labels
        for (PatternSet patternSet : patternSetList) {
            ret += Math.log(patternSet.longestMatchPattern.weight);
        }
        // log probability of the train set
        ret -= Math.log(getZ());
        // scale
        for (PatternSet patternSet : patternSetList) {
            ret -= Math.log(2.0)  * patternSet.scale;
        }
        return ret;
    }
    
    /**
     * Infers the labels.
     * @return
     */
    int[] decode() {
        for (PatternSet patternSet : patternSetList) {
            patternSet.initializeScores();
            patternSet.setPatternWeights();
        }
        
        patternSetList.get(0).setFirstBestScores();
        
        for (int i = 1; i < patternSetList.size(); ++i) {
            int prevLabel = -1; 
            PatternSet patternSet = patternSetList.get(i);
            PatternSet prevPatternSet = patternSetList.get(i - 1);
            int prevPatternSetIndex = 0;
            
            for (int patternSetIndex = patternSet.patternList.size() - 1; patternSetIndex > 0; --patternSetIndex) {
                Pattern pattern = patternSet.patternList.get(patternSetIndex);
                if (pattern.labelSequence.labels[0] != prevLabel) {
                    prevPatternSet.resetBestScoresForLabel();
                    prevPatternSetIndex = prevPatternSet.patternList.size() - 1;
                }
                while (prevPatternSet.patternList.get(prevPatternSetIndex) != pattern.prevPattern) {
                    Pattern prevPattern = prevPatternSet.patternList.get(prevPatternSetIndex);
                    if (prevPattern.bestScoreForLabel > prevPattern.longestSuffixPattern.bestScoreForLabel) {
                        prevPattern.longestSuffixPattern.bestScoreForLabel = prevPattern.bestScoreForLabel;
                        prevPattern.longestSuffixPattern.bestPrefixPattern = prevPattern.bestPrefixPattern;
                    }
                    --prevPatternSetIndex;
                }
                pattern.bestScore = pattern.prevPattern.bestScoreForLabel * pattern.weight;
                pattern.bestPrevPattern = pattern.prevPattern.bestPrefixPattern;
            }
            patternSet.scaleBestScores();
        }
        
        PatternSet lastPatternSet = patternSetList.get(patternSetList.size() - 1);
        Pattern bestPattern = Pattern.DUMMY_PATTERN;
        
        for (int i = 1; i < lastPatternSet.patternList.size(); ++i) {
            Pattern pattern = lastPatternSet.patternList.get(i);
            if (pattern.bestScore > bestPattern.bestScore) {
                bestPattern = pattern;
            }
        }
        
        int[] ret = new int[patternSetList.size()];

        for (int i = patternSetList.size() - 1; i >= 0; --i) {
            ret[i] = bestPattern.labelSequence.labels[0];
            bestPattern = bestPattern.bestPrevPattern;
        }
        return ret;
    }
}
