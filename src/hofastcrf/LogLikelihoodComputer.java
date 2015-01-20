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

import java.util.List;

import parallel.Schedulable;

/**
 * The class for computing the log likelihood of the PatternSetSequence (converted from the raw data).
 * @author Hiroshi Manabe
 */
public class LogLikelihoodComputer implements Schedulable {

    int curID;
    List<PatternSetSequence> patternSetSequence;
    LogLikelihood logLikelihood;
    
    public LogLikelihoodComputer(List<PatternSetSequence> patternSetSequence, LogLikelihood logLikelihood) {
        curID = -1;
        this.patternSetSequence = patternSetSequence;
        this.logLikelihood = logLikelihood;
    }

    public Object compute(int taskID) {
        PatternSetSequence seq = patternSetSequence.get(taskID);
        seq.accumulateFeatureExpectation();
        double ll = seq.calcLogLikelihood();
        return new LogLikelihood(ll);
    }
    
    /**
     * Return total number of tasks (for parallelization).
     * @return Training dataset size
     */
    public int getNumTasks() {
        return patternSetSequence.size();
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
     * Add the partition function and expected feature scores into the final loglikelihood and its derivatives.
     * @param partialResult Partition function and expected feature scores
     */
    public synchronized void update(Object partialResult) {
        LogLikelihood res = (LogLikelihood) partialResult;
        this.logLikelihood.addLogLikelihood(res.getLogLikelihood());
    }    
}
