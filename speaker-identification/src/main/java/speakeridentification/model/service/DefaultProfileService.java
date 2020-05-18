package speakeridentification.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import speakeridentification.domain.Audio;
import speakeridentification.domain.Profile;
import speakeridentification.model.data.ProfileData;
import speakeridentification.model.data.SourceType;
import speakeridentification.model.exceptions.InvalidInputException;
import speakeridentification.model.utils.FileHandler;
import speakeridentification.persistence.ProfileDAO;

@AllArgsConstructor
public class DefaultProfileService implements ProfileService {

    private ProfileDAO dao;
    private FileHandler fileHandler;

    @Override public int createProfile(ProfileData input) {
        checkInput(input);
        List<Audio> audioList = getAudioData(input);
        Profile profileToSave = Profile.builder().name(input.getName()).type(input.getType()).audios(audioList).build();
        return dao.createProfile(profileToSave);
    }

    private void checkInput(ProfileData toSave) {
        if (toSave.getName().isBlank()) throw new InvalidInputException("Name cannot be empty");
        if (toSave.getSource().getSourcePath().isBlank()) throw new InvalidInputException("Source cannot be empty");

    }

    private List<Audio> getAudioData(ProfileData input) {
        String sourcePath = "";
        switch (input.getSource().getType()) {
            case SPECT:
                sourcePath = input.getSource().getSourcePath();
                break;
            case WAV:
                sourcePath = fileHandler.processAudioFile(input.getSource().getSourcePath());
                break;
        }
        List<Audio> result = new ArrayList<>();
        List<byte[]> images = fileHandler.loadSpectrograms(sourcePath);
        for (byte[] image : images) {
            result.add(Audio.builder().content(image).isTrainingData(result.size() < 12).build());
        }
        return result;
    }

    @Override public List<ProfileData> findAllProfiles() {
        return dao.findAllProfiles().stream().map(this::transform).collect(Collectors.toList());
    }

    private ProfileData transform(Profile profile) {
        return ProfileData.builder().id(profile.getId()).name(profile.getName()).type(profile.getType()).build();
    }

    @Override public void deleteProfiles(List<Integer> ids) {
        dao.deleteProfiles(ids);
    }
}
