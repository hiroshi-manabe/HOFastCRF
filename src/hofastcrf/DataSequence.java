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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The class for a data sequence.
 * A data sequence is represented as a list (one for each position) of lists of
 * feature templates that are activated at that position.
 * 
 * A data sequence can be generated from a raw data sequence and a feature template generator.
 * A data sequence can in turn generate a pattern set sequence, which is used for the real computation.
 * 
 * @author Hiroshi Manabe
 */
public class DataSequence {
    
    List<List<FeatureTemplate>> featureTemplateListList;
    int[] labels;
    boolean hasValidLabels;
    int maxOrder;
    
    /**
     * Constructs a data sequence.
     * @param featureTemplateListList
     * @param labels
     */
    public DataSequence(List<List<FeatureTemplate>> featureTemplateListList, int[] labels, int maxOrder, boolean hasValidLabels) {
        this.labels = labels;
        this.featureTemplateListList = featureTemplateListList;
        this.maxOrder = maxOrder;
        this.hasValidLabels = hasValidLabels;
    }

    /**
     * Returns the length of the sequence. 
     * @return length
     */
    public int length() {
        return featureTemplateListList.size();
    }

    /**
     * Returns the reversed label sequence starting from a given position.
     * @param pos 
     * @param length
     * @return reversed label sequence starting from the position
     */
    public LabelSequence getLabelSequence(int pos, int length) {
        if (!hasValidLabels) {
            return LabelSequence.createEmptyLabelSequence();
        }
        if (pos < length - 1) {
            return LabelSequence.createEmptyLabelSequence();
        }
        int[] labels = new int[length];
        for (int i = 0; i < length; ++i) {
            labels[i] = this.labels[pos - i];
        }
        return new LabelSequence(labels);
    }
    
    /**
     * Accumulates counts of the features in this data sequence to the given map.
     * @param featureCountMap
     */
    public void accumulateFeatureCountsToMap(Map<Feature, Integer> featureCountMap) {
        if (!hasValidLabels) {
            return;
        }
        for (int pos = 0; pos < labels.length; ++pos) {
            for (FeatureTemplate featureTemplate : featureTemplateListList.get(pos)) {
                if (pos < featureTemplate.getOrder() - 1) {
                    continue;
                }
                Feature f = new Feature(featureTemplate.getObservation(), getLabelSequence(pos, featureTemplate.getOrder()));
                if (!featureCountMap.containsKey(f)) {
                    featureCountMap.put(f, 0);
                }
                featureCountMap.put(f, featureCountMap.get(f) + 1);
            }
        }
    }
    
    /**
     * Generates a PatternSetSequence for this sequence.
     * @param featureTemplateToFeatureMap
     * @return the resulting PatternSetSequence
     */
    public PatternSetSequence generatePatternSetSequence(Map<FeatureTemplate, List<Feature>> featureTemplateToFeatureMap) {
        List<SortedMap<LabelSequence, Pattern>> mapList = new ArrayList<SortedMap<LabelSequence, Pattern>>();
        LabelSequence emptyLabelSequence = LabelSequence.createEmptyLabelSequence();
        
        for (int pos = 0; pos < this.length(); ++pos) {
            List<FeatureTemplate> curFeatureTemplateList = featureTemplateListList.get(pos);
            mapList.add(new TreeMap<LabelSequence, Pattern>());
            SortedMap<LabelSequence, Pattern> curMap = mapList.get(pos);
            Pattern emptyPattern = new Pattern(LabelSequence.createEmptyLabelSequence());
            curMap.put(emptyLabelSequence, emptyPattern);
            
            for (FeatureTemplate template : curFeatureTemplateList) {
                if (template.getOrder() > pos + 1) {
                    continue;
                }
                List<Feature> featureList = featureTemplateToFeatureMap.get(template);
                for (Feature feature : featureList) {
                    LabelSequence seq = feature.getLabelSequence();
                    if (!curMap.containsKey(seq)) {
                        curMap.put(seq, new Pattern(seq));
                    }
                    Pattern pat = curMap.get(seq);
                    pat.featureList.add(feature);

                    if (pos == 0) {
                        pat.prevPattern = Pattern.DUMMY_PATTERN;
                    } else {
                        int order = seq.getLength();
                        SortedMap<LabelSequence, Pattern> tempMap = mapList.get(pos);
                        for (int i = 1; i <= order; ++i) {
                            SortedMap<LabelSequence, Pattern> prevMap = mapList.get(pos - i);  
                            LabelSequence prefix = seq.createPrefix();

                            boolean prevMapContainsKey = prevMap.containsKey(prefix);  
                            if (!prevMapContainsKey) {
                                prevMap.put(prefix, new Pattern(prefix));
                            }
                            tempMap.get(seq).prevPattern = prevMap.get(prefix);
                            if (prevMapContainsKey) {
                                break;
                            }
                            seq = prefix;
                            tempMap = prevMap;
                        }
                    }
                }
            }
        }
        List<PatternSet> patternSetList = new ArrayList<PatternSet>();
        for (int pos = 0; pos < length(); ++pos) {
            SortedMap<LabelSequence, Pattern> curMap = mapList.get(pos);
            List<Pattern> longestSuffixCandidateList = new ArrayList<Pattern>();
            Pattern emptyPattern = curMap.get(emptyLabelSequence);
            for (int i = 0; i < maxOrder + 1; ++i) {
                longestSuffixCandidateList.add(emptyPattern);
            }
            
            LabelSequence prevLabelSequence = emptyLabelSequence;
            for (Map.Entry<LabelSequence, Pattern> entry : curMap.entrySet()) {
                LabelSequence curLabelSequence = entry.getKey();
                Pattern curPattern = entry.getValue();
                if (curLabelSequence.equals(emptyLabelSequence)) {
                    continue;
                }
                int diffPos = curLabelSequence.getDifferencePosition(prevLabelSequence);
                curPattern.longestSuffixPattern = longestSuffixCandidateList.get(diffPos);
                for (int i = diffPos + 1; i < curLabelSequence.getLength(); ++i) {
                    longestSuffixCandidateList.set(i, longestSuffixCandidateList.get(diffPos));
                }
                longestSuffixCandidateList.set(curLabelSequence.getLength(), curPattern);
                prevLabelSequence = curLabelSequence;
            }
            
            List<Pattern> patternList = new ArrayList<Pattern>();
            patternList.addAll(mapList.get(pos).values());
            Pattern longestMatchPattern = patternList.get(0);
            if (hasValidLabels) {
                SortedMap<LabelSequence, Pattern> prevMap = mapList.get(pos);
                for (int length = Math.min(maxOrder, pos + 1); length > 0; --length) {
                    LabelSequence key = getLabelSequence(pos, length);
                    if (prevMap.containsKey(key)) {
                        longestMatchPattern = prevMap.get(key);
                        break;
                    }
                }
            }
            patternSetList.add(new PatternSet(patternList, longestMatchPattern));
        }
        return new PatternSetSequence(patternSetList);
    }
}
