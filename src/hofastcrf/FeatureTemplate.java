package hofastcrf;

public class FeatureTemplate {
    
    String obs;
    int order;
    
    public FeatureTemplate(String obs, int order) {
        this.obs = obs;
        this.order = order;
    }
    
    public Feature GenerateFeature(LabelSequence seq) {
        return new Feature(this.obs, seq);
    }
    
    public int getOrder() {
        return order;
    }
    
    public String getObservation() {
        return obs;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FeatureTemplate that = (FeatureTemplate)obj;
        return this.obs.equals(that.obs) && this.order == that.order;
    }
    
    @Override
    public int hashCode() {
        return obs.hashCode() + order;
    }
}
