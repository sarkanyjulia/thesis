package speakeridentification.modeltrainer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.CheckpointListener;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.FileStatsStorage;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.slf4j.Logger;

public class Pretrainer {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Pretrainer.class);

    private static final int trainPerc = 80;

    private final String dataDirectoryPath;
    private final String saveDirectoryPath;
    private final String savedModelPath;

    public Pretrainer(String dataDirectoryPath, String saveDirectoryPath, String savedModelPath) {
        this.dataDirectoryPath = dataDirectoryPath;
        this.saveDirectoryPath = saveDirectoryPath;
        this.savedModelPath = savedModelPath;
    }

    public void train(int numEpochs, int batchSize) {
        log.info("STARTED TRAINING");

        SpectrogramIterator.setup(batchSize,trainPerc, dataDirectoryPath);
        DataSetIterator trainIterator = SpectrogramIterator.trainIterator();
        DataSetIterator testIterator = SpectrogramIterator.testIterator();
        int numLabels = trainIterator.totalOutcomes();

        MultiLayerNetwork model = getModel(numLabels);

        File saveDirectory = new File(saveDirectoryPath);
        saveDirectory.mkdir();

        UIServer uiServer = UIServer.getInstance();
        String saveStatsPath = FilenameUtils.concat(saveDirectoryPath,"ui-stats.dl4j");
        StatsStorage statsStorage = new FileStatsStorage(new File(saveStatsPath));
        uiServer.attach(statsStorage);

        model.setListeners(new StatsListener(statsStorage),
            new ScoreIterationListener(50),
            new EvaluativeListener(testIterator, 1, InvocationType.EPOCH_END),
            new CheckpointListener.Builder(saveDirectory).keepAll().saveEveryEpoch().build());

        model.fit(trainIterator, numEpochs);

        log.info("FINISHED TRAINING");
        System.exit(0);
    }

    private MultiLayerNetwork getModel(int numLabels) {
        if (savedModelPath == null) {
            return SpectrogramNet.getModel(numLabels);
        }
        else {
            try {
                return MultiLayerNetwork.load(new File(savedModelPath), true);
            } catch (IOException e) {
                throw new RuntimeException("Unable to load model.", e);
            }
        }
    }
}
