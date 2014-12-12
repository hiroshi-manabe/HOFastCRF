package HOFastCRF;

import java.util.Arrays;
import java.util.List;

import Parallel.Schedulable;

public class LogliComputer implements Schedulable {

    int curID;
    List<PatternSetSequence> patternSetSequence;
    double[] lambda;
    Loglikelihood logli;
    
    public LogliComputer(double[] lambdaValues, List<PatternSetSequence> patternSetSequence, Loglikelihood loglh) {
        curID = -1;
        this.patternSetSequence = patternSetSequence;
        lambda = lambdaValues;
        logli = loglh;
    }

    public Object compute(int taskID) {
        PatternSetSequence seq = patternSetSequence.get(taskID);
        
        double[] expectation = computeExpectation(seq);

        for (int k = 0; k < lambda.length; k++) {
            res.derivatives[k] -= expectation[k];
        }
        res.logli -= logZx;

        return res;
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
        Loglikelihood res = (Loglikelihood) partialResult;
        logli.logli += res.logli;
        for (int i = 0; i < lambda.length; i++) {
            logli.derivatives[i] += res.derivatives[i];
        }
    }
    
}
