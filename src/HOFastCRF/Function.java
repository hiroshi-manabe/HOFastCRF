package HOFastCRF;

import java.util.Arrays;
import java.util.List;

import Parallel.Scheduler;
import edu.stanford.nlp.optimization.DiffFunction;

public class Function implements DiffFunction {

    List<PatternSetSequence> patternSetSequence;
    List<Feature> featureList;
    int[] featureCountArray;
    double[] resultArray;

    public Function(List<PatternSetSequence> patternSetSequence, List<Feature> featureList, int[] featureCountArray) {
        this.patternSetSequence = patternSetSequence;
        this.featureList = featureList;
        this.featureCountArray = featureCountArray;
        this.resultArray = new double[featureList.size()];
    }
    
    @Override
    public double valueAt(double[] lambda) {
        return 0;
    }

    @Override
    public double[] derivativeAt(double[] lambda) {
        for (int i = 0; i < lambda.length; ++i) {
            featureList.get(i).setWeight(lambda[i]);
        }
        computeValueAndDerivatives(lambda);
        return logli.derivatives;
    }    

    public void computeValueAndDerivatives(double[] lambda) {        
        LogliComputer logliComp = new LogliComputer(lambda, featureGen, trainData, logli);
        Scheduler sch = new Scheduler(logliComp, featureGen.params.numthreads, Scheduler.DYNAMIC_NEXT_AVAILABLE);
        try {
            sch.run();
        } catch (Exception e) {
            System.out.println("Errors occur when training in parallel! " + e);
        }

        // Change sign to maximize and divide the values by size of dataset        
        int n = trainData.size();
        for (int i = 0; i < logli.derivatives.length; i++) {
            logli.derivatives[i] = -(logli.derivatives[i] / n);
        }
        logli.logli = -(logli.logli / n);
    }    
}
