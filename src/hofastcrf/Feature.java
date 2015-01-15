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

/**
 * Feature class
 * @author Hiroshi Manabe
 */
@SuppressWarnings("serial")
public class Feature implements Serializable {
    
    String obs;
    LabelSequence pat;
    double expWeight;
    transient double expectation;

    /**
     * Constructor.
     * @param obs observation part
     * @param pat label sequence part
     */
    public Feature(String obs, LabelSequence pat) {
        this.obs = obs;
        this.pat = pat;
        this.expWeight = 0.0;
        this.expectation = 0.0;
    }
    
    /**
     * Adds the given expectation to the feature.
     * @param expectation
     */
    public synchronized void addExpectation(double expectation) {
        this.expectation += expectation;
    }
    
    public double getExpectation() {
        return expectation;
    }
    
    /**
     * Resets the expectation and initialize the weight with the argument.
     * @param weight
     */
    public void reset(double weight) {
        expWeight = Math.exp(weight);
        expectation = 0.0;
    }
    
    /**
     * Gets the exponential weight.
     * @return
     */
    public double getExpWeight() {
        return expWeight;
    }
    
    public LabelSequence getLabelSequence() {
        return pat;
    }
    
    /**
     * Creates a feature template, ignoring labels and just keeping the order. 
     * @return
     */
    public FeatureTemplate createFeatureTemplate() {
        return new FeatureTemplate(obs, pat.getLength());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Feature that = (Feature)obj;
        return this.obs.equals(that.obs) && this.pat.equals(that.pat);
    }
    
    @Override
    public int hashCode() {
        return obs.hashCode() + pat.hashCode();
    }
}
