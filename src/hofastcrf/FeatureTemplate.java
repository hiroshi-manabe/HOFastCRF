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

/**
 * The feature template class.
 * Feature templates have only the order and don't have the concrete labels.
 * @author Hiroshi Manabe
 */
public class FeatureTemplate {
    
    String obs;
    int order;
    
    /**
     * Constructor.
     * @param obs observation
     * @param order
     */
    public FeatureTemplate(String obs, int order) {
        this.obs = obs;
        this.order = order;
    }
    
    /**
     * Generates a feature by applying a label sequence.
     * @param seq the label sequence
     * @return
     */
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
