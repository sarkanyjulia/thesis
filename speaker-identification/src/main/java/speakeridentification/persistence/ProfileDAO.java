package speakeridentification.persistence;

import java.util.Collection;
import java.util.List;

import speakeridentification.persistence.domain.Audio;
import speakeridentification.persistence.domain.Profile;

public interface ProfileDAO {

    int createProfile(Profile profileToSave);

    List<Profile> findAllProfiles();

    void deleteProfiles(List<Integer> ids);

    List<Audio> findAllAudioByProfileIds(Collection<Integer> ids);
}
