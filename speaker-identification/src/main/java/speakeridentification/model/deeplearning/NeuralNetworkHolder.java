package speakeridentification.model.deeplearning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import speakeridentification.model.exceptions.FileException;
import speakeridentification.model.exceptions.ModelStateException;

@Slf4j
public class NeuralNetworkHolder {

    public static final String featureExtractionLayer = "extractor";
    public static final String UNCERTAIN = "uncertain";
    public static final int trainPerc = 80;
    public static final int batchSize = 1;
    public static final int numEpochs = 3;

    private final String trainingDirectory;
    private final String predictDirectory;

    @Setter private MultiLayerNetwork pretrainedModel;
    @Getter @Setter private MultiLayerNetwork modelToUse;
    @Getter @Setter private List<String> labels;
    @Getter private HashMap<String, String> profilesMap;
    private HashMap<String, Iterator<File>> predictIterators;

    public NeuralNetworkHolder(String baseDirectory) {
        trainingDirectory = FilenameUtils.concat(FilenameUtils.concat(baseDirectory, "audio"),"train");
        predictDirectory = FilenameUtils.concat(FilenameUtils.concat(baseDirectory, "audio"),"predict");
        labels = new ArrayList<>();
        predictIterators = new HashMap<>();
        profilesMap = new HashMap<>();
    }

    private void setupForTraining(MultiLayerNetwork pretrainedModel, HashMap<String, String> profilesMap) {
        if (pretrainedModel == null) throw new ModelStateException("Unable to find pretrained model");
        this.pretrainedModel = pretrainedModel;
        this.profilesMap = profilesMap;
        log.info("Loaded pretrained model\n" + pretrainedModel.summary());
    }

    public MultiLayerNetwork train(MultiLayerNetwork pretrainedModel, HashMap<String, String> profilesMap) {
        setupForTraining(pretrainedModel, profilesMap);
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
            .addLayer(
                new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                    .name("output")
                    .nIn(819200)
                    .nOut(numLabels)
                    .activation(Activation.SOFTMAX)
                    .weightInit(WeightInit.XAVIER)
                    .build()
            )
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
        labels.clear();
        predictIterators.clear();
        profilesMap.clear();
    }

    public void setupForPrediction() {
        if (modelToUse == null) throw new ModelStateException("Unable to find trained model");
        predictIterators.clear();
        for (String label : labels) {
            String directoryName = FilenameUtils.concat(predictDirectory, label);
            File directory = new File(directoryName);
            Iterator<File> iterator = FileUtils.iterateFiles(directory, new String[]{"png"}, false);
            predictIterators.put(label, iterator);
        }
    }

    public String predictNext(String label, Double treshold) {
        try {
            File file = predictIterators.get(label).next();
            return predict(file, treshold);
        } catch (IOException e) {
            throw new FileException("Unable to open file", e);
        }
    }

    public String predict(File file, Double treshold) throws IOException {
        String result = UNCERTAIN;
        NativeImageLoader loader = new NativeImageLoader(SpectrogramIterator.height, SpectrogramIterator.width, SpectrogramIterator.channels);
        INDArray image = loader.asMatrix(file);
        INDArray output = modelToUse.output(image);
        if ((Double)output.maxNumber() >= treshold) {
            String label = labels.get(output.argMax(1).getInt());
            result = profilesMap.get(label);
        }
        log.info("Predictions for sample of class " + FilenameUtils.getName(file.getParent()) + ":\n" +
            output.toString() + "\n" + labels.toString() + "\nTreshold: " + treshold + " Result: " + result);
        return result;
    }

    public void setupWithLastSave(MultiLayerNetwork lastUsed, List<String> labels, HashMap<String, String> profilesMap) {
        modelToUse = lastUsed;
        this.labels = labels;
        this.profilesMap = profilesMap;
    }

}
