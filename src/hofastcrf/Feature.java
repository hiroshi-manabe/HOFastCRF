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

    public Feature(String obs, LabelSequence pat) {
        this.obs = obs;
        this.pat = pat;
        this.expWeight = 0.0;
        this.expectation = 0.0;
    }
    
    public synchronized void addExpectation(double expectation) {
        this.expectation += expectation;
    }
    
    public double getExpectation() {
        return expectation;
    }
    
    public void reset(double weight) {
        expWeight = Math.exp(weight);
        expectation = 0.0;
    }
    
    public double getExpWeight() {
        return expWeight;
    }
    
    public LabelSequence getLabelSequence() {
        return pat;
    }
    
    public FeatureTemplate createFeatureTemplate() {
        return new FeatureTemplate(obs, pat.getOrder());
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
