package hofastcrf;

import java.util.ArrayList;
import java.util.List;

public class Pattern {
    
    List<Feature> featureList = new ArrayList<Feature>();
    Pattern prevPattern;
    Pattern longestSuffixPattern;
    Pattern bestNextPattern;  // for decoding
    double weight;
    double alpha;
    double beta;
    double gamma;
    double delta;
    double theta;
    double sigma;
    double bestScore; // for decoding;
    
    public Pattern() {
        prevPattern = DUMMY_PATTERN;
        longestSuffixPattern = DUMMY_PATTERN;
        bestNextPattern = DUMMY_PATTERN;
        bestScore = 0.0;
    }
    
    static final Pattern DUMMY_PATTERN;
    static {
        DUMMY_PATTERN = new Pattern();
        DUMMY_PATTERN.gamma = 1.0;
    }
}
