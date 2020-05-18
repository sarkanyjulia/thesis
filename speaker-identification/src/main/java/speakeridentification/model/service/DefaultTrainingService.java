package speakeridentification.model.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.bytedeco.javacv.FrameFilter;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import speakeridentification.domain.Audio;
import speakeridentification.domain.Profile;
import speakeridentification.model.data.ProfileData;
import speakeridentification.model.deeplearning.NeuralNetworkHolder;
import speakeridentification.model.exceptions.FileException;
import speakeridentification.model.exceptions.InvalidInputException;
import speakeridentification.model.exceptions.ModelStateException;
import speakeridentification.model.utils.FileHandler;
import speakeridentification.persistence.ModelDAO;
import speakeridentification.persistence.ProfileDAO;
import speakeridentification.persistence.Settings;
import speakeridentification.view.TrainingPanel;

@AllArgsConstructor
public class DefaultTrainingService implements TrainingService {

    private ModelDAO modelDAO;
    private ProfileDAO profileDAO;
    private FileHandler fileHandler;
    private NeuralNetworkHolder networkHolder;

    @Override public List<String> listModels() {
        return modelDAO.listModels();
    }

    @Override public void train(String modelToUse, List<ProfileData> profilesChosen, int numProfiles, int numAudio) {
        checkInput(modelToUse, profilesChosen, numProfiles);
        Map<Integer, String> profilesMap = new HashMap<>();
        profilesChosen.forEach(p -> profilesMap.put(p.getId(), p.getName()));
        List<Audio> audios = profileDAO.findAllAudioByProfileIds(profilesMap.keySet());
        fileHandler.saveProfilesForUse(profilesMap, audios, numAudio);
        networkHolder.setPretrainedModel(modelDAO.getPretrainedModel(modelToUse));
        MultiLayerNetwork newModel = networkHolder.train();
        modelDAO.saveLastUsed(newModel);
        modelDAO.saveLastSettings(new Settings(modelToUse, networkHolder.getLabels(), numProfiles, numAudio));
    }

    private void checkInput(String modelToUse, List<ProfileData> profilesChosen, int numProfiles) {
        if (modelToUse.equals(TrainingPanel.NO_MODEL)) throw new InvalidInputException("No pretrained model chosen");
        if (numProfiles!=profilesChosen.size()) throw new InvalidInputException("Not enough profiles chosen");
    }

    @Override public void reset() {
        fileHandler.clearAll();
        networkHolder.clearAll();
    }


    @Override public List<ProfileData> findAllProfiles() {
        return profileDAO.findAllProfiles().stream().map(this::transform).collect(Collectors.toList());
    }

    @Override public List<String> getProfileNamesFromModel() {
        return networkHolder.getLabels(); // TODO ?
    }

    @Override public String getNextPrediction(String profileName, Double treshold) {
        return networkHolder.predictNext(profileName, treshold);
    }

    @Override public boolean lastSaveExists() {
        return modelDAO.lastSaveExists();
    }

    @Override public Settings loadLastSettings() {
        Settings settings = modelDAO.loadLastSettings();
        networkHolder.setModelToUse(modelDAO.getLastUsed());
        networkHolder.setLabels(settings.getLabels());
        return settings;
    }

    @Override public void setupModelForPrediction() {
        checkModelIsTrained();
        networkHolder.setupForPrediction();
    }

    private void checkModelIsTrained() {
        if (networkHolder.getModelToUse() == null) throw new ModelStateException("Model is not trained");
    }

    private ProfileData transform(Profile profile) {
        return ProfileData.builder().id(profile.getId()).name(profile.getName()).type(profile.getType()).build();
    }
}
