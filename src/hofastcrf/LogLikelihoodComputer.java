package hofastcrf;

import java.util.List;

import Parallel.Schedulable;

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
        seq.updateFeatureExpectation();
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
