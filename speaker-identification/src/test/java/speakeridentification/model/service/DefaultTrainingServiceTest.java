package speakeridentification.model.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import speakeridentification.model.data.AudioSource;
import speakeridentification.model.data.ProfileData;
import speakeridentification.model.data.SourceType;
import speakeridentification.model.deeplearning.NeuralNetworkHolder;
import speakeridentification.model.exceptions.InvalidInputException;
import speakeridentification.model.exceptions.ModelStateException;
import speakeridentification.model.utils.FileHandler;
import speakeridentification.persistence.ModelDAO;
import speakeridentification.persistence.ProfileDAO;
import speakeridentification.persistence.domain.Audio;
import speakeridentification.persistence.domain.Settings;
import speakeridentification.persistence.domain.SpeakerType;
import speakeridentification.view.TrainingPanel;

public class DefaultTrainingServiceTest {

    public static final String NAME = "name";
    public static final String NAME2 = "name2";
    public static final String MODEL_TO_USE = "test-model";
    public static final List<String> TEST_LABELS = List.of(NAME, NAME2);
    public static final SpeakerType SPEAKER_TYPE = SpeakerType.MALE;
    public static final String SOURCE_PATH = "source";
    public static final int ID = 1;
    public static final String NO_MODEL = TrainingPanel.NO_MODEL;

    @Mock private ProfileDAO profileDAO;
    @Mock private ModelDAO modelDAO;
    @Mock private FileHandler fileHandler;
    @Mock private NeuralNetworkHolder networkHolder;
    @Mock private MultiLayerNetwork network;
    @Mock private MultiLayerNetwork otherNetwork;
    @InjectMocks private DefaultTrainingService underTest;

    @BeforeMethod
    public void setup() { MockitoAnnotations.initMocks(this); }

    @Test
    public void listModels() {
        underTest.listModels();
        then(modelDAO).should(times(1)).listModels();
    }

    @Test
    public void trainOK() {
        // GIVEN
        ProfileData profileData = createTestProfileData();
        HashMap<String, String> profilesMap = new HashMap<>();
        profilesMap.put(Integer.toString(ID), NAME);
        Audio testAudio = Audio.builder().profileId(ID).build();
        given(profileDAO.findAllAudioByProfileIds(List.of(ID))).willReturn(List.of(testAudio));
        given(modelDAO.getPretrainedModel(MODEL_TO_USE)).willReturn(network);
        given(networkHolder.train(network, profilesMap)).willReturn(otherNetwork);
        given(networkHolder.getLabels()).willReturn(TEST_LABELS);

        // WHEN
        underTest.train(MODEL_TO_USE, List.of(profileData), 1, 1);

        // THEN
        then(profileDAO).should(times(1)).findAllAudioByProfileIds(List.of(ID));
        then(fileHandler).should(times(1))
            .copyProfilesForUse(List.of(ID), List.of(testAudio), 1);
        then(networkHolder).should(times(1)).train(network, profilesMap);
        then(modelDAO).should(times(1)).saveLastUsed(otherNetwork);
        then(modelDAO).should(times(1))
            .saveLastSettings(new Settings(MODEL_TO_USE, TEST_LABELS,1, profilesMap));
    }


    @Test(expectedExceptions = InvalidInputException.class)
    public void trainNoModel() {
        ProfileData profileData = createTestProfileData();
        underTest.train(NO_MODEL, List.of(profileData), 1, 1);
    }

    @Test(expectedExceptions = InvalidInputException.class)
    public void trainWrongProfileNum() {
        ProfileData profileData = createTestProfileData();
        underTest.train(MODEL_TO_USE, List.of(profileData), 2, 1);
    }

    private ProfileData createTestProfileData() {
        return ProfileData.builder()
            .id(ID)
            .name(NAME).type(SPEAKER_TYPE)
            .source(AudioSource.builder().type(SourceType.SPECT).sourcePath(SOURCE_PATH).build())
            .build();
    }

    @Test
    public void reset() {
        underTest.reset();
        then(fileHandler).should(times(1)).clearAll();
        then(networkHolder).should(times(1)).clearAll();
    }

    @Test
    public void lastSaveExists() {
        underTest.lastSaveExists();
        then(modelDAO).should(times(1)).lastSaveExists();
    }

    @Test
    public void getProfileNames() {
        underTest.getProfileNamesFromModel();
        then(networkHolder).should(times(1)).getProfilesMap();
    }

    @Test
    public void nextPrediction() {
        given(networkHolder.predictNext(NAME, 0.0)).willReturn(NAME);
        var result = underTest.getNextPrediction(NAME, 0.0);
        assertEquals(result, NAME);
    }

    @Test
    public void loadSettings() {
        Settings testSettings = createTestSettings();
        HashMap<String, String> profilesMap = new HashMap<>();
        profilesMap.put(Integer.toString(ID), NAME);
        given(modelDAO.loadLastSettings()).willReturn(testSettings);
        given(modelDAO.getLastUsed()).willReturn(network);
        underTest.loadLastSettings();
        then(networkHolder).should(times(1)).setupWithLastSave(network, TEST_LABELS, profilesMap);
    }

    private Settings createTestSettings() {
        Settings testSettings = new Settings();
        testSettings.setModelToUse(MODEL_TO_USE);
        testSettings.setNumAudio(6);
        testSettings.setLabels(TEST_LABELS);
        HashMap<String, String> profilesMap = new HashMap<>();
        profilesMap.put(Integer.toString(ID), NAME);
        testSettings.setProfilesMap(profilesMap);
        return testSettings;
    }

    @Test
    public void setupForPredictionOK() {
        given(networkHolder.getModelToUse()).willReturn(network);
        underTest.setupModelForPrediction();
        then(networkHolder).should(times(1)).getModelToUse();
        then(networkHolder).should(times(1)).setupForPrediction();
    }

    @Test(expectedExceptions = { ModelStateException.class })
    public void setupForPredictionNotTrained() {
        given(networkHolder.getModelToUse()).willReturn(null);
        underTest.setupModelForPrediction();
    }


}
