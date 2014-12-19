package hofastcrf;

import java.util.List;

import Parallel.Scheduler;
import edu.stanford.nlp.optimization.DiffFunction;

public class Function implements DiffFunction {

    List<PatternSetSequence> patternSetSequence;
    List<Feature> featureList;
    int[] featureCountArray;
    double[] resultArray;
    int concurrency;
    double invSigmaSquare;
    double value;

    public Function(List<PatternSetSequence> patternSetSequence, List<Feature> featureList, int[] featureCountArray,
            int concurrency, double invSigmaSquare) {
        this.patternSetSequence = patternSetSequence;
        this.featureList = featureList;
        this.featureCountArray = featureCountArray;
        this.concurrency = concurrency;
        this.invSigmaSquare = invSigmaSquare;
        this.value = 0.0;
        this.resultArray = new double[featureList.size()];
    }
    
    @Override
    public double valueAt(double[] lambda) {
        return value;
    }

    @Override
    public double[] derivativeAt(double[] lambda) {
        for (int i = 0; i < lambda.length; ++i) {
            featureList.get(i).reset(lambda[i]);
        }
        computeValueAndDerivatives(lambda);
        
        int index = 0;
        for (Feature f : featureList) { 
            resultArray[index] = f.expectation;
        }
        return resultArray;
    }    

    public void computeValueAndDerivatives(double[] lambda) {
        LogLikelihood logLikelihood = new LogLikelihood(0.0);
        for (int i = 0; i < lambda.length; i++) {
            Feature f = featureList.get(i);
            f.addExpectation(featureCountArray[i]);
            logLikelihood.addLogLikelihood(-((lambda[i] * lambda[i]) * invSigmaSquare) / 2);
            f.addExpectation(-lambda[i] * invSigmaSquare);
        }
        LogLikelihoodComputer logLikelihoodComputer = new LogLikelihoodComputer(patternSetSequence, logLikelihood);
        Scheduler sch = new Scheduler(logLikelihoodComputer, concurrency, Scheduler.DYNAMIC_NEXT_AVAILABLE);
        try {
            sch.run();
        } catch (Exception e) {
            System.out.println("Errors occur when training in parallel! " + e);
        }
        value = logLikelihood.getLogLikelihood();
    }

    @Override
    public int domainDimension() {
        return featureList.size();
    }    
}
