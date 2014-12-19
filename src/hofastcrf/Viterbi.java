/*
Copyright (C) 2012 Nguyen Viet Cuong, Ye Nan, Sumit Bhagwani

This file is part of HOSemiCRF.

HOSemiCRF is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

HOSemiCRF is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with HOSemiCRF. If not, see <http://www.gnu.org/licenses/>.
*/

package hofastcrf;

import java.util.List;

import Parallel.Schedulable;

/**
 * Implementation of the Viterbi algorithm
 * @author Nguyen Viet Cuong
 * @author Sumit Bhagwani
 */
public class Viterbi implements Schedulable {

    int curID; // Current task ID (for parallelization)
    double[] lambda; // Lambda vector
    List<DataSequence> data; // List of testing sequences
    final int BASE = 1; // Base of the logAlpha array

    /**
     * Construct a Viterbi class.
     * @param lambda Lambda vector
     * @param data Testing data
     */
    public Viterbi(double[] lambda, List<DataSequence> data) {
    }
    
    /**
     * Run the Viterbi algorithm for a given sequence.
     * @param taskID Index of the sequence
     * @return The updated sequence
     */
    public Object compute(int taskID) {
        return null;
    }
    
    /**
     * Return total number of tasks (for parallelization).
     * @return Training dataset size
     */
    public int getNumTasks() {
        return data.size();
    }

    /**
     * Return the next task ID (for parallelization).
     * @return The next sequence ID
     */
    public synchronized int fetchCurrTaskID() {
        if (curID < getNumTasks()) {
            curID++;
        }
        return curID;
    }

    /**
     * Update partial result (for parallelization).
     * Note that this method does nothing in this case.
     * @param partialResult Partial result
     */
    public void update(Object partialResult) {
        // Do nothing
    }
}
