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
