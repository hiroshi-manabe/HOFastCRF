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

import java.util.ArrayList;
import java.util.List;

/**
 * The class to aggregate feature template generators.
 * An aggregated feature template generator can be used as a feature template generator itself.
 * 
 * @author Hiroshi Manabe
 *
 * @param <T> feature template generator type
 */
public class AggregatedFeatureTemplateGenerator<T> extends
        FeatureTemplateGenerator<T> {
    
    private List<FeatureTemplateGenerator<T>> generatorList = new ArrayList<FeatureTemplateGenerator<T>>();
    
    @Override
    public List<FeatureTemplate> generateFeatureTemplatesAt(
            List<T> rawObjectList, int pos) {
        List<FeatureTemplate> templateList = new ArrayList<FeatureTemplate>();
        for (FeatureTemplateGenerator<T> generator : generatorList) {
            templateList.addAll(generator.generateFeatureTemplatesAt(rawObjectList, pos));
        }
        return templateList;
    }
    
    /**
     * Adds the argument to the generator list.
     * @param generator
     */
    public void addFeatureTemplateGenerator(FeatureTemplateGenerator<T> generator) {
        generatorList.add(generator);
    }
}
