package speakeridentification.model.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import speakeridentification.domain.SpeakerType;

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
