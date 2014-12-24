package hofastcrf;

import java.util.Arrays;
import java.util.List;

import Parallel.Scheduler;
import edu.stanford.nlp.optimization.DiffFunction;

public class Function implements DiffFunction {

    List<PatternSetSequence> patternSetSequence;
    List<Feature> featureList;
    int[] featureCountArray;
    double[] resultArray;
    int concurrency;
    double inverseSigmaSquared;
    double logl;
    double[] lambdaCache;

    public Function(List<PatternSetSequence> patternSetSequence, List<Feature> featureList, int[] featureCountArray,
            int concurrency, double invertedSigmaSquared) {
        this.patternSetSequence = patternSetSequence;
        this.featureList = featureList;
        this.featureCountArray = featureCountArray;
        this.concurrency = concurrency;
        this.inverseSigmaSquared = invertedSigmaSquared;
        this.logl = 0.0;
        this.resultArray = new double[featureList.size()];
        this.lambdaCache = null;
    }
    
    @Override
    public double valueAt(double[] lambda) {
        if (Arrays.equals(lambda, lambdaCache)) {
            return logl;
        } else {
            lambdaCache = (double[]) lambda.clone();
            computeValueAndDerivatives(lambda);
            return logl;
        }
    }

    @Override
    public double[] derivativeAt(double[] lambda) {
        if (Arrays.equals(lambda, lambdaCache)) {
            return resultArray;
        } else {
            lambdaCache = (double[]) lambda.clone();
            computeValueAndDerivatives(lambda);
            return resultArray;
        }
    }    

    public void computeValueAndDerivatives(double[] lambda) {
        for (int i = 0; i < lambda.length; ++i) {
            featureList.get(i).reset(lambda[i]);
        }
        LogLikelihood logLikelihood = new LogLikelihood(0.0);
        for (int i = 0; i < lambda.length; i++) {
            Feature f = featureList.get(i);
            f.addExpectation(featureCountArray[i]);
            logLikelihood.addLogLikelihood(-((lambda[i] * lambda[i]) * inverseSigmaSquared) / 2);
            f.addExpectation(-lambda[i] * inverseSigmaSquared);
        }
        LogLikelihoodComputer logLikelihoodComputer = new LogLikelihoodComputer(patternSetSequence, logLikelihood);
        Scheduler sch = new Scheduler(logLikelihoodComputer, concurrency, Scheduler.DYNAMIC_NEXT_AVAILABLE);
        try {
            sch.run();
        } catch (Exception e) {
            System.out.println("Errors occur when training in parallel! " + e);
        }
        logl = logLikelihood.getLogLikelihood();

        for (int i = 0; i < lambda.length; ++i) { 
            resultArray[i] = featureList.get(i).expectation;
            resultArray[i] -= lambda[i] * inverseSigmaSquared;
            logl += inverseSigmaSquared * lambda[i] * lambda[i] * 0.5;
        }
        for (int i = 0; i < lambda.length; ++i) { 
            resultArray[i] = -resultArray[i];
        }
        logl = -logl;
    }

    @Override
    public int domainDimension() {
        return featureList.size();
    }    
}
