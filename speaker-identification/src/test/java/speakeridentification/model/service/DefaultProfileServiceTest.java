package speakeridentification.model.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import speakeridentification.persistence.domain.Profile;
import speakeridentification.persistence.domain.SpeakerType;
import speakeridentification.model.data.AudioSource;
import speakeridentification.model.data.ProfileData;
import speakeridentification.model.data.SourceType;
import speakeridentification.model.exceptions.InvalidInputException;
import speakeridentification.model.utils.FileHandler;
import speakeridentification.persistence.ProfileDAO;

public class DefaultProfileServiceTest {

 public static final int ID = 1;
 public static final String NAME = "name";
 public static final SpeakerType SPEAKER_TYPE = SpeakerType.MALE;
 public static final String SOURCE_PATH = "source";
 public static final String TEMPDIR = "tempdir";

 @Mock private ProfileDAO dao;
 @Mock private FileHandler fileHandler;
 @InjectMocks private DefaultProfileService underTest;

 @BeforeMethod
 public void setup() { MockitoAnnotations.initMocks(this); }

 @Test
 public void createProfileOK() {
  // GIVEN
  ProfileData input = createTestProfileData();
  Profile profileToSave = createTestProfile();
  given(fileHandler.loadSpectrograms(SOURCE_PATH)).willReturn(List.of());
  given(dao.createProfile(profileToSave)).willReturn(ID);

  // WHEN
   var result = underTest.createProfile(input);

  // THEN
   then(dao).should(times(1)).createProfile(profileToSave);
   assertEquals(result, ID);
 }

 private ProfileData createTestProfileData() {
  return ProfileData.builder()
      .name(NAME).type(SPEAKER_TYPE)
      .source(AudioSource.builder().type(SourceType.SPECT).sourcePath(SOURCE_PATH).build())
      .build();
 }

 private Profile createTestProfile() {
  return Profile.builder().name(NAME).type(SPEAKER_TYPE).audios(List.of()).build();
 }

 @Test public void createWithWavSource() {
  // GIVEN
  ProfileData profileData = createTestProfileData();
  profileData.getSource().setType(SourceType.WAV);
  given(fileHandler.processAudioFile(any())).willReturn(TEMPDIR);

  // WHEN
  underTest.createProfile(profileData);

  // THEN
  then(fileHandler).should(times(1)).processAudioFile(SOURCE_PATH);
  then(fileHandler).should(times(1)).loadSpectrograms(TEMPDIR);
 }

 @Test(expectedExceptions = { InvalidInputException.class })
 public void createProfileWithEmptyName() {
  ProfileData input = createTestProfileData();
  input.setName("");
  underTest.createProfile(input);
 }

 @Test(expectedExceptions = { InvalidInputException.class })
 public void createProfileWithEmptySource() {
  ProfileData input = createTestProfileData();
  input.getSource().setSourcePath("");
  underTest.createProfile(input);
 }

 @Test
 public void findAllProfiles() {
  // GIVEN
  ProfileData expectedProfileData = createTestProfileData();
  expectedProfileData.setSource(null);
  Profile profile = createTestProfile();
  given(dao.findAllProfiles()).willReturn(List.of(profile));

  // WHEN
  var result = underTest.findAllProfiles();

  // THEN
  assertEquals(result, List.of(expectedProfileData));
 }

 @Test
 public void delete() {
  List<Integer> ids = List.of(1, 2, 3);
  underTest.deleteProfiles(ids);
  then(dao).should(times(1)).deleteProfiles(ids);
 }

}
