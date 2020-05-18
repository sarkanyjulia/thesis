package speakeridentification.persistence;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import speakeridentification.domain.Audio;
import speakeridentification.domain.Profile;

public interface ProfileDAO {

    int createProfile(Profile profileToSave);

    List<Profile> findAllProfiles();

    void deleteProfiles(List<Integer> ids);

    List<Audio> findAllAudioByProfileIds(Collection<Integer> ids);
}
