package speakeridentification.model.service;

import java.util.List;
import java.util.Map;

import speakeridentification.model.data.ProfileData;
import speakeridentification.persistence.domain.Settings;

public interface TrainingService {

    List<String> listModels();

    void train(String modelToUse, List<ProfileData> profilesToUse, int numProfiles, int numAudio);

    void reset();

    List<ProfileData> findAllProfiles();

    Map<String, String> getProfileNamesFromModel();

    String getNextPrediction(String profileName, Double treshold);

    boolean lastSaveExists();

    Settings loadLastSettings();

    void setupModelForPrediction();
}
