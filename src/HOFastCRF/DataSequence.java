/*
Copyright (C) 2014 Hiroshi Manabe
*/

package HOFastCRF;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class for a data sequence
 * @author Hiroshi Manabe
 */
public class DataSequence {
    
    List<List<FeatureTemplate>> featureTemplateListList;
    int[] labels;
    boolean hasValidLabels;
    
    /**
     * Constructs a data sequence.
     * @param featureTemplateListList
     * @param labels
     */
    public DataSequence(List<List<FeatureTemplate>> featureTemplateListList, int[] labels, boolean hasValidLabels) {
        this.labels = labels;
        this.featureTemplateListList = featureTemplateListList;
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
     * @return if the labels match the label sequence or not
     */
    public boolean matches(int[] labels, int pos) {
        if (pos < labels.length || pos >= this.labels.length) {
            return false;
        }
        for (int i = 0; i < labels.length; ++i) {
            if (this.labels[pos - i] != labels[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return reversed label sequence starting from the position
     */
    public LabelSequence getLabelSequence(int pos, int length) {
        if (!hasValidLabels) {
            return new LabelSequence(new int[0]);
        }
        if (pos < labels.length - 1 || pos >= this.labels.length) {
            return new LabelSequence(new int[0]);
        }
        int[] labels = new int[length];
        for (int i = 0; i < length; ++i) {
            labels[i] = this.labels[pos - i];
        }
        return new LabelSequence(labels);
    }
    
    public void accumulateFeatureCountsToMap(Map<Feature, Integer> arg) {
        if (!hasValidLabels) {
            return;
        }
        for (int pos = 0; pos < labels.length; ++pos) {
            for (FeatureTemplate featureTemplate : featureTemplateListList.get(pos)) {
                if (pos < featureTemplate.getOrder() - 1) {
                    continue;
                }
                Feature f = new Feature(featureTemplate.getObservation(), getLabelSequence(pos, featureTemplate.getOrder()));
                if (!arg.containsKey(f)) {
                    arg.put(f, 0);
                }
                arg.put(f, arg.get(f) + 1);
            }
        }
    }
    
    public PatternSetSequence generatePatternSetSequence(Map<FeatureTemplate, List<Feature>> featureTemplateToFeatureMap) {
        List<SortedMap<LabelSequence, Pattern>> mapList = new ArrayList<SortedMap<LabelSequence, Pattern>>();
        for (int pos = 0; pos < this.length(); ++pos) {
            List<FeatureTemplate> curFeatureTemplateList = featureTemplateListList.get(pos);
            mapList.add(new TreeMap<LabelSequence, Pattern>());
            SortedMap<LabelSequence, Pattern> curMap = mapList.get(pos);
            Pattern emptyPattern = new Pattern();
            LabelSequence emptyLabelSequence = LabelSequence.createEmptyLabelSequence(); 
            curMap.put(emptyLabelSequence, emptyPattern);
            
            for (FeatureTemplate template : curFeatureTemplateList) {
                if (template.getOrder() - 1 < pos) {
                    continue;
                }
                List<Feature> featureList = featureTemplateToFeatureMap.get(template);
                for (Feature feature : featureList) {
                    LabelSequence seq = feature.getLabelSequence();
                    if (!curMap.containsKey(seq)) {
                        curMap.put(seq, new Pattern());
                    }
                    Pattern pat = curMap.get(seq);
                    pat.featureList.add(feature);
                    if (pos > 0) {
                        for (int i = 0; i < seq.getOrder(); ++i) {
                            LabelSequence prefix = seq.createPrefix();
                            SortedMap<LabelSequence, Pattern> prevMap = mapList.get(pos - i);  
                            if (!prevMap.containsKey(prefix)) {
                                prevMap.put(prefix, new Pattern());
                            }
                            curMap.get(seq).prevPattern = prevMap.get(prefix);
                            seq = prefix;
                            curMap = prevMap;
                        }
                    }
                }
            }
            
            List<Pattern> longestSuffixCandidateList = new ArrayList<Pattern>();
            for (int i = 0; i < Config.MAX_ORDER; ++i) {
                longestSuffixCandidateList.add(emptyPattern);
            }
            
            if (pos == 0) {
                for (Map.Entry<LabelSequence, Pattern> entry : curMap.entrySet()) {
                    entry.getValue().prevPattern = Pattern.DUMMY_PATTERN;
                }
                
            } else {
                LabelSequence prevLabelSequence = emptyLabelSequence;
                for (Map.Entry<LabelSequence, Pattern> entry : curMap.entrySet()) {
                    LabelSequence curLabelSequence = entry.getKey();
                    Pattern curPattern = entry.getValue();
                    if (curLabelSequence.equals(emptyLabelSequence)) {
                        continue;
                    }
                    int diffPos = curLabelSequence.getDifferencePosition(prevLabelSequence);
                    curPattern.longestSuffixPattern = longestSuffixCandidateList.get(diffPos);
                    for (int i = diffPos; i < curLabelSequence.getOrder(); ++i) {
                        longestSuffixCandidateList.set(i, longestSuffixCandidateList.get(diffPos));
                    }
                    longestSuffixCandidateList.set(curLabelSequence.getOrder(), curPattern);
                }
            }
        }
        List<PatternSet> patternSetList = new ArrayList<PatternSet>();
        for (int pos = 0; pos < length(); ++pos) {
            List<Pattern> patternList = new ArrayList<Pattern>();
            patternList.addAll(mapList.get(pos).values());
            Pattern longestMatchPattern = patternList.get(0);
            if (hasValidLabels && pos > 1) {
                SortedMap<LabelSequence, Pattern> prevMap = mapList.get(pos);
                for (int length = Math.min(Config.MAX_ORDER, pos + 1); length > 0; --length) {
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
