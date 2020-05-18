package speakeridentification.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import speakeridentification.persistence.exceptions.PersistenceException;

public class DefaultModelDAO implements ModelDAO {

    public static final String TRAINED_MODELS_DIRECTORY = "trained_models";
    private final String lastSaveFile;
    private final String modelSettingsFile;

    public DefaultModelDAO(String baseDirectory) {
        lastSaveFile = FilenameUtils.concat(baseDirectory, "last_used.zip");
        modelSettingsFile = FilenameUtils.concat(baseDirectory, "modelstate.json");
    }

    @Override public List<String> listModels() {
        List<String> modelNames = new ArrayList<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(TRAINED_MODELS_DIRECTORY);
        assert url != null;
        String path = url.getPath();
        File[] files = new File(path).listFiles();
        for (int i=0; i < files.length; ++i) {
            modelNames.add(files[i].getName());
        }
        return modelNames;
    }

    @Override public MultiLayerNetwork getPretrainedModel(String modelName) {
        MultiLayerNetwork result = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource(TRAINED_MODELS_DIRECTORY);
            assert url != null;
            String fileName = FilenameUtils.concat(url.getPath(), modelName);
            result = MultiLayerNetwork.load(new File(fileName), false);
        } catch (IOException e) {
            throw new PersistenceException("Unable to load pretrained model", e);
        }
        return result;
    }

    @Override public void saveLastUsed(MultiLayerNetwork newModel) {
        try {
            newModel.save(new File(lastSaveFile), false);
        } catch (IOException e) {
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
        boolean exists;
        File file = new File(lastSaveFile);
        return file.exists();
    }

    @Override public void saveLastSettings(Settings settings) {
        JSONObject modelSettings = new JSONObject();
        modelSettings.put("modelToUse", settings.getModelToUse());
        modelSettings.put("numProfiles", settings.getNumProfiles());
        modelSettings.put("numAudio", settings.getNumAudio());
        JSONArray labelsArray = new JSONArray();
        labelsArray.addAll(settings.getLabels());
        modelSettings.put("labels", labelsArray);
        try {
            Files.write(Paths.get(modelSettingsFile), modelSettings.toJSONString().getBytes());
        } catch (IOException e) {
            throw new PersistenceException("Failed to load last used model", e);
        }
    }

    @Override public Settings loadLastSettings() {
        Settings settings = new Settings();
        try {
            FileReader reader = new FileReader(modelSettingsFile);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            settings.setModelToUse((String) jsonObject.get("modelToUse"));
            settings.setNumAudio(Math.toIntExact((Long) jsonObject.get("numAudio")));
            settings.setNumProfiles(Math.toIntExact((Long) jsonObject.get("numProfiles")));
            JSONArray labelsArray = (JSONArray) jsonObject.get("labels");
            for (var item : labelsArray) {
                settings.getLabels().add((String) item);
            }
        } catch (ParseException | IOException e) {
            throw new PersistenceException("Failed to load last settings", e);
        }
        return settings;
    }
}
