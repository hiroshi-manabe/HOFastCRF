package postagger;

import hofastcrf.*;

import java.io.*;
import java.util.*;

import postagger.features.PrefixSuffixFeatureTemplateGenerator;
import postagger.features.UnconditionalFeatureTemplateGenerator;
import postagger.features.WordRangeFeatureTemplateGenerator;

public class PosTagger {

    HighOrderFastCRF<String> highOrderCrfModel; // High-order CRF model

    public PosTagger() {
    }

    public RawDataSet<String> readData(String filename, boolean hasValidLabels) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filename));

        List<RawDataSequence<String>> rawDataSequenceList = new ArrayList<RawDataSequence<String>>();
        List<String> observationList = new ArrayList<String>();
        List<String> labelList = new ArrayList<String>();
        String line;

        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) {
                RawDataSequence<String> rawDataSequence = new RawDataSequence<String>(observationList, labelList, hasValidLabels);
                rawDataSequenceList.add(rawDataSequence);
                observationList = new ArrayList<String>();
                labelList = new ArrayList<String>();
                continue;
            }
            String[] elements = line.split("\t", -1);
            if (elements.length < 2) {
                continue;
            }
            labelList.add(elements[0]);
            observationList.add(elements[1]);
        }

        in.close();
        return new RawDataSet<String>(rawDataSequenceList);
    }
    
    public void train() throws Exception {
    
        // Set training file name and create output directory
        String trainFilename = "train.txt";
        
        // Read training data and save the label map
        RawDataSet<String> trainData = readData(trainFilename, true);
        
        AggregatedFeatureTemplateGenerator<String> gen = new AggregatedFeatureTemplateGenerator<String>();
        
        gen.addFeatureTemplateGenerator(new UnconditionalFeatureTemplateGenerator(2));
        gen.addFeatureTemplateGenerator(new WordRangeFeatureTemplateGenerator(0, 0, 2));
        gen.addFeatureTemplateGenerator(new WordRangeFeatureTemplateGenerator(-1, -1, 3));
        gen.addFeatureTemplateGenerator(new WordRangeFeatureTemplateGenerator(+1, +1, 1));
        gen.addFeatureTemplateGenerator(new PrefixSuffixFeatureTemplateGenerator(true, 1, 2));
        gen.addFeatureTemplateGenerator(new PrefixSuffixFeatureTemplateGenerator(false, 1, 2));
        
        // Train and save model
        highOrderCrfModel = new HighOrderFastCRF<String>(gen);
        highOrderCrfModel.train(trainData, 3, 1000, 1, 1.0, 0.001);
    }

    public void test() throws Exception {
        // Read label map, features, and CRF model
//        labelmap.read("learntModels/fold" + trainFold + "/labelmap");
//        highOrderCrfModel = new HighOrderFastCRF(featureGen);
//        highOrderCrfModel.read("learntModels/fold" + trainFold + "/crf");

        // Run Viterbi algorithm
//        System.out.print("Running Viterbi...");
//        String testFilename = "letter.data";
//        long startTime = System.currentTimeMillis();
//        System.out.println("done in " + (System.currentTimeMillis() - startTime) + " ms");
//
//        // Print out the predicted data and score
//        File dir = new File("out/");
//        dir.mkdirs();
//
//        // Score the result
//        System.out.println("Scoring results...");
//        startTime = System.currentTimeMillis();
//        DataSet trueTestData = readTagged(testFilename, trainFold, false);
//        Scorer scr = new Scorer(trueTestData.getSeqList(), testData.getSeqList(), labelmap, false);
//        scr.tokenScore();
//        System.out.println("done in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public static void main(String argv[]) throws Exception {
        PosTagger posTagger = new PosTagger();
        if (argv[0].toLowerCase().equals("all")) {
            posTagger.train();
            posTagger.test();
        } else if (argv[0].toLowerCase().equals("train")) {
            posTagger.train();
        } else if (argv[0].toLowerCase().equals("test")) {
            posTagger.test();
        }
    }
}
