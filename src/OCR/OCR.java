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

package OCR;

import java.io.*;
import java.util.*;
import HOFastCRF.*;
import OCR.Features.*;

/**
 * Handwritten Character Recognition
 * @author Nguyen Viet Cuong
 */
public class OCR {

    int trainFold = 0;
    HighOrderFastCRF highOrderCrfModel; // High-order CRF model
    OCRFeatureTemplateGenerator featureGen; // Feature generator
    LabelMap labelmap = new LabelMap(); // Label map
    String configFile; // Configuration filename

    public OCR(String filename, String fold) {
        configFile = filename;
        trainFold = Integer.parseInt(fold);
    }

    public DataSet readTagged(String filename, int trainFold, boolean isTraining) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(filename));

        List<DataSequence> td = new ArrayList<DataSequence>();
        List<CharDetails> inps = new ArrayList<CharDetails>();
        List<String> labels = new ArrayList<String>();
        String line;

        while ((line = in.readLine()) != null) {
            if (line.length() > 0) {
                StringTokenizer toks = new StringTokenizer(line);
                
                toks.nextToken();
                String tagChar = toks.nextToken();
                int nextID = Integer.parseInt(toks.nextToken());
                toks.nextToken();
                toks.nextToken();
                int fold = Integer.parseInt(toks.nextToken());
                
                int[][] p = new int[CharDetails.ROWS][CharDetails.COLS];
                for (int r = 0; r < CharDetails.ROWS; r++) {
                    for (int c = 0; c < CharDetails.COLS; c++) {
                        p[r][c] = Integer.parseInt(toks.nextToken());
                    }
                }

                if (isTraining && fold == trainFold) {
                    inps.add(new CharDetails(p));
                    labels.add(tagChar);
                } else if (!isTraining && fold != trainFold) {
                    inps.add(new CharDetails(p));
                    labels.add(tagChar);
                }

                if (nextID == -1 && labels.size() > 0) {
                    td.add(new DataSequence(labelmap.mapList(labels), inps.toArray(), labelmap));
                    inps = new ArrayList<CharDetails>();
                    labels = new ArrayList<String>();
                }
            }
        }

        in.close();
        return new DataSet(td);
    }
    
    public void train() throws Exception {
    
        // Set training file name and create output directory
        String trainFilename = "letter.data";
        File dir = new File("learntModels/fold" + trainFold);
        dir.mkdirs();
        
        // Read training data and save the label map
        DataSet trainData = readTagged(trainFilename, trainFold, true);
        labelmap.write("learntModels/fold" + trainFold + "/labelmap");
        
        // Train and save model
        highOrderCrfModel = new HighOrderFastCRF(featureGen);
        highOrderCrfModel.train(trainData.getSeqList());
        highOrderCrfModel.write("learntModels/fold" + trainFold + "/crf");
    }

    public void test() throws Exception {
        // Read label map, features, and CRF model
        labelmap.read("learntModels/fold" + trainFold + "/labelmap");
        createFeatureGenerator();
        featureGen.read("learntModels/fold" + trainFold + "/features");
        highOrderCrfModel = new HighOrderFastCRF(featureGen);
        highOrderCrfModel.read("learntModels/fold" + trainFold + "/crf");

        // Run Viterbi algorithm
        System.out.print("Running Viterbi...");
        String testFilename = "letter.data";
        DataSet testData = readTagged(testFilename, trainFold, false);
        long startTime = System.currentTimeMillis();
        highOrderCrfModel.runViterbi(testData.getSeqList());
        System.out.println("done in " + (System.currentTimeMillis() - startTime) + " ms");

        // Print out the predicted data and score
        File dir = new File("out/");
        dir.mkdirs();
        testData.writeToFile("out/letter" + trainFold + ".out");

        // Score the result
        System.out.println("Scoring results...");
        startTime = System.currentTimeMillis();
        DataSet trueTestData = readTagged(testFilename, trainFold, false);
        Scorer scr = new Scorer(trueTestData.getSeqList(), testData.getSeqList(), labelmap, false);
        scr.tokenScore();
        System.out.println("done in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public static void main(String argv[]) throws Exception {
        OCR ocr = new OCR(argv[1], argv[2]);
        if (argv[0].toLowerCase().equals("all")) {
            ocr.train();
            ocr.test();
        } else if (argv[0].toLowerCase().equals("train")) {
            ocr.train();
        } else if (argv[0].toLowerCase().equals("test")) {
            ocr.test();
        }
    }
}
