package speakeridentification.model.data;

import lombok.Builder;
import lombok.Data;
import speakeridentification.persistence.domain.SpeakerType;

@Data
@Builder
public class ProfileData {

    private int id;
    private String name;
    private SpeakerType type;
    private AudioSource source;

    public String toString() {
        return name + " - " + type;
    }
}
