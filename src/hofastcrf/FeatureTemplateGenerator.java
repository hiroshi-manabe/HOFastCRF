package hofastcrf;

import java.util.List;

public abstract class FeatureTemplateGenerator<T> {
    public abstract List<FeatureTemplate> generateFeatureTemplatesAt(List<T> rawObjectList, int pos);
}
