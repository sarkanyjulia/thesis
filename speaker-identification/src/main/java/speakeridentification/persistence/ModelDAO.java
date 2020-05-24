package speakeridentification.persistence;

import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import speakeridentification.persistence.domain.Settings;

public interface ModelDAO {

    List<String> listModels();
    MultiLayerNetwork getPretrainedModel(String modelName);
    void saveLastUsed(MultiLayerNetwork newModel);
    MultiLayerNetwork getLastUsed();
    boolean lastSaveExists();
    void saveLastSettings(Settings settings);
    Settings loadLastSettings();
}
