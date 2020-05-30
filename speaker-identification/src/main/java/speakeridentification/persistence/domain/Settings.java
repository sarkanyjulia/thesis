package speakeridentification.persistence.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    private String modelToUse;
    private List<String> labels = new ArrayList();
    private int numAudio;
    private HashMap<String, String> profilesMap = new HashMap<>();

}
