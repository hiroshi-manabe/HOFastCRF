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

import java.util.List;

/**
 * The class for the feature template generators.
 * @author Hiroshi Manabe
 * @param <T> the class for the observations.
 */
public abstract class FeatureTemplateGenerator<T> {
    /**
     * Generates feature templates for a specific position.
     * @param observationList the whole list of the observations
     * @param pos the position
     * @return
     */
    public abstract List<FeatureTemplate> generateFeatureTemplatesAt(List<T> observationList, int pos);
}
