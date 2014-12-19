package hofastcrf;

import java.util.ArrayList;
import java.util.List;

public class Pattern {
    List<Feature> featureList = new ArrayList<Feature>();
    Pattern prevPattern;
    Pattern longestSuffixPattern;
    double weight;
    double alpha;
    double beta;
    double gamma;
    double delta;
    double theta;
    double sigma;
    
    static final Pattern DUMMY_PATTERN;
    static {
        DUMMY_PATTERN = new Pattern();
        DUMMY_PATTERN.gamma = 1.0;
    }
}
