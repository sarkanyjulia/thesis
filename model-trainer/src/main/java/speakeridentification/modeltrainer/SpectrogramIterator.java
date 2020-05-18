package speakeridentification.modeltrainer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.BaseImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.slf4j.Logger;

public class SpectrogramIterator {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(SpectrogramIterator.class);

    private static final String [] allowedExtensions = BaseImageLoader.ALLOWED_FORMATS;
    private static final Random rng  = new Random(13);

    private static final int height = 513;
    private static final int width = 800;
    private static final int channels = 3;

    private static ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
    private static InputSplit trainData,testData;
    private static int batchSize;
    private static int numClasses;

    public static DataSetIterator trainIterator() {
        return makeIterator(trainData);
    }

    public static DataSetIterator testIterator() {
        return makeIterator(testData);
    }

    public static int setup(int batchSizeArg, int trainPerc, String parentDirPath) {
        batchSize = batchSizeArg;
        File parentDir = new File(parentDirPath);
        numClasses = countSubdirectories(parentDir);
        log.info("Number of classes set to " + numClasses);
        FileSplit filesInDir = new FileSplit(parentDir, allowedExtensions, rng);
        BalancedPathFilter pathFilter = new BalancedPathFilter(rng, allowedExtensions, labelMaker);
        if (trainPerc >= 100) {
            throw new IllegalArgumentException("Percentage of data set aside for training has to be less than 100%. Test percentage = 100 - training percentage, has to be greater than 0");
        }
        InputSplit[] filesInDirSplit = filesInDir.sample(pathFilter, trainPerc, 100-trainPerc);
        trainData = filesInDirSplit[0];
        testData = filesInDirSplit[1];
        return numClasses;
    }

    private static int countSubdirectories(File parentDir) {
        File[] directories = parentDir.listFiles();
        long count = Arrays.stream(Objects.requireNonNull(directories)).filter(File::isDirectory).count();
        return (int) count;
    }

    private static DataSetIterator makeIterator(InputSplit split) {
        ImageRecordReader recordReader = new ImageRecordReader(height,width,channels,labelMaker);
        try {
            recordReader.initialize(split);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read training data.", e);
        }
        return new RecordReaderDataSetIterator(recordReader, batchSize, 1, numClasses);
    }
}
