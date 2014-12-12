package HOFastCRF;

import java.util.List;

public abstract class FeatureTemplateGenerator {

    public abstract List<FeatureTemplate> generateFeatureTemplatesAt(List<Object> rawObjectList, int pos);
}
