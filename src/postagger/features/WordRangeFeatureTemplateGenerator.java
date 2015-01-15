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

package postagger.features;

import java.util.ArrayList;
import java.util.List;

import hofastcrf.FeatureTemplate;
import hofastcrf.FeatureTemplateGenerator;

public class WordRangeFeatureTemplateGenerator extends
        FeatureTemplateGenerator<String> {
    private final int maxOrder;
    private final int startPos;
    private final int endPos;
    private final String tag;
    
    public WordRangeFeatureTemplateGenerator(int startPos, int endPos, int maxOrder) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.maxOrder = maxOrder;
        StringBuilder b = new StringBuilder("W");
        for (int i = startPos; i <= endPos; ++i) {
            b.append(String.format("%+d", i));
        }
        b.append("_");
        tag = b.toString();
    }
    
    @Override
    public List<FeatureTemplate> generateFeatureTemplatesAt(
            List<String> rawObjectList, int pos) {
        StringBuilder b = new StringBuilder(tag);
        int len = rawObjectList.size();
        for (int i = startPos; i <= endPos; ++i) {
            int curPos = pos + i;
            b.append((curPos >= 0 && curPos < len) ? rawObjectList.get(curPos) : "");
            if (i < endPos) {
                b.append("_");
            }
        }
        String attr = b.toString();
        
        List<FeatureTemplate> ret = new ArrayList<FeatureTemplate>();
        for (int order = 1; order <= maxOrder; ++order) {
            ret.add(new FeatureTemplate(attr, order));
        }
        return ret;
    }
}
