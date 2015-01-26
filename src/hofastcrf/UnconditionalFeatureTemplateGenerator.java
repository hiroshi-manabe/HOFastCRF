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
 * A feature template generator that generates feature templates for any observations.
 * @author Hiroshi Manabe
 * @param <T> the class for the observations
 */
public class UnconditionalFeatureTemplateGenerator<T> extends FeatureTemplateGenerator<T> {
    
    private final int maxLabelLength;
    
    /**
     * Constructor
     * @param maxLabelLength
     */
    public UnconditionalFeatureTemplateGenerator(int maxLabelLength) {
        this.maxLabelLength = maxLabelLength;
    }
    
    @Override
    public List<FeatureTemplate> generateFeatureTemplatesAt(
            List<T> observationList, int pos) {
        List<FeatureTemplate> templateList = new ArrayList<FeatureTemplate>();
        for (int i = 1; i <= Math.min(maxLabelLength, pos + 1); ++i) {
            templateList.add(new FeatureTemplate("", i));
        }
        return templateList;
    }
}
