package speakeridentification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import javax.swing.SwingUtilities;

import speakeridentification.model.deeplearning.NeuralNetworkHolder;
import speakeridentification.model.service.DefaultTrainingService;
import speakeridentification.model.service.DefaultProfileService;
import speakeridentification.model.service.TrainingService;
import speakeridentification.model.service.ProfileService;
import speakeridentification.model.utils.FileHandler;
import speakeridentification.persistence.DbHandler;
import speakeridentification.persistence.DefaultModelDAO;
import speakeridentification.persistence.DefaultProfileDAO;
import speakeridentification.persistence.ModelDAO;
import speakeridentification.persistence.ProfileDAO;
import speakeridentification.view.MainWindow;

public class App {

    public static final String PROPERTIES_FILE = "speakeridentification.properties";
    public static Properties properties;

    public static void main(String[] args) throws IOException {

        properties = new Properties();
        InputStream inputStream = App.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new FileNotFoundException("Property file " + PROPERTIES_FILE + " not found on classpath.");
        }

        String baseDirAbsolutePath = new File(properties.getProperty("baseDirectory")).getAbsolutePath();

        DbHandler db = new DbHandler(properties.getProperty("connectionString"));
        db.initialize();

        FileHandler fileHandler = new FileHandler(baseDirAbsolutePath);
        ProfileDAO profileDAO = new DefaultProfileDAO(properties.getProperty("connectionString"));
        ModelDAO modelDAO = new DefaultModelDAO(baseDirAbsolutePath);
        ProfileService profileService = new DefaultProfileService(profileDAO, fileHandler);
        TrainingService trainingService = new DefaultTrainingService(
            modelDAO, profileDAO, fileHandler, new NeuralNetworkHolder(baseDirAbsolutePath));

        SwingUtilities.invokeLater(() -> {
            MainWindow m = new MainWindow(profileService, trainingService, checkSoxIsPresent());
            m.setVisible(true);
        });
    }

    private static boolean checkSoxIsPresent() {
        String path = System.getenv("PATH");
        Optional<String> found = Arrays.stream(path.split(";")).filter(s -> s.contains("sox")).findFirst();
        return found.isPresent() && new File(found.get()).exists();
    }
}
