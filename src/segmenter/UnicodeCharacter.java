package segmenter;

public class UnicodeCharacter {
    int codePoint;
    
    public UnicodeCharacter(int codePoint) {
        this.codePoint = codePoint;
    }

    public int getCodePoint() {
        return codePoint;
    }

    public void setCodePoint(int codePoint) {
        this.codePoint = codePoint;
    }
    
    public String getCharacterType() {
        return Character.UnicodeScript.of(codePoint).toString();
    }
}
