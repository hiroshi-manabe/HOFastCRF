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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class that represents a label pattern in the lattice of the high-order CRF.
 * This class has both members for training and members for decoding (thus leaving room for optimization).
 * @author Hiroshi Manabe
 */
public class Pattern {
    List<Feature> featureList = new ArrayList<Feature>();
    Pattern prevPattern;
    Pattern longestSuffixPattern;
    Pattern bestPrefixPattern;  // for decoding
    Pattern bestPrevPattern;  // for decoding
    LabelSequence labelSequence;  // for decoding
    double expWeight;
    double alpha;
    double beta;
    double gamma;
    double delta;
    double theta;
    double sigma;
    double bestScore;  // for decoding
    double bestScoreForLabel;  // for decoding
    
    /**
     * Constructor.
     * @param labelSequence
     */
    public Pattern(LabelSequence labelSequence) {
        prevPattern = DUMMY_PATTERN;
        longestSuffixPattern = DUMMY_PATTERN;
        bestPrefixPattern = DUMMY_PATTERN;
        bestScore = 0.0;
        expWeight = 1.0;
        this.labelSequence = labelSequence;
    }
    
    @Override
    public String toString() {
        if (!DebugInfoManager.getInstance().getDebugMode()) {
            return "";
        } else {
            @SuppressWarnings("unchecked")
            Map<Integer, String> reversedLabelMap = (Map<Integer, String>)DebugInfoManager.getInstance().getDebugData("ReversedLabelMap");
            
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(bs);
            ps.print(String.join("-", Arrays.stream(labelSequence.labels).boxed()
                    .map(x -> { return reversedLabelMap.get(x); }).collect(Collectors.toList())));
            return bs.toString();
        }
    }
    
    static final Pattern DUMMY_PATTERN;
    static {
        DUMMY_PATTERN = new Pattern(new LabelSequence(new int[0]));
        DUMMY_PATTERN.gamma = 1.0;
    }
}
