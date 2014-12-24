/*
Copyright (C) 2012 Nguyen Viet Cuong, Ye Nan, Sumit Bhagwani

This file is part of HOSemiCRF.

HOSemiCRF is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

HOSemiCRF is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with HOSemiCRF. If not, see <http://www.gnu.org/licenses/>.
*/

package OCR.Features;

import hofastcrf.FeatureTemplate;
import hofastcrf.FeatureTemplateGenerator;

import java.util.ArrayList;
import java.util.List;

import OCR.CharDetails;

/**
 * Generates OCR features
 * @author Nguyen Viet Cuong
 */
public class OCRFeatureTemplateGenerator extends FeatureTemplateGenerator<CharDetails> {
    
	public List<FeatureTemplate> generateFeatureTemplatesAt(List<CharDetails> rawObservationList, int pos) {
        List<FeatureTemplate> obs = new ArrayList<FeatureTemplate>();
        CharDetails cd = rawObservationList.get(pos);
        for (int r = 0; r < CharDetails.ROWS; r++) {
            for (int c = 0; c < CharDetails.COLS; c++) {
                if (cd.getPixels(r, c) != 0) {
                    obs.add(new FeatureTemplate(r + "." + c, 1));
                }
            }
        }
        return obs;
    }

    public int order() {
        return 0;
    }
}
