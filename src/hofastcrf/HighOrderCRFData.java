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

package hofastcrf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class for the serialization of the high-order CRF model data.
 * @author Hiroshi Manabe
 */

public class HighOrderCRFData implements Serializable {
    
    private static final long serialVersionUID = 100L;
    
    private final List<Feature> featureList;
    private final Map<String, Integer> labelMap;
    
    /**
     * Constructor.
     * @param featureList
     * @param labelMap
     */
    public HighOrderCRFData(List<Feature> featureList, Map<String, Integer> labelMap) {
        this.featureList = featureList;
        this.labelMap = labelMap;
    }

    public List<Feature> getFeatureList() {
        return featureList;
    }
    
    public Map<String, Integer> getLabelMap() {
        return labelMap;
    }
    
    public Map<Integer, String> getReversedLabelMap() {
        Map<Integer, String> reversedMap = new HashMap<Integer, String>();
        
        for (Map.Entry<String, Integer> entry : labelMap.entrySet()) {
            reversedMap.put(entry.getValue(), entry.getKey());
        }
        return reversedMap;
    }
    
    public void DumpFeatures(String filename) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
        Map<Integer, String> reversedMap = getReversedLabelMap();
        
        for (Feature feature : featureList) {
            out.print(feature.expWeight);
            out.print("\t");
            out.print(feature.obs);
            for (int label : feature.pat.labels) {
                out.print("\t" + reversedMap.get(label));
            }
            out.println();
        }
        out.close();
        
    }
}
