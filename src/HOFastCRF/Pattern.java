package HOFastCRF;

import java.util.List;

public class Pattern {
    List<Feature> featureList;
    Pattern prevPattern;
    Pattern longestSuffixPattern;
    double weight;
    double forwardScore;
    double backwardScore;
    
    static final Pattern DUMMY_PATTERN;
    static {
        DUMMY_PATTERN = new Pattern();
        DUMMY_PATTERN.forwardScore = 1.0;
        DUMMY_PATTERN.weight = 1.0;
    }
}
