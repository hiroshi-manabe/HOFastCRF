/*
Copyright (C) 2014 Hiroshi Manabe
*/

package hofastcrf;

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
        LabelSequence emptyLabelSequence = LabelSequence.createEmptyLabelSequence();
        
        for (int pos = 0; pos < this.length(); ++pos) {
            List<FeatureTemplate> curFeatureTemplateList = featureTemplateListList.get(pos);
            mapList.add(new TreeMap<LabelSequence, Pattern>());
            SortedMap<LabelSequence, Pattern> curMap = mapList.get(pos);
            Pattern emptyPattern = new Pattern();
            curMap.put(emptyLabelSequence, emptyPattern);
            
            for (FeatureTemplate template : curFeatureTemplateList) {
                if (template.getOrder() > pos + 1) {
                    continue;
                }
                List<Feature> featureList = featureTemplateToFeatureMap.get(template);
                for (Feature feature : featureList) {
                    //FIXME
                    if ((pos == 0 || pos == this.length() - 1) &&feature.getLabelSequence().labels[0] != 0) {
                        continue;
                    }
                    LabelSequence seq = feature.getLabelSequence();
                    if (!curMap.containsKey(seq)) {
                        curMap.put(seq, new Pattern());
                    }
                    Pattern pat = curMap.get(seq);
                    pat.featureList.add(feature);
                    if (pos == 0) {
                        pat.prevPattern = Pattern.DUMMY_PATTERN;
                    } else {
                        int order = seq.getOrder();
                        for (int i = 1; i <= order; ++i) {
                            SortedMap<LabelSequence, Pattern> prevMap = mapList.get(pos - i);  
                            LabelSequence prefix = seq.createPrefix();

                            boolean prevMapContainsKey = prevMap.containsKey(prefix);  
                            if (!prevMapContainsKey) {
                                prevMap.put(prefix, new Pattern());
                            }
                            curMap.get(seq).prevPattern = prevMap.get(prefix);
                            if (prevMapContainsKey) {
                                break;
                            }
                            seq = prefix;
                            curMap = prevMap;
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
                for (int i = diffPos; i < curLabelSequence.getOrder(); ++i) {
                    longestSuffixCandidateList.set(i, longestSuffixCandidateList.get(diffPos));
                }
                longestSuffixCandidateList.set(curLabelSequence.getOrder(), curPattern);
                if ((pos > 0 && curPattern.prevPattern == null) || curPattern.longestSuffixPattern == null) {
                    pos = 10000;
                }
            }
            
            List<Pattern> patternList = new ArrayList<Pattern>();
            patternList.addAll(mapList.get(pos).values());
            Pattern longestMatchPattern = patternList.get(0);
            if (hasValidLabels && pos > 1) {
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
