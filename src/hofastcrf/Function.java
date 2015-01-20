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

import java.util.Arrays;
import java.util.List;

import parallel.Scheduler;
import edu.stanford.nlp.optimization.DiffFunction;

/**
 * The class that estimates the feature weights.
 * @author Hiroshi Manabe
 */
public class Function implements DiffFunction {

    List<PatternSetSequence> patternSetSequenceList;
    List<Feature> featureList;
    int[] featureCountArray;
    double[] resultArray;
    int concurrency;
    double regularizationCoefficientL2;
    double logl;
    double[] lambdaCache;
    
    /**
     * Constructor.
     * @param patternSetSequenceList
     * @param featureList
     * @param featureCountArray the counts of the features observed in the training set 
     * @param concurrency
     * @param regularizationCoefficientL2 set this parameter to 0.0 when using the L1 optimization
     */
    public Function(List<PatternSetSequence> patternSetSequenceList, List<Feature> featureList, int[] featureCountArray,
            int concurrency, double regularizationCoefficientL2) {
        this.patternSetSequenceList = patternSetSequenceList;
        this.featureList = featureList;
        this.featureCountArray = featureCountArray;
        this.concurrency = concurrency;
        this.regularizationCoefficientL2 = regularizationCoefficientL2;
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
    
    /**
     * Executes the real computation and updates the feature expectations and the log likelihood.
     * @param lambda
     */
    public void computeValueAndDerivatives(double[] lambda) {
        for (int i = 0; i < lambda.length; ++i) {
            featureList.get(i).reset(lambda[i]);
        }
        LogLikelihood logLikelihood = new LogLikelihood(0.0);
        for (int i = 0; i < lambda.length; i++) {
            Feature f = featureList.get(i);
            f.addExpectation(featureCountArray[i]);
        }
        
        if (regularizationCoefficientL2 != 0.0) {
            for (int i = 0; i < lambda.length; i++) {
                Feature f = featureList.get(i);
                logLikelihood.addLogLikelihood(-((lambda[i] * lambda[i]) * regularizationCoefficientL2) / 2);
                f.addExpectation(-lambda[i] * regularizationCoefficientL2);
            }
        }
        
        LogLikelihoodComputer logLikelihoodComputer = new LogLikelihoodComputer(patternSetSequenceList, logLikelihood);
        Scheduler sch = new Scheduler(logLikelihoodComputer, concurrency, Scheduler.DYNAMIC_NEXT_AVAILABLE);
        try {
            sch.run();
        } catch (Exception e) {
            System.out.println("Errors occur when training in parallel! " + e);
        }
        logl = logLikelihood.getLogLikelihood();
        
        for (int i = 0; i < lambda.length; ++i) { 
            resultArray[i] = featureList.get(i).expectation;
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
