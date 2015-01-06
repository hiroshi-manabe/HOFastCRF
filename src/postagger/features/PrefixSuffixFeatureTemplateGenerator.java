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

public class PrefixSuffixFeatureTemplateGenerator extends
        FeatureTemplateGenerator<String> {
    private final boolean isPrefix;
    private final int minLength;
    private final int maxLength;
    private final String tag;
    
    public PrefixSuffixFeatureTemplateGenerator(boolean isPrefix, int minLength, int maxLength) {
        this.isPrefix = isPrefix;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.tag = isPrefix ? "PRE" : "SUF";
    }
    
    @Override
    public List<FeatureTemplate> generateFeatureTemplatesAt(List<String> rawObjectList, int pos) {
        List<FeatureTemplate> templateList = new ArrayList<FeatureTemplate>();
        
        String curString = rawObjectList.get(pos);
        int strLen = curString.length();
        for (int affixLen = minLength; affixLen <= maxLength; ++affixLen) {
            if (affixLen > strLen) {
                continue;
            }
            int startPos = isPrefix ? 0 : strLen - affixLen;
            int endPos = !isPrefix ? strLen : affixLen;
            String affix = curString.substring(startPos, endPos);
            templateList.add(new FeatureTemplate(tag + affixLen + "_" + affix, 1));
        }
        return templateList;
    }
}
