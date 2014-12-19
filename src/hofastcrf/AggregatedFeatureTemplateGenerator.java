package hofastcrf;

import java.util.ArrayList;
import java.util.List;

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
    
    public void addFeatureTemplateGenerator(FeatureTemplateGenerator<T> generator) {
        generatorList.add(generator);
    }
}
