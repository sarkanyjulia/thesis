package speakeridentification.model.service;

import java.util.List;

import speakeridentification.model.data.ProfileData;

public interface ProfileService {

    int createProfile(ProfileData toSave);

    List<ProfileData> findAllProfiles();

    void deleteProfiles(List<Integer> ids);
}
