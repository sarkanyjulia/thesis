package speakeridentification.persistence.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    private String modelToUse;
    private List<String> labels = new ArrayList();
    private int numProfiles;
    private int numAudio;

}
