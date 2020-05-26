package speakeridentification.model.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import lombok.AllArgsConstructor;
import speakeridentification.persistence.domain.Audio;
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
            throw new InvalidInputException("Not enough data,\nplease upload at least 20 spectrograms per profile or a longer audio file!");
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

    public void copyProfilesForUse(Collection<Integer> profileIds, List<Audio> audioList, int numAudio) {
        clearAll();
        String audioDirectory = FilenameUtils.concat(baseDirectory, "audio");
        String trainDirectory = FilenameUtils.concat(audioDirectory, "train");
        String predictDirectory = FilenameUtils.concat(audioDirectory, "predict");
        for (Integer id : profileIds) {
            createDirectory(FilenameUtils.concat(trainDirectory, Integer.toString(id)));
            createDirectory(FilenameUtils.concat(predictDirectory, Integer.toString(id)));
        }
        HashMap<Integer, Integer> counters = new HashMap<>();
        for (Integer id : profileIds) {
            counters.put(id, 0);
        }
        try {
            int i = 0;
            for (Audio audio : audioList) {
                if (audio.isTrainingData()) {
                    int profileId = audio.getProfileId();
                    Integer alreadySaved = counters.get(profileId);
                    if (alreadySaved < numAudio) {
                        copy(audio, trainDirectory, i);
                        counters.replace(profileId, ++alreadySaved);
                    }

                } else {
                    copy(audio, predictDirectory, i);
                }
                ++i;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new FileException("Failed to copy spectrogram files");
        }
    }

    private void copy(Audio audio, String saveDirectory, int i) throws IOException {
        String fileName = FilenameUtils.concat(FilenameUtils.concat(
            saveDirectory, Integer.toString(audio.getProfileId())),
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
