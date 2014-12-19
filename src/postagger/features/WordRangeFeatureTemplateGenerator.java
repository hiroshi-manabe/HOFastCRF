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
    
    public WordRangeFeatureTemplateGenerator(int maxOrder, int startPos, int endPos) {
        this.maxOrder = maxOrder;
        this.startPos = startPos;
        this.endPos = endPos;
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
        //FIXME
        if (pos == 0) {
            return new ArrayList<FeatureTemplate>();
        }
        
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
