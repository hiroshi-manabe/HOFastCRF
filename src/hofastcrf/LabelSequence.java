package hofastcrf;

import java.util.Arrays;

public class LabelSequence implements Comparable<LabelSequence> {
    
    int[] labels;
    
    public LabelSequence(int[] labels) {
        this.labels = labels;
    }

    public int getOrder() {
        return labels.length;
    }
    
    public LabelSequence createPrefix() {
        return new LabelSequence(Arrays.copyOfRange(labels, 1, labels.length));
    }
    
    public int compareTo(LabelSequence that) {
        for (int i = 0; i < Math.min(this.labels.length, that.labels.length); ++i) {
            if (labels[i] != that.labels[i]) {
                return labels[i] - that.labels[i];
            }
        }
        if (this.labels.length != that.labels.length) {
            return labels.length - that.labels.length;
        }
        return 0;
    }
    
    public int getDifferencePosition(LabelSequence that) {
        int minLength = Math.min(this.labels.length, that.labels.length);
        for (int i = 0; i < minLength; ++i) {
            if (labels[i] != that.labels[i]) {
                return i;
            }
        }
        return minLength;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LabelSequence that = (LabelSequence)obj;
        return Arrays.equals(this.labels, that.labels);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(labels);
    }
    
    public static LabelSequence createEmptyLabelSequence() {
        return new LabelSequence(new int[0]);
    }
}
