package speakeridentification.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
public class Profile {

    private int id;
    private String name;
    private SpeakerType type;
    List<Audio> audios;
}
