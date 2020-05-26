package speakeridentification.persistence;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
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

import lombok.extern.slf4j.Slf4j;
import speakeridentification.persistence.domain.Settings;
import speakeridentification.persistence.exceptions.PersistenceException;

@Slf4j
public class DefaultModelDAO implements ModelDAO {

    public static final String TRAINED_MODELS_DIRECTORY = "trained_models/";
    private final String lastSaveFile;
    private final String modelSettingsFile;

    public DefaultModelDAO(String baseDirectory) {
        lastSaveFile = FilenameUtils.concat(baseDirectory, "last_used.zip");
        modelSettingsFile = FilenameUtils.concat(baseDirectory, "modelstate.json");
    }

    /*
    https://stackoverflow.com/questions/11012819/how-can-i-get-a-resource-folder-from-inside-my-jar-file
     */
    @Override public List<String> listModels() {
        List<String> filenames = new ArrayList<>();
        /*
        final String path = TRAINED_MODELS_DIRECTORY;
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        if(jarFile.isFile()) {  // Run with JAR file
            JarFile jar = null;
            try {
                jar = new JarFile(jarFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            while(entries.hasMoreElements()) {
                final String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    String nameToSave = FilenameUtils.getName(name);
                    if (!nameToSave.isBlank()) {
                        filenames.add(nameToSave);
                    }
                }
            }
            try {
                jar.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else { // Run with IDE
            final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            if (url != null) {
                try {
                    final File apps = new File(url.toURI());
                    for (File app : apps.listFiles()) {
                        filenames.add(app.getName());
                    }
                } catch (URISyntaxException ex) {
                    // never happens
                }
            }
        }

         */
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
            /*ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource(TRAINED_MODELS_DIRECTORY);
            assert url != null;
            String fileName = FilenameUtils.concat(url.getPath(), modelName);
             */
            String fileName = FilenameUtils.concat(TRAINED_MODELS_DIRECTORY, modelName);

            result = MultiLayerNetwork.load(new File(fileName), false);
        } catch (IOException e) {
            throw new PersistenceException("Failed to load pretrained model", e);
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
        try {
            FileReader reader = new FileReader(modelSettingsFile);
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
