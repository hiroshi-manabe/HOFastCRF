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

import hofastcrf.*;

import java.io.*;
import java.util.*;

import OCR.Features.*;

/**
 * Handwritten Character Recognition
 * @author Nguyen Viet Cuong
 */
public class OCR {

    int trainFold = 0;
    HighOrderFastCRF<CharDetails> highOrderCrfModel; // High-order CRF model
    String configFile; // Configuration filename
    FeatureTemplateGenerator<CharDetails> generator;

    public OCR(String filename, String fold) {
        configFile = filename;
        trainFold = Integer.parseInt(fold);
        AggregatedFeatureTemplateGenerator<CharDetails> gen = new AggregatedFeatureTemplateGenerator<CharDetails>();
        gen.addFeatureTemplateGenerator(new OCRFeatureTemplateGenerator());
        gen.addFeatureTemplateGenerator(new UnconditionalFeatureTemplateGenerator<>(3));
        generator = gen; 
    }

    public List<RawDataSequence<CharDetails>> readTagged(String filename, int trainFold, boolean isTraining) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filename));

        List<RawDataSequence<CharDetails>> td = new ArrayList<RawDataSequence<CharDetails>>();
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
                    td.add(new RawDataSequence<CharDetails>(inps, labels, true));
                    inps = new ArrayList<CharDetails>();
                    labels = new ArrayList<String>();
                }
            }
        }

        in.close();
        return td;
    }
    
    public void train() throws IOException {
    
        // Set training file name and create output directory
        String trainFilename = "letter.data";
        File dir = new File("learntModels/fold" + trainFold);
        dir.mkdirs();
        
        // Read training data and save the label map
        RawDataSet<CharDetails> trainData = new RawDataSet<CharDetails>(readTagged(trainFilename, trainFold, true));
        
        // Train and save model
        highOrderCrfModel = new HighOrderFastCRF<CharDetails>();
        highOrderCrfModel.train(trainData, generator, 3, 1000, 1, 1.0, 0.001);
        highOrderCrfModel.write("learntModels/fold" + trainFold + "/crfmodel");
    }

    @SuppressWarnings("unchecked")
    public void test() throws IOException, ClassNotFoundException, InterruptedException {
        
        // Read the model
        ObjectInput input = new ObjectInputStream(new BufferedInputStream(new FileInputStream("learntModels/fold" + trainFold + "/crfmodel")));
        highOrderCrfModel = (HighOrderFastCRF<CharDetails>)input.readObject();
        input.close();
        
        // Run Viterbi algorithm
        System.out.print("Running Viterbi...");
        String testFilename = "letter.data";
        List<RawDataSequence<CharDetails>> testData = readTagged(testFilename, trainFold, false);
        String[][] trueLabels = highOrderCrfModel.extractLabels(testData);
        
        long startTime = System.currentTimeMillis();
        String[][] predictedLabels = highOrderCrfModel.decode(testData, generator);
        System.out.println("done in " + (System.currentTimeMillis() - startTime) + " ms");

        // Print out the predicted data and score
        File dir = new File("out/");
        dir.mkdirs();

        // Score the result
        System.out.println("Scoring results...");
        startTime = System.currentTimeMillis();
        Scorer scr = new Scorer(trueLabels, predictedLabels, false);
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
