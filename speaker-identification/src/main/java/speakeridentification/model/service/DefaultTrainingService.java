package speakeridentification.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import lombok.AllArgsConstructor;
import speakeridentification.persistence.domain.Audio;
import speakeridentification.persistence.domain.Profile;
import speakeridentification.persistence.domain.Settings;
import speakeridentification.model.data.ProfileData;
import speakeridentification.model.deeplearning.NeuralNetworkHolder;
import speakeridentification.model.exceptions.InvalidInputException;
import speakeridentification.model.exceptions.ModelStateException;
import speakeridentification.model.utils.FileHandler;
import speakeridentification.persistence.ModelDAO;
import speakeridentification.persistence.ProfileDAO;
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
        HashMap<String, String> profilesMap = new HashMap<>();
        List<Integer> profileIds = new ArrayList<>();
        profilesChosen.forEach(p -> {profilesMap.put(Integer.toString(p.getId()), p.getName()); profileIds.add(p.getId());});
        List<Audio> audios = profileDAO.findAllAudioByProfileIds(profileIds);
        fileHandler.copyProfilesForUse(profileIds, audios, numAudio);
        MultiLayerNetwork newModel = networkHolder.train(modelDAO.getPretrainedModel(modelToUse), profilesMap);
        modelDAO.saveLastUsed(newModel);
        modelDAO.saveLastSettings(new Settings(modelToUse, networkHolder.getLabels(), numAudio, profilesMap));
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

    @Override public Map<String, String> getProfileNamesFromModel() {
        return networkHolder.getProfilesMap();
    }

    @Override public String getNextPrediction(String profileName, Double treshold) {
        return networkHolder.predictNext(profileName, treshold);
    }

    @Override public boolean lastSaveExists() {
        return modelDAO.lastSaveExists();
    }

    @Override public Settings loadLastSettings() {
        Settings settings = modelDAO.loadLastSettings();
        MultiLayerNetwork lastUsed = modelDAO.getLastUsed();
        networkHolder.setupWithLastSave(lastUsed, settings.getLabels(), settings.getProfilesMap());
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
