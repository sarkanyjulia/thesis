package speakeridentification.persistence.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Profile {

    private int id;
    private String name;
    private SpeakerType type;
    List<Audio> audios;
}
