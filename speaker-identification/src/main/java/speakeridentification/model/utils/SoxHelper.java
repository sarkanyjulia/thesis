package speakeridentification.model.utils;

import java.io.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import lombok.extern.slf4j.Slf4j;
import speakeridentification.model.exceptions.SoxException;

@Slf4j
public class SoxHelper {

    public static final String CONVERT_TO_MONO = "sox %s -c 1 -r 16000 -b 16 %s";
    public static final String TRIM = "sox %s %s silence 1 0.1 1%% reverse silence 1 0.1 1%% reverse";
    public static final String REMOVE_SILENCE = "sox %s %s silence -l 1 0.1 1%% -1 1.0 1%%";
    public static final String LIMIT_LENGTH = "sox %s %s trim 0 150";
    public static final String SPLIT = "sox %s %s trim 0 5 : newfile : restart";
    public static final String CONVERT_TO_SPECTROGRAM = "sox %s -n spectrogram -r -o %s";
    public static final String BASE_DIR = "tempAudio";
    private final static boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public static void convertWavToSpectrograms(String inputFile, String outputDir) throws IOException {
        File base = new File(BASE_DIR);
        String basePath = base.getAbsolutePath();
        String temp1 = FilenameUtils.concat(basePath, "temp1.wav");
        String temp2 = FilenameUtils.concat(basePath, "temp2.wav");
        String temp3 = FilenameUtils.concat(basePath, "temp3.wav");
        String temp4 = FilenameUtils.concat(basePath, "temp4.wav");
        String split = FilenameUtils.concat(basePath, "split");
        File splitDir = new File(split);

        if (base.exists()) {
            FileUtils.deleteDirectory(base);
        }
        splitDir.mkdirs();

        executeSoxCommand(CONVERT_TO_MONO, inputFile, temp1);
        executeSoxCommand(TRIM, temp1, temp2);
        executeSoxCommand(REMOVE_SILENCE, temp2, temp3);
        executeSoxCommand(LIMIT_LENGTH, temp3, temp4);
        executeSoxCommand(SPLIT, temp4, FilenameUtils.concat(split, "out.wav"));

        for (File input : splitDir.listFiles()) {
            String inputName = FilenameUtils.getBaseName(input.getName());
            String inputPath = FilenameUtils.concat(split, inputName + ".wav");
            String output = FilenameUtils.concat(outputDir ,inputName + ".png");
            executeSoxCommand(CONVERT_TO_SPECTROGRAM, inputPath, output);
        }

        FileUtils.deleteDirectory(base);
        log.info(inputFile + " processed using sox");
    }

    private static void executeSoxCommand(String commandFormat, String input, String output) throws IOException {
        String cmd = String.format(commandFormat, input, output);
        log.debug("Command to be executed: " + cmd);
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", cmd);
        } else {
            builder.command("sh", "-c", cmd);
        }
        builder.redirectErrorStream(true);
        Process process = builder.start();
        try {
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            if (process.exitValue() != 0) {
                while ((line = reader.readLine()) != null) {
                    log.error(line);
                    throw new RuntimeException(line);
                }
            }
            else {
                while ((line = reader.readLine()) != null) {
                    log.debug(line);
                }
            }
        } catch (InterruptedException e) {
            log.error("Processing " + input + " was interrupted", e.getMessage());
        } catch (RuntimeException e) {
            throw new SoxException("Unable to process audio file", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
