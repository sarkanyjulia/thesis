package speakeridentification.modeltrainer;

import java.io.File;
import java.io.IOException;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.nd4j.linalg.learning.config.Adam;
import org.slf4j.Logger;

public class Transformer {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Transformer.class);

    public static void transform(String from, String to) throws IOException {

        File fileFrom = new File(from);
        File fileTo = new File(to);

        MultiLayerNetwork modelToTransform = MultiLayerNetwork.load(fileFrom, false);
        log.info("Loaded model\n" + modelToTransform.summary());

        FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder()
            .updater(new Adam())
            .seed(123)
            .build();

        MultiLayerNetwork newModel = new TransferLearning.Builder(modelToTransform)
            .fineTuneConfiguration(fineTuneConf)
            .removeOutputLayer()
            .build();
        log.info("Transformed model\n" + newModel.summary());

        newModel.save(fileTo, false);
        log.info("Saved transformed model\n");
    }
}
