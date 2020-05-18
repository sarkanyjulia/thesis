package speakeridentification.model.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import lombok.AllArgsConstructor;
import speakeridentification.domain.Audio;
import speakeridentification.model.exceptions.FileException;
import speakeridentification.model.exceptions.InvalidInputException;

@AllArgsConstructor
public class FileHandler {

    private final static int maxAudioCount = 30;
    private final static int minAudioCount = 20;
    private String baseDirectory;

    public List<byte[]> loadSpectrograms(String directoryPath) {
        List<byte[]> imageList = new ArrayList<>();
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        final boolean directoryHasContent = files!=null && files.length >= minAudioCount;
        if (directoryHasContent) {
            for (int i = 0; i < files.length && i < maxAudioCount; ++i) {
                imageList.add(loadSpectrogram(files[i]));
            }
        } else {
            throw new InvalidInputException("Folder does not contain enough data,\nplease upload at least 20 spectrograms per profile!");
        }
        return imageList;
    }

    private byte[] loadSpectrogram(File file) {
        byte[] result;
        final boolean isPicture = file.isFile() && file.getName().endsWith(".png");
        if (!isPicture) {
            throw new InvalidInputException("Folder should contain only .png files");
        }
        try {
            BufferedImage image = ImageIO.read(file);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            result = output.toByteArray();

        } catch (IOException ex) {
            throw new InvalidInputException("Unable to load file " + file.getName());
        }
        return result;
    }

    public void saveProfilesForUse(Map<Integer, String> profilesMap, List<Audio> audioList, int numAudio) {
        String audioDirectory = FilenameUtils.concat(baseDirectory, "audio");
        String trainDirectory = FilenameUtils.concat(audioDirectory, "train");
        String predictDirectory = FilenameUtils.concat(audioDirectory, "predict");
        for (String profileName : profilesMap.values()) {
            createDirectory(FilenameUtils.concat(trainDirectory, profileName));
            createDirectory(FilenameUtils.concat(predictDirectory, profileName));
        }
        HashMap<Integer, Integer> counters = new HashMap<>();
        for (Integer id : profilesMap.keySet()) {
            counters.put(id, 0);
        }
        try {
            int i = 0;
            for (Audio audio : audioList) {
                if (audio.isTrainingData()) {
                    Integer alreadySaved = counters.get(audio.getProfileId());
                    if (alreadySaved < numAudio) {
                        copy(audio, trainDirectory, profilesMap, i);
                        counters.replace(audio.getProfileId(), ++alreadySaved);
                    }

                } else {
                    copy(audio, predictDirectory, profilesMap, i);
                }
                ++i;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new FileException("Failed to copy spectrogram files");
        }
    }

    private void copy(Audio audio, String saveDirectory, Map<Integer, String> profilesMap, int i) throws IOException {
        String fileName = FilenameUtils.concat(FilenameUtils.concat(
            saveDirectory, profilesMap.get(audio.getProfileId())),
            "a_" + i + ".png");
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(audio.getContent()));
        ImageIO.write(image, "png", new File(fileName));

    }

    private void createDirectory(String directory) {
        File dir = new File(directory);
        dir.mkdirs();
    }

    public void clearAll() {
        try {
            FileUtils.deleteDirectory(new File(baseDirectory));
        } catch (IOException e) {
            throw new FileException("Failed to clear files", e);
        }
    }

    public String processAudioFile(String sourcePath) {
        String converted = FilenameUtils.concat(baseDirectory, "converted");
        File convertedDir = new File(converted);
        try {
            if (convertedDir.exists()) {
                FileUtils.deleteDirectory(convertedDir);
            }
            convertedDir.mkdirs();
            SoxHelper.convertWavToSpectrograms(sourcePath, converted);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return converted;
    }
}
