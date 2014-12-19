package postagger.features;

import java.util.ArrayList;
import java.util.List;

import hofastcrf.FeatureTemplate;
import hofastcrf.FeatureTemplateGenerator;

public class UnconditionalFeatureTemplateGenerator extends FeatureTemplateGenerator<String> {
    private final int maxOrder;
    
    public UnconditionalFeatureTemplateGenerator(int maxOrder) {
        this.maxOrder = maxOrder;
    }
    
    @Override
    public List<FeatureTemplate> generateFeatureTemplatesAt(
            List<String> rawObjectList, int pos) {
        List<FeatureTemplate> templateList = new ArrayList<FeatureTemplate>();
        for (int i = 1; i <= Math.min(maxOrder, pos + 1); ++i) {
            templateList.add(new FeatureTemplate("LABEL", i));
        }
        return templateList;
    }
}
