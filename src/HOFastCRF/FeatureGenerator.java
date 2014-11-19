/*
Copyright (C) 2012 Nguyen Viet Cuong, Ye Nan, Sumit Bhagwani

This file is part of HOSemiCRF.

HOSemiCRF is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

HOSemiCRF is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with HOSemiCRF. If not, see <http://www.gnu.org/licenses/>.
*/

package HOFastCRF;

import java.io.*;
import java.util.*;
import Parallel.*;

/**
 * Feature generator class
 * @author Nguyen Viet Cuong
 */
public class FeatureGenerator {

    List<FeatureType> featureTypes; // Feature types list
    int maxOrder; // Maximum order of the CRF
    Params params; // Parameters
    
    Map<String, Integer> obsMap; // Map from feature observation to its ID
    Map<String, Integer> patternMap; // Map from feature pattern to index
    Map<FeatureIndex, Integer> featureMap; // Map from FeatureIndex to its ID in lambda vector
    List<Feature> featureList; // Map from feature ID to features
    
    Map<String, Integer> forwardStateMap; // Map from forward state to index
    List<List<Integer>> forwardTransition1; // Map from piID to list of pkID (see paper)
    List<List<Integer>> forwardTransition2; // Map from piID to list of pkyID (see paper)
    
    Map<String, Integer> backwardStateMap; // Map from backward state to index
    int[][] backwardTransition; // Map from [siID,y] to skID (see paper)
    List<List<Integer>> allSuffixes; // Map from sID to its suffixes patID
    List<String> backwardStateList; // List of backward states
	
    List<List<Integer>> patternTransition1; // Map from z to piID (see paper)
    List<List<Integer>> patternTransition2; // Map from z to piyID (see paper)
	
    /**
     * Constructor a feature generator.
     * @param fts List of feature types
     * @param pr Parameters
     */
    public FeatureGenerator(List<FeatureType> fts, Params pr) {
        featureTypes = fts;
        maxOrder = getMaxOrder();
        params = pr;
    }
	
    /**
     * Initialize the feature generator with the training data.
     * This method needs to be called before the training process.
     * @param trainData List of training sequences
     */
    public void initialize(List<DataSequence> trainData) throws Exception {
        generateFeatureMap(trainData);
        generateForwardStatesMap();
        generateBackwardStatesMap();
        generateSentenceFeat(trainData);
        buildForwardTransition();
        buildBackwardTransition();
        buildPatternTransition();
    }
    
    /**
     * Write the feature generator to a file.
     * @param filename Name of the output file
     */
    public void write(String filename) throws Exception {
        PrintWriter out = new PrintWriter(new FileOutputStream(filename));
        
        // Write observation map
        out.println(obsMap.size());
        Iterator<String> iter = obsMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            out.println(key + " " + obsMap.get(key));
        }
        
