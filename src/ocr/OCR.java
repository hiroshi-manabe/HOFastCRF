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

package ocr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ocr.features.OCRFeatureTemplateGenerator;
import hofastcrf.AggregatedFeatureTemplateGenerator;
import hofastcrf.FeatureTemplateGenerator;
import hofastcrf.HighOrderFastCRF;
import hofastcrf.ObservationSequence;
import hofastcrf.Scorer;
import hofastcrf.UnconditionalFeatureTemplateGenerator;

/**
 * Handwritten Character Recognition
 * @author Hiroshi Manabe
 * @author Nguyen Viet Cuong
 */
public class OCR {

    int trainFold = 0;
    FeatureTemplateGenerator<CharDetails> generator;

    public OCR(String filename, String fold) {
        trainFold = Integer.parseInt(fold);
        AggregatedFeatureTemplateGenerator<CharDetails> gen = new AggregatedFeatureTemplateGenerator<CharDetails>();
        gen.addFeatureTemplateGenerator(new OCRFeatureTemplateGenerator());
        gen.addFeatureTemplateGenerator(new UnconditionalFeatureTemplateGenerator<CharDetails>(4));
        generator = gen; 
    }

    public List<ObservationSequence<CharDetails>> readTagged(String filename, int trainFold, boolean isTraining) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filename));

        List<ObservationSequence<CharDetails>> td = new ArrayList<ObservationSequence<CharDetails>>();
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
                    td.add(new ObservationSequence<CharDetails>(inps, labels, true));
                    inps = new ArrayList<CharDetails>();
                    labels = new ArrayList<String>();
                }
            }
        }

        in.close();
        return td;
    }
    
    public void train() throws IOException {
        HighOrderFastCRF<CharDetails> highOrderCrfModel; // High-order CRF model
        
        // Set training file name and create output directory
        String trainFilename = "letter.data";
        File dir = new File("learntModels/fold" + trainFold);
        dir.mkdirs();
        
        // Read training data and save the label map
        List<ObservationSequence<CharDetails>> trainDataSequenceList = readTagged(trainFilename, trainFold, true);
        
        // Train and save model
        highOrderCrfModel = new HighOrderFastCRF<CharDetails>(false);
        highOrderCrfModel.train(trainDataSequenceList, generator, 4, 1000, 4, true, 1.0, 0.001);
        highOrderCrfModel.write("learntModels/fold" + trainFold + "/crfmodel");
    }

    public void test() throws IOException, ClassNotFoundException, InterruptedException {
        // Read the model
        HighOrderFastCRF<CharDetails> highOrderCRFModel = new HighOrderFastCRF<CharDetails>(false);
        highOrderCRFModel.read("learntModels/fold" + trainFold + "/crfmodel");
        
        // Infer labels
        System.out.print("Inferring labels...");
        String testFilename = "letter.data";
        List<ObservationSequence<CharDetails>> testData = readTagged(testFilename, trainFold, false);
        String[][] trueLabels = highOrderCRFModel.extractLabels(testData);
        
        long startTime = System.currentTimeMillis();
        String[][] predictedLabels = highOrderCRFModel.decode(testData, generator, 4);
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

    public static void main(String argv[]) throws IOException, ClassNotFoundException, InterruptedException {
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
