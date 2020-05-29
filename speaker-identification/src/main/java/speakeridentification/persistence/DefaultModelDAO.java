package speakeridentification.persistence;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import speakeridentification.persistence.domain.Settings;
import speakeridentification.persistence.exceptions.PersistenceException;

public class DefaultModelDAO implements ModelDAO {

    public static final String TRAINED_MODELS_DIRECTORY = "trained_models/";
    private final String lastSaveFile;
    private final String modelSettingsFile;

    public DefaultModelDAO(String baseDirectory) {
        lastSaveFile = FilenameUtils.concat(baseDirectory, "last_used.zip");
        modelSettingsFile = FilenameUtils.concat(baseDirectory, "modelstate.json");
    }

    @Override public List<String> listModels() {
        List<String> filenames = new ArrayList<>();
        final File models = new File(TRAINED_MODELS_DIRECTORY);
        File[] files = models.listFiles();
        if (files != null) {
            for (File model : files) {
                filenames.add(model.getName());
            }
        } else throw new PersistenceException("Unable to find trained models");
        return filenames;
    }

    @Override public MultiLayerNetwork getPretrainedModel(String modelName) {
        MultiLayerNetwork result = null;
        try {
            String fileName = FilenameUtils.concat(TRAINED_MODELS_DIRECTORY, modelName);
            result = MultiLayerNetwork.load(new File(fileName), false);
        } catch (Exception e) {
            throw new PersistenceException("Failed to load pretrained model", e);
        }
        return result;
    }

    @Override public void saveLastUsed(MultiLayerNetwork newModel) {
        try {
            newModel.save(new File(lastSaveFile), false);
        } catch (Exception e) {
            throw new PersistenceException("Failed to save model");
        }
    }

    @Override public MultiLayerNetwork getLastUsed() {
        MultiLayerNetwork lastUsedModel = null;
        try {
            lastUsedModel = MultiLayerNetwork.load(new File(lastSaveFile), false);
        } catch(IOException e) {
            throw new PersistenceException("Failed to load last used model", e);
        }
        return lastUsedModel;
    }

    @Override public boolean lastSaveExists() {
        File file = new File(lastSaveFile);
        return file.exists();
    }

    @Override public void saveLastSettings(Settings settings) {
        JSONObject modelSettings = new JSONObject();
        modelSettings.put("modelToUse", settings.getModelToUse());
        modelSettings.put("numAudio", settings.getNumAudio());
        JSONArray labelsArray = new JSONArray();
        labelsArray.addAll(settings.getLabels());
        modelSettings.put("labels", labelsArray);
        modelSettings.put("profiles", settings.getProfilesMap());
        try {
            Files.write(Paths.get(modelSettingsFile), modelSettings.toJSONString().getBytes());
        } catch (Exception e) {
            throw new PersistenceException("Failed to save settings", e);
        }
    }

    @Override public Settings loadLastSettings() {
        Settings settings = new Settings();
        try (FileReader reader = new FileReader(modelSettingsFile)) {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            settings.setModelToUse((String) jsonObject.get("modelToUse"));
            settings.setNumAudio(Math.toIntExact((Long) jsonObject.get("numAudio")));
            JSONArray labelsArray = (JSONArray) jsonObject.get("labels");
            for (var item : labelsArray) {
                settings.getLabels().add((String) item);
            }
            HashMap<String, String> profilesMap = (HashMap<String, String>)jsonObject.get("profiles");
            settings.setProfilesMap(profilesMap);
        } catch (Exception e) {
            throw new PersistenceException("Failed to load last settings", e);
        }
        return settings;
    }
}
