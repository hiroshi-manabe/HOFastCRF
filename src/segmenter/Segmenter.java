package segmenter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import segmenter.features.CharacterFeatureGenerator;
import segmenter.features.CharacterTypeFeatureGenerator;
import hofastcrf.AggregatedFeatureTemplateGenerator;
import hofastcrf.FeatureTemplateGenerator;
import hofastcrf.HighOrderFastCRF;
import hofastcrf.ObservationSequence;
import hofastcrf.Scorer;
import hofastcrf.UnconditionalFeatureTemplateGenerator;

public class Segmenter {

    HighOrderFastCRF<UnicodeCharacter> highOrderCrfModel; // High-order CRF model
    FeatureTemplateGenerator<UnicodeCharacter> featureGenerator;

    public Segmenter() {
        AggregatedFeatureTemplateGenerator<UnicodeCharacter> gen = new AggregatedFeatureTemplateGenerator<UnicodeCharacter>();
        gen.addFeatureTemplateGenerator(new UnconditionalFeatureTemplateGenerator<UnicodeCharacter>(1));
        gen.addFeatureTemplateGenerator(new CharacterFeatureGenerator(3, 3, 4));
        gen.addFeatureTemplateGenerator(new CharacterTypeFeatureGenerator(3, 3, 1));
        
        this.featureGenerator = gen;
    }

    public List<ObservationSequence<UnicodeCharacter>> readData(String filename, boolean hasValidLabels) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filename));

        List<ObservationSequence<UnicodeCharacter>> ObservationSequenceList = new ArrayList<ObservationSequence<UnicodeCharacter>>();
        String line;

        while ((line = in.readLine()) != null) {
            List<UnicodeCharacter> observationList = new ArrayList<UnicodeCharacter>();
            List<String> labelList = new ArrayList<String>();
            String[] elements = line.split(" ", -1);
            String prevIsSpace = "1";
            
            for (String element : elements) {
                int length = element.length();
                UnicodeCharacter uchar;
                for (int i = 0; i < length; i += Character.charCount(uchar.getCodePoint())) {
                    uchar = new UnicodeCharacter(element.codePointAt(i));
                    if (uchar.getCodePoint() == '/') {
                        break;
                    }
                    observationList.add(uchar);
                    labelList.add(prevIsSpace);
                    prevIsSpace = "0";
                }
                prevIsSpace = "1";
            }
            ObservationSequenceList.add(new ObservationSequence<UnicodeCharacter>(observationList, labelList, hasValidLabels));
        }

        in.close();
        return ObservationSequenceList;
    }
    
    public void train() throws IOException {
    
        // Set training file name and create output directory
        String trainFilename = "train_without_dev.full";
        
        // Read training data and save the label map
        List<ObservationSequence<UnicodeCharacter>> trainDataSequenceList = readData(trainFilename, true);
                
        // Train and save model
        highOrderCrfModel = new HighOrderFastCRF<UnicodeCharacter>(false);
        highOrderCrfModel.train(trainDataSequenceList, featureGenerator, 4, 1000, 1, false, 0.7, 0.001);
        
        highOrderCrfModel.write("learntModels/crfmodel");
    }

    public void test() throws IOException, ClassNotFoundException, InterruptedException {
        
        // Read the model
        HighOrderFastCRF<UnicodeCharacter> highOrderCRFModel = new HighOrderFastCRF<UnicodeCharacter>(true);
        highOrderCRFModel.read("learntModels/crfmodel");
        
        // Infer labels
        System.out.print("Inferring labels...");
        String testFilename = "dev.full";
        List<ObservationSequence<UnicodeCharacter>> testDataSequenceList = readData(testFilename, false);
        String[][] trueLabels = highOrderCRFModel.extractLabels(testDataSequenceList);
        
        long startTime = System.currentTimeMillis();
        String[][] predictedLabels = highOrderCRFModel.decode(testDataSequenceList, featureGenerator, 4);
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
        
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("decoded.txt")));
        for (int i = 0; i < testDataSequenceList.size(); ++i) {
            StringBuilder sb = new StringBuilder();
            List<UnicodeCharacter> seq = testDataSequenceList.get(i).getObservationList();
            for (int j = 0; j < seq.size(); ++j) {
                if (j > 0 && predictedLabels[i][j].equals("1")) {
                    sb.append(" ");
                }
                sb.appendCodePoint(seq.get(j).getCodePoint());
            }
            out.println(sb.toString());
        }
        out.close();
    }

    public static void main(String argv[]) throws Exception {
        Segmenter segmenter = new Segmenter();
        if (argv[0].toLowerCase().equals("all")) {
            segmenter.train();
            segmenter.test();
        } else if (argv[0].toLowerCase().equals("train")) {
            segmenter.train();
        } else if (argv[0].toLowerCase().equals("test")) {
            segmenter.test();
        }
    }
}