        // Write pattern map
        out.println(patternMap.size());
        iter = patternMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            out.println(key + " " + patternMap.get(key));
        }
        
        // Write feature map
        out.println(featureMap.size());
        Iterator<FeatureIndex>feature_iter = featureMap.keySet().iterator();
        while (feature_iter.hasNext()) {
            FeatureIndex fi = feature_iter.next();
            int index = featureMap.get(fi);
            Feature f = featureList.get(index);
            out.println(f.obs + " " + f.pat + " " + f.value + " " + index);
        }
        
        // Write forward state map
        out.println(forwardStateMap.size());
        iter = forwardStateMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            if (!key.equals("")) {
                out.println(key + " " + forwardStateMap.get(key));
            }
        }
        
        // Write backward state map
        out.println(backwardStateMap.size());
        iter = backwardStateMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            out.println(key + " " + backwardStateMap.get(key));
        }

        out.close();
    }
	
    /**
     * Load the feature generator from a file.
     * @param filename Name of the file that contains the feature generator information
     */
    public void read(String filename) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        
        // Read observation map
        int mapSize = Integer.parseInt(in.readLine());
        obsMap = new HashMap<String, Integer>();
        for (int i = 0; i < mapSize; i++) {
            String line = in.readLine();
            StringTokenizer toks = new StringTokenizer(line);
            String key = toks.nextToken();
            int index = Integer.parseInt(toks.nextToken());
            obsMap.put(key, index);
        }
        
        // Read pattern map
        mapSize = Integer.parseInt(in.readLine());
        patternMap = new HashMap<String, Integer>();
        for (int i = 0; i < mapSize; i++) {
            String line = in.readLine();
            StringTokenizer toks = new StringTokenizer(line);
            String key = toks.nextToken();
            int index = Integer.parseInt(toks.nextToken());
            patternMap.put(key, index);
        }
        
        // Read feature map
        mapSize = Integer.parseInt(in.readLine());
        featureMap = new HashMap<FeatureIndex, Integer>();
        featureList = new ArrayList<Feature>(mapSize);
        for (int i = 0; i < mapSize; i++) featureList.add(null);
        for (int i = 0; i < mapSize; i++) {
            String line = in.readLine();
            StringTokenizer toks = new StringTokenizer(line);
            String obs = toks.nextToken();
            String pat = toks.nextToken();
            double value = Double.parseDouble(toks.nextToken());
            int index = Integer.parseInt(toks.nextToken());
            Feature f = new Feature(obs, pat, value);
            featureMap.put(getFeatureIndex(f), index);
            featureList.set(index, f);
        }
        
        // Read forward state map
        mapSize = Integer.parseInt(in.readLine());
        forwardStateMap = new HashMap<String, Integer>();
        forwardStateMap.put("", new Integer(0));
        for (int i = 0; i < mapSize-1; i++) {
            String line = in.readLine();
            StringTokenizer toks = new StringTokenizer(line);
            String key = toks.nextToken();
            int index = Integer.parseInt(toks.nextToken());
            forwardStateMap.put(key, index);
        }
        
        // Read backward state map
        mapSize = Integer.parseInt(in.readLine());
        backwardStateMap = new HashMap<String, Integer>();
        backwardStateList = new ArrayList<String>(mapSize);
        for (int i = 0; i < mapSize; i++) backwardStateList.add(null);
        for (int i = 0; i < mapSize; i++) {
            String line = in.readLine();
            StringTokenizer toks = new StringTokenizer(line);
            String key = toks.nextToken();
            int index = Integer.parseInt(toks.nextToken());
            backwardStateMap.put(key, index);
            backwardStateList.set(index, key);
        }

        buildForwardTransition();
        buildBackwardTransition();
        buildPatternTransition();

        in.close();
    }
	
    /**
     * Get the index of a feature.
     * @param f Feature
     * @return The feature index
     */
    public FeatureIndex getFeatureIndex(Feature f) {
        Integer obs = getObsIndex(f.obs);
        Integer pat = getPatternIndex(f.pat);
        if (obs == null || pat == null) {
            return null;
        } else {
            return new FeatureIndex(getObsIndex(f.obs), getPatternIndex(f.pat));
        }
    }
    
    /**
     * Get the index of an observation string.
     * @param obs Observation string
     * @return Observation index
     */
    public Integer getObsIndex(String obs) {
        return obsMap.get(obs);
    }

    /**
     * Get the index of a pattern string.
     * @param p Pattern string
     * @return Pattern index
     */
    public Integer getPatternIndex(String p) {
        return patternMap.get(p);
    }
    
    /**
     * Get the index of a forward state.
     * @param p Forward state
     * @return Index of the forward state
     */
    public Integer getForwardStateIndex(String p) {
        return forwardStateMap.get(p);
    }

    /**
     * Get the index of a backward state.
     * @param p Backward state
     * @return Index of the backward state
     */
    public Integer getBackwardStateIndex(String p) {
        return backwardStateMap.get(p);
    }

    /**
     * Get the maximum order of the CRF.
     * @return Maximum order of the CRF
     */
    public int getMaxOrder() {
        int res = -1;
        for (int i = 0; i < featureTypes.size(); i++) {
            if (res < featureTypes.get(i).order()) {
                res = featureTypes.get(i).order();
            }
        }
        return res;
    }
    
    /**
     * Generate the features for each training sequence.
     * @param trainData List of training sequences
     */
    public void generateSentenceFeat(List<DataSequence> trainData) throws Exception {
        SentenceFeatGenerator gen = new SentenceFeatGenerator(trainData, this);
        Scheduler sch = new Scheduler(gen, params.numthreads, Scheduler.DYNAMIC_NEXT_AVAILABLE);
        sch.run();
    }
    
    /**
     * Generate the observation map, pattern map, feature map, and feature list from training data.
     * @param trainData List of training sequences
     */
    public void generateFeatureMap(List<DataSequence> trainData) {
        obsMap = new HashMap<String, Integer>();
        patternMap = new HashMap<String, Integer>();
        featureMap = new HashMap<FeatureIndex, Integer>();
        featureList = new ArrayList<Feature>();
        for (int t = 0; t < trainData.size(); t++) {
            DataSequence seq = trainData.get(t);
            for (int pos = 0; pos < seq.length(); pos++) {
                String labelPat = generateLabelPattern(seq, pos);
                List<Feature> features = generateFeatures(seq, pos, labelPat);
                
                for (Feature f : features) {
                    Integer obs_index = getObsIndex(f.obs);
                    if (obs_index == null) {
                        obsMap.put(f.obs, obsMap.size());
                    }
                    
                    Integer pat_index = getPatternIndex(f.pat);
                    if (pat_index == null) {
                        patternMap.put(f.pat, patternMap.size());
                    }
					
                    FeatureIndex index = getFeatureIndex(f);
                    if (!featureMap.containsKey(index)) {
                        featureMap.put(index, featureMap.size());
                        featureList.add(f);
                    }
                }
            }
        }
        
        //System.out.println("Num pattern = " + patternMap.size());
    }
    
    /**
     * Generate the forward state map.
     */
    public void generateForwardStatesMap() {
        forwardStateMap = new HashMap<String, Integer>();
        forwardStateMap.put("", new Integer(0));
        for (int i = 0; i < params.numLabels; i++) {
            forwardStateMap.put("" + i, new Integer(forwardStateMap.size()));
        }
        Iterator<String> iter = patternMap.keySet().iterator();
        while (iter.hasNext()) {
            String labelPat = iter.next();
            List<String> pats = Utility.generateProperPrefixes(labelPat);
            for (String pat : pats) {
                if (getForwardStateIndex(pat) == null) {
                    forwardStateMap.put(pat, forwardStateMap.size());
                }
            }
        }
    }

    /**
     * Generate the backward state map and the backward state list.
     */
    public void generateBackwardStatesMap() {
        backwardStateMap = new HashMap<String, Integer>();
        backwardStateList = new ArrayList<String>();
        Iterator <String>iter = forwardStateMap.keySet().iterator();
        while (iter.hasNext()) {
            String p = iter.next();
            for (int y = 0; y < params.numLabels; y++) {
                String py = p.equals("") ? y + "" : y + "|" + p;
                if (getBackwardStateIndex(py) == null) {
                    backwardStateMap.put(py, backwardStateMap.size());
                    backwardStateList.add(py);
                }
            }
        }
    }

    /**
     * Generate the maximum posible pattern for a position.
     * Note that patterns are in reversed order: y(t)|y(t-1)|y(t-2)|...
     * @param seq Data sequence
     * @param pos Input position
     * @return Pattern string
     */
    public String generateLabelPattern(DataSequence seq, int pos) {
        String labelPat = "";
        for (int i = 0; i <= maxOrder && pos-i >= 0; i++) {
            labelPat = labelPat + "|" + seq.y(pos-i);
        }
        labelPat = labelPat.substring(1);
        return labelPat;
    }

    /**
     * Generate all features activated at a position with a given label pattern.
     * @param seq Data sequence
     * @param pos Input position
     * @param labelPat Label pattern
     * @return List of activated features
     */
    public List<Feature> generateFeatures(DataSequence seq, int pos, String labelPat) {
        List<Feature> features = new ArrayList<Feature>();
        List<String> suffixes = Utility.generateSuffixes(labelPat);
        for (String s : suffixes) {
            List<Feature> fi = generateFeaturesWithExactPattern(seq, pos, s);
            features.addAll(fi);
        }
        return features;
    }
    
    /**
     * Generate all features activated at a position with an exact label pattern.
     * @param seq Data sequence
     * @param pos Input position
     * @param labelPat Exact label pattern of the activated features
     * @return List of activated features
     */
    public List<Feature> generateFeaturesWithExactPattern(DataSequence seq, int pos, String labelPat) {
        List<Feature> features = new ArrayList<Feature>();
        for (FeatureType ft : featureTypes) {
            List<Feature> fi = ft.generateFeaturesAt(seq, pos, labelPat);
            features.addAll(fi);
        }
        return features;
    }
    
    /**
     * Generate all observations at a position.
     * @param seq Data sequence
     * @param pos Input position
     * @return List of observations
     */
    public List<String> generateObs(DataSequence seq, int pos) {
        List<String> obs = new ArrayList<String>();
        for (FeatureType ft : featureTypes) {
            obs.addAll(ft.generateObsAt(seq, pos));
        }
        return obs;
    }
    
    /**
     * Return the index of the longest suffix of a string.
     * @param p The input string
     * @param map Map from strings to indices
     * @return Index of the longest suffix of the input string from the input map.
     */
    public Integer getLongestSuffixID(String p, Map<String, Integer> map) {
        List<String> suffixes = Utility.generateSuffixes(p);
        for (int i = 0; i < suffixes.size(); i++) {
            Integer index = map.get(suffixes.get(i));
            if (index != null) {
                return index;
            }
        }
        throw new UnsupportedOperationException("No longest suffix index!\n");
    }

    /**
     * Return the longest suffix of a string.
     * @param p The input string
     * @param map Map from strings to indices
     * @return The longest suffix of the input string from the input map.
     */
    public String getLongestSuffix(String p, Map<String, Integer> map) {
        List<String> suffixes = Utility.generateSuffixes(p);
        for (int i = 0; i < suffixes.size(); i++) {
            Integer index = map.get(suffixes.get(i));
            if (index != null) {
                return suffixes.get(i);
            }
        }
        throw new UnsupportedOperationException("No longest suffix!\n");
    }	

    /**
     * Build the information for the forward algorithm.
     */
    public void buildForwardTransition() {
        forwardTransition1 = new ArrayList<List<Integer>>(forwardStateMap.size());
        forwardTransition2 = new ArrayList<List<Integer>>(forwardStateMap.size());
        for (int i = 0; i < forwardStateMap.size(); ++i) {
        	forwardTransition1.add(new ArrayList<Integer>());
        	forwardTransition2.add(new ArrayList<Integer>());
        }
        
        Iterator<String> iter = forwardStateMap.keySet().iterator();
        while (iter.hasNext()) {
            String pk = iter.next();
            int pkID = getForwardStateIndex(pk);
            
            for (int y = 0; y < params.numLabels; y++) {
                String pky = pk.equals("") ? y + "" : y + "|" + pk;
                Integer index = getLongestSuffixID(pky, forwardStateMap);
                forwardTransition1.get(index).add(pkID);
                forwardTransition2.get(index).add(getBackwardStateIndex(pky));
            }
        }
    }

    /**
     * Build the information for the backward algorithm.
     */
    public void buildBackwardTransition() {
        backwardTransition = new int[backwardStateMap.size()][params.numLabels];
        allSuffixes = new ArrayList<List<Integer>>(backwardStateMap.size());
        for (int i = 0; i < backwardStateMap.size(); ++i) {
        	allSuffixes.add(new ArrayList<Integer>());
        }
		
        Iterator<String> iter = backwardStateMap.keySet().iterator();
        while (iter.hasNext()) {
            String si = iter.next();
            int siID = getBackwardStateIndex(si);
            for (int y = 0; y < params.numLabels; y++) {
                String siy = y + "|" + si;
                String sk = getLongestSuffix(siy, backwardStateMap);
                backwardTransition[siID][y] = getBackwardStateIndex(sk);
            }
            
            List<String> suffixes = Utility.generateSuffixes(si);
            for (String suffix : suffixes) {
                Integer patID = getPatternIndex(suffix);
                if (patID != null) {
                    allSuffixes.get(siID).add(patID);
                }
            }
        }
    }

    /**
     * Build the information to compute the marginals and expected feature scores.
     */
    public void buildPatternTransition() {
        patternTransition1 = new ArrayList<List<Integer>>(patternMap.size());
        patternTransition2 = new ArrayList<List<Integer>>(patternMap.size());
        for (int i = 0; i < patternMap.size(); ++i) {
        	patternTransition1.add(new ArrayList<Integer>());
        	patternTransition2.add(new ArrayList<Integer>());
        }
		
        Iterator<String> forwardIter = forwardStateMap.keySet().iterator();
        while (forwardIter.hasNext()) {
            String pi = forwardIter.next();
            int piID = getForwardStateIndex(pi);
            for (int y = 0; y < params.numLabels; y++) {
                String piy = pi.equals("") ? y + "" : y + "|" + pi;
                Integer piyID = getBackwardStateIndex(piy);
                List<String> suffixes = Utility.generateSuffixes(piy);
                for (String zi : suffixes) {
                    Integer ziIndex = getPatternIndex(zi);
                    if (ziIndex != null) {
                        patternTransition1.get(ziIndex).add(piID);
                        patternTransition2.get(ziIndex).add(piyID);
                    }
                }
            }
        }
    }
    
    /**
     * Get the IDs of the features activated at a position.
     * @param seq Data sequence
     * @param pos Input position
     * @param patID Pattern ID
     * @return List of feature IDs
     */
    public List<Integer> getFeatures(DataSequence seq, int pos, int patID) {
        return seq.getFeatures(pos, patID);
    }
    
    /**
     * Get the IDs of a list of features.
     * @param fs List of features
     * @return List of feature IDs
     */
    public List<Integer> getFeatureID(List<Feature> fs) {
        List<Integer> feats = new ArrayList<Integer>();
        for (Feature f : fs) {
            Integer feat = featureMap.get(getFeatureIndex(f));
            if (feat != null) {
                feats.add(feat);
            }
        }
        return feats;
    }
    
    /**
     * Compute the feature scores of a list of features and a weight vector.
     * @param feats List of feature IDs
     * @param lambda Weights of all the features
     * @return The total feature score
     */
    public double computeFeatureScores(List<Integer> feats, double[] lambda) {
        double featuresScore = 0.0;
        for (int index : feats) {
            Feature feat = featureList.get(index);
            featuresScore += lambda[index] * feat.value;
        }
        return featuresScore;
    }
    
    /**
     * Print all statistics for testing.
     */
    public void printStatesStatistics() {
        System.out.println("Forward Transition:");
        for (int piID = 0; piID < forwardStateMap.size(); piID++) {
            System.out.println(piID);
            for (int i = 0; i < forwardTransition1.get(piID).size(); i++) {
            	System.out.println(forwardTransition1.get(piID).get(i) + " " + forwardTransition2.get(piID).get(i));
            }
        }
        
        System.out.println("Backward Transition:");
        for (int sID = 0; sID < backwardStateMap.size(); sID++) {
            for (int y = 0; y < params.numLabels; y++) {
                System.out.println(sID + " " + y + " --> " + backwardTransition[sID][y]);
            }
        }

        System.out.println("Pattern Transition:");
        for (int pID = 0; pID < patternMap.size(); pID++) {
            System.out.println(pID);
            if (patternTransition1.get(pID) != null) {
                for (int i = 0; i < patternTransition1.get(pID).size(); i++) {
                    System.out.println(patternTransition1.get(pID).get(i) + " " + patternTransition2.get(pID).get(i));
                }
            }
        }
    }
}
