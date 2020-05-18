package speakeridentification.model.deeplearning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import speakeridentification.model.exceptions.FileException;
import speakeridentification.model.exceptions.ModelStateException;

@Slf4j
public class NeuralNetworkHolder {

    public static final String featureExtractionLayer = "extractor";
    public static final String UNIDENTIFIED = "unidentified";
    public static final int trainPerc = 80;
    public static final int batchSize = 1;
    public static final int numEpochs = 3;

    private final String trainingDirectory;

    @Setter private MultiLayerNetwork pretrainedModel;
    @Getter @Setter private MultiLayerNetwork modelToUse;
    @Getter @Setter private List<String> labels;
    private HashMap<String, Iterator<File>> predictIterators;

    public NeuralNetworkHolder(String baseDirectory) {
        trainingDirectory = FilenameUtils.concat(FilenameUtils.concat(baseDirectory, "audio"),"train");
        predictIterators = new HashMap<>();
    }

    public MultiLayerNetwork train() {
        if (pretrainedModel == null) throw new ModelStateException("No model set"); // TODO
        log.info("Loaded pretrained model\n" + pretrainedModel.summary());
        int layerIndex = pretrainedModel.getLayer(featureExtractionLayer).getIndex();
        SpectrogramIterator.setup(batchSize,trainPerc, trainingDirectory);
        DataSetIterator trainIterator = SpectrogramIterator.trainIterator();
        DataSetIterator testIterator = SpectrogramIterator.testIterator();
        int numLabels = trainIterator.totalOutcomes();
        labels = trainIterator.getLabels();

            FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder()
            .updater(new Adam())
            .seed(123)
            .build();

        modelToUse = new TransferLearning.Builder(pretrainedModel)
            .fineTuneConfiguration(fineTuneConf)
            .setFeatureExtractor(layerIndex)
            .nOutReplace(layerIndex+1, numLabels, WeightInit.XAVIER)
            .build();
        log.info("Started training with transformed model\n" + modelToUse.summary());

        modelToUse.setListeners(new ScoreIterationListener(10), new EvaluativeListener(testIterator, 1, InvocationType.EPOCH_END));
        modelToUse.fit(trainIterator, numEpochs);
        log.info("Model training complete");
        return modelToUse;
    }

    public void clearAll() {
        pretrainedModel = null;
        modelToUse = null;
        labels = new ArrayList<>();
    }

    public String predict(File file, Double treshold) throws IOException {
        String result = UNIDENTIFIED;
        NativeImageLoader loader = new NativeImageLoader(SpectrogramIterator.height, SpectrogramIterator.width, SpectrogramIterator.channels);
        INDArray image = loader.asMatrix(file);
        INDArray output = modelToUse.output(image);
        if ((Double)output.maxNumber() >= treshold) {
            result = labels.get(output.argMax(1).getInt());
        }
        log.info("Predictions for sample of class " + FilenameUtils.getName(file.getParent()) + ":\n" +
            output.toString() + "\n" + labels.toString() + "\nTreshold: " + treshold + " Result: " + result);
        return result;
    }

    public String predictNext(String label, Double treshold) {
        try {
            File file = predictIterators.get(label).next();
            return predict(file, treshold);
        } catch (IOException e) {
            throw new FileException("Unable to open file", e);
        }
    }

    public void setupForPrediction() {
        predictIterators.clear();
        for (String label : labels) {
            String directoryName = FilenameUtils.concat(trainingDirectory, label);
            File directory = new File(directoryName);
            Iterator<File> iterator = FileUtils.iterateFiles(directory, new String[]{"png"}, false);
            predictIterators.put(label, iterator);
        }
    }
}
