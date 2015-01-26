package hofastcrf;

import java.util.HashMap;
import java.util.Map;

public class DebugInfoManager {
    private static final DebugInfoManager instance = new DebugInfoManager();
    private boolean isDebugging = false;
    Map<String, Object> data = new HashMap<String, Object>();
    
    private DebugInfoManager() {
    }
    
    public static DebugInfoManager getInstance() {
        return instance;
    }
    
    public boolean getDebugMode() {
        return isDebugging;
    }
    
    public void setDebugMode(boolean isDebugging) {
        this.isDebugging = isDebugging;
    }
    
    public void setDebugData(String key, Object value) {
        data.put(key,  value);
    }
    
    public Object getDebugData(String key) {
        return data.get(key);
    }
}
