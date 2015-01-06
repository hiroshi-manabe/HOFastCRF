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
        weight = 1.0;
    }
    
    static final Pattern DUMMY_PATTERN;
    static {
        DUMMY_PATTERN = new Pattern();
        DUMMY_PATTERN.gamma = 1.0;
    }
}
