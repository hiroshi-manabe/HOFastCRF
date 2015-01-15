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

import java.io.Serializable;
import java.util.Arrays;

/**
 * The class for label sequences.  
 * @author Hiroshi Manabe
 */
@SuppressWarnings("serial")
public class LabelSequence implements Comparable<LabelSequence>, Serializable {
    
    int[] labels;
    
    /**
     * Constructor.
     * @param labels
     */
    public LabelSequence(int[] labels) {
        this.labels = labels;
    }

    public int getLength() {
        return labels.length;
    }
    
    /**
     * Creates the prefix of the label sequence by truncating the first (last in position) label.
     * @return
     */
    public LabelSequence createPrefix() {
        return new LabelSequence(Arrays.copyOfRange(labels, 1, labels.length));
    }
    
    @Override
    public int compareTo(LabelSequence that) {
        for (int i = 0; i < Math.min(this.labels.length, that.labels.length); ++i) {
            if (labels[i] != that.labels[i]) {
                return labels[i] - that.labels[i];
            }
        }
        if (this.labels.length != that.labels.length) {
            return labels.length - that.labels.length;
        }
        return 0;
    }
    
    /**
     * Compares the label sequence with the argument and returns the first different label. 
     * @param that
     * @return
     */
    public int getDifferencePosition(LabelSequence that) {
        int minLength = Math.min(this.labels.length, that.labels.length);
        for (int i = 0; i < minLength; ++i) {
            if (labels[i] != that.labels[i]) {
                return i;
            }
        }
        return minLength;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LabelSequence that = (LabelSequence)obj;
        return Arrays.equals(this.labels, that.labels);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(labels);
    }
    
    /**
     * Creates an empty (of length 0) label sequence.
     * @return
     */
    public static LabelSequence createEmptyLabelSequence() {
        return new LabelSequence(new int[0]);
    }
}
